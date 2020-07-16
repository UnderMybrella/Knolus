package dev.brella.knolus

import dev.brella.knolus.context.KnolusContext
import dev.brella.knolus.types.KnolusObject
import dev.brella.knolus.types.KnolusTypedValue
import org.abimon.kornea.errors.common.*

@ExperimentalUnsignedTypes
sealed class KnolusUnion {
    interface Action<T> {
        suspend fun <R> run(context: KnolusContext<R>): KorneaResult<T>
    }

    abstract class UnionAction<T>: KnolusUnion(), Action<T>

    sealed class StringComponent : KnolusUnion() {
        data class RawText(val text: String) : StringComponent()
        data class VariableReference(val variableName: String) : StringComponent()
    }

    sealed class VariableValue<out T : KnolusTypedValue>(open val value: T) : KnolusUnion() {
        data class Lazy<out E : KnolusTypedValue, T : KnolusTypedValue.RuntimeValue<E>>(override val value: T) :
            VariableValue<T>(value)

        data class Stable<T : KnolusTypedValue>(override val value: T) : VariableValue<T>(value)
    }

    data class ArrayContents(val inner: Array<KnolusTypedValue>) : KnolusUnion() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ArrayContents

            if (!inner.contentEquals(other.inner)) return false

            return true
        }

        override fun hashCode(): Int {
            return inner.contentHashCode()
        }
    }

    data class ScopeType(val lines: Array<KnolusUnion>) : KnolusUnion() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ScopeType

            if (!lines.contentEquals(other.lines)) return false

            return true
        }

        override fun hashCode(): Int {
            return lines.contentHashCode()
        }
    }

    class FunctionParameterType private constructor(val name: String?, val parameter: KnolusTypedValue) :
        KnolusUnion() {
        companion object {
            operator fun invoke(name: String?, parameter: KnolusTypedValue) =
                FunctionParameterType(name?.sanitiseFunctionIdentifier(), parameter)
        }

        operator fun component1() = name
        operator fun component2() = parameter

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as FunctionParameterType

            if (name != other.name) return false
            if (parameter != other.parameter) return false

            return true
        }

        override fun hashCode(): Int {
            var result = name?.hashCode() ?: 0
            result = 31 * result + parameter.hashCode()
            return result
        }
    }

    //    data class ScriptParameterType(val name: String?, val parameter: KnolusTypedValue) : KnolusUnion()
    data class ReturnStatement(val value: KnolusTypedValue) : KnolusUnion()

