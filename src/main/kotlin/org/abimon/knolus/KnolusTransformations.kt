package org.abimon.knolus

import kotlin.math.roundToInt

@ExperimentalUnsignedTypes
typealias KnolusTransform<T> = suspend VariableValue.(context: KnolusContext) -> T
sealed class ParameterSpec<T> {
    abstract val name: String
    abstract val transformation: KnolusTransform<T>
}
data class LeafParameterSpec<T>(override val name: String, override val transformation: KnolusTransform<T>): ParameterSpec<T>()
data class TypeParameterSpec<T>(val typeName: String, override val transformation: KnolusTransform<T>): ParameterSpec<T>() {
    override val name: String = "self"
}

@ExperimentalUnsignedTypes
object KnolusTransformations {
    val FLATTENED: KnolusTransform<VariableValue> = VariableValue::flatten

    val TO_STRING: KnolusTransform<String> = VariableValue::asString
    val TO_BOOLEAN: KnolusTransform<Boolean> = VariableValue::asBoolean
    val TO_NUMBER: KnolusTransform<Number> = VariableValue::asNumber

    val TO_CHAR: KnolusTransform<Char> = {  context ->
        when (val flat = fullyFlattened(context)) {
            is VariableValue.StringType -> flat.string.firstOrNull() ?: '\u0000'
            is VariableValue.CharType -> flat.char
            else -> flat.asNumber(context).toChar()
        }
    }
    val TO_INT: KnolusTransform<Int> = { asNumber(it).toInt() }
    val TO_DOUBLE: KnolusTransform<Double> = { asNumber(it).toDouble() }

    val TO_CHAR_ARRAY: KnolusTransform<CharArray> = { context ->
        when (val flat = fullyFlattened(context)) {
            is VariableValue.StringType -> flat.string.toCharArray()
            is VariableValue.ArrayType<*> -> {
                when {
                    flat.array.isArrayOf<VariableValue.CharType>() -> {
                        val array = (flat.array as Array<VariableValue.CharType>)
                        CharArray(array.size) { array[it].char }
                    }
                    flat.array.isArrayOf<VariableValue.IntegerType>() -> {
                        val array = (flat.array as Array<VariableValue.IntegerType>)
                        CharArray(array.size) { array[it].integer.toChar() }
                    }
                    flat.array.isArrayOf<VariableValue.DecimalType>() -> {
                        val array = (flat.array as Array<VariableValue.DecimalType>)
                        CharArray(array.size) { array[it].decimal.roundToInt().toChar() }
                    }
                    flat.array.isArrayOf<VariableValue.StringType>() -> (flat.array as Array<VariableValue.StringType>).flatMap { str -> str.string.toList() }.toCharArray()
                    else -> CharArray(flat.array.size) { flat.array[it].asNumber(context).toChar() }
                }
            }
            is VariableValue.CharType -> charArrayOf(flat.char)
            is VariableValue.NullType -> charArrayOf()
            is VariableValue.UndefinedType -> charArrayOf()
            else -> charArrayOf(flat.asNumber(context).toChar())
        }
    }
    val TO_CHAR_OR_NULL: KnolusTransform<Char?> = { context ->
        when (val flat = fullyFlattened(context)) {
            is VariableValue.StringType -> flat.string.firstOrNull()
            is VariableValue.CharType -> flat.char
            is VariableValue.NullType -> null
            is VariableValue.UndefinedType -> null
            else -> flat.asNumber(context).toChar()
        }
    }

