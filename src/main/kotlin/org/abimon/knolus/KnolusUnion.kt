package org.abimon.knolus

import org.abimon.knolus.types.KnolusConstants
import org.abimon.knolus.types.KnolusTypedValue

@ExperimentalUnsignedTypes
sealed class KnolusUnion {
    interface Action {
        suspend fun run(context: KnolusContext)
    }

    sealed class StringComponent : KnolusUnion() {
        data class RawText(val text: String) : StringComponent()
        data class VariableReference(val variableName: String) : StringComponent()
    }

    sealed class VariableValue<T : KnolusTypedValue>(open val value: T) : KnolusUnion() {
        data class Lazy<T : KnolusTypedValue.RuntimeValue>(override val value: T) : VariableValue<T>(value)
        data class Stable<T : KnolusTypedValue>(override val value: T) : VariableValue<T>(value)
    }

    data class ScopeType(val lines: Array<KnolusUnion>) : KnolusUnion()
    data class FunctionParameterType(val name: String?, val parameter: KnolusTypedValue) : KnolusUnion()

    //    data class ScriptParameterType(val name: String?, val parameter: KnolusTypedValue) : KnolusUnion()
    data class ReturnStatement(val value: KnolusTypedValue) : KnolusUnion()

//    data class ScriptCallAction(val name: String, val parameters: Array<ScriptParameterType>) : KnolusUnion(), Action {
//        override suspend fun run(context: KnolusContext) {
//            context.invokeScript(name, parameters)
//        }
//    }

    data class DeclareVariableAction(
        val variableName: String,
        val variableValue: KnolusTypedValue?,
        val global: Boolean = false,
    ) : KnolusUnion(), Action {
        override suspend fun run(context: KnolusContext) {
            if (variableValue is KnolusTypedValue.UnsureValue && variableValue.needsEvaluation(context))
                context[variableName, global] = variableValue.evaluate(context)
            else
                context[variableName, global] = variableValue ?: KnolusConstants.Undefined
        }
    }

    data class AssignVariableAction(
        val variableName: String,
        val variableValue: KnolusTypedValue,
        val global: Boolean = false,
    ) : KnolusUnion(), Action {
        override suspend fun run(context: KnolusContext) {
            if (variableName in context) {
                if (variableValue is KnolusTypedValue.UnsureValue && variableValue.needsEvaluation(context))
                    context[variableName, global] = variableValue.evaluate(context)
                else
                    context[variableName, global] = variableValue
            } else {
                println("Undeclared variable!!")
            }
        }
    }

    data class FunctionDeclaration(
        val functionName: String,
        val parameterNames: Array<String>,
        val global: Boolean = false,
        val body: ScopeType?,
    ) : KnolusUnion(), Action {
        fun asPipelineFunction(): KnolusFunction<KnolusTypedValue?> = KnolusFunction(
            *Array(parameterNames.size) { Pair(parameterNames[it], null) },
            func = this::invoke
        )

        suspend operator fun invoke(context: KnolusContext, parameters: Map<String, Any?>): KnolusTypedValue? =
            body?.run(context, parameters)

        override suspend fun run(context: KnolusContext) {
            context.register(functionName, asPipelineFunction(), global)
        }
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