//    data class ScriptCallAction(val name: String, val parameters: Array<ScriptParameterType>) : KnolusUnion(), Action {
//        override suspend fun <R> run(context: KnolusContext<R>) {
//            context.invokeScript(name, parameters)
//        }
//    }

    data class DeclareVariableAction(
        val variableName: String,
        private val initialVariableValue: KnolusTypedValue,
        val global: Boolean = false,
    ) : KnolusUnion(), Action<KnolusTypedValue?> {
        override suspend fun <R> run(context: KnolusContext<R>): KorneaResult<KnolusTypedValue?> {
            if (initialVariableValue is KnolusTypedValue.UnsureValue<*> && initialVariableValue.needsEvaluation(context)) {
                val evaluated = initialVariableValue.evaluate(context).getOrBreak { error ->
                    return KorneaResult.errorAsIllegalState(
                        KnolusContext.FAILED_TO_SET_VARIABLE,
                        "Failed to set variable $variableName (evaluation failed)",
                        error
                    )
                }

                val result = context.set(variableName, global, evaluated)
                if (result is KorneaResult.Success)
                    return KorneaResult.successInline(evaluated)

                return KorneaResult.errorAsIllegalState(
                    KnolusContext.FAILED_TO_SET_VARIABLE,
                    "Failed to set variable $variableName with value $evaluated",
                    result
                )
            } else {
                val result = context.set(variableName, global, initialVariableValue)
                if (result is KorneaResult.Success)
                    return KorneaResult.successInline(initialVariableValue)

                return KorneaResult.errorAsIllegalState(
                    KnolusContext.FAILED_TO_SET_VARIABLE,
                    "Failed to set variable $variableName with value $initialVariableValue",
                    result
                )
            }
        }
    }

    data class AssignVariableAction(
        val variableName: String,
        private val initialVariableValue: KnolusTypedValue,
        val global: Boolean = false,
    ) : KnolusUnion(), Action<KnolusTypedValue?> {
        override suspend fun <R> run(context: KnolusContext<R>): KorneaResult<KnolusTypedValue?> =
            context.containsWithResult(variableName)
                .flatMap {
                    if (initialVariableValue is KnolusTypedValue.UnsureValue<*> && initialVariableValue.needsEvaluation(context)) {
                        val evaluated = initialVariableValue.evaluate(context).getOrBreak { error ->
                            return KorneaResult.errorAsIllegalState(
                                KnolusContext.FAILED_TO_SET_VARIABLE,
                                "Failed to set variable $variableName (evaluation failed)",
                                error
                            )
                        }

                        val result = context.set(variableName, global, evaluated)
                        if (result is KorneaResult.Success)
                            return@flatMap KorneaResult.successInline(evaluated)

                        return@flatMap KorneaResult.errorAsIllegalState(
                            KnolusContext.FAILED_TO_SET_VARIABLE,
                            "Failed to set variable $variableName with value $evaluated",
                            result
                        )
                    } else {
                        return@flatMap context.set(variableName, global, initialVariableValue)
                    }
                }.switchIfEmpty { empty ->
                    KorneaResult.errorAsIllegalState(
                        KnolusContext.UNDECLARED_VARIABLE,
                        "Undeclared variable $variableName",
                        empty
                    )
                }
    }

    //  TODO: Implement proper typing
    //  TODO: Implement optional/default policies
    data class FunctionDeclaration(
        val functionName: String,
        val parameterNames: Array<String>,
        val global: Boolean = false,
        val body: ScopeType?,
    ) : KnolusUnion(), Action<KnolusFunction<KnolusTypedValue?, *, *>> {
        //TODO: Make sure this is actually invoked after restrictions are checked
        suspend operator fun <R> invoke(
            context: KnolusContext<out R>,
            parameters: Map<String, KnolusTypedValue>,
        ): KnolusTypedValue? = (body?.runDirect(context, parameters) as? ScopeResult.Returned<*>)?.value

        override suspend fun <R> run(context: KnolusContext<R>): KorneaResult<KnolusFunction<KnolusTypedValue?, R, *>> =
            context.register(
                functionName, KnolusFunction<KnolusTypedValue?, R, KnolusContext<out R>>(
                    Array(parameterNames.size) {
                        KnolusDeclaredFunctionParameter.Concrete(
                            parameterNames[it],
                            KnolusObject,
                            KnolusFunctionParameterMissingPolicy.Mandatory
                        )
                    }, func = this::invoke
                ), global
            )
    }
}

sealed class ExpressionOperator(val functionCallName: String) : KnolusUnion() {
    object PLUS : ExpressionOperator("plus")
    object MINUS : ExpressionOperator("minus")
    object DIVIDE : ExpressionOperator("div")
    object MULTIPLY : ExpressionOperator("times")

    //    object REMAINDER: ExpressionOperator("rem")
    object EXPONENTIAL : ExpressionOperator("exp")

//    object AND: ExpressionOperator("and")
//    object OR: ExpressionOperator("or")
//    object SHIFT_LEFT: ExpressionOperator("shl")
//    object SHIFT_RIGHT: ExpressionOperator("shr")
//    object INVERT: ExpressionOperator("inv")
}

