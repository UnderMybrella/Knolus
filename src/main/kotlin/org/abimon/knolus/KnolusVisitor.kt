package org.abimon.knolus

import org.abimon.antlr.knolus.KnolusParser
import org.abimon.antlr.knolus.KnolusParserBaseVisitor
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.TerminalNode

@ExperimentalUnsignedTypes
/** TODO: Keep KnolusResult in mind, see how allocs do */
class KnolusVisitor(val parser: KnolusParser) : KnolusParserBaseVisitor<KnolusResult<out KnolusUnion>>() {
    companion object {
        const val NO_VALID_VARIABLE_VALUE = 0x1200
        const val NO_VALID_NUMBER_TYPE = 0x1201
        const val NO_VALID_EXPRESSION_OPERATION = 0x1202

        const val NUMBER_FORMAT_ERROR = 0x1300
    }

    override fun visitScope(ctx: KnolusParser.ScopeContext): KnolusResult<KnolusUnion.ScopeType> =
        ctx.children?.fold<ParseTree, KnolusResult<MutableList<KnolusUnion>>>(KnolusResult.Success(ArrayList())) { acc, child ->
            acc.flatMapOrSelf { list ->
                visit(child)?.map { union ->
                    list.add(union)
                    list
                }
            }
        }?.map { list -> KnolusUnion.ScopeType(list.toTypedArray()) } ?: KnolusResult.Empty()

    override fun visitDeclareVariable(ctx: KnolusParser.DeclareVariableContext): KnolusResult<KnolusUnion> =
        visitVariableValue(ctx.variableValue())
            .map { variableValue ->
                KnolusUnion.DeclareVariableAction(
                    ctx.variableName.text,
                    variableValue,
                    ctx.GLOBAL() != null
                )
            }

    override fun visitVariableValue(ctx: KnolusParser.VariableValueContext?): KnolusResult<out KnolusUnion.VariableValue> {
        if (ctx == null) return KnolusResult.Empty()
        if (ctx.TRUE() != null) return KnolusResult.unionVariable(KnolusUnion.VariableValue.BooleanType(true))
        if (ctx.FALSE() != null) return KnolusResult.unionVariable(KnolusUnion.VariableValue.BooleanType(false))
        if (ctx.NULL() != null) return KnolusResult.unionVariable(KnolusUnion.VariableValue.NullType)

        ctx.quotedString()?.let { return visitQuotedString(it) }
        ctx.number()?.let { return visitNumber(it) }
        ctx.variableReference()?.let { return visitVariableReference(it) }
        ctx.memberVariableReference()?.let { return visitMemberVariableReference(it) }
        ctx.functionCall()?.let { return visitFunctionCall(it) }
        ctx.memberFunctionCall()?.let { return visitMemberFunctionCall(it) }
        ctx.expression()?.let { return visitExpression(it) }

        return KnolusResult.Error(NO_VALID_VARIABLE_VALUE, "No valid variable value in \"${ctx.text}\" (${ctx.toString(parser)})")
    }

    override fun visitQuotedString(ctx: KnolusParser.QuotedStringContext): KnolusResult<KnolusUnion.VariableValue.StringComponents> {
        val components: MutableList<KnolusUnion.StringComponent> = ArrayList()
        val builder = StringBuilder()

        ctx.children.forEach { node ->
            when (node) {
                is TerminalNode -> {
                    when (node.symbol.type) {
                        KnolusParser.ESCAPES -> {
                            when (val c = node.text[1]) {
                                'b' -> builder.append('\b')
                                'f' -> builder.append(0x0C.toChar())
                                'n' -> builder.append('\n')
                                'r' -> builder.append('\r')
                                't' -> builder.append('\t')
                                'u' -> builder.append(node.text.substring(2).toInt(16).toChar())
                                else -> builder.append(c)
                            }
                        }

                        KnolusParser.STRING_CHARACTERS -> builder.append(node.text)
                        KnolusParser.STRING_WHITESPACE -> builder.append(node.text)
                        KnolusParser.QUOTED_STRING_LINE_BREAK -> builder.append("\n")
                    }
                }
                is KnolusParser.QuotedStringVariableReferenceContext -> {
                    if (builder.isNotEmpty()) {
                        components.add(KnolusUnion.StringComponent.RawText(builder.toString()))
                        builder.clear()
                    }

                    components.add(KnolusUnion.StringComponent.VariableReference(node.variableReference().variableName.text))
                }
                else -> println("$node is unaccounted for!")
            }
        }

        if (builder.isNotEmpty()) components.add(KnolusUnion.StringComponent.RawText(builder.toString()))

        return KnolusResult.unionVariable(KnolusUnion.VariableValue.StringComponents(components.toTypedArray()))
    }

