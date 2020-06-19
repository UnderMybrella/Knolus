@file:Suppress("EXPERIMENTAL_API_USAGE")

package org.abimon.knolus

import org.abimon.knolus.context.KnolusContext
import org.abimon.knolus.types.*
import org.abimon.kornea.errors.common.KorneaResult
import org.abimon.kornea.errors.common.flatMap
import org.abimon.kornea.errors.common.map
import org.abimon.kornea.errors.common.successInline
import kotlin.math.floor
import kotlin.math.log
import kotlin.math.roundToInt

@ExperimentalUnsignedTypes
typealias KnolusTransform<V, T, C> = suspend V.(context: C) -> KorneaResult<T>
typealias KnolusGenericTransform<V, T> = KnolusTransform<V, T, KnolusContext<in Any?>>

inline fun <V, T> asGenericTransform(crossinline lambda: suspend V.(context: KnolusContext<Any?>) -> T): KnolusGenericTransform<V, T> = { t -> KorneaResult.success(lambda(t)) }
inline fun <V, T> asGenericTransformOrNull(crossinline lambda: suspend V.(context: KnolusContext<Any?>) -> T?): KnolusGenericTransform<V, T> = { t -> KorneaResult.successOrEmpty(lambda(t)) }

inline fun <V, T, R, C: KnolusContext<in R>> asTransform(crossinline lambda: suspend V.(context: C) -> T): KnolusTransform<V, T, C> = { t -> KorneaResult.success(lambda(t)) }
inline fun <V, T, R, C: KnolusContext<in R>> asTransformOrNull(crossinline lambda: suspend V.(context: C) -> T?): KnolusTransform<V, T, C> = { t -> KorneaResult.successOrEmpty(lambda(t)) }

sealed class ParameterSpec<V : KnolusTypedValue, T, R, in C: KnolusContext<out R>> {
    abstract val name: String
    abstract val type: KnolusTypedValue.TypeInfo<V>
    abstract val default: T?
    abstract val transformation: KnolusTransform<in V, T, C>

    fun getMemberFunctionName(functionName: String): String =
        type.getMemberFunctionName(type.typeName, functionName)

    fun getMemberPropertyGetterName(propertyName: String): String =
        type.getMemberPropertyGetterName(type.typeName, propertyName)

    fun getMemberOperatorName(operator: ExpressionOperator): String =
        type.getMemberOperatorName(type.typeName, operator)

    abstract infix fun withName(name: String): ParameterSpec<V, T, R, C>
    abstract infix fun withDefault(value: T?): ParameterSpec<V, T, R, C>
}

data class RegularParameterSpec<V : KnolusTypedValue, T, R, in C: KnolusContext<out R>>(
    override val name: String,
    override val type: KnolusTypedValue.TypeInfo<V>,
    override val default: T?,
    override val transformation: KnolusTransform<V, T, C>,
) : ParameterSpec<V, T, R, C>() {
    override fun withName(name: String): ParameterSpec<V, T, R, C> = copy(name = name.sanitiseFunctionIdentifier())
    override fun withDefault(value: T?): ParameterSpec<V, T, R, C> = copy(default = value)
}

data class TypeParameterSpec<V : KnolusTypedValue, T, R, in C: KnolusContext<out R>>(
    override val type: KnolusTypedValue.TypeInfo<V>,
    override val transformation: KnolusTransform<V, T, C>,
) : ParameterSpec<V, T, R, C>() {
    override val name: String = "self"
    override val default: T? = null

    override fun withName(name: String): ParameterSpec<V, T, R, C> = RegularParameterSpec(name, type, null, transformation)
    override fun withDefault(value: T?): ParameterSpec<V, T, R, C> = RegularParameterSpec(name, type, value, transformation)
}

@ExperimentalUnsignedTypes
object KnolusTransformations {
    val NONE: KnolusGenericTransform<KnolusTypedValue, KnolusTypedValue> = { KorneaResult.successInline(this) }
    val TO_STRING: KnolusGenericTransform<KnolusTypedValue, String> = KnolusTypedValue::asString
    val TO_BOOLEAN: KnolusGenericTransform<KnolusTypedValue, Boolean> = KnolusTypedValue::asBoolean
    val TO_NUMBER: KnolusGenericTransform<KnolusTypedValue, Number> = KnolusTypedValue::asNumber

