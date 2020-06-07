package org.abimon.knolus

import kotlin.math.pow
import kotlin.math.roundToInt

@ExperimentalUnsignedTypes
sealed class KnolusUnion {
    interface Action {
        suspend fun run(context: KnolusContext)
    }

    sealed class StringComponent : KnolusUnion() {
        data class RawText(val text: String) : StringComponent()
        data class VariableReference(val variableName: String) : StringComponent()
    }

    data class ScopeType(val lines: Array<KnolusUnion>) : KnolusUnion()
    data class FunctionParameterType(val name: String?, val parameter: VariableValue) : KnolusUnion()
    data class ScriptParameterType(val name: String?, val parameter: VariableValue) : KnolusUnion()
    data class ReturnStatement(val value: VariableValue) : KnolusUnion()

    data class ScriptCallAction(val name: String, val parameters: Array<ScriptParameterType>) : KnolusUnion(), Action {
        override suspend fun run(context: KnolusContext) {
            context.invokeScript(name, parameters)
        }
    }

    data class DeclareVariableAction(
        val variableName: String,
        val variableValue: VariableValue?,
        val global: Boolean = false,
    ) : KnolusUnion(), Action {
        override suspend fun run(context: KnolusContext) {
            context[variableName, global] = variableValue ?: VariableValue.UndefinedType
        }
    }

