package org.abimon.knolus

import kotlin.math.pow

@ExperimentalUnsignedTypes
sealed class KnolusUnion {
    interface Action {
        suspend fun run(context: KnolusContext)
    }

    sealed class StringComponent : KnolusUnion() {
        data class RawText(val text: String) : StringComponent()
        data class VariableReference(val variableName: String) : StringComponent()
    }

    sealed class VariableValue(val typeName: String) : KnolusUnion() {
        open fun getMemberPropertyGetterName(propertyName: String): String = "Get${typeName}MemberProperty_${propertyName}"
        open fun getMemberFunctionName(propertyName: String): String = "Get${typeName}MemberFunction_${propertyName}"

        abstract suspend fun asString(context: KnolusContext): String
        abstract suspend fun asNumber(context: KnolusContext): Number
        abstract suspend fun flatten(context: KnolusContext): VariableValue
        abstract suspend fun asBoolean(context: KnolusContext): Boolean

        data class StringComponents(val components: Array<StringComponent>) : VariableValue("StringComponents") {
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

        data class StringType(val string: String) : VariableValue("String") {
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

        data class BooleanType(val boolean: Boolean) : VariableValue("Boolean") {
            override suspend fun asString(context: KnolusContext): String = boolean.toString()
            override suspend fun asNumber(context: KnolusContext): Number = if (boolean) 1 else 0
            override suspend fun flatten(context: KnolusContext): VariableValue = this

            override suspend fun asBoolean(context: KnolusContext): Boolean = boolean
        }

        data class IntegerType(val integer: Int) : VariableValue("Integer") {
            override suspend fun asString(context: KnolusContext): String = integer.toString()
            override suspend fun asNumber(context: KnolusContext): Number = integer
            override suspend fun flatten(context: KnolusContext): VariableValue = this

            override suspend fun asBoolean(context: KnolusContext): Boolean = integer != 0
        }

        data class DecimalType(val decimal: Double) : VariableValue("Decimal") {
            override suspend fun asString(context: KnolusContext): String = decimal.toString()
            override suspend fun asNumber(context: KnolusContext): Number = decimal
            override suspend fun flatten(context: KnolusContext): VariableValue = this
            override suspend fun asBoolean(context: KnolusContext): Boolean = decimal.toInt() != 0
        }

        data class VariableReferenceType(val variableName: String) : VariableValue("VariableReference") {
            override suspend fun flatten(context: KnolusContext): VariableValue =
                context[variableName]?.flatten(context)
                    ?: NullType

            override suspend fun asString(context: KnolusContext): String = flatten(context).asString(context)
            override suspend fun asNumber(context: KnolusContext): Number = flatten(context).asNumber(context)
            override suspend fun asBoolean(context: KnolusContext): Boolean = flatten(context).asBoolean(context)
        }

        data class MemberVariableReferenceType(val variableName: String, val propertyName: String) :
            VariableValue("MemberVariableReference") {
            override suspend fun flatten(context: KnolusContext): VariableValue {
                val member = context[variableName]?.flatten(context) ?: NullType
                return context.invokeFunction(
                    member.getMemberPropertyGetterName(propertyName),
                    arrayOf(FunctionParameterType("self", member))
                )?.flatten(context) ?: NullType
            }

            override suspend fun asString(context: KnolusContext): String = flatten(context).asString(context)
            override suspend fun asNumber(context: KnolusContext): Number = flatten(context).asNumber(context)
            override suspend fun asBoolean(context: KnolusContext): Boolean = flatten(context).asBoolean(context)
        }

        data class FunctionCallType(val name: String, val parameters: Array<FunctionParameterType>) :
            VariableValue("FunctionCall"), Action {
//            constructor(functionCall: FunctionCallAction) : this(functionCall.name, functionCall.parameters)

            override suspend fun flatten(context: KnolusContext): VariableValue =
                context.invokeFunction(name, parameters)?.flatten(context)
                    ?: NullType

            override suspend fun asString(context: KnolusContext): String = flatten(context).asString(context)
            override suspend fun asNumber(context: KnolusContext): Number = flatten(context).asNumber(context)
            override suspend fun asBoolean(context: KnolusContext): Boolean = flatten(context).asBoolean(context)

            override suspend fun run(context: KnolusContext) {
                context.invokeFunction(name, parameters)
            }
        }