    val TO_CHAR: KnolusGenericTransform<KnolusTypedValue, Char> = { context ->
        when (this) {
            is KnolusString -> if (string.isEmpty()) KorneaResult.empty() else KorneaResult.success(string.first())
            is KnolusChar -> KorneaResult.success(char)
            is KnolusNumericalType -> asNumber(context).flatMap { num -> KorneaResult.success(num.toChar()) }
            else -> KorneaResult.empty()
        }
    }
    val TO_INT: KnolusGenericTransform<KnolusTypedValue, Int> = { asNumber(it).flatMap { num -> KorneaResult.success(num.toInt()) } }
    val TO_DOUBLE: KnolusGenericTransform<KnolusTypedValue, Double> = { asNumber(it).flatMap { num -> KorneaResult.success(num.toDouble()) } }

    val TO_CHAR_ARRAY: KnolusGenericTransform<KnolusTypedValue, CharArray> = { context ->
        when (this) {
            is KnolusString -> KorneaResult.success(string.toCharArray())
            is KnolusArray<*> -> {
                when {
                    array.isArrayOf<KnolusChar>() -> {
                        val array = (array as Array<KnolusChar>)
                        KorneaResult.success(CharArray(array.size) { array[it].char })
                    }
                    array.isArrayOf<KnolusInt>() -> {
                        val array = (array as Array<KnolusInt>)
                        KorneaResult.success(CharArray(array.size) { array[it].number.toChar() })
                    }
                    array.isArrayOf<KnolusDouble>() -> {
                        val array = (array as Array<KnolusDouble>)
                        KorneaResult.success(CharArray(array.size) { array[it].number.roundToInt().toChar() })
                    }
                    array.isArrayOf<KnolusString>() -> KorneaResult.success((array as Array<KnolusString>).flatMap { str -> str.string.toList() }.toCharArray())
                    else -> KorneaResult.empty()
                }
            }
            is KnolusChar -> KorneaResult.success(charArrayOf(char))
            is KnolusConstants.Null -> KorneaResult.empty()
            is KnolusConstants.Undefined -> KorneaResult.empty()
            else -> KorneaResult.empty()
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


    val LAZY_STRING_SELF: KnolusGenericTransform<KnolusLazyString, KnolusLazyString> = { KorneaResult.successInline(this) }
    val LAZY_STRING_TO_INNER: KnolusGenericTransform<KnolusLazyString, Array<KnolusUnion.StringComponent>> = { KorneaResult.success(components) }
    val LAZY_STRING_AS_STRING_ARRAY: KnolusGenericTransform<KnolusLazyString, Array<String>> = { context ->
        val initial: KorneaResult<MutableList<String>> = KorneaResult.success(ArrayList())

        components.fold(initial) { acc, component ->
            acc.flatMap { list ->
                when (component) {
                    is KnolusUnion.StringComponent.RawText ->
                        KorneaResult.success(list.withElement(component.text))
                    is KnolusUnion.StringComponent.VariableReference ->
                        context[component.variableName].flatMap { value ->
                            value.asString(context).map(list::withElement)
                        }
                }
            }
        }.flatMap { list -> KorneaResult.success(list.toTypedArray()) }
    }

    val VARIABLE_REFERENCE_SELF: KnolusGenericTransform<KnolusVariableReference, KnolusVariableReference> = { KorneaResult.successInline(this) }
    val VARIABLE_REFERENCE_TO_INNER: KnolusGenericTransform<KnolusVariableReference, String> = { KorneaResult.success(variableName) }

    val MAX_HEX_STRING = String(CharArray((floor(log(Int.MAX_VALUE.toDouble(), 16.0)).toInt() shl 1) + 2) { '0' })
    val NUMBER_TO_HEX_STRING: KnolusGenericTransform<KnolusNumericalType, String> = {
        asNumber(it).flatMap { num ->
            val int = num.toInt()

            KorneaResult.success(buildString {
                append("0x")
                if (log(int.toDouble(), 16.0).toInt() and 1 == 0) append('0')
                append(int.toString(16))
            })
        }
    }

    val NUMBER_TO_2_BYTE_HEX_STRING: KnolusGenericTransform<KnolusNumericalType, String> by lazy { numberToByteHexString(2) }
    val NUMBER_TO_3_BYTE_HEX_STRING: KnolusGenericTransform<KnolusNumericalType, String> by lazy { numberToByteHexString(3) }
    val NUMBER_TO_4_BYTE_HEX_STRING: KnolusGenericTransform<KnolusNumericalType, String> by lazy { numberToByteHexString(4) }
    val NUMBER_TO_5_BYTE_HEX_STRING: KnolusGenericTransform<KnolusNumericalType, String> by lazy { numberToByteHexString(5) }
    val NUMBER_TO_6_BYTE_HEX_STRING: KnolusGenericTransform<KnolusNumericalType, String> by lazy { numberToByteHexString(6) }
    val NUMBER_TO_7_BYTE_HEX_STRING: KnolusGenericTransform<KnolusNumericalType, String> by lazy { numberToByteHexString(7) }
    val NUMBER_TO_8_BYTE_HEX_STRING: KnolusGenericTransform<KnolusNumericalType, String> by lazy { numberToByteHexString(8) }

    fun numberToByteHexString(byteCount: Int): KnolusGenericTransform<KnolusNumericalType, String> {
        val len = (byteCount shl 1)
        return {
            val int = number.toInt()

            KorneaResult.success(buildString {
                append("0x")
                append(MAX_HEX_STRING.slice(0 until (len - (log(int.toDouble(), 16.0).toInt()))))
                append(int.toString(16))
            })
        }
    }
}

//inline fun <reified T: KnolusTypedValue.TypeInfo<*>, R> typeParameterFor(noinline transformation: KnolusTransform<R>): TypeParameterSpec<R> = TypeParameterSpec(T::class.objectInstance!!, transformation)

fun <V : KnolusTypedValue, T, R, C: KnolusContext<in R>> KnolusTypedValue.TypeInfo<V>.operatorSpecWith(
    transformation: KnolusTransform<V, T, C>,
) = TypeParameterSpec<V, T, R, C>(this, transformation)

fun <V : KnolusTypedValue, T, R, C: KnolusContext<in R>> KnolusTypedValue.TypeInfo<V>.parameterSpecWith(
    name: String,
    default: T? = null,
    transformation: KnolusTransform<V, T, C>,
) = RegularParameterSpec<V, T, R, C>(name, this, default, transformation)

fun <V : KnolusTypedValue, T, R, C: KnolusContext<in R>> KnolusTypedValue.TypeInfo<V>.typeSpecWith(
    name: String? = null,
    default: T? = null,
    transformation: KnolusTransform<V, T, C>,
) =
    if (name == null) TypeParameterSpec(this, transformation) else RegularParameterSpec(
        name,
        this,
        default,
        transformation
    )

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

suspend fun <R, C: KnolusContext<out R>, V : KnolusTypedValue, T> ParameterSpec<V, T, R, C>.transform(context: C, value: V): KorneaResult<T> =
    transformation(value, context)

suspend fun <R, C: KnolusContext<out R>, V : KnolusTypedValue, T> Map<String, KnolusTypedValue>.getValue(
    context: C,
    spec: ParameterSpec<V, T, R, C>,
): KorneaResult<T> {
    val value = get(spec.name.sanitiseFunctionIdentifier())
                ?: return KorneaResult.success(spec.default ?: throw NoSuchElementException("Parameter ${spec.name} was not passed, despite being mandatory"))

    return spec.transform(context, value as V)
}

suspend fun <R, C: KnolusContext<out R>, V : KnolusTypedValue, T> Map<String, KnolusTypedValue>.get(
    context: C,
    spec: ParameterSpec<V, T, R, C>,
): KorneaResult<T> {
    val value = get(spec.name.sanitiseFunctionIdentifier()) as? V ?: return KorneaResult.successOrEmpty(spec.default)
    return spec.transform(context, value)
}