    data class AssignVariableAction(
        val variableName: String,
        val variableValue: VariableValue,
        val global: Boolean = false,
    ) : KnolusUnion(), Action {
        override suspend fun run(context: KnolusContext) {
            if (variableName in context) {
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
        fun asPipelineFunction(): KnolusFunction<VariableValue?> = KnolusFunction(
            *Array(parameterNames.size) { Pair(parameterNames[it], null) },
            func = this::invoke
        )

        suspend operator fun invoke(context: KnolusContext, parameters: Map<String, Any?>): VariableValue? =
            body?.run(context, parameters)

        override suspend fun run(context: KnolusContext) {
            context.register(functionName, asPipelineFunction(), global)
        }
    }
}

sealed class VariableValue(vararg val typeNames: String) : KnolusUnion() {
    open fun getMemberPropertyGetterNames(propertyName: String): Array<String> =
        typeNames.mapToArray { typeName -> "Get${typeName}MemberProperty_${propertyName}" }

    open fun getMemberFunctionNames(propertyName: String): Array<String> =
        typeNames.mapToArray { typeName -> "Get${typeName}MemberFunction_${propertyName}" }

    abstract suspend fun asString(context: KnolusContext): String
    abstract suspend fun asNumber(context: KnolusContext): Number
    abstract suspend fun asBoolean(context: KnolusContext): Boolean

    abstract suspend fun flatten(context: KnolusContext): VariableValue

    data class StringComponents(val components: Array<StringComponent>) : VariableValue("StringComponents", "Object") {
        override suspend fun flatten(context: KnolusContext): StringType =
            StringType(
                components.mapNotNull { component ->
                    when (component) {
                        is StringComponent.RawText -> component.text
                        is StringComponent.VariableReference -> context[component.variableName]?.asString(context)
                    }
                }.joinToString("")
            )

        override suspend fun asString(context: KnolusContext): String = flatten(context).string
        override suspend fun asNumber(context: KnolusContext): Number {
            val str = flatten(context).string
            if (str.contains('.'))
                return str.toDouble()
            return str.toIntBaseN()
        }

        override suspend fun asBoolean(context: KnolusContext): Boolean {
            val str = asString(context)
            if (str.equals("true", true) || str.equals("false", true))
                return str.toBoolean()
            return (str.toIntOrNullBaseN() ?: 0) != 0
        }
    }

    data class StringType(val string: String) : VariableValue("String", "Object") {
        override suspend fun asString(context: KnolusContext): String = string
        override suspend fun asNumber(context: KnolusContext): Number =
            if (string.contains('.')) string.toDouble() else string.toIntBaseN()

        override suspend fun flatten(context: KnolusContext): VariableValue = this
        override suspend fun asBoolean(context: KnolusContext): Boolean {
            val str = asString(context)
            if (str.equals("true", true) || str.equals("false", true))
                return str.toBoolean()
            return (str.toIntOrNullBaseN() ?: 0) != 0
        }
    }

    sealed class BooleanType private constructor(val boolean: Boolean) : VariableValue("Boolean", "Object") {
        companion object {
            operator fun invoke(boolean: Boolean) = if (boolean) TRUE else FALSE
        }

        object TRUE : BooleanType(true)
        object FALSE : BooleanType(false)

        override suspend fun asString(context: KnolusContext): String = boolean.toString()
        override suspend fun asNumber(context: KnolusContext): Number = if (boolean) 1 else 0
        override suspend fun flatten(context: KnolusContext): VariableValue = this

        override suspend fun asBoolean(context: KnolusContext): Boolean = boolean
    }

    data class IntegerType(val integer: Int) : VariableValue("Integer", "Number", "Object") {
        override suspend fun asString(context: KnolusContext): String = integer.toString()
        override suspend fun asNumber(context: KnolusContext): Number = integer
        override suspend fun flatten(context: KnolusContext): VariableValue = this

        override suspend fun asBoolean(context: KnolusContext): Boolean = integer != 0
    }

    data class DecimalType(val decimal: Double) : VariableValue("Decimal", "Number", "Object") {
        override suspend fun asString(context: KnolusContext): String = decimal.toString()
        override suspend fun asNumber(context: KnolusContext): Number = decimal
        override suspend fun flatten(context: KnolusContext): VariableValue = this
        override suspend fun asBoolean(context: KnolusContext): Boolean = decimal.toInt() != 0
    }

    data class CharType(val char: Char): VariableValue("Char", "Number", "Object") {
        override suspend fun asString(context: KnolusContext): String = char.toString()
        override suspend fun asNumber(context: KnolusContext): Number = char.toInt()
        override suspend fun asBoolean(context: KnolusContext): Boolean = char != '\u0000'
        override suspend fun flatten(context: KnolusContext): VariableValue = this
    }

    data class VariableReferenceType(val variableName: String) :
        VariableValue("VariableReference", "Reference", "Object") {
        override suspend fun flatten(context: KnolusContext): VariableValue =
            context[variableName] ?: NullType

        override suspend fun asString(context: KnolusContext): String = flatten(context).asString(context)
        override suspend fun asNumber(context: KnolusContext): Number = flatten(context).asNumber(context)
        override suspend fun asBoolean(context: KnolusContext): Boolean = flatten(context).asBoolean(context)
    }

    data class MemberVariableReferenceType(val variableName: String, val propertyName: String) :
        VariableValue("MemberVariableReference", "Reference", "Object") {
        override suspend fun flatten(context: KnolusContext): VariableValue {
            val member = context[variableName] ?: NullType
            val params = arrayOf(FunctionParameterType("self", member))

            return member.getMemberPropertyGetterNames(propertyName).fold(null as VariableValue?) { acc, funcName ->
                acc ?: context.invokeFunction(funcName, params)?.flatten(context)
            } ?: UndefinedType
        }

        override suspend fun asString(context: KnolusContext): String = flatten(context).asString(context)
        override suspend fun asNumber(context: KnolusContext): Number = flatten(context).asNumber(context)
        override suspend fun asBoolean(context: KnolusContext): Boolean = flatten(context).asBoolean(context)
    }

    data class FunctionCallType(val name: String, val parameters: Array<FunctionParameterType>) :
        VariableValue("FunctionCall", "Reference", "Object"),
        Action {
//            constructor(functionCall: FunctionCallAction) : this(functionCall.name, functionCall.parameters)

        override suspend fun flatten(context: KnolusContext): VariableValue =
            context.invokeFunction(name, parameters) ?: NullType

        override suspend fun asString(context: KnolusContext): String = flatten(context).asString(context)
        override suspend fun asNumber(context: KnolusContext): Number = flatten(context).asNumber(context)
        override suspend fun asBoolean(context: KnolusContext): Boolean = flatten(context).asBoolean(context)

        override suspend fun run(context: KnolusContext) {
            context.invokeFunction(name, parameters)
        }
    }

    data class MemberFunctionCallType(
        val variableName: String,
        val functionName: String,
        val parameters: Array<FunctionParameterType>,
    ) :
        VariableValue("FunctionCall", "Reference", "Object"),
        Action {
//            constructor(functionCall: FunctionCallAction) : this(functionCall.name, functionCall.parameters)

//            override suspend fun flatten(context: KnolusContext): VariableValue =
//                context.invokeFunction(functionName, parameters)?.flatten(context)
//                    ?: NullType

        override suspend fun flatten(context: KnolusContext): VariableValue {
            val member = context[variableName]?.flatten(context) ?: NullType

            val params = arrayOfNulls<FunctionParameterType>(parameters.size + 1)
            params[0] = FunctionParameterType("self", member)
            parameters.copyInto(params, 1)

            @Suppress("UNCHECKED_CAST")
            params as Array<FunctionParameterType>

            return member.getMemberFunctionNames(functionName).fold(null as VariableValue?) { acc, funcName ->
                acc ?: context.invokeFunction(funcName, params)
            } ?: UndefinedType
        }

        override suspend fun asString(context: KnolusContext): String = flatten(context).asString(context)
        override suspend fun asNumber(context: KnolusContext): Number = flatten(context).asNumber(context)
        override suspend fun asBoolean(context: KnolusContext): Boolean = flatten(context).asBoolean(context)

        override suspend fun run(context: KnolusContext) {
            val member = context[variableName]?.flatten(context) ?: NullType

            val params = arrayOfNulls<FunctionParameterType>(parameters.size + 1)
            params[0] = FunctionParameterType("self", member)
            parameters.copyInto(params, 1)

            @Suppress("UNCHECKED_CAST")
            params as Array<FunctionParameterType>

            member.getMemberFunctionNames(functionName).forEach { funcName ->
                context.invokeFunction(funcName, params) ?: return@forEach
                return
            }
        }
    }

/*        data class WrappedScriptCallType(val name: String, val parameters: Array<ScriptParameterType>) : VariableValue() {
        constructor(scriptCall: ScriptCallAction) : this(scriptCall.name, scriptCall.parameters)

        override suspend fun flatten(context: KnolusContext): VariableValue = context.invokeScript(name, parameters)?.flatten(context)
                ?: NullType

        override suspend fun asString(context: KnolusContext): String = flatten(context).asString(context)
        override suspend fun asNumber(context: KnolusContext): Number = flatten(context).asNumber(context)
        override suspend fun asBoolean(context: KnolusContext): Boolean = flatten(context).asBoolean(context)
    }*/

    data class ArrayType<T : VariableValue>(val array: Array<T>) :
        VariableValue("Array", "Object") {
        companion object {
//            inline operator fun <reified T : VariableValue> invoke(array: Array<T>): ArrayType<T> =
//                ArrayType(array, T::class)
        }

        override suspend fun flatten(context: KnolusContext): ArrayType<VariableValue> =
            ArrayType(Array(array.size) { i -> array[i].flatten(context) })

        override suspend fun asBoolean(context: KnolusContext): Boolean =
            array.isNotEmpty()

        override suspend fun asNumber(context: KnolusContext): Number =
            array.size

        override suspend fun asString(context: KnolusContext): String =
            flatten(context)
                .array
                .map { t -> t.asString(context) }
                .joinToString(prefix = "arrayOf(", postfix = ")") { "\"$it\"" }

        @Suppress("UNCHECKED_CAST")
        suspend fun addByCoercion(context: KnolusContext, value: VariableValue): ArrayType<T> = array.copyOf().let { copy ->
            ArrayType(ArrayCoercion.addByCoercion(copy, context, value))
        }

        suspend fun dropByCoersion(context: KnolusContext, value: VariableValue): ArrayType<T> = array.copyOf().let { copy ->
            ArrayType(ArrayCoercion.dropByCoercion(copy, context, value))
        }
        suspend fun copyByTaking(count: Int): ArrayType<T> = ArrayType(array.sliceArray(0 until count))
        suspend fun copyByStriping(count: Int): ArrayType<T> = ArrayType(array.copyWithStripe(count))
        suspend fun copyByGrouping(count: Int): ArrayType<T> = ArrayType(array.copyWithGrouping(count))

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ArrayType<*>

            if (!array.contentEquals(other.array)) return false

            return true
        }

        override fun hashCode(): Int {
            return array.contentHashCode()
        }
    }

    /*data class DataSourceType<T : InputFlow>(val dataSource: DataSource<T>) : VariableValue() {
        override suspend fun asBoolean(context: KnolusContext): Boolean =
                dataSource.canOpenInputFlow()

        override suspend fun asNumber(context: KnolusContext): Number =
                dataSource.dataSize?.toLong() ?: 0L

        override suspend fun asString(context: KnolusContext): String =
                dataSource.toString()

        override suspend fun flatten(context: KnolusContext): VariableValue =
                this
    }*/

    object NullType : VariableValue("Null", "Constant", "Object") {
        override suspend fun asNumber(context: KnolusContext): Number = 0
        override suspend fun asString(context: KnolusContext): String = "[null]"
        override suspend fun flatten(context: KnolusContext): VariableValue = this
        override suspend fun asBoolean(context: KnolusContext): Boolean = false
    }

    object UndefinedType : VariableValue("Undefined", "Constant", "Object") {
        override suspend fun asNumber(context: KnolusContext): Number = 0
        override suspend fun asString(context: KnolusContext): String = "[undefined]"
        override suspend fun flatten(context: KnolusContext): VariableValue = this
        override suspend fun asBoolean(context: KnolusContext): Boolean = false
    }

    data class ExpressionType(
        val startValue: VariableValue,
        val ops: Array<Pair<ExpressionOperation, VariableValue>>,
    ) : VariableValue("Expression", "Object") {
        override suspend fun asNumber(context: KnolusContext): Number =
            flatten(context).asNumber(context)

        override suspend fun asString(context: KnolusContext): String =
            flatten(context).asString(context)

        override suspend fun asBoolean(context: KnolusContext): Boolean =
            flatten(context).asBoolean(context)

//            override suspend fun flatten(context: KnolusContext): VariableValue =
//                    ops.fold(startValue) { first, (operation, second) -> operation.operate(context, first, second) }
//                            .flatten(context)

        override suspend fun flatten(context: KnolusContext): VariableValue {
            var value: VariableValue = this.startValue
            val remainingOps: MutableList<Pair<ExpressionOperation, VariableValue>> = ArrayList(this.ops.size)
            remainingOps.addAll(this.ops)

            suspend fun handleOperations(vararg operations: ExpressionOperation) {
                val ops = remainingOps.toTypedArray()
                remainingOps.clear()

                ops.forEach { pair ->
                    if (pair.first !in operations) remainingOps.add(pair)
                    else {
                        val op: ExpressionOperation?
                        val first: VariableValue
                        if (remainingOps.isEmpty()) {
                            op = null
                            first = value
                        } else {
                            val lastPair = remainingOps.removeAt(remainingOps.lastIndex)
                            op = lastPair.first
                            first = lastPair.second
                        }

                        val second = pair.second

                        val result = pair.first.operate(context, first, second)
                        if (op == null) value = result
                        else if (remainingOps.isEmpty()) value = op.operate(context, value, result)
                        else remainingOps.add(Pair(op, result))
                    }
                }
            }

            //PEDMAS
            //(P)arenthesis, already handled by flattening

            //(E)xponentials
            handleOperations(ExpressionOperation.EXPONENTIAL)

            //(D)ivision, (M)ultiplication
            handleOperations(
                ExpressionOperation.DIVIDE,
                ExpressionOperation.MULTIPLY
            )

            //(A)ddition, (S)ubtraction
            handleOperations(
                ExpressionOperation.PLUS,
                ExpressionOperation.MINUS
            )

            return value
        }
    }
}

sealed class ExpressionOperation : KnolusUnion() {
    object PLUS : ExpressionOperation() {
        override suspend fun operate(
            context: KnolusContext,
            first: VariableValue,
            second: VariableValue,
        ): VariableValue = when (first) {
            is VariableValue.StringType -> VariableValue.StringType(first.string.plus(second.asString(context)))
            is VariableValue.CharType -> when (second) {
                VariableValue.NullType -> first
                VariableValue.UndefinedType -> VariableValue.UndefinedType

                is VariableValue.StringComponents -> operate(context, first, second.flatten(context))
                is VariableValue.VariableReferenceType -> operate(context, first, second.flatten(context))
                is VariableValue.MemberVariableReferenceType -> operate(context, first, second.flatten(context))
                is VariableValue.FunctionCallType -> operate(context, first, second.flatten(context))
                is VariableValue.MemberFunctionCallType -> operate(context, first, second.flatten(context))
                is VariableValue.ExpressionType -> operate(context, first, second.flatten(context))

                is VariableValue.StringType -> VariableValue.StringType("${first.char}${second.string}")

                else -> VariableValue.CharType((first.char.toInt() + second.asNumber(context).toInt()).toChar())
            }
            is VariableValue.BooleanType -> VariableValue.BooleanType(
                first.boolean.xor(
                    second.asNumber(context).toInt() != 0
                )
            )
            is VariableValue.IntegerType -> VariableValue.IntegerType(
                first.integer + second.asNumber(context).toInt()
            )
            is VariableValue.DecimalType -> VariableValue.DecimalType(
                first.decimal + second.asNumber(context).toDouble()
            )
            is VariableValue.ArrayType<*> -> first.addByCoercion(context, second)

            is VariableValue.StringComponents -> operate(context, first.flatten(context), second)
            is VariableValue.VariableReferenceType -> operate(context, first.flatten(context), second)
            is VariableValue.MemberVariableReferenceType -> operate(context, first.flatten(context), second)
            is VariableValue.FunctionCallType -> operate(context, first.flatten(context), second)
            is VariableValue.MemberFunctionCallType -> operate(context, first.flatten(context), second)
            is VariableValue.ExpressionType -> operate(context, first.flatten(context), second)

            VariableValue.NullType -> second
            VariableValue.UndefinedType -> second
        }
    }