        data class MemberFunctionCallType(val variableName: String, val functionName: String, val parameters: Array<FunctionParameterType>) :
            VariableValue("FunctionCall"), Action {
//            constructor(functionCall: FunctionCallAction) : this(functionCall.name, functionCall.parameters)

//            override suspend fun flatten(context: KnolusContext): VariableValue =
//                context.invokeFunction(functionName, parameters)?.flatten(context)
//                    ?: NullType

            override suspend fun flatten(context: KnolusContext): VariableValue {
                val member = context[variableName]?.flatten(context) ?: NullType

                val params = arrayOfNulls<FunctionParameterType>(parameters.size + 1)
                params[0] = FunctionParameterType("self", member)
                parameters.copyInto(params, 1)

                return context.invokeFunction(
                    member.getMemberFunctionName(functionName),
                    params.requireNoNulls()
                )?.flatten(context) ?: NullType
            }

            override suspend fun asString(context: KnolusContext): String = flatten(context).asString(context)
            override suspend fun asNumber(context: KnolusContext): Number = flatten(context).asNumber(context)
            override suspend fun asBoolean(context: KnolusContext): Boolean = flatten(context).asBoolean(context)

            override suspend fun run(context: KnolusContext) {
                val member = context[variableName]?.flatten(context) ?: NullType

                val params = arrayOfNulls<FunctionParameterType>(parameters.size + 1)
                params[0] = FunctionParameterType("self", member)
                parameters.copyInto(params, 1)

                context.invokeFunction(
                    member.getMemberFunctionName(functionName),
                    params.requireNoNulls()
                )
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

        data class ArrayType<T : VariableValue>(val array: Array<T>) : VariableValue("Array") {
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

        object NullType : VariableValue("Null") {
            override suspend fun asNumber(context: KnolusContext): Number = 0
            override suspend fun asString(context: KnolusContext): String = "[null]"
            override suspend fun flatten(context: KnolusContext): VariableValue = this
            override suspend fun asBoolean(context: KnolusContext): Boolean = false
        }

        object UndefinedType : VariableValue("Undefined") {
            override suspend fun asNumber(context: KnolusContext): Number = 0
            override suspend fun asString(context: KnolusContext): String = "[undefined]"
            override suspend fun flatten(context: KnolusContext): VariableValue = this
            override suspend fun asBoolean(context: KnolusContext): Boolean = false
        }

        data class ExpressionType(
            val startValue: VariableValue,
            val ops: Array<Pair<ExpressionOperation, VariableValue>>
        ) : VariableValue("Expression") {
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
                val ops = Array(this.ops.size) { i ->
                    val (op, value) = this.ops[i]
                    Pair(op, value.flatten(context))
                }

                var value: VariableValue = this.startValue.flatten(context)
                val remainingOps: MutableList<Pair<ExpressionOperation, VariableValue>> =
                    this.ops.mapTo(ArrayList(this.ops.size)) { (op, value) -> Pair(op, value.flatten(context)) }

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
                handleOperations(ExpressionOperation.DIVIDE, ExpressionOperation.MULTIPLY)

                //(A)ddition, (S)ubtraction
                handleOperations(ExpressionOperation.PLUS, ExpressionOperation.MINUS)

                return value
            }
        }
    }

    sealed class ExpressionOperation : KnolusUnion() {
        object PLUS : ExpressionOperation() {
            override suspend fun _operate(
                context: KnolusContext,
                first: VariableValue,
                second: VariableValue
            ): VariableValue = when (first) {
                is VariableValue.StringComponents -> VariableValue.StringType(
                    first.asString(context).plus(second.asString(context))
                )
                is VariableValue.StringType -> VariableValue.StringType(
                    first.asString(context).plus(second.asString(context))
                )
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
//                is VariableValue.VariableReferenceType -> _operate(context, pipelineContext[first.variableName]
//                        ?: VariableValue.NullType, second)
//                is VariableValue.WrappedFunctionCallType -> _operate(context, pipelineContext.invokeFunction(spiralContext, first.name, first.parameters)
//                        ?: VariableValue.NullType, second)
//                is VariableValue.WrappedScriptCallType -> _operate(context, pipelineContext.invokeScript(spiralContext, first.name, first.parameters)
//                        ?: VariableValue.NullType, second)
                VariableValue.NullType -> second
                else -> error("Non-flat $first")
            }
        }

        object MINUS : ExpressionOperation() {
            override suspend fun _operate(
                context: KnolusContext,
                first: VariableValue,
                second: VariableValue
            ): VariableValue = when (first) {
                is VariableValue.StringComponents -> when (second) {
                    is VariableValue.StringComponents -> VariableValue.StringType(
                        first.asString(context).removeSuffix(second.asString(context))
                    )
                    is VariableValue.StringType -> VariableValue.StringType(
                        first.asString(context).removeSuffix(second.string)
                    )
                    is VariableValue.BooleanType -> VariableValue.StringType(
                        first.asString(context).removeSuffix(second.boolean.toString())
                    )
                    is VariableValue.IntegerType -> VariableValue.StringType(
                        first.asString(context).dropLast(second.integer)
                    )
                    is VariableValue.DecimalType -> VariableValue.StringType(
                        first.asString(context).dropLast(second.decimal.toInt())
                    )
//                    is VariableValue.VariableReferenceType -> _operate(context, first, pipelineContext[second.variableName]
//                            ?: VariableValue.NullType)
//                    is VariableValue.WrappedFunctionCallType -> _operate(context, first, pipelineContext.invokeFunction(spiralContext, second.name, second.parameters)
//                            ?: VariableValue.NullType)
//                    is VariableValue.WrappedScriptCallType -> _operate(context, first, pipelineContext.invokeScript(spiralContext, second.name, second.parameters)
//                            ?: VariableValue.NullType)
                    VariableValue.NullType -> first
                    else -> error("Non-flat $second")
                }
                is VariableValue.StringType -> when (second) {
                    is VariableValue.StringComponents -> VariableValue.StringType(
                        first.string.removeSuffix(
                            second.asString(
                                context
                            )
                        )
                    )
                    is VariableValue.StringType -> VariableValue.StringType(first.string.removeSuffix(second.string))
                    is VariableValue.BooleanType -> VariableValue.StringType(first.string.removeSuffix(second.boolean.toString()))
                    is VariableValue.IntegerType -> VariableValue.StringType(first.string.dropLast(second.integer))
                    is VariableValue.DecimalType -> VariableValue.StringType(first.string.dropLast(second.decimal.toInt()))
//                    is VariableValue.VariableReferenceType -> _operate(context, first, pipelineContext[second.variableName]
//                            ?: VariableValue.NullType)
//                    is VariableValue.WrappedFunctionCallType -> _operate(context, first, pipelineContext.invokeFunction(spiralContext, second.name, second.parameters)
//                            ?: VariableValue.NullType)
//                    is VariableValue.WrappedScriptCallType -> _operate(context, first, pipelineContext.invokeScript(spiralContext, second.name, second.parameters)
//                            ?: VariableValue.NullType)
                    VariableValue.NullType -> first
                    else -> error("Non-flat $second")
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
//                is VariableValue.VariableReferenceType -> _operate(context, pipelineContext[first.variableName]
//                        ?: VariableValue.NullType, second)
//                is VariableValue.WrappedFunctionCallType -> _operate(context, pipelineContext.invokeFunction(spiralContext, first.name, first.parameters)
//                        ?: VariableValue.NullType, second)
//                is VariableValue.WrappedScriptCallType -> _operate(context, pipelineContext.invokeScript(spiralContext, first.name, first.parameters)
//                        ?: VariableValue.NullType, second)
                VariableValue.NullType -> second
                else -> error("Non-flat $first")
            }
        }

        object DIVIDE : ExpressionOperation() {
            override suspend fun _operate(
                context: KnolusContext,
                first: VariableValue,
                second: VariableValue
            ): VariableValue = when (first) {
                is VariableValue.StringComponents -> when (second) {
                    is VariableValue.StringComponents -> VariableValue.StringType(
                        first.asString(context)
                            .let { str -> str.take(str.length / second.asNumber(context).toInt().coerceAtLeast(1)) })
                    is VariableValue.StringType -> VariableValue.StringType(
                        first.asString(context)
                            .let { str -> str.take(str.length / second.asNumber(context).toInt().coerceAtLeast(1)) })
                    is VariableValue.BooleanType -> VariableValue.StringType(
                        first.asString(context).takeIf(second.boolean)
                            ?: ""
                    )
                    is VariableValue.IntegerType -> VariableValue.StringType(
                        first.asString(context).let { str -> str.take(str.length / second.integer) })
                    is VariableValue.DecimalType -> VariableValue.StringType(
                        first.asString(context)
                            .let { str -> str.take(if (second.decimal < 1.0) (str.length * second.decimal).toInt() else str.length / second.decimal.toInt()) })
//                    is VariableValue.VariableReferenceType -> _operate(context, first, pipelineContext[second.variableName]
//                            ?: VariableValue.NullType)
//                    is VariableValue.WrappedFunctionCallType -> _operate(context, first, pipelineContext.invokeFunction(spiralContext, second.name, second.parameters)
//                            ?: VariableValue.NullType)
//                    is VariableValue.WrappedScriptCallType -> _operate(context, first, pipelineContext.invokeScript(spiralContext, second.name, second.parameters)
//                            ?: VariableValue.NullType)
                    VariableValue.NullType -> first
                    else -> error("Non-flat $second")
                }
                is VariableValue.StringType -> when (second) {
                    is VariableValue.StringComponents -> VariableValue.StringType(
                        first.string.take(
                            first.string.length / second.asNumber(
                                context
                            ).toInt().coerceAtLeast(1)
                        )
                    )
                    is VariableValue.StringType -> VariableValue.StringType(
                        first.string.take(
                            first.string.length / second.asNumber(
                                context
                            ).toInt().coerceAtLeast(1)
                        )
                    )
                    is VariableValue.BooleanType -> VariableValue.StringType(first.string.takeIf(second.boolean) ?: "")
                    is VariableValue.IntegerType -> VariableValue.StringType(first.string.take(first.string.length / second.integer))
                    is VariableValue.DecimalType -> VariableValue.StringType(first.string.take(if (second.decimal < 1.0) (first.string.length * second.decimal).toInt() else first.string.length / second.decimal.toInt()))
//                    is VariableValue.VariableReferenceType -> _operate(context, first, pipelineContext[second.variableName]
//                            ?: VariableValue.NullType)
//                    is VariableValue.WrappedFunctionCallType -> _operate(context, first, pipelineContext.invokeFunction(spiralContext, second.name, second.parameters)
//                            ?: VariableValue.NullType)
//                    is VariableValue.WrappedScriptCallType -> _operate(context, first, pipelineContext.invokeScript(spiralContext, second.name, second.parameters)
//                            ?: VariableValue.NullType)
                    VariableValue.NullType -> first
                    else -> error("Non-flat $second")
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
//                is VariableValue.VariableReferenceType -> _operate(context, pipelineContext[first.variableName]
//                        ?: VariableValue.NullType, second)
//                is VariableValue.WrappedFunctionCallType -> _operate(context, pipelineContext.invokeFunction(spiralContext, first.name, first.parameters)
//                        ?: VariableValue.NullType, second)
//                is VariableValue.WrappedScriptCallType -> _operate(context, pipelineContext.invokeScript(spiralContext, first.name, first.parameters)
//                        ?: VariableValue.NullType, second)
                VariableValue.NullType -> second
                else -> error("Non-flat $first")
            }
        }

        object MULTIPLY : ExpressionOperation() {
            override suspend fun _operate(
                context: KnolusContext,
                first: VariableValue,
                second: VariableValue
            ): VariableValue = when (first) {
                is VariableValue.StringComponents -> when (second) {
                    is VariableValue.StringComponents -> VariableValue.StringType(
                        first.asString(context)
                            .let { str -> buildString { repeat(second.asNumber(context).toInt()) { append(str) } } })
                    is VariableValue.StringType -> VariableValue.StringType(
                        first.asString(context)
                            .let { str -> buildString { repeat(second.asNumber(context).toInt()) { append(str) } } })
                    is VariableValue.BooleanType -> VariableValue.StringType(
                        first.asString(context).takeIf(second.boolean)
                            ?: ""
                    )
                    is VariableValue.IntegerType -> VariableValue.StringType(
                        first.asString(context).let { str -> buildString { repeat(second.integer) { append(str) } } })
                    is VariableValue.DecimalType -> VariableValue.StringType(
                        first.asString(context)
                            .let { str -> buildString { repeat(second.decimal.toInt()) { append(str) } } })
//                    is VariableValue.VariableReferenceType -> _operate(context, first, pipelineContext[second.variableName]
//                            ?: VariableValue.NullType)
//                    is VariableValue.WrappedFunctionCallType -> _operate(context, first, pipelineContext.invokeFunction(spiralContext, second.name, second.parameters)
//                            ?: VariableValue.NullType)
//                    is VariableValue.WrappedScriptCallType -> _operate(context, first, pipelineContext.invokeScript(spiralContext, second.name, second.parameters)
//                            ?: VariableValue.NullType)
                    VariableValue.NullType -> first
                    else -> error("Non-flat $second")
                }
                is VariableValue.StringType -> when (second) {
                    is VariableValue.StringComponents -> VariableValue.StringType(buildString {
                        repeat(
                            second.asNumber(
                                context
                            ).toInt()
                        ) { append(first.string) }
                    })
                    is VariableValue.StringType -> VariableValue.StringType(buildString {
                        repeat(
                            second.asNumber(context).toInt()
                        ) { append(first.string) }
                    })
                    is VariableValue.BooleanType -> VariableValue.StringType(first.string.takeIf(second.boolean) ?: "")
                    is VariableValue.IntegerType -> VariableValue.StringType(buildString {
                        repeat(second.integer) {
                            append(
                                first.string
                            )
                        }
                    })
                    is VariableValue.DecimalType -> VariableValue.StringType(buildString {
                        repeat(second.decimal.toInt()) {
                            append(
                                first.string
                            )
                        }
                    })
//                    is VariableValue.VariableReferenceType -> _operate(context, first, pipelineContext[second.variableName]
//                            ?: VariableValue.NullType)
//                    is VariableValue.WrappedFunctionCallType -> _operate(context, first, pipelineContext.invokeFunction(spiralContext, second.name, second.parameters)
//                            ?: VariableValue.NullType)
//                    is VariableValue.WrappedScriptCallType -> _operate(context, first, pipelineContext.invokeScript(spiralContext, second.name, second.parameters)
//                            ?: VariableValue.NullType)
                    VariableValue.NullType -> first
                    else -> error("Non-flat $second")
                }
                is VariableValue.BooleanType -> VariableValue.BooleanType(
                    first.boolean.and(
                        second.asNumber(context).toInt() != 0
                    )
                )
                is VariableValue.IntegerType -> VariableValue.IntegerType(
                    first.integer * second.asNumber(context).toInt()
                )
                is VariableValue.DecimalType -> VariableValue.DecimalType(
                    first.decimal * second.asNumber(context).toDouble()
                )
//                is VariableValue.VariableReferenceType -> _operate(context, pipelineContext[first.variableName]
//                        ?: VariableValue.NullType, second)
//                is VariableValue.WrappedFunctionCallType -> _operate(context, pipelineContext.invokeFunction(spiralContext, first.name, first.parameters)
//                        ?: VariableValue.NullType, second)
//                is VariableValue.WrappedScriptCallType -> _operate(context, pipelineContext.invokeScript(spiralContext, first.name, first.parameters)
//                        ?: VariableValue.NullType, second)
                VariableValue.NullType -> second
                else -> error("Non-flat $first")
            }
        }

        object EXPONENTIAL : ExpressionOperation() {
            override suspend fun _operate(
                context: KnolusContext,
                first: VariableValue,
                second: VariableValue
            ): VariableValue = when (first) {
                is VariableValue.StringComponents -> when (second) {
                    is VariableValue.StringComponents -> VariableValue.StringType(
                        first.asString(context)
                            .let { str ->
                                buildString {
                                    append(str)
                                    repeat(second.asNumber(context).toInt() - 1) { append(this.toString()) }
                                }
                            }
                    )
                    is VariableValue.StringType -> VariableValue.StringType(
                        first.asString(context)
                            .let { str ->
                                buildString {
                                    append(str)
                                    repeat(second.asNumber(context).toInt() - 1) { append(this.toString()) }
                                }
                            }
                    )
                    is VariableValue.BooleanType -> VariableValue.StringType(
                        first.asString(context).takeIf(second.boolean)
                            ?: ""
                    )
                    is VariableValue.IntegerType -> VariableValue.StringType(
                        first.asString(context)
                            .let { str ->
                                buildString {
                                    append(str)
                                    repeat(second.integer - 1) { append(this.toString()) }
                                }
                            }
                    )
                    is VariableValue.DecimalType -> VariableValue.StringType(
                        first.asString(context)
                            .let { str ->
                                buildString {
                                    append(str)
                                    repeat(second.decimal.toInt() - 1) { append(this.toString()) }
                                }
                            }
                    )
                    VariableValue.NullType -> first
                    else -> error("Non-flat $second")
                }
                is VariableValue.StringType -> when (second) {
                    is VariableValue.StringComponents -> VariableValue.StringType(
                        buildString {
                            append(first.string)
                            repeat(second.asNumber(context).toInt() - 1) { append(this.toString()) }
                        }
                    )
                    is VariableValue.StringType -> VariableValue.StringType(
                        buildString {
                            append(first.string)
                            repeat(second.asNumber(context).toInt() - 1) { append(this.toString()) }
                        }
                    )
                    is VariableValue.BooleanType -> VariableValue.StringType(
                        first.string.takeIf(second.boolean) ?: ""
                    )
                    is VariableValue.IntegerType -> VariableValue.StringType(
                        buildString {
                            append(first.string)
                            repeat(second.integer - 1) { append(this.toString()) }
                        }
                    )
                    is VariableValue.DecimalType -> VariableValue.StringType(
                        buildString {
                            append(first.string)
                            repeat(second.decimal.toInt() - 1) { append(this.toString()) }
                        }
                    )
                    VariableValue.NullType -> first
                    else -> error("Non-flat $second")
                }
                is VariableValue.BooleanType -> VariableValue.BooleanType(
                    //Check if even
                    if (second.asNumber(context).toInt().and(0b1) == 0) first.boolean else !first.boolean
                )
                is VariableValue.IntegerType -> VariableValue.IntegerType(
                    first.integer.toDouble().pow(second.asNumber(context).toInt()).toInt()
                )
                is VariableValue.DecimalType -> VariableValue.DecimalType(
                    first.decimal.pow(second.asNumber(context).toDouble())
                )
                VariableValue.NullType -> second
                else -> error("Non-flat $first")
            }
        }

        abstract suspend fun _operate(
            context: KnolusContext,
            first: VariableValue,
            second: VariableValue
        ): VariableValue

        suspend fun operate(context: KnolusContext, first: VariableValue, second: VariableValue): VariableValue =
            _operate(context, first.flatten(context), second.flatten(context))
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
        val global: Boolean = false
    ) : KnolusUnion(), Action {
        override suspend fun run(context: KnolusContext) {
            context[variableName, global] = variableValue ?: VariableValue.UndefinedType
        }
    }

    data class AssignVariableAction(
        val variableName: String,
        val variableValue: VariableValue,
        val global: Boolean = false
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
        val body: ScopeType?
    ) : KnolusUnion(), Action {
        fun asPipelineFunction(): KnolusFunction<VariableValue?> = KnolusFunction(
            functionName,
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

suspend fun KnolusUnion.VariableValue.flattenIfPresent(context: KnolusContext): KnolusUnion.VariableValue? =
    flatten(context).takeIfPresent()

suspend fun KnolusUnion.VariableValue.asFlattenedStringIfPresent(context: KnolusContext): String? =
    flatten(context).takeIfPresent()?.asString(context)

@ExperimentalUnsignedTypes
public inline fun <T : KnolusUnion.VariableValue> T.takeIfPresent(): T? = when (this) {
    is KnolusUnion.VariableValue.NullType -> null
    else -> this
}

public inline fun <T> T.takeIf(predicate: Boolean): T? {
    return if (predicate) this else null
}

fun String.toIntBaseN(): Int = when {
    startsWith("0b") -> substring(2).toInt(2)
    startsWith("0o") -> substring(2).toInt(8)
    startsWith("0x") -> substring(2).toInt(16)
    startsWith("0d") -> substring(2).toInt()
    else -> toInt()
}

fun String.toIntOrNullBaseN(): Int? = when {
    startsWith("0b") -> substring(2).toIntOrNull(2)
    startsWith("0o") -> substring(2).toIntOrNull(8)
    startsWith("0x") -> substring(2).toIntOrNull(16)
    startsWith("0d") -> substring(2).toIntOrNull()
    else -> toIntOrNull()
}