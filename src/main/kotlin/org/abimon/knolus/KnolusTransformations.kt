@file:Suppress("EXPERIMENTAL_API_USAGE")

package org.abimon.knolus

import org.abimon.knolus.context.KnolusContext
import org.abimon.knolus.types.*
import kotlin.math.floor
import kotlin.math.log
import kotlin.math.roundToInt

@ExperimentalUnsignedTypes
typealias KnolusTransform<V, T> = suspend V.(context: KnolusContext) -> T

sealed class ParameterSpec<V : KnolusTypedValue, T> {
    abstract val name: String
    abstract val type: KnolusTypedValue.TypeInfo<V>
    abstract val default: T?
    abstract val transformation: KnolusTransform<in V, T>

    fun getMemberFunctionName(functionName: String): String =
        type.getMemberFunctionName(type.typeName, functionName)

    fun getMemberPropertyGetterName(propertyName: String): String =
        type.getMemberPropertyGetterName(type.typeName, propertyName)

    fun getMemberOperatorName(operator: ExpressionOperator): String =
        type.getMemberOperatorName(type.typeName, operator)

    abstract infix fun withName(name: String): ParameterSpec<V, T>
    abstract infix fun withDefault(value: T?): ParameterSpec<V, T>
}

data class RegularParameterSpec<V : KnolusTypedValue, T>(
    override val name: String,
    override val type: KnolusTypedValue.TypeInfo<V>,
    override val default: T?,
    override val transformation: KnolusTransform<V, T>,
) : ParameterSpec<V, T>() {
    override fun withName(name: String): ParameterSpec<V, T> = copy(name = name.sanitiseFunctionIdentifier())
    override fun withDefault(value: T?): ParameterSpec<V, T> = copy(default = value)
}

data class TypeParameterSpec<V : KnolusTypedValue, T>(
    override val type: KnolusTypedValue.TypeInfo<V>,
    override val transformation: KnolusTransform<V, T>,
) : ParameterSpec<V, T>() {
    override val name: String = "self"
    override val default: T? = null

    override fun withName(name: String): ParameterSpec<V, T> = RegularParameterSpec(name, type, null, transformation)
    override fun withDefault(value: T?): ParameterSpec<V, T> = RegularParameterSpec(name, type, value, transformation)
}

@ExperimentalUnsignedTypes
object KnolusTransformations {
    val NONE: KnolusTransform<KnolusTypedValue, KnolusTypedValue> = { this }
    val TO_STRING: KnolusTransform<KnolusTypedValue, String> = KnolusTypedValue::asString
    val TO_BOOLEAN: KnolusTransform<KnolusTypedValue, Boolean> = KnolusTypedValue::asBoolean
    val TO_NUMBER: KnolusTransform<KnolusTypedValue, Number> = KnolusTypedValue::asNumber

    val TO_CHAR: KnolusTransform<KnolusTypedValue, Char> = { context ->
        when (this) {
            is KnolusString -> string.firstOrNull() ?: '\u0000'
            is KnolusChar -> char
            is KnolusNumericalType -> asNumber(context).toChar()
            else -> asNumber(context).toChar()
        }
    }
    val TO_INT: KnolusTransform<KnolusTypedValue, Int> = { asNumber(it).toInt() }
    val TO_DOUBLE: KnolusTransform<KnolusTypedValue, Double> = { asNumber(it).toDouble() }