    object MINUS : ExpressionOperation() {
        override suspend fun operate(
            context: KnolusContext,
            first: VariableValue,
            second: VariableValue,
        ): VariableValue = when (first) {
            is VariableValue.StringType -> when (second) {
                is VariableValue.StringType -> VariableValue.StringType(first.string.removeSuffix(second.string))
                is VariableValue.CharType -> VariableValue.StringType(first.string.trimEnd(second.char))

                is VariableValue.StringComponents -> operate(context, first, second.flatten(context))
                is VariableValue.VariableReferenceType -> operate(context, first, second.flatten(context))
                is VariableValue.MemberVariableReferenceType -> operate(context, first, second.flatten(context))
                is VariableValue.FunctionCallType -> operate(context, first, second.flatten(context))
                is VariableValue.MemberFunctionCallType -> operate(context, first, second.flatten(context))
                is VariableValue.ExpressionType -> operate(context, first, second.flatten(context))

                is VariableValue.NullType -> first
                else -> VariableValue.UndefinedType
            }
            is VariableValue.CharType -> when (second) {
                VariableValue.NullType -> first
                VariableValue.UndefinedType -> VariableValue.UndefinedType

                is VariableValue.VariableReferenceType -> operate(context, first, second.flatten(context))
                is VariableValue.MemberVariableReferenceType -> operate(context, first, second.flatten(context))
                is VariableValue.FunctionCallType -> operate(context, first, second.flatten(context))
                is VariableValue.MemberFunctionCallType -> operate(context, first, second.flatten(context))
                is VariableValue.ExpressionType -> operate(context, first, second.flatten(context))

                is VariableValue.IntegerType -> VariableValue.CharType((first.char.toInt() - second.integer).toChar())
                is VariableValue.DecimalType -> VariableValue.CharType((first.char.toInt() - second.decimal.toInt()).toChar())

                else -> VariableValue.UndefinedType
            }
            is VariableValue.BooleanType -> VariableValue.BooleanType(
                first.boolean.xor(
                    second.asNumber(context).toInt() != 0
                )
            )
            is VariableValue.IntegerType -> VariableValue.IntegerType(
                first.integer - second.asNumber(context).toInt()
            )
            is VariableValue.DecimalType -> VariableValue.DecimalType(
                first.decimal - second.asNumber(context).toDouble()
            )
            is VariableValue.ArrayType<*> -> first.dropByCoersion(context, second)

            is VariableValue.StringComponents -> operate(context, first.flatten(context), second)
            is VariableValue.VariableReferenceType -> operate(context, first.flatten(context), second)
            is VariableValue.MemberVariableReferenceType -> operate(context, first.flatten(context), second)
            is VariableValue.FunctionCallType -> operate(context, first.flatten(context), second)
            is VariableValue.MemberFunctionCallType -> operate(context, first.flatten(context), second)
            is VariableValue.ExpressionType -> operate(context, first.flatten(context), second)

            is VariableValue.NullType -> second
            is VariableValue.UndefinedType -> second
        }
    }

