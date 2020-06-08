@file:Suppress("EXPERIMENTAL_API_USAGE")

package org.abimon.knolus

import org.abimon.knolus.types.*
import kotlin.math.roundToInt

@ExperimentalUnsignedTypes
typealias KnolusTransform<T> = suspend KnolusTypedValue.(context: KnolusContext) -> T
sealed class ParameterSpec<T> {
    abstract val name: String
    abstract val transformation: KnolusTransform<T>
}
data class LeafParameterSpec<T>(override val name: String, override val transformation: KnolusTransform<T>): ParameterSpec<T>()
data class TypeParameterSpec<T>(val typeInfo: KnolusTypedValue.TypeInfo<*>, override val transformation: KnolusTransform<T>): ParameterSpec<T>() {
    override val name: String = "self"

    fun getMemberFunctionName(functionName: String): String = typeInfo.getMemberFunctionName(typeInfo.typeName, functionName)
    fun getMemberPropertyGetterName(propertyName: String): String = typeInfo.getMemberPropertyGetterName(typeInfo.typeName, propertyName)
    fun getMemberOperatorName(operator: ExpressionOperator): String = typeInfo.getMemberOperatorName(typeInfo.typeName, operator)
}

@ExperimentalUnsignedTypes
object KnolusTransformations {
    val NONE: KnolusTransform<KnolusTypedValue> = { this }
    val TO_STRING: KnolusTransform<String> = KnolusTypedValue::asString
    val TO_BOOLEAN: KnolusTransform<Boolean> = KnolusTypedValue::asBoolean
    val TO_NUMBER: KnolusTransform<Number> = KnolusTypedValue::asNumber

    val TO_CHAR: KnolusTransform<Char> = {  context ->
        when (this) {
            is KnolusString -> string.firstOrNull() ?: '\u0000'
            is KnolusChar -> char
            is KnolusNumericalType -> asNumber(context).toChar()
            else -> asNumber(context).toChar()
        }
    }
    val TO_INT: KnolusTransform<Int> = { asNumber(it).toInt() }
    val TO_DOUBLE: KnolusTransform<Double> = { asNumber(it).toDouble() }

    val TO_CHAR_ARRAY: KnolusTransform<CharArray> = { context ->
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
                    array.isArrayOf<KnolusString>() -> (array as Array<KnolusString>).flatMap { str -> str.string.toList() }.toCharArray()
                    else -> CharArray(array.size) { array[it].asNumber(context).toChar() }
                }
            }
            is KnolusChar -> charArrayOf(char)
            is KnolusConstants.Null -> charArrayOf()
            is KnolusConstants.Undefined -> charArrayOf()
            else -> charArrayOf(asNumber(context).toChar())
        }
    }
    val TO_CHAR_OR_NULL: KnolusTransform<Char?> = { context ->
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


    val STRING_COMPONENTS_CAST: KnolusTransform<KnolusLazyString> = { this as KnolusLazyString }
    val STRING_COMPONENTS_TO_INNER: KnolusTransform<Array<KnolusUnion.StringComponent>> = { (this as KnolusLazyString).components }
    val STRING_COMPONENTS_AS_STRING_ARRAY: KnolusTransform<Array<String>> = { context ->
        (this as KnolusLazyString).components.mapNotNull { component ->
            when (component) {
                is KnolusUnion.StringComponent.RawText -> component.text
                is KnolusUnion.StringComponent.VariableReference -> context[component.variableName]?.asString(context)
            }
        }.toTypedArray()
    }

    val VARIABLE_REFERENCE_CAST: KnolusTransform<KnolusVariableReference> = { this as KnolusVariableReference }
    val VARIABLE_REFERENCE_TO_INNER: KnolusTransform<String> = { (this as KnolusVariableReference).variableName }
}

//inline fun <reified T: KnolusTypedValue.TypeInfo<*>, R> typeParameterFor(noinline transformation: KnolusTransform<R>): TypeParameterSpec<R> = TypeParameterSpec(T::class.objectInstance!!, transformation)

fun <T> KnolusTypedValue.TypeInfo<*>.parameterSpecWith(transformation: KnolusTransform<T>) = TypeParameterSpec(this, transformation)

fun objectTypeParameter() = KnolusObject.parameterSpecWith(KnolusTransformations.NONE)
fun objectTypeAsStringParameter() = KnolusObject.parameterSpecWith(KnolusTransformations.TO_STRING)

fun stringTypeParameter() = KnolusString.parameterSpecWith(KnolusTransformations.TO_STRING)