    val TO_CHAR_ARRAY: KnolusTransform<KnolusTypedValue, CharArray> = { context ->
        when (this) {
            is KnolusString -> string.toCharArray()
            is KnolusArray<*> -> {
                when {
                    array.isArrayOf<KnolusChar>() -> {
                        val array = (array as Array<KnolusChar>)
                        CharArray(array.size) { array[it].char }
                    }
                    array.isArrayOf<KnolusInt>() -> {
                        val array = (array as Array<KnolusInt>)
                        CharArray(array.size) { array[it].number.toChar() }
                    }
                    array.isArrayOf<KnolusDouble>() -> {
                        val array = (array as Array<KnolusDouble>)
                        CharArray(array.size) { array[it].number.roundToInt().toChar() }
                    }
                    array.isArrayOf<KnolusString>() -> (array as Array<KnolusString>).flatMap { str -> str.string.toList() }
                        .toCharArray()
                    else -> CharArray(array.size) { array[it].asNumber(context).toChar() }
                }
            }
            is KnolusChar -> charArrayOf(char)
            is KnolusConstants.Null -> charArrayOf()
            is KnolusConstants.Undefined -> charArrayOf()
            else -> charArrayOf(asNumber(context).toChar())
        }
    }
    val TO_CHAR_OR_NULL: KnolusTransform<KnolusTypedValue, Char?> = { context ->
        when (this) {
            is KnolusString -> string.firstOrNull()
            is KnolusChar -> char
            is KnolusConstants.Null -> null
            is KnolusConstants.Undefined -> null
            else -> asNumber(context).toChar()
        }
    }

//    val FLATTENED_TO_INNER: KnolusTransform<Any> = { context ->
//        when (val flat = this.flatten(context)) {
//            is KnolusLazyString -> flat.components
//            is KnolusString -> flat.string
//            is KnolusBoolean -> flat.boolean
//            is KnolusTypedValue.CharType -> flat.char
//            is KnolusInt -> flat.integer
//            is KnolusDouble -> flat.decimal
//            is KnolusVariableReference -> flat.variableName
//            is KnolusPropertyReference -> Pair(flat.variableName, flat.propertyName)
//            is KnolusLazyFunctionCall -> Pair(flat.name, flat.parameters)
//            is KnolusLazyMemberFunctionCall -> Triple(flat.variableName, flat.functionName, flat.parameters)
//            is KnolusArray<*> -> flat.array
//            is KnolusNull -> flat
//            is KnolusUndefined -> flat
//            is KnolusLazyExpression -> Pair(flat.startValue, flat.ops)
//        }
//    }
//    val TO_INNER: KnolusTransform<Any> = { context ->
//        when (val flat = this) {
//            is KnolusLazyString -> flat.components
//            is KnolusString -> flat.string
//            is KnolusBoolean -> flat.boolean
//            is KnolusTypedValue.CharType -> flat.char
//            is KnolusInt -> flat.number
//            is KnolusDouble -> flat.number
//            is KnolusVariableReference -> flat.variableName
//            is KnolusPropertyReference -> Pair(flat.variableName, flat.propertyName)
//            is KnolusLazyFunctionCall -> Pair(flat.name, flat.parameters)
//            is KnolusLazyMemberFunctionCall -> Triple(flat.variableName, flat.functionName, flat.parameters)
//            is KnolusArray<*> -> flat.array
//            is KnolusNull -> flat
//            is KnolusUndefined -> flat
//            is KnolusLazyExpression -> Pair(flat.startValue, flat.ops)
//        }
//    }


    val LAZY_STRING_SELF: KnolusTransform<KnolusLazyString, KnolusLazyString> = { this }
    val LAZY_STRING_TO_INNER: KnolusTransform<KnolusLazyString, Array<KnolusUnion.StringComponent>> = { components }
    val LAZY_STRING_AS_STRING_ARRAY: KnolusTransform<KnolusLazyString, Array<String>> = { context ->
        components.mapNotNull { component ->
            when (component) {
                is KnolusUnion.StringComponent.RawText -> component.text
                is KnolusUnion.StringComponent.VariableReference -> context[component.variableName]?.asString(context)
            }
        }.toTypedArray()
    }

    val VARIABLE_REFERENCE_SELF: KnolusTransform<KnolusVariableReference, KnolusVariableReference> = { this }
    val VARIABLE_REFERENCE_TO_INNER: KnolusTransform<KnolusVariableReference, String> = { variableName }

    val MAX_HEX_STRING = String(CharArray((floor(log(Int.MAX_VALUE.toDouble(), 16.0)).toInt() shl 1) + 2) { '0' })
    val NUMBER_TO_HEX_STRING: KnolusTransform<KnolusNumericalType, String> = {
        val int = asNumber(it).toInt()

        buildString {
            append("0x")
            if (log(int.toDouble(), 16.0).toInt() and 1 == 0) append('0')
            append(int.toString(16))
        }
    }