    object DIVIDE : ExpressionOperation() {
        override suspend fun operate(
            context: KnolusContext,
            first: VariableValue,
            second: VariableValue,
        ): VariableValue = when (first) {
            is VariableValue.StringType -> when (second) {
                VariableValue.BooleanType.TRUE -> first
                VariableValue.BooleanType.FALSE -> VariableValue.NullType

                is VariableValue.VariableReferenceType -> operate(context, first, second.flatten(context))
                is VariableValue.MemberVariableReferenceType -> operate(context, first, second.flatten(context))
                is VariableValue.FunctionCallType -> operate(context, first, second.flatten(context))
                is VariableValue.MemberFunctionCallType -> operate(context, first, second.flatten(context))
                is VariableValue.ExpressionType -> operate(context, first, second.flatten(context))

                is VariableValue.NullType -> first

                else -> VariableValue.UndefinedType
            }
            is VariableValue.CharType -> when (second) {
                VariableValue.BooleanType.TRUE -> first
                VariableValue.BooleanType.FALSE -> VariableValue.NullType
                VariableValue.NullType -> first

                is VariableValue.VariableReferenceType -> operate(context, first, second.flatten(context))
                is VariableValue.MemberVariableReferenceType -> operate(context, first, second.flatten(context))
                is VariableValue.FunctionCallType -> operate(context, first, second.flatten(context))
                is VariableValue.MemberFunctionCallType -> operate(context, first, second.flatten(context))
                is VariableValue.ExpressionType -> operate(context, first, second.flatten(context))

                is VariableValue.IntegerType -> VariableValue.CharType((first.char.toInt() / second.integer).toChar())
                is VariableValue.DecimalType -> VariableValue.CharType((first.char.toDouble() / second.decimal).roundToInt().toChar())

                else -> VariableValue.UndefinedType
            }
            is VariableValue.BooleanType -> VariableValue.BooleanType(
                first.boolean.and(
                    second.asNumber(context).toInt() != 0
                )
            )
            is VariableValue.IntegerType -> VariableValue.IntegerType(
                first.integer / second.asNumber(context).toInt()
            )
            is VariableValue.DecimalType -> VariableValue.DecimalType(
                first.decimal / second.asNumber(context).toDouble()
            )
            is VariableValue.ArrayType<*> -> VariableValue.UndefinedType

            is VariableValue.StringComponents -> operate(context, first.flatten(context), second)
            is VariableValue.VariableReferenceType -> operate(context, first.flatten(context), second)
            is VariableValue.MemberVariableReferenceType -> operate(context, first.flatten(context), second)
            is VariableValue.FunctionCallType -> operate(context, first.flatten(context), second)
            is VariableValue.MemberFunctionCallType -> operate(context, first.flatten(context), second)
            is VariableValue.ExpressionType -> operate(context, first.flatten(context), second)

            is VariableValue.NullType -> second
            is VariableValue.UndefinedType -> second
        }
    }

