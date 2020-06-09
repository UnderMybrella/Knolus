package org.abimon.knolus

import org.abimon.antlr.knolus.KnolusLexer
import org.abimon.antlr.knolus.KnolusParser
import org.abimon.antlr.knolus.KnolusParserBaseVisitor
import org.abimon.knolus.types.*
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.misc.ParseCancellationException
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.TerminalNode

@ExperimentalUnsignedTypes
/** TODO: Keep KnolusResult in mind, see how allocs do */
class KnolusVisitor(val parser: KnolusParser) : KnolusParserBaseVisitor<KnolusResult<out KnolusUnion>>() {
    companion object {
        const val NO_VALID_VARIABLE_VALUE = 0x1200
        const val NO_VALID_NUMBER_TYPE = 0x1201
        const val NO_VALID_EXPRESSION_OPERATION = 0x1202
        const val NO_VALID_CHAR_VALUE = 0x1203

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
                    variableValue.value,
                    ctx.GLOBAL() != null
                )
            }

    override fun visitSetVariableValue(ctx: KnolusParser.SetVariableValueContext): KnolusResult<out KnolusUnion> =
        visitVariableValue(ctx.variableValue())
            .map { variableValue ->
                KnolusUnion.AssignVariableAction(
                    ctx.variableName.text,
                    variableValue.value,
                    ctx.GLOBAL() != null
                )
            }

    override fun visitVariableValue(ctx: KnolusParser.VariableValueContext?): KnolusResult<out KnolusUnion.VariableValue<out KnolusTypedValue>> {
        if (ctx == null) return KnolusResult.Empty()
        if (ctx.NULL() != null) return KnolusResult.knolusValue(KnolusConstants.Null)

        ctx.quotedCharacter()?.let { return visitQuotedCharacter(it) }
        ctx.quotedString()?.let { return visitQuotedString(it) }
        ctx.number()?.let { return visitNumber(it) }
        ctx.variableReference()?.let { return visitVariableReference(it) }
        ctx.memberVariableReference()?.let { return visitMemberVariableReference(it) }
        ctx.functionCall()?.let { return visitFunctionCall(it) }
        ctx.memberFunctionCall()?.let { return visitMemberFunctionCall(it) }
        ctx.expression()?.let { return visitExpression(it) }
        ctx.bool()?.let { return visitBool(it) }
        ctx.array()?.let { return visitArrayContents(it.arrayContents()) }

        return KnolusResult.Error(NO_VALID_VARIABLE_VALUE,
            "No valid variable value in \"${ctx.text}\" (${ctx.toString(parser)})")
    }

    override fun visitBool(ctx: KnolusParser.BoolContext): KnolusResult<KnolusUnion.VariableValue<KnolusBoolean>> {
        if (ctx.TRUE() != null) return KnolusResult.knolusValue(KnolusBoolean(true))
        if (ctx.FALSE() != null) return KnolusResult.knolusValue(KnolusBoolean(false))

        return KnolusResult.Error(NO_VALID_VARIABLE_VALUE,
            "No valid variable value in \"${ctx.text}\" (${ctx.toString(parser)})")
    }

    override fun visitQuotedString(ctx: KnolusParser.QuotedStringContext): KnolusResult<KnolusUnion.VariableValue<KnolusLazyString>> {
        val components: MutableList<KnolusUnion.StringComponent> = ArrayList()
        val builder = StringBuilder()

        ctx.children.forEach { node ->
            when (node) {
                is TerminalNode -> {
                    when (node.symbol.type) {
                        KnolusParser.ESCAPES -> {
                            when (val c = node.text[1]) {
                                'b' -> builder.append('\b')
                                'f' -> builder.append('\u000C')
                                'n' -> builder.append('\n')
                                'r' -> builder.append('\r')
                                't' -> builder.append('\t')
                                'u' -> builder.append(node.text.substring(2).toInt(16).toChar())
                                else -> builder.append(c)
                            }
                        }

                        KnolusParser.STRING_CHARACTERS -> builder.append(node.text)
                        KnolusParser.STRING_WHITESPACE -> builder.append(node.text)
                        KnolusParser.QUOTED_STRING_LINE_BREAK -> builder.append('\n')
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
        val lazyComponents = components.toTypedArray()
        val lazyStr = KnolusLazyString(lazyComponents)
        return KnolusResult.knolusLazyString(lazyStr)
    }

    override fun visitQuotedCharacter(ctx: KnolusParser.QuotedCharacterContext): KnolusResult<KnolusUnion.VariableValue<KnolusChar>> {
        ctx.CHARACTER_ESCAPES()?.let { node ->
            return KnolusResult.knolusCharValue(when (val c = node.text[1]) {
                'b' -> '\b'
                'f' -> '\u000C'
                'n' -> '\n'
                'r' -> '\r'
                't' -> '\t'
                'u' -> node.text.substring(2).toInt(16).toChar()
                else -> c
            })
        }

        ctx.QUOTED_CHARACTERS()?.let { node -> return KnolusResult.knolusCharValue(node.text[0]) }
        ctx.QUOTED_CHARACTER_LINE_BREAK()?.let { return KnolusResult.knolusCharValue('\n') }

        return KnolusResult.Error(NO_VALID_CHAR_VALUE,
            "No valid char value in \"${ctx.text}\" (${ctx.toString(parser)})")
    }

    override fun visitVariableReference(ctx: KnolusParser.VariableReferenceContext): KnolusResult<KnolusUnion.VariableValue<KnolusVariableReference>> =
        KnolusResult.knolusValue(KnolusVariableReference(ctx.variableName.text.removePrefix("$")))

    override fun visitMemberVariableReference(ctx: KnolusParser.MemberVariableReferenceContext): KnolusResult<KnolusUnion.VariableValue<KnolusPropertyReference>> =
        KnolusResult.knolusValue(KnolusPropertyReference(
            ctx.memberName.text.removeSuffix("."),
            ctx.variableReference().variableName.text
        ))

    override fun visitFunctionCall(ctx: KnolusParser.FunctionCallContext): KnolusResult<KnolusUnion.VariableValue<KnolusLazyFunctionCall>> {
        val functionName = ctx.functionName.text.removeSuffix("(")
        val parameters = ctx.parameters
            .map(this::visitFunctionCallParameter)
            .mapNotNull(KnolusResult<KnolusUnion.FunctionParameterType>::getOrNull)

        return KnolusResult.knolusLazy(
            KnolusLazyFunctionCall(functionName, parameters.toTypedArray())
        )
    }

    override fun visitMemberFunctionCall(ctx: KnolusParser.MemberFunctionCallContext): KnolusResult<KnolusUnion.VariableValue<KnolusLazyMemberFunctionCall>> =
        visitFunctionCall(ctx.functionCall()).flatMap { functionCallType ->
            val memberName = ctx.memberName.text.removeSuffix(".")
            KnolusResult.knolusLazy(KnolusLazyMemberFunctionCall(
                memberName,
                functionCallType.value.name,
                functionCallType.value.parameters
            ))
        }

    override fun visitFunctionCallParameter(ctx: KnolusParser.FunctionCallParameterContext): KnolusResult<KnolusUnion.FunctionParameterType> =
        visitVariableValue(ctx.variableValue()).flatMap { value ->
            KnolusResult.union(
                KnolusUnion.FunctionParameterType(ctx.parameterName?.text?.removeSuffix("="), value.value)
            )
        }

    override fun visitNumber(ctx: KnolusParser.NumberContext): KnolusResult<KnolusUnion.VariableValue<KnolusNumericalType>> {
        ctx.wholeNumber()?.INTEGER()?.let { node ->
            val int = node.text.toIntOrNullBaseN() ?: return KnolusResult.Error(NUMBER_FORMAT_ERROR,
                "${node.text} was not a valid int string")
            return KnolusResult.knolusValue(KnolusInt(int))
        }

        ctx.decimalNumber()?.DECIMAL_NUMBER()?.let { node ->
            val double = node.text.toDoubleOrNull() ?: return KnolusResult.Error(NUMBER_FORMAT_ERROR,
                "${node.text} was not a valid double string")
            return KnolusResult.knolusValue(KnolusDouble(double))
        }

        return KnolusResult.Error(NO_VALID_NUMBER_TYPE,
            "No valid number type in \"${ctx.text}\" (${ctx.toString(parser)})")
    }

    override fun visitWholeNumber(ctx: KnolusParser.WholeNumberContext): KnolusResult<KnolusUnion.VariableValue<KnolusInt>> {
        val int = ctx.INTEGER().text.toIntOrNullBaseN() ?: return KnolusResult.Error(NUMBER_FORMAT_ERROR,
            "${ctx.INTEGER().text} was not a valid int string")
        return KnolusResult.knolusValue(KnolusInt(int))
    }

    override fun visitDecimalNumber(ctx: KnolusParser.DecimalNumberContext): KnolusResult<KnolusUnion.VariableValue<KnolusDouble>> {
        val double = ctx.DECIMAL_NUMBER().text.toDoubleOrNull() ?: return KnolusResult.Error(NUMBER_FORMAT_ERROR,
            "${ctx.DECIMAL_NUMBER().text} was not a valid double string")
        return KnolusResult.knolusValue(KnolusDouble(double))
    }

    override fun visitExpression(ctx: KnolusParser.ExpressionContext): KnolusResult<KnolusUnion.VariableValue<KnolusLazyExpression>> {
        val starting = visitVariableValue(ctx.startingValue)
        val ops: MutableList<Pair<ExpressionOperator, KnolusTypedValue>> = ArrayList()

        repeat(ctx.exprOps.size) { i ->
            val expr = visitExpressionOperation(ctx.exprOps[i]).doOnFailure { return@repeat }
            val value = visitVariableValue(ctx.exprVals[i]).doOnFailure { return@repeat }

            ops.add(Pair(expr, value.value))
        }

        return starting.flatMap { startingValue ->
            KnolusResult.knolusValue(KnolusLazyExpression(startingValue.value, ops.toTypedArray()))
        }
    }

    override fun visitExpressionOperation(ctx: KnolusParser.ExpressionOperationContext): KnolusResult<ExpressionOperator> {
        if (ctx.EXPR_EXPONENTIAL() != null) return KnolusResult.unionExprExponential()

        if (ctx.EXPR_PLUS() != null) return KnolusResult.unionExprPlus()
        if (ctx.EXPR_MINUS() != null) return KnolusResult.unionExprMinus()
        if (ctx.EXPR_DIVIDE() != null) return KnolusResult.unionExprDivide()
        if (ctx.EXPR_MULTIPLY() != null) return KnolusResult.unionExprMultiply()

        return KnolusResult.Error(NO_VALID_EXPRESSION_OPERATION,
            "No valid expression operation in \"${ctx.text}\" (${ctx.toString(parser)})")
    }

    override fun visitArray(ctx: KnolusParser.ArrayContext): KnolusResult<KnolusUnion.VariableValue<KnolusArray<out KnolusTypedValue>>> =
        visitArrayContents(ctx.arrayContents())

    override fun visitArrayContents(ctx: KnolusParser.ArrayContentsContext): KnolusResult<KnolusUnion.VariableValue<KnolusArray<out KnolusTypedValue>>> =
        ctx.children.fold<ParseTree, KnolusResult<MutableList<KnolusTypedValue>>>(KnolusResult.Success(ArrayList())) { acc, child ->
            acc.flatMapOrSelf { list ->
                visit(child)?.filterToInstance<KnolusUnion.VariableValue<KnolusTypedValue>>()?.map { union ->
                    list.add(union.value)
                    list
                }
            }
        }.map { list -> KnolusUnion.VariableValue.Stable(KnolusArray.of(list.toTypedArray())) }
}

@Suppress("UnnecessaryVariable")
@ExperimentalUnsignedTypes
fun parseKnolusScope(text: String): KnolusResult<KnolusUnion.ScopeType> {
    try {
        val charStream = CharStreams.fromString(text)
        val lexer = KnolusLexer(charStream)
        lexer.removeErrorListeners()
        lexer.addErrorListener(ThrowingErrorListener)

        val tokens = CommonTokenStream(lexer)
        val parser = KnolusParser(tokens)
        parser.removeErrorListeners()
        parser.addErrorListener(ThrowingErrorListener)

        val visitor = KnolusVisitor(parser)
        val union = visitor.visitScope(parser.scope())
            .filter { scope -> scope.lines.isNotEmpty() }
        return union
    } catch (pce: ParseCancellationException) {
        return KnolusResult.Thrown(pce)
    }
}

sealed class ScopeResult {
    data class Returned<T: KnolusTypedValue>(val value: T) : ScopeResult()
    data class LineMap(val lines: Array<Pair<KnolusUnion, KnolusResult<*>>>) : ScopeResult() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as LineMap

            if (!lines.contentEquals(other.lines)) return false

            return true
        }

        override fun hashCode(): Int {
            return lines.contentHashCode()
        }
    }
}

@ExperimentalUnsignedTypes
suspend fun KnolusUnion.ScopeType.run(
    parentContext: KnolusContext,
    parameters: Map<String, Any?> = emptyMap(),
    init: KnolusContext.() -> Unit = {},
) = run(parentContext.restrictions, parentContext, parameters, init)

suspend fun KnolusUnion.ScopeType.run(
    restrictions: KnolusRestrictions,
    parentContext: KnolusContext? = null,
    parameters: Map<String, Any?> = emptyMap(),
    init: KnolusContext.() -> Unit = {},
) = runDirect(KnolusContext(parentContext, restrictions), parameters, init)

@ExperimentalUnsignedTypes
suspend fun KnolusUnion.ScopeType.runDirect(
    knolusContext: KnolusContext,
    parameters: Map<String, Any?> = emptyMap(),
    init: KnolusContext.() -> Unit = {},
): ScopeResult {
    parameters.forEach { (k, v) -> knolusContext[k] = v as? KnolusTypedValue ?: return@forEach }

    knolusContext.init()

    return ScopeResult.LineMap(lines.mapWith { union ->
        when (union) {
            is KnolusUnion.Action<*> -> union.run(knolusContext)
            is KnolusUnion.VariableValue<*> ->
                if (union.value is KnolusUnion.Action<*>)
                    (union.value as KnolusUnion.Action<*>).run(knolusContext)
                else
                    KnolusResult.Empty()
            is KnolusUnion.ReturnStatement -> return ScopeResult.Returned(union.value.evaluateOrSelf(knolusContext))
            else -> KnolusResult.Empty()
        }
    })
}