    val NUMBER_TO_2_BYTE_HEX_STRING: KnolusTransform<KnolusNumericalType, String> by lazy { numberToByteHexString(2) }
    val NUMBER_TO_3_BYTE_HEX_STRING: KnolusTransform<KnolusNumericalType, String> by lazy { numberToByteHexString(3) }
    val NUMBER_TO_4_BYTE_HEX_STRING: KnolusTransform<KnolusNumericalType, String> by lazy { numberToByteHexString(4) }
    val NUMBER_TO_5_BYTE_HEX_STRING: KnolusTransform<KnolusNumericalType, String> by lazy { numberToByteHexString(5) }
    val NUMBER_TO_6_BYTE_HEX_STRING: KnolusTransform<KnolusNumericalType, String> by lazy { numberToByteHexString(6) }
    val NUMBER_TO_7_BYTE_HEX_STRING: KnolusTransform<KnolusNumericalType, String> by lazy { numberToByteHexString(7) }
    val NUMBER_TO_8_BYTE_HEX_STRING: KnolusTransform<KnolusNumericalType, String> by lazy { numberToByteHexString(8) }

    fun numberToByteHexString(byteCount: Int): KnolusTransform<KnolusNumericalType, String> {
        val len = (byteCount shl 1)
        return {
            val int = number.toInt()

            buildString {
                append("0x")
                append(MAX_HEX_STRING.slice(0 until (len - (log(int.toDouble(), 16.0).toInt()))))
                append(int.toString(16))
            }
        }
    }
}

//inline fun <reified T: KnolusTypedValue.TypeInfo<*>, R> typeParameterFor(noinline transformation: KnolusTransform<R>): TypeParameterSpec<R> = TypeParameterSpec(T::class.objectInstance!!, transformation)

fun <V : KnolusTypedValue, T> KnolusTypedValue.TypeInfo<V>.operatorSpecWith(
    transformation: KnolusTransform<V, T>,
) = TypeParameterSpec(this, transformation)

fun <V : KnolusTypedValue, T> KnolusTypedValue.TypeInfo<V>.parameterSpecWith(
    name: String,
    default: T? = null,
    transformation: KnolusTransform<V, T>,
) = RegularParameterSpec(name, this, default, transformation)

fun <V : KnolusTypedValue, T> KnolusTypedValue.TypeInfo<V>.typeSpecWith(
    name: String? = null,
    default: T? = null,
    transformation: KnolusTransform<V, T>,
) =
    if (name == null) TypeParameterSpec(this, transformation) else RegularParameterSpec(name,
        this,
        default,
        transformation)

fun objectTypeParameter(name: String? = null, default: KnolusTypedValue? = null) =
    KnolusObject.typeSpecWith(name, default, KnolusTransformations.NONE)

fun objectTypeAsStringParameter(name: String? = null, default: String? = null) =
    KnolusObject.typeSpecWith(name, default, KnolusTransformations.TO_STRING)

fun stringTypeParameter(name: String? = null, default: String? = null) =
    KnolusString.typeSpecWith(name, default, KnolusTransformations.TO_STRING)

//fun stringCompsTypeParameter(): TypeParameterSpec<KnolusLazyString> = TypeParameterSpec("StringComponents", KnolusTransformations.STRING_COMPONENTS_CAST)
//fun stringCompsTypeAsComponentsParameter(): TypeParameterSpec<Array<KnolusUnion.StringComponent>> = TypeParameterSpec("StringComponents", KnolusTransformations.STRING_COMPONENTS_TO_INNER)
//fun stringCompsTypeAsStringArrayParameter(): TypeParameterSpec<Array<String>> = TypeParameterSpec("StringComponents", KnolusTransformations.STRING_COMPONENTS_AS_STRING_ARRAY)
//fun stringCompsTypeAsStringParameter(): TypeParameterSpec<String> = TypeParameterSpec("StringComponents", KnolusTransformations.TO_STRING)

fun booleanTypeParameter(name: String? = null, default: Boolean? = null) =
    KnolusBoolean.typeSpecWith(name, default, KnolusTransformations.TO_BOOLEAN)

fun booleanTypeAsIntParameter(name: String? = null, default: Int? = null) =
    KnolusBoolean.typeSpecWith(name, default, KnolusTransformations.TO_INT)

fun booleanTypeAsStringParameter(name: String? = null, default: String? = null) =
    KnolusBoolean.typeSpecWith(name, default, KnolusTransformations.TO_STRING)

fun numberTypeParameter(name: String? = null, default: Number? = null) =
    KnolusNumericalType.typeSpecWith(name, default, KnolusTransformations.TO_NUMBER)

fun numberTypeAsIntParameter(name: String? = null, default: Int? = null) =
    KnolusNumericalType.typeSpecWith(name, default, KnolusTransformations.TO_INT)

fun numberTypeAsDoubleParameter(name: String? = null, default: Double? = null) =
    KnolusNumericalType.typeSpecWith(name, default, KnolusTransformations.TO_DOUBLE)