    object MULTIPLY : ExpressionOperation() {
        override suspend fun operate(
            context: KnolusContext,
            first: VariableValue,
            second: VariableValue,
        ): VariableValue = when (first) {
            is VariableValue.StringType -> when (second) {
                is VariableValue.BooleanType -> if (second.boolean) first else VariableValue.NullType
                is VariableValue.IntegerType -> buildStringVariable {
                    repeat(second.integer) {
                        append(first.string)
                    }
                }
                is VariableValue.DecimalType -> buildStringVariable {
                    repeat(second.decimal.toInt()) {
                        append(first.string)
                    }
                }
                is VariableValue.CharType -> buildStringVariable {
                    repeat(second.char.toInt()) {
                        append(first.string)
                    }
                }

                is VariableValue.VariableReferenceType -> operate(context, first, second.flatten(context))
                is VariableValue.MemberVariableReferenceType -> operate(context, first, second.flatten(context))
                is VariableValue.FunctionCallType -> operate(context, first, second.flatten(context))
                is VariableValue.MemberFunctionCallType -> operate(context, first, second.flatten(context))
                is VariableValue.ExpressionType -> operate(context, first, second.flatten(context))

                is VariableValue.NullType -> first

                else -> VariableValue.UndefinedType
            }
            is VariableValue.CharType -> VariableValue.CharType((first.char.toInt() * second.asNumber(context).toDouble()).roundToInt().toChar())
            is VariableValue.BooleanType -> VariableValue.BooleanType(
                first.boolean and second.asBoolean(context)
            )
            is VariableValue.IntegerType -> VariableValue.IntegerType(
                first.integer * second.asNumber(context).toInt()
            )
            is VariableValue.DecimalType -> VariableValue.DecimalType(
                first.decimal * second.asNumber(context).toDouble()
            )
            is VariableValue.ArrayType<*> -> VariableValue.UndefinedType

            is VariableValue.StringComponents -> operate(context, first.flatten(context), second)
            is VariableValue.VariableReferenceType -> operate(context, first.flatten(context), second)
            is VariableValue.MemberVariableReferenceType -> operate(context, first.flatten(context), second)
            is VariableValue.FunctionCallType -> operate(context, first.flatten(context), second)
            is VariableValue.MemberFunctionCallType -> operate(context, first.flatten(context), second)
            is VariableValue.ExpressionType -> operate(context, first.flatten(context), second)

            is VariableValue.NullType -> second
            is VariableValue.UndefinedType -> second
        }
    }

