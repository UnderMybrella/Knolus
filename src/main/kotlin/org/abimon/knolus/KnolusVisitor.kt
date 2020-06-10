package org.abimon.knolus

import org.abimon.antlr.knolus.KnolusLexer
import org.abimon.antlr.knolus.KnolusParser
import org.abimon.antlr.knolus.KnolusParserBaseVisitor
import org.abimon.knolus.context.KnolusContext
import org.abimon.knolus.context.KnolusScopeContext
import org.abimon.knolus.restrictions.KnolusRestrictions
import org.abimon.knolus.restrictions.KnolusVisitorRestrictions
import org.abimon.knolus.types.*
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.misc.ParseCancellationException
import org.antlr.v4.runtime.tree.TerminalNode

@ExperimentalUnsignedTypes
/** TODO: Keep KnolusResult in mind, see how allocs do */
class KnolusVisitor(val restrictions: KnolusVisitorRestrictions, val parser: KnolusParser) :
    KnolusParserBaseVisitor<KnolusResult<out KnolusUnion>>() {
    companion object {
        const val NO_VALID_VARIABLE_VALUE = 0x1200
        const val NO_VALID_NUMBER_TYPE = 0x1201
        const val NO_VALID_EXPRESSION_OPERATION = 0x1202
        const val NO_VALID_CHAR_VALUE = 0x1203

        const val NUMBER_FORMAT_ERROR = 0x1300

        const val SCOPE_VISIT_DENIED = 0x1E00
        const val VARIABLE_DECL_VISIT_DENIED = 0x1E01
        const val VARIABLE_ASSIGN_VISIT_DENIED = 0x1E02
        const val VARIABLE_VALUE_VISIT_DENIED = 0x1E03
        const val BOOLEAN_VISIT_DENIED = 0x1E04
        const val QUOTED_STRING_VISIT_DENIED = 0x1E05
        const val QUOTED_CHARACTER_VISIT_DENIED = 0x1E06
        const val VARIABLE_REF_VISIT_DENIED = 0x1E07
        const val MEMBER_VARIABLE_REF_VISIT_DENIED = 0x1E08
        const val FUNCTION_CALL_VISIT_DENIED = 0x1E09
        const val MEMBER_FUNCTION_CALL_VISIT_DENIED = 0x1E0A
        const val FUNCTION_CALL_PARAM_VISIT_DENIED = 0x1E0B
        const val NUMBER_VISIT_DENIED = 0x1E0C
        const val WHOLE_NUMBER_VISIT_DENIED = 0x1E0D
        const val DECIMAL_NUMBER_VISIT_DENIED = 0x1E0E
        const val EXPRESSION_VISIT_DENIED = 0x1E0F
        const val EXPRESSION_OPERATION_VISIT_DENIED = 0x1E10
        const val ARRAY_VISIT_DENIED = 0x1E11
        const val ARRAY_CONTENTS_VISIT_DENIED = 0x1E12

        const val SCOPE_RESULT_DENIED = 0x1F00
        const val VARIABLE_DECL_RESULT_DENIED = 0x1F01
        const val VARIABLE_ASSIGN_RESULT_DENIED = 0x1F02
        const val VARIABLE_VALUE_RESULT_DENIED = 0x1F03
        const val BOOLEAN_RESULT_DENIED = 0x1F04
        const val QUOTED_STRING_RESULT_DENIED = 0x1F05
        const val QUOTED_CHARACTER_RESULT_DENIED = 0x1F06
        const val VARIABLE_REF_RESULT_DENIED = 0x1F07
        const val MEMBER_VARIABLE_REF_RESULT_DENIED = 0x1F08
        const val FUNCTION_CALL_RESULT_DENIED = 0x1F09
        const val MEMBER_FUNCTION_CALL_RESULT_DENIED = 0x1F0A
        const val FUNCTION_CALL_PARAM_RESULT_DENIED = 0x1F0B
        const val NUMBER_RESULT_DENIED = 0x1F0C
        const val WHOLE_NUMBER_RESULT_DENIED = 0x1F0D
        const val DECIMAL_NUMBER_RESULT_DENIED = 0x1F0E
        const val EXPRESSION_RESULT_DENIED = 0x1F0F
        const val EXPRESSION_OPERATION_RESULT_DENIED = 0x1F10
        const val ARRAY_RESULT_DENIED = 0x1F11
        const val ARRAY_CONTENTS_RESULT_DENIED = 0x1F12
    }

    override fun visitScope(ctx: KnolusParser.ScopeContext): KnolusResult<KnolusUnion.ScopeType> {
        if (!restrictions.canVisitScope(ctx))
            return KnolusResult.Error(SCOPE_VISIT_DENIED, "Restriction denied scope visit")

        if (ctx.children?.isNotEmpty() != true) return KnolusResult.empty()
        val initial: KnolusResult<MutableList<KnolusUnion>> = KnolusResult.success(ArrayList())

        return ctx.children.fold(initial) { acc, child ->
            acc.flatMapOrSelf { list ->
                visit(child)?.map { union ->
                    list.add(union)
                    list
                }
            }
        }.filter(List<KnolusUnion>::isNotEmpty).map { list ->
            KnolusUnion.ScopeType(list.toTypedArray())
        }.filterTo { scope ->
            if (restrictions.shouldTakeScope(ctx, scope)) null
            else KnolusResult.Error(SCOPE_RESULT_DENIED, "Restriction denied scope result")
        }
    }

    override fun visitDeclareVariable(ctx: KnolusParser.DeclareVariableContext): KnolusResult<KnolusUnion.DeclareVariableAction> {
        if (!restrictions.canVisitVariableDeclaration(ctx))
            return KnolusResult.Error(VARIABLE_DECL_VISIT_DENIED, "Restriction denied variable declaration visit")

        return ctx.variableValue()?.let(this::visitVariableValue)
            .switchIfNull { KnolusResult.knolusValue(KnolusConstants.Undefined) }
            .map { variableValue ->
                KnolusUnion.DeclareVariableAction(
                    ctx.variableName.text,
                    variableValue.value,
                    ctx.GLOBAL() != null
                )
            }.filterTo { variableDeclaration ->
                if (restrictions.shouldTakeVariableDeclaration(ctx, variableDeclaration)) null
                else KnolusResult.Error(VARIABLE_DECL_RESULT_DENIED, "Restriction denied variable declaration result")
            }
    }

    override fun visitSetVariableValue(ctx: KnolusParser.SetVariableValueContext): KnolusResult<out KnolusUnion> {
        if (!restrictions.canVisitVariableAssignment(ctx))
            return KnolusResult.Error(VARIABLE_ASSIGN_VISIT_DENIED, "Restriction denied variable assign visit")

        return visitVariableValue(ctx.variableValue())
            .map { variableValue ->
                KnolusUnion.AssignVariableAction(
                    ctx.variableName.text,
                    variableValue.value,
                    ctx.GLOBAL() != null
                )
            }.filterTo { variableAssign ->
                if (restrictions.shouldTakeVariableAssignment(ctx, variableAssign)) null
                else KnolusResult.Error(VARIABLE_ASSIGN_RESULT_DENIED, "Restriction denied variable assign result")
            }
    }

    override fun visitVariableValue(ctx: KnolusParser.VariableValueContext?): KnolusResult<KnolusUnion.VariableValue<KnolusTypedValue>> {
        if (ctx == null) return KnolusResult.Empty()

        if (!restrictions.canVisitVariableValue(ctx))
            return KnolusResult.Error(VARIABLE_VALUE_VISIT_DENIED, "Restriction denied variable value visit")

        val variableValue =
            (if (ctx.NULL() != null) KnolusResult.knolusValue(KnolusConstants.Null) else null)
                ?: ctx.quotedCharacter()?.let(this::visitQuotedCharacter)
                ?: ctx.quotedString()?.let(this::visitQuotedString)
                ?: ctx.number()?.let(this::visitNumber)
                ?: ctx.variableReference()?.let(this::visitVariableReference)
                ?: ctx.memberVariableReference()?.let(this::visitMemberVariableReference)
                ?: ctx.functionCall()?.let(this::visitFunctionCall)
                ?: ctx.memberFunctionCall()?.let(this::visitMemberFunctionCall)
                ?: ctx.expression()?.let(this::visitExpression)
                ?: ctx.bool()?.let(this::visitBool)
                ?: ctx.array()?.let(this::visitArray)
                ?: KnolusResult.Error(NO_VALID_VARIABLE_VALUE,
                    "No valid variable value in \"${ctx.text}\" (${ctx.toString(parser)})")

        return variableValue.filterTo { value ->
            if (restrictions.shouldTakeVariableValue(ctx, value)) null
            else KnolusResult.Error(VARIABLE_VALUE_RESULT_DENIED, "Restriction denied variable value result")
        }
    }

    override fun visitBool(ctx: KnolusParser.BoolContext): KnolusResult<KnolusUnion.VariableValue<KnolusBoolean>> {
        if (!restrictions.canVisitBoolean(ctx))
            return KnolusResult.Error(BOOLEAN_VISIT_DENIED, "Restriction denied boolean visit")

        val value =
            if (ctx.TRUE() != null) KnolusResult.knolusValue(KnolusBoolean(true))
            else if (ctx.FALSE() != null) KnolusResult.knolusValue(KnolusBoolean(false))
            else KnolusResult.Error(NO_VALID_VARIABLE_VALUE,
                "No valid variable value in \"${ctx.text}\" (${ctx.toString(parser)})")

        return value.filterTo { bool ->
            if (restrictions.shouldTakeBoolean(ctx, bool)) null
            else KnolusResult.Error(BOOLEAN_RESULT_DENIED, "Restriction denied boolean result")
        }
    }

    override fun visitQuotedString(ctx: KnolusParser.QuotedStringContext): KnolusResult<KnolusUnion.VariableValue<KnolusLazyString>> {
        if (!restrictions.canVisitQuotedString(ctx))
            return KnolusResult.Error(QUOTED_STRING_VISIT_DENIED, "Restriction denied quoted string visit")

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

        return KnolusResult.knolusLazyString(lazyStr).filterTo { string ->
            if (restrictions.shouldTakeQuotedString(ctx, string)) null
            else KnolusResult.Error(QUOTED_STRING_RESULT_DENIED, "Restriction denied quoted string result")
        }
    }

    override fun visitQuotedCharacter(ctx: KnolusParser.QuotedCharacterContext): KnolusResult<KnolusUnion.VariableValue<KnolusChar>> {
        if (!restrictions.canVisitQuotedCharacter(ctx))
            return KnolusResult.Error(QUOTED_CHARACTER_VISIT_DENIED, "Restriction denied quoted character visit")

        val charValue = ctx.CHARACTER_ESCAPES()?.let { node ->
            KnolusResult.knolusCharValue(when (val c = node.text[1]) {
                'b' -> '\b'
                'f' -> '\u000C'
                'n' -> '\n'
                'r' -> '\r'
                't' -> '\t'
                'u' -> node.text.substring(2).toInt(16).toChar()
                else -> c
            })
        } ?: ctx.QUOTED_CHARACTERS()?.let { node -> KnolusResult.knolusCharValue(node.text[0]) }
        ?: ctx.QUOTED_CHARACTER_LINE_BREAK()?.let { node -> KnolusResult.knolusCharValue('\n') }
        ?: ctx.QUOTED_CHARACTER_LINE_BREAK()?.let { return KnolusResult.knolusCharValue('\n') }
        ?: KnolusResult.Error(NO_VALID_CHAR_VALUE, "No valid char value in \"${ctx.text}\" (${ctx.toString(parser)})")

        return charValue.filterTo { char ->
            if (restrictions.shouldTakeQuotedCharacter(ctx, char)) null
            else KnolusResult.Error(QUOTED_CHARACTER_RESULT_DENIED, "Restriction denied quoted character result")
        }
    }

    override fun visitVariableReference(ctx: KnolusParser.VariableReferenceContext): KnolusResult<KnolusUnion.VariableValue<KnolusVariableReference>> {
        if (!restrictions.canVisitVariableReference(ctx))
            return KnolusResult.Error(VARIABLE_REF_VISIT_DENIED, "Restriction denied variable reference visit")

        return KnolusResult.knolusValue(KnolusVariableReference(ctx.variableName.text.removePrefix("$")))
            .filterTo { ref ->
                if (restrictions.shouldTakeVariableReference(ctx, ref)) null
                else KnolusResult.Error(VARIABLE_REF_RESULT_DENIED, "Restriction denied variable reference result")
            }
    }

    override fun visitMemberVariableReference(ctx: KnolusParser.MemberVariableReferenceContext): KnolusResult<KnolusUnion.VariableValue<KnolusPropertyReference>> {
        if (!restrictions.canVisitMemberVariableReference(ctx))
            return KnolusResult.Error(MEMBER_VARIABLE_REF_VISIT_DENIED,
                "Restriction denied member variable reference visit")

        return KnolusResult.knolusValue(KnolusPropertyReference(
            ctx.memberName.text.removeSuffix("."),
            ctx.variableReference().variableName.text
        )).filterTo { ref ->
            if (restrictions.shouldTakeMemberVariableReference(ctx, ref)) null
            else KnolusResult.Error(MEMBER_VARIABLE_REF_RESULT_DENIED,
                "Restriction denied member variable reference result")
        }
    }

    override fun visitFunctionCall(ctx: KnolusParser.FunctionCallContext): KnolusResult<KnolusUnion.VariableValue<KnolusLazyFunctionCall>> {
        if (!restrictions.canVisitFunctionCall(ctx))
            return KnolusResult.Error(FUNCTION_CALL_VISIT_DENIED, "Restriction denied function call visit")

        val functionName = ctx.functionName.text.removeSuffix("(")
        val initial: KnolusResult<MutableList<KnolusUnion.FunctionParameterType>> = KnolusResult.Success(ArrayList())
        return ctx.parameters
            .fold(initial) { acc, param ->
                acc.flatMap { list ->
                    visitFunctionCallParameter(param).map(list::withElement)
                }
            }.flatMap { params -> KnolusResult.knolusLazy(KnolusLazyFunctionCall(functionName, params.toTypedArray())) }
            .filterTo { func ->
                if (restrictions.shouldTakeFunctionCall(ctx, func)) null
                else KnolusResult.Error(FUNCTION_CALL_RESULT_DENIED, "Restriction denied function call result")
            }
    }

    override fun visitMemberFunctionCall(ctx: KnolusParser.MemberFunctionCallContext): KnolusResult<KnolusUnion.VariableValue<KnolusLazyMemberFunctionCall>> {
        if (!restrictions.canVisitMemberFunctionCall(ctx))
            return KnolusResult.Error(MEMBER_FUNCTION_CALL_VISIT_DENIED,
                "Restriction denied member function call visit")

        return visitFunctionCall(ctx.functionCall())
            .flatMap { functionCallType ->
                val memberName = ctx.memberName.text.removeSuffix(".")
                KnolusResult.knolusLazy(KnolusLazyMemberFunctionCall(
                    memberName,
                    functionCallType.value.name,
                    functionCallType.value.parameters
                ))
            }.filterTo { func ->
                if (restrictions.shouldTakeMemberFunctionCall(ctx, func)) null
                else KnolusResult.Error(MEMBER_FUNCTION_CALL_RESULT_DENIED,
                    "Restriction denied member function call result")
            }
    }

    override fun visitFunctionCallParameter(ctx: KnolusParser.FunctionCallParameterContext): KnolusResult<KnolusUnion.FunctionParameterType> {
        if (!restrictions.canVisitFunctionCallParameter(ctx))
            return KnolusResult.Error(FUNCTION_CALL_PARAM_VISIT_DENIED,
                "Restriction denied function call parameter visit")

        return visitVariableValue(ctx.variableValue())
            .flatMap { value ->
                KnolusResult.union(KnolusUnion.FunctionParameterType(
                    ctx.parameterName?.text?.removeSuffix("="),
                    value.value
                ))
            }.filterTo { param ->
                if (restrictions.shouldTakeFunctionCallParameter(ctx, param)) null
                else KnolusResult.Error(FUNCTION_CALL_PARAM_RESULT_DENIED,
                    "Restriction denied function call parameter result")
            }
    }

    override fun visitNumber(ctx: KnolusParser.NumberContext): KnolusResult<KnolusUnion.VariableValue<KnolusNumericalType>> {
        if (!restrictions.canVisitNumber(ctx))
            return KnolusResult.Error(NUMBER_VISIT_DENIED, "Restriction denied number visit")

        val number = ctx.wholeNumber()?.let(this::visitWholeNumber)
            ?: ctx.decimalNumber()?.let(this::visitDecimalNumber)
            ?: KnolusResult.Error(
                NO_VALID_NUMBER_TYPE, "No valid number type in \"${ctx.text}\" (${ctx.toString(parser)})"
            )

        return number.filterTo { num ->
            if (restrictions.shouldTakeNumber(ctx, num)) null
            else KnolusResult.Error(NUMBER_RESULT_DENIED, "Restriction denied number result")
        }
    }

    override fun visitWholeNumber(ctx: KnolusParser.WholeNumberContext): KnolusResult<KnolusUnion.VariableValue<KnolusInt>> {
        if (!restrictions.canVisitWholeNumber(ctx))
            return KnolusResult.Error(WHOLE_NUMBER_VISIT_DENIED, "Restriction denied whole number visit")

        val int = ctx.INTEGER().text.toIntOrNullBaseN()
            ?: return KnolusResult.Error(NUMBER_FORMAT_ERROR, "${ctx.INTEGER().text} was not a valid int string")

        return KnolusResult.knolusValue(KnolusInt(int)).filterTo { num ->
            if (restrictions.shouldTakeWholeNumber(ctx, num)) null
            else KnolusResult.Error(WHOLE_NUMBER_RESULT_DENIED, "Restriction denied whole number result")
        }
    }

    override fun visitDecimalNumber(ctx: KnolusParser.DecimalNumberContext): KnolusResult<KnolusUnion.VariableValue<KnolusDouble>> {
        if (!restrictions.canVisitDecimalNumber(ctx))
            return KnolusResult.Error(DECIMAL_NUMBER_VISIT_DENIED, "Restriction denied decimal number visit")

        val double = ctx.DECIMAL_NUMBER().text.toDoubleOrNull()
            ?: return KnolusResult.Error(NUMBER_FORMAT_ERROR,
                "${ctx.DECIMAL_NUMBER().text} was not a valid double string")

        return KnolusResult.knolusValue(KnolusDouble(double)).filterTo { num ->
            if (restrictions.shouldTakeDecimalNumber(ctx, num)) null
            else KnolusResult.Error(DECIMAL_NUMBER_RESULT_DENIED, "Restriction denied decimal number result")
        }
    }

    override fun visitExpression(ctx: KnolusParser.ExpressionContext): KnolusResult<KnolusUnion.VariableValue<KnolusLazyExpression>> {
        if (!restrictions.canVisitExpression(ctx))
            return KnolusResult.Error(EXPRESSION_VISIT_DENIED, "Restriction denied expression visit")

        val starting = visitVariableValue(ctx.startingValue).doOnFailure { return it.cast() }
        val ops: MutableList<Pair<ExpressionOperator, KnolusTypedValue>> = ArrayList()

        for (i in ctx.exprOps.indices) {
            val expr = visitExpressionOperation(ctx.exprOps[i]).doOnFailure { return it.cast() }
            val value = visitVariableValue(ctx.exprVals[i]).doOnFailure { return it.cast() }

            ops.add(Pair(expr, value.value))
        }

        return KnolusResult.knolusValue(KnolusLazyExpression(starting.value, ops.toTypedArray())).filterTo { expr ->
            if (restrictions.shouldTakeExpression(ctx, expr)) null
            else KnolusResult.Error(EXPRESSION_RESULT_DENIED, "Restriction denied expression result")
        }
    }

    override fun visitExpressionOperation(ctx: KnolusParser.ExpressionOperationContext): KnolusResult<ExpressionOperator> {
        if (!restrictions.canVisitExpressionOperation(ctx))
            return KnolusResult.Error(EXPRESSION_OPERATION_VISIT_DENIED,
                "Restriction denied expression operation visit")

        val value =
            if (ctx.EXPR_EXPONENTIAL() != null) KnolusResult.unionExprExponential()
            else if (ctx.EXPR_PLUS() != null) KnolusResult.unionExprPlus()
            else if (ctx.EXPR_MINUS() != null) KnolusResult.unionExprMinus()
            else if (ctx.EXPR_DIVIDE() != null) KnolusResult.unionExprDivide()
            else if (ctx.EXPR_MULTIPLY() != null) KnolusResult.unionExprMultiply()
            else KnolusResult.Error(NO_VALID_EXPRESSION_OPERATION,
                "No valid expression operation in \"${ctx.text}\" (${ctx.toString(parser)})")

        return value.filterTo { op ->
            if (restrictions.shouldTakeExpressionOperation(ctx, op)) null
            else KnolusResult.Error(EXPRESSION_OPERATION_RESULT_DENIED,
                "Restriction denied expression operation result")
        }
    }

    override fun visitArray(ctx: KnolusParser.ArrayContext): KnolusResult<KnolusUnion.VariableValue<KnolusArray<out KnolusTypedValue>>> {
        if (!restrictions.canVisitArray(ctx))
            return KnolusResult.Error(ARRAY_VISIT_DENIED, "Restriction denied array visit")

        return visitArrayContents(ctx.arrayContents())
            .flatMap { array -> KnolusResult.knolusValue(KnolusArray.of(array.inner)) }
            .filterTo { array ->
                if (restrictions.shouldTakeArray(ctx, array)) null
                else KnolusResult.Error(ARRAY_RESULT_DENIED, "Restriction denied array result")
            }
    }

    override fun visitArrayContents(ctx: KnolusParser.ArrayContentsContext): KnolusResult<KnolusUnion.ArrayContents> {
        if (!restrictions.canVisitArrayContents(ctx))
            return KnolusResult.Error(ARRAY_CONTENTS_VISIT_DENIED, "Restriction denied array contents visit")

        val initial: KnolusResult<MutableList<KnolusTypedValue>> = KnolusResult.success(ArrayList())

        return ctx.children.fold(initial) { acc, child ->
            acc.flatMapOrSelf { list ->
                visit(child)?.filterToInstance<KnolusUnion.VariableValue<KnolusTypedValue>>()?.map { union ->
                    list.add(union.value)
                    list
                }
            }
        }.map { list ->
            KnolusUnion.ArrayContents(list.toTypedArray())
        }.filterTo { array ->
            if (restrictions.shouldTakeArrayContents(ctx, array)) null
            else KnolusResult.Error(ARRAY_CONTENTS_RESULT_DENIED, "Restriction denied array contents result")
        }
    }
}

@Suppress("UnnecessaryVariable")
@ExperimentalUnsignedTypes
fun parseKnolusScope(text: String, restrictions: KnolusVisitorRestrictions): KnolusResult<KnolusUnion.ScopeType> {
    try {
        val charStream = CharStreams.fromString(text)
        val lexer = KnolusLexer(charStream)
        lexer.removeErrorListeners()
        lexer.addErrorListener(ThrowingErrorListener)

        val tokens = CommonTokenStream(lexer)
        val parser = KnolusParser(tokens)
        parser.removeErrorListeners()
        parser.addErrorListener(ThrowingErrorListener)

        val visitor = KnolusVisitor(restrictions, parser)
        val union = visitor.visitScope(parser.scope())
            .filter { scope -> scope.lines.isNotEmpty() }
        return union
    } catch (pce: ParseCancellationException) {
        return KnolusResult.Thrown(pce)
    }
}

sealed class ScopeResult {
    data class Returned<T : KnolusTypedValue>(val value: T) : ScopeResult()
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
) = runDirect(KnolusScopeContext(this, parentContext, restrictions), parameters, init)

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