//fun stringCompsTypeParameter(): TypeParameterSpec<KnolusLazyString> = TypeParameterSpec("StringComponents", KnolusTransformations.STRING_COMPONENTS_CAST)
//fun stringCompsTypeAsComponentsParameter(): TypeParameterSpec<Array<KnolusUnion.StringComponent>> = TypeParameterSpec("StringComponents", KnolusTransformations.STRING_COMPONENTS_TO_INNER)
//fun stringCompsTypeAsStringArrayParameter(): TypeParameterSpec<Array<String>> = TypeParameterSpec("StringComponents", KnolusTransformations.STRING_COMPONENTS_AS_STRING_ARRAY)
//fun stringCompsTypeAsStringParameter(): TypeParameterSpec<String> = TypeParameterSpec("StringComponents", KnolusTransformations.TO_STRING)

fun booleanTypeParameter() = KnolusBoolean.parameterSpecWith(KnolusTransformations.TO_BOOLEAN)
fun booleanTypeAsIntParameter() = KnolusBoolean.parameterSpecWith(KnolusTransformations.TO_INT)

fun numberTypeParameter() = KnolusNumericalType.parameterSpecWith(KnolusTransformations.TO_NUMBER)
fun numberTypeAsIntParameter() = KnolusNumericalType.parameterSpecWith(KnolusTransformations.TO_INT)
fun numberTypeAsDoubleParameter() = KnolusNumericalType.parameterSpecWith(KnolusTransformations.TO_DOUBLE)
fun numberTypeAsBooleanParameter() = KnolusNumericalType.parameterSpecWith(KnolusTransformations.TO_BOOLEAN)
fun numberTypeAsCharParameter() = KnolusNumericalType.parameterSpecWith(KnolusTransformations.TO_CHAR)

fun intTypeParameter() = KnolusInt.parameterSpecWith(KnolusTransformations.TO_INT)
fun intTypeAsDoubleParameter() = KnolusInt.parameterSpecWith(KnolusTransformations.TO_DOUBLE)
fun intTypeAsBooleanParameter() = KnolusInt.parameterSpecWith(KnolusTransformations.TO_BOOLEAN)
fun intTypeAsCharParameter() = KnolusInt.parameterSpecWith(KnolusTransformations.TO_CHAR)

fun doubleTypeParameter() = KnolusDouble.parameterSpecWith(KnolusTransformations.TO_DOUBLE)
fun doubleTypeAsIntParameter() = KnolusDouble.parameterSpecWith(KnolusTransformations.TO_INT)
fun doubleTypeAsBooleanParameter() = KnolusDouble.parameterSpecWith(KnolusTransformations.TO_BOOLEAN)
fun doubleTypeAsCharParameter() = KnolusDouble.parameterSpecWith(KnolusTransformations.TO_CHAR)

fun charTypeParameter() = KnolusChar.parameterSpecWith(KnolusTransformations.TO_CHAR)
fun charTypeAsIntParameter() = KnolusChar.parameterSpecWith(KnolusTransformations.TO_INT)
fun charTypeAsDoubleParameter() = KnolusChar.parameterSpecWith(KnolusTransformations.TO_DOUBLE)
fun charTypeAsBooleanParameter() = KnolusChar.parameterSpecWith(KnolusTransformations.TO_BOOLEAN)

fun variableReferenceTypeParameter() = KnolusVariableReference.parameterSpecWith(KnolusTransformations.VARIABLE_REFERENCE_CAST)



fun stringParameter(name: String): LeafParameterSpec<String> = LeafParameterSpec(name, KnolusTransformations.TO_STRING)
fun charParameter(name: String): LeafParameterSpec<Char> = LeafParameterSpec(name, KnolusTransformations.TO_CHAR)
fun charArrayParameter(name: String): LeafParameterSpec<CharArray> = LeafParameterSpec(name, KnolusTransformations.TO_CHAR_ARRAY)
fun charOrNullParameter(name: String): LeafParameterSpec<Char?> = LeafParameterSpec(name, KnolusTransformations.TO_CHAR_OR_NULL)
fun booleanParameter(name: String): LeafParameterSpec<Boolean> = LeafParameterSpec(name, KnolusTransformations.TO_BOOLEAN)
fun numericalParameter(name: String): LeafParameterSpec<Number> = LeafParameterSpec(name, KnolusTransformations.TO_NUMBER)
fun intParameter(name: String): LeafParameterSpec<Int> = LeafParameterSpec(name, KnolusTransformations.TO_INT)
fun doubleParameter(name: String): LeafParameterSpec<Double> = LeafParameterSpec(name, KnolusTransformations.TO_DOUBLE)

suspend fun <T> ParameterSpec<T>.transform(context: KnolusContext, value: KnolusTypedValue): T =
    transformation(value, context)

suspend fun <T> Map<String, KnolusTypedValue>.getValue(context: KnolusContext, spec: ParameterSpec<T>): T =
    spec.transformation(getValue(spec.name.sanitiseFunctionIdentifier()), context)

fun <T> KnolusFunctionBuilder<T>.addParameter(
    spec: ParameterSpec<*>,
    default: KnolusTypedValue? = null
): KnolusFunctionBuilder<T> {
    parameters.add(Pair(spec.name.sanitiseFunctionIdentifier(), default))

    return this
}