    override fun visitVariableReference(ctx: KnolusParser.VariableReferenceContext): KnolusResult<KnolusUnion.VariableValue.VariableReferenceType> =
        KnolusResult.unionVariable(KnolusUnion.VariableValue.VariableReferenceType(ctx.variableName.text.removePrefix("$")))

    override fun visitMemberVariableReference(ctx: KnolusParser.MemberVariableReferenceContext): KnolusResult<KnolusUnion.VariableValue.MemberVariableReferenceType> =
        KnolusResult.unionVariable(KnolusUnion.VariableValue.MemberVariableReferenceType(ctx.memberName.text.removeSuffix("."), ctx.variableReference().variableName.text))

    override fun visitFunctionCall(ctx: KnolusParser.FunctionCallContext): KnolusResult<KnolusUnion.VariableValue.FunctionCallType> {
        val functionName = ctx.functionName.text.removeSuffix("(")
        val parameters = ctx.parameters
            .map(this::visitFunctionCallParameter)
            .mapNotNull(KnolusResult<KnolusUnion.FunctionParameterType>::getOrNull)

        return KnolusResult.unionVariable(
            KnolusUnion.VariableValue.FunctionCallType(functionName, parameters.toTypedArray())
        )
    }

    override fun visitMemberFunctionCall(ctx: KnolusParser.MemberFunctionCallContext): KnolusResult<KnolusUnion.VariableValue.MemberFunctionCallType> =
        visitFunctionCall(ctx.functionCall()).flatMap { functionCallType ->
            val memberName = ctx.memberName.text.removeSuffix(".")
            KnolusResult.unionVariable(KnolusUnion.VariableValue.MemberFunctionCallType(memberName, functionCallType.name, functionCallType.parameters))
        }

    override fun visitFunctionCallParameter(ctx: KnolusParser.FunctionCallParameterContext): KnolusResult<KnolusUnion.FunctionParameterType> =
        visitVariableValue(ctx.variableValue())
            .flatMap { value -> KnolusResult.union(KnolusUnion.FunctionParameterType(ctx.parameterName?.text?.removeSuffix("="), value)) }

    override fun visitNumber(ctx: KnolusParser.NumberContext): KnolusResult<KnolusUnion.VariableValue> {
        ctx.wholeNumber()?.INTEGER()?.let { node ->
            val int = node.text.toIntOrNullBaseN() ?: return KnolusResult.Error(NUMBER_FORMAT_ERROR, "${node.text} was not a valid int string")
            return KnolusResult.unionVariable(KnolusUnion.VariableValue.IntegerType(int))
        }

        ctx.decimalNumber()?.DECIMAL_NUMBER()?.let { node ->
            val double = node.text.toDoubleOrNull() ?: return KnolusResult.Error(NUMBER_FORMAT_ERROR, "${node.text} was not a valid double string")
            return KnolusResult.unionVariable(KnolusUnion.VariableValue.DecimalType(double))
        }

        return KnolusResult.Error(NO_VALID_NUMBER_TYPE, "No valid number type in \"${ctx.text}\" (${ctx.toString(parser)})")
    }

    override fun visitExpression(ctx: KnolusParser.ExpressionContext): KnolusResult<KnolusUnion.VariableValue.ExpressionType> {
        val starting = visitVariableValue(ctx.startingValue)
        val ops: MutableList<Pair<KnolusUnion.ExpressionOperation, KnolusUnion.VariableValue>> = ArrayList()

        repeat(ctx.exprOps.size) { i ->
            val expr = visitExpressionOperation(ctx.exprOps[i]).doOnFailure { return@repeat }
            val value = visitVariableValue(ctx.exprVals[i]).doOnFailure { return@repeat }

            ops.add(Pair(expr, value))
        }

        return starting.flatMap { startingValue -> KnolusResult.unionVariable(KnolusUnion.VariableValue.ExpressionType(startingValue, ops.toTypedArray())) }
    }

    override fun visitExpressionOperation(ctx: KnolusParser.ExpressionOperationContext): KnolusResult<KnolusUnion.ExpressionOperation> {
        if (ctx.EXPR_EXPONENTIAL() != null) return KnolusResult.unionExprExponential()

        if (ctx.EXPR_PLUS() != null) return KnolusResult.unionExprPlus()
        if (ctx.EXPR_MINUS() != null) return KnolusResult.unionExprMinus()
        if (ctx.EXPR_DIVIDE() != null) return KnolusResult.unionExprDivide()
        if (ctx.EXPR_MULTIPLY() != null) return KnolusResult.unionExprMultiply()

        return KnolusResult.Error(NO_VALID_EXPRESSION_OPERATION, "No valid expression operation in \"${ctx.text}\" (${ctx.toString(parser)})")
    }
}