    val FLATTENED_TO_INNER: KnolusTransform<Any> = { context ->
        when (val flat = this.flatten(context)) {
            is VariableValue.StringComponents -> flat.components
            is VariableValue.StringType -> flat.string
            is VariableValue.BooleanType -> flat.boolean
            is VariableValue.CharType -> flat.char
            is VariableValue.IntegerType -> flat.integer
            is VariableValue.DecimalType -> flat.decimal
            is VariableValue.VariableReferenceType -> flat.variableName
            is VariableValue.MemberVariableReferenceType -> Pair(flat.variableName, flat.propertyName)
            is VariableValue.FunctionCallType -> Pair(flat.name, flat.parameters)
            is VariableValue.MemberFunctionCallType -> Triple(flat.variableName, flat.functionName, flat.parameters)
            is VariableValue.ArrayType<*> -> flat.array
            is VariableValue.NullType -> flat
            is VariableValue.UndefinedType -> flat
            is VariableValue.ExpressionType -> Pair(flat.startValue, flat.ops)
        }
    }
    val TO_INNER: KnolusTransform<Any> = { context ->
        when (val flat = this) {
            is VariableValue.StringComponents -> flat.components
            is VariableValue.StringType -> flat.string
            is VariableValue.BooleanType -> flat.boolean
            is VariableValue.CharType -> flat.char
            is VariableValue.IntegerType -> flat.integer
            is VariableValue.DecimalType -> flat.decimal
            is VariableValue.VariableReferenceType -> flat.variableName
            is VariableValue.MemberVariableReferenceType -> Pair(flat.variableName, flat.propertyName)
            is VariableValue.FunctionCallType -> Pair(flat.name, flat.parameters)
            is VariableValue.MemberFunctionCallType -> Triple(flat.variableName, flat.functionName, flat.parameters)
            is VariableValue.ArrayType<*> -> flat.array
            is VariableValue.NullType -> flat
            is VariableValue.UndefinedType -> flat
            is VariableValue.ExpressionType -> Pair(flat.startValue, flat.ops)
        }
    }


    val STRING_COMPONENTS_CAST: KnolusTransform<VariableValue.StringComponents> = { this as VariableValue.StringComponents }
    val STRING_COMPONENTS_TO_INNER: KnolusTransform<Array<KnolusUnion.StringComponent>> = { (this as VariableValue.StringComponents).components }
    val STRING_COMPONENTS_AS_STRING_ARRAY: KnolusTransform<Array<String>> = { context ->
        (this as VariableValue.StringComponents).components.mapNotNull { component ->
            when (component) {
                is KnolusUnion.StringComponent.RawText -> component.text
                is KnolusUnion.StringComponent.VariableReference -> context[component.variableName]?.asString(context)
            }
        }.toTypedArray()
    }

    val VARIABLE_REFERENCE_CAST: KnolusTransform<VariableValue.VariableReferenceType> = { this as VariableValue.VariableReferenceType }
    val VARIABLE_REFERENCE_TO_INNER: KnolusTransform<String> = { (this as VariableValue.VariableReferenceType).variableName }
}

fun stringTypeParameter(): TypeParameterSpec<String> = TypeParameterSpec("String", KnolusTransformations.TO_STRING)

fun stringCompsTypeParameter(): TypeParameterSpec<VariableValue.StringComponents> = TypeParameterSpec("StringComponents", KnolusTransformations.STRING_COMPONENTS_CAST)
fun stringCompsTypeAsComponentsParameter(): TypeParameterSpec<Array<KnolusUnion.StringComponent>> = TypeParameterSpec("StringComponents", KnolusTransformations.STRING_COMPONENTS_TO_INNER)
fun stringCompsTypeAsStringArrayParameter(): TypeParameterSpec<Array<String>> = TypeParameterSpec("StringComponents", KnolusTransformations.STRING_COMPONENTS_AS_STRING_ARRAY)
fun stringCompsTypeAsStringParameter(): TypeParameterSpec<String> = TypeParameterSpec("StringComponents", KnolusTransformations.TO_STRING)

fun booleanTypeParameter(): TypeParameterSpec<Boolean> = TypeParameterSpec("Boolean", KnolusTransformations.TO_BOOLEAN)
fun booleanTypeAsIntParameter(): TypeParameterSpec<Int> = TypeParameterSpec("Boolean", KnolusTransformations.TO_INT)

fun numberTypeParameter(): TypeParameterSpec<Number> = TypeParameterSpec("Number", KnolusTransformations.TO_NUMBER)
fun numberTypeAsIntParameter(): TypeParameterSpec<Int> = TypeParameterSpec("Number", KnolusTransformations.TO_INT)
fun numberTypeAsDoubleParameter(): TypeParameterSpec<Double> = TypeParameterSpec("Number", KnolusTransformations.TO_DOUBLE)
fun numberTypeAsBooleanParameter(): TypeParameterSpec<Boolean> = TypeParameterSpec("Number", KnolusTransformations.TO_BOOLEAN)
fun numberTypeAsCharParameter(): TypeParameterSpec<Char> = TypeParameterSpec("Number", KnolusTransformations.TO_CHAR)