fun numberTypeAsBooleanParameter(name: String? = null, default: Boolean? = null) =
    KnolusNumericalType.typeSpecWith(name, default, KnolusTransformations.TO_BOOLEAN)

fun numberTypeAsCharParameter(name: String? = null, default: Char? = null) =
    KnolusNumericalType.typeSpecWith(name, default, KnolusTransformations.TO_CHAR)

fun numberTypeAsStringParameter(name: String? = null, default: String? = null) =
    KnolusNumericalType.typeSpecWith(name, default, KnolusTransformations.TO_STRING)

fun intTypeParameter(name: String? = null, default: Int? = null) =
    KnolusInt.typeSpecWith(name, default, KnolusTransformations.TO_INT)

fun intTypeAsDoubleParameter(name: String? = null, default: Double? = null) =
    KnolusInt.typeSpecWith(name, default, KnolusTransformations.TO_DOUBLE)

fun intTypeAsBooleanParameter(name: String? = null, default: Boolean? = null) =
    KnolusInt.typeSpecWith(name, default, KnolusTransformations.TO_BOOLEAN)

fun intTypeAsCharParameter(name: String? = null, default: Char? = null) =
    KnolusInt.typeSpecWith(name, default, KnolusTransformations.TO_CHAR)

fun doubleTypeParameter(name: String? = null, default: Double? = null) =
    KnolusDouble.typeSpecWith(name, default, KnolusTransformations.TO_DOUBLE)

fun doubleTypeAsIntParameter(name: String? = null, default: Int? = null) =
    KnolusDouble.typeSpecWith(name, default, KnolusTransformations.TO_INT)

fun doubleTypeAsBooleanParameter(name: String? = null, default: Boolean? = null) =
    KnolusDouble.typeSpecWith(name, default, KnolusTransformations.TO_BOOLEAN)

fun doubleTypeAsCharParameter(name: String? = null, default: Char? = null) =
    KnolusDouble.typeSpecWith(name, default, KnolusTransformations.TO_CHAR)

fun charTypeParameter(name: String? = null, default: Char? = null) =
    KnolusChar.typeSpecWith(name, default, KnolusTransformations.TO_CHAR)

fun charTypeAsIntParameter(name: String? = null, default: Int? = null) =
    KnolusChar.typeSpecWith(name, default, KnolusTransformations.TO_INT)

fun charTypeAsDoubleParameter(name: String? = null, default: Double? = null) =
    KnolusChar.typeSpecWith(name, default, KnolusTransformations.TO_DOUBLE)

fun charTypeAsBooleanParameter(name: String? = null, default: Boolean? = null) =
    KnolusChar.typeSpecWith(name, default, KnolusTransformations.TO_BOOLEAN)

fun arrayTypeAsCharArrayParameter(name: String? = null, default: CharArray? = null) =
    KnolusArray.typeSpecWith(name, default, KnolusTransformations.TO_CHAR_ARRAY)

fun variableReferenceTypeParameter(name: String? = null, default: KnolusVariableReference? = null) =
    KnolusVariableReference.typeSpecWith(name, default, KnolusTransformations.VARIABLE_REFERENCE_SELF)

fun nullTypeAsStringParameter(name: String? = null, default: String? = null) =
    KnolusConstants.Null.typeSpecWith(name, default, KnolusTransformations.TO_STRING)

fun undefinedTypeAsStringParameter(name: String? = null, default: String? = null) =
    KnolusConstants.Undefined.typeSpecWith(name, default, KnolusTransformations.TO_STRING)

suspend fun <V : KnolusTypedValue, T> ParameterSpec<V, T>.transform(context: KnolusContext, value: V): T =
    transformation(value, context)

suspend fun <V : KnolusTypedValue, T> Map<String, KnolusTypedValue>.getValue(
    context: KnolusContext,
    spec: ParameterSpec<V, T>,
): T {
    val value = (get(spec.name.sanitiseFunctionIdentifier())?: return spec.default ?: throw NoSuchElementException("Parameter ${spec.name} was not passed, despite being mandatory")) as V
    return spec.transform(context, value)
}

suspend fun <V : KnolusTypedValue, T> Map<String, KnolusTypedValue>.get(
    context: KnolusContext,
    spec: ParameterSpec<V, T>,
): T? {
    val value = get(spec.name.sanitiseFunctionIdentifier()) as? V ?: return spec.default
    return spec.transform(context, value)
}