//sealed class ExpressionOperation : KnolusUnion() {
//    object PLUS : ExpressionOperation() {
//        override suspend fun operate(
//            context: KnolusContext,
//            first: VariableValue,
//            second: VariableValue,
//        ): VariableValue = when (first) {
//            is VariableValue.StringType -> VariableValue.StringType(first.string.plus(second.asString(context)))
//            is VariableValue.CharType -> when (second) {
//                VariableValue.NullType -> first
//                VariableValue.UndefinedType -> VariableValue.UndefinedType
//
//                is VariableValue.StringComponents -> operate(context, first, second.flatten(context))
//                is VariableValue.VariableReferenceType -> operate(context, first, second.flatten(context))
//                is VariableValue.MemberVariableReferenceType -> operate(context, first, second.flatten(context))
//                is VariableValue.FunctionCallType -> operate(context, first, second.flatten(context))
//                is VariableValue.MemberFunctionCallType -> operate(context, first, second.flatten(context))
//                is VariableValue.ExpressionType -> operate(context, first, second.flatten(context))
//
//                is VariableValue.StringType -> VariableValue.StringType("${first.char}${second.string}")
//
//                else -> VariableValue.CharType((first.char.toInt() + second.asNumber(context).toInt()).toChar())
//            }
//            is VariableValue.BooleanType -> VariableValue.BooleanType(
//                first.boolean.xor(
//                    second.asNumber(context).toInt() != 0
//                )
//            )
//            is VariableValue.IntegerType -> VariableValue.IntegerType(
//                first.integer + second.asNumber(context).toInt()
//            )
//            is VariableValue.DecimalType -> VariableValue.DecimalType(
//                first.decimal + second.asNumber(context).toDouble()
//            )
//            is VariableValue.ArrayType<*> -> first.addByCoercion(context, second)
//
//            is VariableValue.StringComponents -> operate(context, first.flatten(context), second)
//            is VariableValue.VariableReferenceType -> operate(context, first.flatten(context), second)
//            is VariableValue.MemberVariableReferenceType -> operate(context, first.flatten(context), second)
//            is VariableValue.FunctionCallType -> operate(context, first.flatten(context), second)
//            is VariableValue.MemberFunctionCallType -> operate(context, first.flatten(context), second)
//            is VariableValue.ExpressionType -> operate(context, first.flatten(context), second)
//
//            VariableValue.NullType -> second
//            VariableValue.UndefinedType -> second
//        }
//    }
//
//    object MINUS : ExpressionOperation() {
//        override suspend fun operate(
//            context: KnolusContext,
//            first: VariableValue,
//            second: VariableValue,
//        ): VariableValue = when (first) {
//            is VariableValue.StringType -> when (second) {
//                is VariableValue.StringType -> VariableValue.StringType(first.string.removeSuffix(second.string))
//                is VariableValue.CharType -> VariableValue.StringType(first.string.trimEnd(second.char))
//
//                is VariableValue.StringComponents -> operate(context, first, second.flatten(context))
//                is VariableValue.VariableReferenceType -> operate(context, first, second.flatten(context))
//                is VariableValue.MemberVariableReferenceType -> operate(context, first, second.flatten(context))
//                is VariableValue.FunctionCallType -> operate(context, first, second.flatten(context))
//                is VariableValue.MemberFunctionCallType -> operate(context, first, second.flatten(context))
//                is VariableValue.ExpressionType -> operate(context, first, second.flatten(context))
//
//                is VariableValue.NullType -> first
//                else -> VariableValue.UndefinedType
//            }
//            is VariableValue.CharType -> when (second) {
//                VariableValue.NullType -> first
//                VariableValue.UndefinedType -> VariableValue.UndefinedType
//
//                is VariableValue.VariableReferenceType -> operate(context, first, second.flatten(context))
//                is VariableValue.MemberVariableReferenceType -> operate(context, first, second.flatten(context))
//                is VariableValue.FunctionCallType -> operate(context, first, second.flatten(context))
//                is VariableValue.MemberFunctionCallType -> operate(context, first, second.flatten(context))
//                is VariableValue.ExpressionType -> operate(context, first, second.flatten(context))
//
//                is VariableValue.IntegerType -> VariableValue.CharType((first.char.toInt() - second.integer).toChar())
//                is VariableValue.DecimalType -> VariableValue.CharType((first.char.toInt() - second.decimal.toInt()).toChar())
//
//                else -> VariableValue.UndefinedType
//            }
//            is VariableValue.BooleanType -> VariableValue.BooleanType(
//                first.boolean.xor(
//                    second.asNumber(context).toInt() != 0
//                )
//            )
//            is VariableValue.IntegerType -> VariableValue.IntegerType(
//                first.integer - second.asNumber(context).toInt()
//            )
//            is VariableValue.DecimalType -> VariableValue.DecimalType(
//                first.decimal - second.asNumber(context).toDouble()
//            )
//            is VariableValue.ArrayType<*> -> first.dropByCoersion(context, second)
//
//            is VariableValue.StringComponents -> operate(context, first.flatten(context), second)
//            is VariableValue.VariableReferenceType -> operate(context, first.flatten(context), second)
//            is VariableValue.MemberVariableReferenceType -> operate(context, first.flatten(context), second)
//            is VariableValue.FunctionCallType -> operate(context, first.flatten(context), second)
//            is VariableValue.MemberFunctionCallType -> operate(context, first.flatten(context), second)
//            is VariableValue.ExpressionType -> operate(context, first.flatten(context), second)
//
//            is VariableValue.NullType -> second
//            is VariableValue.UndefinedType -> second
//        }
//    }
//
//    object DIVIDE : ExpressionOperation() {
//        override suspend fun operate(
//            context: KnolusContext,
//            first: VariableValue,
//            second: VariableValue,
//        ): VariableValue = when (first) {
//            is VariableValue.StringType -> when (second) {
//                VariableValue.BooleanType.TRUE -> first
//                VariableValue.BooleanType.FALSE -> VariableValue.NullType
//
//                is VariableValue.VariableReferenceType -> operate(context, first, second.flatten(context))
//                is VariableValue.MemberVariableReferenceType -> operate(context, first, second.flatten(context))
//                is VariableValue.FunctionCallType -> operate(context, first, second.flatten(context))
//                is VariableValue.MemberFunctionCallType -> operate(context, first, second.flatten(context))
//                is VariableValue.ExpressionType -> operate(context, first, second.flatten(context))
//
//                is VariableValue.NullType -> first
//
//                else -> VariableValue.UndefinedType
//            }
//            is VariableValue.CharType -> when (second) {
//                VariableValue.BooleanType.TRUE -> first
//                VariableValue.BooleanType.FALSE -> VariableValue.NullType
//                VariableValue.NullType -> first
//
//                is VariableValue.VariableReferenceType -> operate(context, first, second.flatten(context))
//                is VariableValue.MemberVariableReferenceType -> operate(context, first, second.flatten(context))
//                is VariableValue.FunctionCallType -> operate(context, first, second.flatten(context))
//                is VariableValue.MemberFunctionCallType -> operate(context, first, second.flatten(context))
//                is VariableValue.ExpressionType -> operate(context, first, second.flatten(context))
//
//                is VariableValue.IntegerType -> VariableValue.CharType((first.char.toInt() / second.integer).toChar())
//                is VariableValue.DecimalType -> VariableValue.CharType((first.char.toDouble() / second.decimal).roundToInt().toChar())
//
//                else -> VariableValue.UndefinedType
//            }
//            is VariableValue.BooleanType -> VariableValue.BooleanType(
//                first.boolean.and(
//                    second.asNumber(context).toInt() != 0
//                )
//            )
//            is VariableValue.IntegerType -> VariableValue.IntegerType(
//                first.integer / second.asNumber(context).toInt()
//            )
//            is VariableValue.DecimalType -> VariableValue.DecimalType(
//                first.decimal / second.asNumber(context).toDouble()
//            )
//            is VariableValue.ArrayType<*> -> VariableValue.UndefinedType
//
//            is VariableValue.StringComponents -> operate(context, first.flatten(context), second)
//            is VariableValue.VariableReferenceType -> operate(context, first.flatten(context), second)
//            is VariableValue.MemberVariableReferenceType -> operate(context, first.flatten(context), second)
//            is VariableValue.FunctionCallType -> operate(context, first.flatten(context), second)
//            is VariableValue.MemberFunctionCallType -> operate(context, first.flatten(context), second)
//            is VariableValue.ExpressionType -> operate(context, first.flatten(context), second)
//
//            is VariableValue.NullType -> second
//            is VariableValue.UndefinedType -> second
//        }
//    }
//
//    object MULTIPLY : ExpressionOperation() {
//        override suspend fun operate(
//            context: KnolusContext,
//            first: VariableValue,
//            second: VariableValue,
//        ): VariableValue = when (first) {
//            is VariableValue.StringType -> when (second) {
//                is VariableValue.BooleanType -> if (second.boolean) first else VariableValue.NullType
//                is VariableValue.IntegerType -> buildStringVariable {
//                    repeat(second.integer) {
//                        append(first.string)
//                    }
//                }
//                is VariableValue.DecimalType -> buildStringVariable {
//                    repeat(second.decimal.toInt()) {
//                        append(first.string)
//                    }
//                }
//                is VariableValue.CharType -> buildStringVariable {
//                    repeat(second.char.toInt()) {
//                        append(first.string)
//                    }
//                }
//
//                is VariableValue.VariableReferenceType -> operate(context, first, second.flatten(context))
//                is VariableValue.MemberVariableReferenceType -> operate(context, first, second.flatten(context))
//                is VariableValue.FunctionCallType -> operate(context, first, second.flatten(context))
//                is VariableValue.MemberFunctionCallType -> operate(context, first, second.flatten(context))
//                is VariableValue.ExpressionType -> operate(context, first, second.flatten(context))
//
//                is VariableValue.NullType -> first
//
//                else -> VariableValue.UndefinedType
//            }
//            is VariableValue.CharType -> VariableValue.CharType((first.char.toInt() * second.asNumber(context).toDouble()).roundToInt().toChar())
//            is VariableValue.BooleanType -> VariableValue.BooleanType(
//                first.boolean and second.asBoolean(context)
//            )
//            is VariableValue.IntegerType -> VariableValue.IntegerType(
//                first.integer * second.asNumber(context).toInt()
//            )
//            is VariableValue.DecimalType -> VariableValue.DecimalType(
//                first.decimal * second.asNumber(context).toDouble()
//            )
//            is VariableValue.ArrayType<*> -> VariableValue.UndefinedType
//
//            is VariableValue.StringComponents -> operate(context, first.flatten(context), second)
//            is VariableValue.VariableReferenceType -> operate(context, first.flatten(context), second)
//            is VariableValue.MemberVariableReferenceType -> operate(context, first.flatten(context), second)
//            is VariableValue.FunctionCallType -> operate(context, first.flatten(context), second)
//            is VariableValue.MemberFunctionCallType -> operate(context, first.flatten(context), second)
//            is VariableValue.ExpressionType -> operate(context, first.flatten(context), second)
//
//            is VariableValue.NullType -> second
//            is VariableValue.UndefinedType -> second
//        }
//    }
//
//    object EXPONENTIAL : ExpressionOperation() {
//        override suspend fun operate(
//            context: KnolusContext,
//            first: VariableValue,
//            second: VariableValue,
//        ): VariableValue = when (first) {
//            is VariableValue.StringType -> when (second) {
//                is VariableValue.BooleanType -> if (second.boolean) first else VariableValue.NullType
//                is VariableValue.CharType -> buildStringVariable {
//                    append(first.string)
//                    repeat(second.char.toInt() - 1) { append(this.toString()) }
//                }
//                is VariableValue.IntegerType -> buildStringVariable {
//                    append(first.string)
//                    repeat(second.integer - 1) { append(this.toString()) }
//                }
//                is VariableValue.DecimalType -> buildStringVariable {
//                    append(first.string)
//                    repeat(second.decimal.toInt() - 1) { append(this.toString()) }
//                }
//
//                is VariableValue.VariableReferenceType -> operate(context, first, second.flatten(context))
//                is VariableValue.MemberVariableReferenceType -> operate(context, first, second.flatten(context))
//                is VariableValue.FunctionCallType -> operate(context, first, second.flatten(context))
//                is VariableValue.MemberFunctionCallType -> operate(context, first, second.flatten(context))
//                is VariableValue.ExpressionType -> operate(context, first, second.flatten(context))
//
//                is VariableValue.NullType -> first
//
//                else -> VariableValue.UndefinedType
//            }
//            is VariableValue.CharType -> VariableValue.CharType(first.char.toDouble().pow(second.asNumber(context).toDouble()).roundToInt().toChar())
//            is VariableValue.BooleanType ->
//                if (second.asNumber(context).toInt().and(0b1) == 0) first
//                else VariableValue.BooleanType(!first.boolean)
//
//            is VariableValue.IntegerType -> VariableValue.IntegerType(
//                first.integer.toDouble().pow(second.asNumber(context).toInt()).toInt()
//            )
//            is VariableValue.DecimalType -> VariableValue.DecimalType(
//                first.decimal.pow(second.asNumber(context).toDouble())
//            )
//
//            is VariableValue.ArrayType<*> -> VariableValue.UndefinedType
//
//            is VariableValue.StringComponents -> operate(context, first.flatten(context), second)
//            is VariableValue.VariableReferenceType -> operate(context, first.flatten(context), second)
//            is VariableValue.MemberVariableReferenceType -> operate(context, first.flatten(context), second)
//            is VariableValue.FunctionCallType -> operate(context, first.flatten(context), second)
//            is VariableValue.MemberFunctionCallType -> operate(context, first.flatten(context), second)
//            is VariableValue.ExpressionType -> operate(context, first.flatten(context), second)
//
//            is VariableValue.NullType -> second
//            is VariableValue.UndefinedType -> second
//        }
//    }
//
//    abstract suspend fun operate(
//        context: KnolusContext,
//        first: VariableValue,
//        second: VariableValue,
//    ): VariableValue
//}