fun intTypeParameter(): TypeParameterSpec<Int> = TypeParameterSpec("Integer", KnolusTransformations.TO_INT)
fun intTypeAsDoubleParameter(): TypeParameterSpec<Double> = TypeParameterSpec("Integer", KnolusTransformations.TO_DOUBLE)
fun intTypeAsBooleanParameter(): TypeParameterSpec<Boolean> = TypeParameterSpec("Integer", KnolusTransformations.TO_BOOLEAN)
fun intTypeAsCharParameter(): TypeParameterSpec<Char> = TypeParameterSpec("Integer", KnolusTransformations.TO_CHAR)

fun doubleTypeParameter(): TypeParameterSpec<Double> = TypeParameterSpec("Decimal", KnolusTransformations.TO_DOUBLE)
fun doubleTypeAsIntParameter(): TypeParameterSpec<Int> = TypeParameterSpec("Decimal", KnolusTransformations.TO_INT)
fun doubleTypeAsBooleanParameter(): TypeParameterSpec<Boolean> = TypeParameterSpec("Decimal", KnolusTransformations.TO_BOOLEAN)
fun doubleTypeAsCharParameter(): TypeParameterSpec<Char> = TypeParameterSpec("Decimal", KnolusTransformations.TO_CHAR)

fun charTypeParameter(): TypeParameterSpec<Char> = TypeParameterSpec("Char", KnolusTransformations.TO_CHAR)
fun charTypeAsIntParameter(): TypeParameterSpec<Int> = TypeParameterSpec("Char", KnolusTransformations.TO_INT)
fun charTypeAsDoubleParameter(): TypeParameterSpec<Double> = TypeParameterSpec("Char", KnolusTransformations.TO_DOUBLE)
fun charTypeAsBooleanParameter(): TypeParameterSpec<Boolean> = TypeParameterSpec("Char", KnolusTransformations.TO_BOOLEAN)

fun variableReferenceTypeParameter(): TypeParameterSpec<VariableValue.VariableReferenceType> = TypeParameterSpec("VariableReference", KnolusTransformations.VARIABLE_REFERENCE_CAST)



fun stringParameter(name: String): LeafParameterSpec<String> = LeafParameterSpec(name, KnolusTransformations.TO_STRING)
fun charParameter(name: String): LeafParameterSpec<Char> = LeafParameterSpec(name, KnolusTransformations.TO_CHAR)
fun charArrayParameter(name: String): LeafParameterSpec<CharArray> = LeafParameterSpec(name, KnolusTransformations.TO_CHAR_ARRAY)
fun charOrNullParameter(name: String): LeafParameterSpec<Char?> = LeafParameterSpec(name, KnolusTransformations.TO_CHAR_OR_NULL)
fun booleanParameter(name: String): LeafParameterSpec<Boolean> = LeafParameterSpec(name, KnolusTransformations.TO_BOOLEAN)
fun numericalParameter(name: String): LeafParameterSpec<Number> = LeafParameterSpec(name, KnolusTransformations.TO_NUMBER)
fun intParameter(name: String): LeafParameterSpec<Int> = LeafParameterSpec(name, KnolusTransformations.TO_INT)
fun doubleParameter(name: String): LeafParameterSpec<Double> = LeafParameterSpec(name, KnolusTransformations.TO_DOUBLE)

suspend fun <T> ParameterSpec<T>.transform(context: KnolusContext, value: VariableValue): T =
    transformation(value, context)

suspend fun <T> Map<String, VariableValue>.getValue(context: KnolusContext, spec: ParameterSpec<T>): T =
    spec.transformation(getValue(spec.name.sanitiseFunctionIdentifier()), context)

fun <T> KnolusFunctionBuilder<T>.addParameter(
    spec: ParameterSpec<*>,
    default: VariableValue? = null
): KnolusFunctionBuilder<T> {
    parameters.add(Pair(spec.name.sanitiseFunctionIdentifier(), default))

    return this
}