    object EXPONENTIAL : ExpressionOperation() {
        override suspend fun operate(
            context: KnolusContext,
            first: VariableValue,
            second: VariableValue,
        ): VariableValue = when (first) {
            is VariableValue.StringType -> when (second) {
                is VariableValue.BooleanType -> if (second.boolean) first else VariableValue.NullType
                is VariableValue.CharType -> buildStringVariable {
                    append(first.string)
                    repeat(second.char.toInt() - 1) { append(this.toString()) }
                }
                is VariableValue.IntegerType -> buildStringVariable {
                    append(first.string)
                    repeat(second.integer - 1) { append(this.toString()) }
                }
                is VariableValue.DecimalType -> buildStringVariable {
                    append(first.string)
                    repeat(second.decimal.toInt() - 1) { append(this.toString()) }
                }

                is VariableValue.VariableReferenceType -> operate(context, first, second.flatten(context))
                is VariableValue.MemberVariableReferenceType -> operate(context, first, second.flatten(context))
                is VariableValue.FunctionCallType -> operate(context, first, second.flatten(context))
                is VariableValue.MemberFunctionCallType -> operate(context, first, second.flatten(context))
                is VariableValue.ExpressionType -> operate(context, first, second.flatten(context))

                is VariableValue.NullType -> first

                else -> VariableValue.UndefinedType
            }
            is VariableValue.CharType -> VariableValue.CharType(first.char.toDouble().pow(second.asNumber(context).toDouble()).roundToInt().toChar())
            is VariableValue.BooleanType ->
                if (second.asNumber(context).toInt().and(0b1) == 0) first
                else VariableValue.BooleanType(!first.boolean)

            is VariableValue.IntegerType -> VariableValue.IntegerType(
                first.integer.toDouble().pow(second.asNumber(context).toInt()).toInt()
            )
            is VariableValue.DecimalType -> VariableValue.DecimalType(
                first.decimal.pow(second.asNumber(context).toDouble())
            )

            is VariableValue.ArrayType<*> -> VariableValue.UndefinedType

            is VariableValue.StringComponents -> operate(context, first.flatten(context), second)
            is VariableValue.VariableReferenceType -> operate(context, first.flatten(context), second)
            is VariableValue.MemberVariableReferenceType -> operate(context, first.flatten(context), second)
            is VariableValue.FunctionCallType -> operate(context, first.flatten(context), second)
            is VariableValue.MemberFunctionCallType -> operate(context, first.flatten(context), second)
            is VariableValue.ExpressionType -> operate(context, first.flatten(context), second)

            is VariableValue.NullType -> second
            is VariableValue.UndefinedType -> second
        }
    }

    abstract suspend fun operate(
        context: KnolusContext,
        first: VariableValue,
        second: VariableValue,
    ): VariableValue
}