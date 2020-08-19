package dev.brella.knolus

import dev.brella.antlr.knolus.KnolusLexer
import dev.brella.antlr.knolus.KnolusParser
import dev.brella.antlr.knolus.KnolusParserBaseVisitor
import dev.brella.knolus.context.KnolusContext
import dev.brella.knolus.context.KnolusScopeContext
import dev.brella.knolus.restrictions.KnolusRestriction
import dev.brella.knolus.restrictions.KnolusVisitorRestrictions
import dev.brella.knolus.transform.KnolusTokenBlueprint
import dev.brella.knolus.transform.TransKnolusVisitor
import dev.brella.knolus.types.*
import dev.brella.kornea.annotations.ChangedSince
import dev.brella.kornea.errors.common.*
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.Recognizer
import org.antlr.v4.runtime.misc.ParseCancellationException
import org.antlr.v4.runtime.tree.TerminalNode
import dev.brella.kornea.toolkit.common.switchIfNull

@ExperimentalUnsignedTypes
@ChangedSince(Knolus.VERSION_1_3_0)
/** TODO: Keep KorneaResult in mind, see how allocs do */
class KnolusVisitor(val restrictions: KnolusVisitorRestrictions<*>, val parser: Recognizer<*, *>) : KnolusParserBaseVisitor<KorneaResult<KnolusUnion>>() {
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
        const val STRING_VALUE_VISIT_DENIED = 0x1E04
        const val BOOLEAN_VISIT_DENIED = 0x1E05
        const val QUOTED_STRING_VISIT_DENIED = 0x1E06
        const val PLAIN_STRING_VISIT_DENIED = 0x1E07
        const val QUOTED_CHARACTER_VISIT_DENIED = 0x1E08
        const val VARIABLE_REF_VISIT_DENIED = 0x1E09
        const val MEMBER_VARIABLE_REF_VISIT_DENIED = 0x1E0A
        const val FUNCTION_CALL_VISIT_DENIED = 0x1E0B
        const val MEMBER_FUNCTION_CALL_VISIT_DENIED = 0x1E0C
        const val FUNCTION_CALL_PARAM_VISIT_DENIED = 0x1E0D
        const val NUMBER_VISIT_DENIED = 0x1E0E
        const val WHOLE_NUMBER_VISIT_DENIED = 0x1E0F
        const val DECIMAL_NUMBER_VISIT_DENIED = 0x1E10
        const val EXPRESSION_VISIT_DENIED = 0x1E11
        const val EXPRESSION_OPERATION_VISIT_DENIED = 0x1E12
        const val ARRAY_VISIT_DENIED = 0x1E13
        const val ARRAY_CONTENTS_VISIT_DENIED = 0x1E14

        const val SCOPE_RESULT_DENIED = 0x1F00
        const val VARIABLE_DECL_RESULT_DENIED = 0x1F01
        const val VARIABLE_ASSIGN_RESULT_DENIED = 0x1F02
        const val VARIABLE_VALUE_RESULT_DENIED = 0x1F03
        const val STRING_VALUE_RESULT_DENIED = 0x1F04
        const val BOOLEAN_RESULT_DENIED = 0x1F05
        const val QUOTED_STRING_RESULT_DENIED = 0x1F06
        const val PLAIN_STRING_RESULT_DENIED = 0x1F07
        const val QUOTED_CHARACTER_RESULT_DENIED = 0x1F08
        const val VARIABLE_REF_RESULT_DENIED = 0x1F09
        const val MEMBER_VARIABLE_REF_RESULT_DENIED = 0x1F0A
        const val FUNCTION_CALL_RESULT_DENIED = 0x1F0B
        const val MEMBER_FUNCTION_CALL_RESULT_DENIED = 0x1F0C
        const val FUNCTION_CALL_PARAM_RESULT_DENIED = 0x1F0D
        const val NUMBER_RESULT_DENIED = 0x1F0E
        const val WHOLE_NUMBER_RESULT_DENIED = 0x1F0F
        const val DECIMAL_NUMBER_RESULT_DENIED = 0x1F10
        const val EXPRESSION_RESULT_DENIED = 0x1F11
        const val EXPRESSION_OPERATION_RESULT_DENIED = 0x1F12
        const val ARRAY_RESULT_DENIED = 0x1F13
        const val ARRAY_CONTENTS_RESULT_DENIED = 0x1F14
    }

    override fun visitScope(ctx: KnolusParser.ScopeContext): KorneaResult<KnolusUnion.ScopeType> {
        if (restrictions.canVisitScope(ctx) !is KorneaResult.Success<*>)
            return KorneaResult.errorAsIllegalState(SCOPE_VISIT_DENIED, "Restriction denied scope visit")

        if (ctx.children?.isNotEmpty() != true) return KorneaResult.empty()

        return ctx.children.fold(KorneaResult.foldingMutableListOf<KnolusUnion>(null)) { acc, child ->
            acc.flatMap { list ->
                visit(child)?.map { union ->
                    list.add(union)
                    list
                } ?: KorneaResult.success(list, null)
            }
        }.filter(List<KnolusUnion>::isNotEmpty).map { list ->
            KnolusUnion.ScopeType(list.toTypedArray())
        }.flatMap { scope ->
            if (restrictions.shouldTakeScope(ctx, scope) is KorneaResult.Success<*>) KorneaResult.success(scope, null)
            else KorneaResult.errorAsIllegalState(SCOPE_RESULT_DENIED, "Restriction denied scope result")
        }
    }

    override fun visitDeclareVariable(ctx: KnolusParser.DeclareVariableContext): KorneaResult<KnolusUnion.DeclareVariableAction> {
        if (restrictions.canVisitVariableDeclaration(ctx) !is KorneaResult.Success<*>)
            return KorneaResult.errorAsIllegalState(VARIABLE_DECL_VISIT_DENIED, "Restriction denied variable declaration visit")

        return ctx.variableValue()?.let(this::visitVariableValue)
            .switchIfNull { KorneaResult.successVar(KnolusConstants.Undefined) }
            .map { variableValue ->
                KnolusUnion.DeclareVariableAction(
                    ctx.variableName.text,
                    variableValue.value,
                    ctx.GLOBAL() != null
                )
            }.flatMap { variableDeclaration ->
                if (restrictions.shouldTakeVariableDeclaration(ctx, variableDeclaration) is KorneaResult.Success<*>) KorneaResult.successInline(variableDeclaration)
                else KorneaResult.errorAsIllegalState(VARIABLE_DECL_RESULT_DENIED, "Restriction denied variable declaration result")
            }
    }

    override fun visitSetVariableValue(ctx: KnolusParser.SetVariableValueContext): KorneaResult<out KnolusUnion> {
        if (restrictions.canVisitVariableAssignment(ctx) !is KorneaResult.Success<*>)
            return KorneaResult.errorAsIllegalState(VARIABLE_ASSIGN_VISIT_DENIED, "Restriction denied variable assign visit")

        return visitVariableValue(ctx.variableValue())
            .map { variableValue ->
                KnolusUnion.AssignVariableAction(
                    ctx.variableName.text,
                    variableValue.value,
                    ctx.GLOBAL() != null
                )
            }.flatMap { variableAssign ->
                if (restrictions.shouldTakeVariableAssignment(ctx, variableAssign) is KorneaResult.Success<*>) KorneaResult.successInline(variableAssign)
                else KorneaResult.errorAsIllegalState(VARIABLE_ASSIGN_RESULT_DENIED, "Restriction denied variable assign result")
            }
    }

    override fun visitVariableValue(ctx: KnolusParser.VariableValueContext?): KorneaResult<KnolusUnion.VariableValue<KnolusTypedValue>> {
        if (ctx == null) return KorneaResult.empty()

        if (restrictions.canVisitVariableValue(ctx) !is KorneaResult.Success<*>)
            return KorneaResult.errorAsIllegalState(VARIABLE_VALUE_VISIT_DENIED, "Restriction denied variable value visit")

        val variableValue =
            (if (ctx.NULL() != null) KorneaResult.successVar(KnolusConstants.Null) else null)
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
            ?: KorneaResult.errorAsIllegalState(
                NO_VALID_VARIABLE_VALUE,
                "No valid variable value in \"${ctx.text}\" (${ctx.toString(parser)})"
            )

        return variableValue.flatMap { value ->
            if (restrictions.shouldTakeVariableValue(ctx, value) is KorneaResult.Success<*>) KorneaResult.successInline(value)
            else KorneaResult.errorAsIllegalState(VARIABLE_VALUE_RESULT_DENIED, "Restriction denied variable value result")
        }
    }

    override fun visitStringValue(ctx: KnolusParser.StringValueContext?): KorneaResult<KnolusUnion> {
        if (ctx == null) return KorneaResult.empty()

        if (restrictions.canVisitStringValue(ctx) !is KorneaResult.Success<*>)
            return KorneaResult.errorAsIllegalState(STRING_VALUE_VISIT_DENIED, "Restriction denied string value visit")

        val variableValue =
            ctx.quotedCharacter()?.let(this::visitQuotedCharacter)
            ?: ctx.quotedString()?.let(this::visitQuotedString)
            ?: ctx.plainString()?.let(this::visitPlainString)
            ?: ctx.variableReference()?.let(this::visitVariableReference)
            ?: ctx.memberVariableReference()?.let(this::visitMemberVariableReference)
            ?: ctx.functionCall()?.let(this::visitFunctionCall)
            ?: ctx.memberFunctionCall()?.let(this::visitMemberFunctionCall)
            ?: KorneaResult.errorAsIllegalState(
                NO_VALID_VARIABLE_VALUE,
                "No valid variable value in \"${ctx.text}\" (${ctx.toString(parser)})"
            )

        return variableValue.flatMap { value ->
            if (restrictions.shouldTakeStringValue(ctx, value) is KorneaResult.Success<*>) KorneaResult.successInline(value)
            else KorneaResult.errorAsIllegalState(STRING_VALUE_RESULT_DENIED, "Restriction denied string value result")
        }
    }

    override fun visitBool(ctx: KnolusParser.BoolContext): KorneaResult<KnolusUnion.VariableValue<KnolusBoolean>> {
        if (restrictions.canVisitBoolean(ctx) !is KorneaResult.Success<*>)
            return KorneaResult.errorAsIllegalState(BOOLEAN_VISIT_DENIED, "Restriction denied boolean visit")

        val value =
            if (ctx.TRUE() != null) KorneaResult.successVar(KnolusBoolean(true))
            else if (ctx.FALSE() != null) KorneaResult.successVar(KnolusBoolean(false))
            else KorneaResult.errorAsIllegalState(
                NO_VALID_VARIABLE_VALUE,
                "No valid variable value in \"${ctx.text}\" (${ctx.toString(parser)})"
            )

        return value.flatMap { bool ->
            if (restrictions.shouldTakeBoolean(ctx, bool) is KorneaResult.Success<*>) KorneaResult.successInline(bool)
            else KorneaResult.errorAsIllegalState(BOOLEAN_RESULT_DENIED, "Restriction denied boolean result")
        }
    }

    override fun visitQuotedString(ctx: KnolusParser.QuotedStringContext): KorneaResult<KnolusUnion.VariableValue<KnolusLazyString>> {
        if (restrictions.canVisitQuotedString(ctx) !is KorneaResult.Success<*>)
            return KorneaResult.errorAsIllegalState(QUOTED_STRING_VISIT_DENIED, "Restriction denied quoted string visit")

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

        return KorneaResult.successVar(lazyStr).flatMap { string ->
            if (restrictions.shouldTakeQuotedString(ctx, string) is KorneaResult.Success<*>) KorneaResult.successInline(string)
            else KorneaResult.errorAsIllegalState(QUOTED_STRING_RESULT_DENIED, "Restriction denied quoted string result")
        }
    }

    override fun visitPlainString(ctx: KnolusParser.PlainStringContext): KorneaResult<KnolusUnion.VariableValue<KnolusString>> {
        if (restrictions.canVisitPlainString(ctx) !is KorneaResult.Success<*>)
            return KorneaResult.errorAsIllegalState(PLAIN_STRING_VISIT_DENIED, "Restriction denied plain string visit")

        val builder = StringBuilder()

        ctx.children.forEach { node ->
            if (node !is TerminalNode) return@forEach

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

                KnolusParser.PLAIN_CHARACTERS -> builder.append(node.text)
            }
        }

        return KorneaResult.successVar(KnolusString(builder.toString())).flatMapOrSelf { string ->
            restrictions.shouldTakePlainString(ctx, string)
                .doOnFailure { KorneaResult.WithErrorDetails.asIllegalState(TransKnolusVisitor.PLAIN_STRING_RESULT_DENIED, "Restriction denied plain string result", it) }
                .takeUnless { it is KorneaResult.Success<*> }
                ?.cast()
        }
    }

    override fun visitQuotedCharacter(ctx: KnolusParser.QuotedCharacterContext): KorneaResult<KnolusUnion.VariableValue<KnolusChar>> {
        if (restrictions.canVisitQuotedCharacter(ctx) !is KorneaResult.Success<*>)
            return KorneaResult.errorAsIllegalState(QUOTED_CHARACTER_VISIT_DENIED, "Restriction denied quoted character visit")

        val charValue = ctx.CHARACTER_ESCAPES()?.let { node ->
            KorneaResult.successVar(
                KnolusChar(
                    when (val c = node.text[1]) {
                        'b' -> '\b'
                        'f' -> '\u000C'
                        'n' -> '\n'
                        'r' -> '\r'
                        't' -> '\t'
                        'u' -> node.text.substring(2).toInt(16).toChar()
                        else -> c
                    }
                )
            )
        } ?: ctx.QUOTED_CHARACTERS()?.let { node -> KorneaResult.successVar(KnolusChar(node.text[0])) }
                        ?: ctx.QUOTED_CHARACTER_LINE_BREAK()?.let { node -> KorneaResult.successVar(KnolusChar('\n')) }
                        ?: KorneaResult.errorAsIllegalState(NO_VALID_CHAR_VALUE, "No valid char value in \"${ctx.text}\" (${ctx.toString(parser)})")

        return charValue.flatMap { char ->
            if (restrictions.shouldTakeQuotedCharacter(ctx, char) is KorneaResult.Success<*>) KorneaResult.successInline(char)
            else KorneaResult.errorAsIllegalState(QUOTED_CHARACTER_RESULT_DENIED, "Restriction denied quoted character result")
        }
    }

    override fun visitVariableReference(ctx: KnolusParser.VariableReferenceContext): KorneaResult<KnolusUnion.VariableValue<KnolusVariableReference>> {
        if (restrictions.canVisitVariableReference(ctx) !is KorneaResult.Success<*>)
            return KorneaResult.errorAsIllegalState(VARIABLE_REF_VISIT_DENIED, "Restriction denied variable reference visit")

        return KorneaResult.successVar(KnolusVariableReference(ctx.variableName.text.removePrefix("$")))
            .flatMap { ref ->
                if (restrictions.shouldTakeVariableReference(ctx, ref) is KorneaResult.Success<*>) KorneaResult.successInline(ref)
                else KorneaResult.errorAsIllegalState(VARIABLE_REF_RESULT_DENIED, "Restriction denied variable reference result")
            }
    }

    override fun visitMemberVariableReference(ctx: KnolusParser.MemberVariableReferenceContext): KorneaResult<KnolusUnion.VariableValue<KnolusPropertyReference>> {
        if (restrictions.canVisitMemberVariableReference(ctx) !is KorneaResult.Success<*>)
            return KorneaResult.errorAsIllegalState(
                MEMBER_VARIABLE_REF_VISIT_DENIED,
                "Restriction denied member variable reference visit"
            )

        return KorneaResult.successVar(
            KnolusPropertyReference(
                ctx.memberName.text.removeSuffix("."),
                ctx.variableReference().variableName.text
            )
        ).flatMap { ref ->
            if (restrictions.shouldTakeMemberVariableReference(ctx, ref) is KorneaResult.Success<*>) KorneaResult.successInline(ref)
            else KorneaResult.errorAsIllegalState(
                MEMBER_VARIABLE_REF_RESULT_DENIED,
                "Restriction denied member variable reference result"
            )
        }
    }

    override fun visitFunctionCall(ctx: KnolusParser.FunctionCallContext): KorneaResult<KnolusUnion.VariableValue<KnolusLazyFunctionCall>> {
        if (restrictions.canVisitFunctionCall(ctx) !is KorneaResult.Success<*>)
            return KorneaResult.errorAsIllegalState(FUNCTION_CALL_VISIT_DENIED, "Restriction denied function call visit")

        val functionName = ctx.functionName.text.removeSuffix("(")
        return ctx.parameters
            .fold(KorneaResult.foldingMutableListOf<KnolusUnion.FunctionParameterType>(null)) { acc, param ->
                acc.flatMap { list -> visitFunctionCallParameter(param).map(list::withElement) }
            }.flatMap { params ->
                KorneaResult.successVar(KnolusLazyFunctionCall(functionName, params.toTypedArray()))
            }.flatMap { func ->
                if (restrictions.shouldTakeFunctionCall(ctx, func) is KorneaResult.Success<*>) KorneaResult.successInline(func)
                else KorneaResult.errorAsIllegalState(FUNCTION_CALL_RESULT_DENIED, "Restriction denied function call result")
            }
    }

    override fun visitMemberFunctionCall(ctx: KnolusParser.MemberFunctionCallContext): KorneaResult<KnolusUnion.VariableValue<KnolusLazyMemberFunctionCall>> {
        if (restrictions.canVisitMemberFunctionCall(ctx) !is KorneaResult.Success<*>)
            return KorneaResult.errorAsIllegalState(
                MEMBER_FUNCTION_CALL_VISIT_DENIED,
                "Restriction denied member function call visit"
            )

        return visitFunctionCall(ctx.functionCall())
            .flatMap { functionCallType ->
                val memberName = ctx.memberName.text.removeSuffix(".")
                KorneaResult.successVar(
                    KnolusLazyMemberFunctionCall(
                        memberName,
                        functionCallType.value.name,
                        functionCallType.value.parameters
                    )
                )
            }.flatMap { func ->
                if (restrictions.shouldTakeMemberFunctionCall(ctx, func) is KorneaResult.Success<*>) KorneaResult.success(func, null)
                else KorneaResult.errorAsIllegalState(
                    MEMBER_FUNCTION_CALL_RESULT_DENIED,
                    "Restriction denied member function call result"
                )
            }
    }

    override fun visitFunctionCallParameter(ctx: KnolusParser.FunctionCallParameterContext): KorneaResult<KnolusUnion.FunctionParameterType> {
        if (restrictions.canVisitFunctionCallParameter(ctx) !is KorneaResult.Success<*>)
            return KorneaResult.errorAsIllegalState(
                FUNCTION_CALL_PARAM_VISIT_DENIED,
                "Restriction denied function call parameter visit"
            )

        return visitVariableValue(ctx.variableValue())
            .flatMap { value ->
                KorneaResult.successInline(
                    KnolusUnion.FunctionParameterType(
                        ctx.parameterName?.text?.removeSuffix("="),
                        value.value
                    )
                )
            }.flatMap { param ->
                if (restrictions.shouldTakeFunctionCallParameter(ctx, param) is KorneaResult.Success<*>) KorneaResult.successInline(param)
                else KorneaResult.errorAsIllegalState(
                    FUNCTION_CALL_PARAM_RESULT_DENIED,
                    "Restriction denied function call parameter result"
                )
            }
    }

    override fun visitNumber(ctx: KnolusParser.NumberContext): KorneaResult<KnolusUnion.VariableValue<KnolusNumericalType>> {
        if (restrictions.canVisitNumber(ctx) !is KorneaResult.Success<*>)
            return KorneaResult.errorAsIllegalState(NUMBER_VISIT_DENIED, "Restriction denied number visit")

        val number = ctx.wholeNumber()?.let(this::visitWholeNumber)
                     ?: ctx.decimalNumber()?.let(this::visitDecimalNumber)
                     ?: KorneaResult.errorAsIllegalArgument(
                         NO_VALID_NUMBER_TYPE, "No valid number type in \"${ctx.text}\" (${ctx.toString(parser)})"
                     )

        return number.flatMap { num ->
            if (restrictions.shouldTakeNumber(ctx, num) is KorneaResult.Success<*>) KorneaResult.successInline(num)
            else KorneaResult.errorAsIllegalState(NUMBER_RESULT_DENIED, "Restriction denied number result")
        }
    }

    override fun visitWholeNumber(ctx: KnolusParser.WholeNumberContext): KorneaResult<KnolusUnion.VariableValue<KnolusInt>> {
        if (restrictions.canVisitWholeNumber(ctx) !is KorneaResult.Success<*>)
            return KorneaResult.errorAsIllegalState(WHOLE_NUMBER_VISIT_DENIED, "Restriction denied whole number visit")

        val int = ctx.INTEGER().text.toIntOrNullBaseN()
                  ?: return KorneaResult.errorAsIllegalArgument(NUMBER_FORMAT_ERROR, "${ctx.INTEGER().text} was not a valid int string")

        return KorneaResult.successVar(KnolusInt(int)).flatMap { num ->
            if (restrictions.shouldTakeWholeNumber(ctx, num) is KorneaResult.Success<*>) KorneaResult.successInline(num)
            else KorneaResult.errorAsIllegalArgument(WHOLE_NUMBER_RESULT_DENIED, "Restriction denied whole number result")
        }
    }

    override fun visitDecimalNumber(ctx: KnolusParser.DecimalNumberContext): KorneaResult<KnolusUnion.VariableValue<KnolusDouble>> {
        if (restrictions.canVisitDecimalNumber(ctx) !is KorneaResult.Success<*>)
            return KorneaResult.errorAsIllegalState(DECIMAL_NUMBER_VISIT_DENIED, "Restriction denied decimal number visit")

        val double = ctx.DECIMAL_NUMBER().text.toDoubleOrNull()
                     ?: return KorneaResult.errorAsIllegalArgument(
                         NUMBER_FORMAT_ERROR,
                         "${ctx.DECIMAL_NUMBER().text} was not a valid double string"
                     )

        return KorneaResult.successVar(KnolusDouble(double)).flatMap { num ->
            if (restrictions.shouldTakeDecimalNumber(ctx, num) is KorneaResult.Success<*>) KorneaResult.successInline(num)
            else KorneaResult.errorAsIllegalState(DECIMAL_NUMBER_RESULT_DENIED, "Restriction denied decimal number result")
        }
    }

    override fun visitExpression(ctx: KnolusParser.ExpressionContext): KorneaResult<KnolusUnion.VariableValue<KnolusLazyExpression>> {
        if (restrictions.canVisitExpression(ctx) !is KorneaResult.Success<*>)
            return KorneaResult.errorAsIllegalState(EXPRESSION_VISIT_DENIED, "Restriction denied expression visit")

        val starting = visitVariableValue(ctx.startingValue).getOrBreak { return it.asType() }
        val ops: MutableList<Pair<ExpressionOperator, KnolusTypedValue>> = ArrayList()

        for (i in ctx.exprOps.indices) {
            val expr = visitExpressionOperation(ctx.exprOps[i]).getOrBreak { return it.cast() }
            val value = visitVariableValue(ctx.exprVals[i]).getOrBreak { return it.cast() }

            ops.add(Pair(expr, value.value))
        }

        return KorneaResult.successVar(KnolusLazyExpression(starting.value, ops.toTypedArray())).flatMap { expr ->
            if (restrictions.shouldTakeExpression(ctx, expr) is KorneaResult.Success<*>) KorneaResult.successInline(expr)
            else KorneaResult.errorAsIllegalState(EXPRESSION_RESULT_DENIED, "Restriction denied expression result")
        }
    }

    override fun visitExpressionOperation(ctx: KnolusParser.ExpressionOperationContext): KorneaResult<ExpressionOperator> {
        if (restrictions.canVisitExpressionOperation(ctx) !is KorneaResult.Success<*>)
            return KorneaResult.errorAsIllegalState(
                EXPRESSION_OPERATION_VISIT_DENIED,
                "Restriction denied expression operation visit"
            )

        val value =
            if (ctx.EXPR_EXPONENTIAL() != null) KorneaResult.successInline(ExpressionOperator.EXPONENTIAL)
            else if (ctx.EXPR_PLUS() != null) KorneaResult.successInline(ExpressionOperator.PLUS)
            else if (ctx.EXPR_MINUS() != null) KorneaResult.successInline(ExpressionOperator.MINUS)
            else if (ctx.EXPR_DIVIDE() != null) KorneaResult.successInline(ExpressionOperator.DIVIDE)
            else if (ctx.EXPR_MULTIPLY() != null) KorneaResult.successInline(ExpressionOperator.MULTIPLY)
            else KorneaResult.errorAsIllegalArgument(
                NO_VALID_EXPRESSION_OPERATION,
                "No valid expression operation in \"${ctx.text}\" (${ctx.toString(parser)})"
            )

        return value.flatMap { op ->
            if (restrictions.shouldTakeExpressionOperation(ctx, op) is KorneaResult.Success<*>) KorneaResult.successInline(op)
            else KorneaResult.errorAsIllegalState(
                EXPRESSION_OPERATION_RESULT_DENIED,
                "Restriction denied expression operation result"
            )
        }
    }

    override fun visitArray(ctx: KnolusParser.ArrayContext): KorneaResult<KnolusUnion.VariableValue<KnolusArray<out KnolusTypedValue>>> {
        if (restrictions.canVisitArray(ctx) !is KorneaResult.Success<*>)
            return KorneaResult.errorAsIllegalState(ARRAY_VISIT_DENIED, "Restriction denied array visit")

        return visitArrayContents(ctx.arrayContents())
            .flatMap { array -> KorneaResult.successVar(KnolusArray.of(array.inner)) }
            .flatMap { array ->
                if (restrictions.shouldTakeArray(ctx, array) is KorneaResult.Success<*>) KorneaResult.successInline(array)
                else KorneaResult.errorAsIllegalState(ARRAY_RESULT_DENIED, "Restriction denied array result")
            }
    }

    override fun visitArrayContents(ctx: KnolusParser.ArrayContentsContext): KorneaResult<KnolusUnion.ArrayContents> {
        if (restrictions.canVisitArrayContents(ctx) !is KorneaResult.Success<*>)
            return KorneaResult.errorAsIllegalState(ARRAY_CONTENTS_VISIT_DENIED, "Restriction denied array contents visit")

        val initial: KorneaResult<MutableList<KnolusTypedValue>> = KorneaResult.success(ArrayList(), null)

        return ctx.children.fold(KorneaResult.foldingMutableListOf<KnolusTypedValue>(null)) { acc, child ->
            acc.flatMap { list ->
                visit(child)?.filterToInstance<KnolusUnion.VariableValue<KnolusTypedValue>>()?.map { union ->
                    list.add(union.value)
                    list
                } ?: KorneaResult.successInline(list)
            }
        }.map { list ->
            KnolusUnion.ArrayContents(list.toTypedArray())
        }.flatMap { array ->
            if (restrictions.shouldTakeArrayContents(ctx, array) is KorneaResult.Success<*>) KorneaResult.successInline(array)
            else KorneaResult.errorAsIllegalState(ARRAY_CONTENTS_RESULT_DENIED, "Restriction denied array contents result")
        }
    }
}

@Suppress("UnnecessaryVariable")
@ExperimentalUnsignedTypes
fun parseKnolusScope(text: String, restrictions: KnolusVisitorRestrictions<*>): KorneaResult<KnolusUnion.ScopeType> {
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
        return KorneaResult.thrown(pce)
    }
}

sealed class ScopeResult {
    data class Returned<T : KnolusTypedValue>(val value: T) : ScopeResult()
    data class LineMap(val lines: Array<Pair<KnolusUnion, *>>) : ScopeResult() {
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
suspend fun <R> KnolusUnion.ScopeType.run(
    parentContext: KnolusContext,
    parameters: Map<String, Any?> = emptyMap(),
    init: KnolusContext.() -> Unit = {},
) = run(parentContext.restrictions, parentContext, parameters, init)

suspend fun <R> KnolusUnion.ScopeType.run(
    restrictions: KnolusRestriction<R>,
    parentContext: KnolusContext? = null,
    parameters: Map<String, Any?> = emptyMap(),
    init: KnolusContext.() -> Unit = {},
) = runDirect(KnolusScopeContext(this, parentContext, restrictions), parameters, init)

@ExperimentalUnsignedTypes
suspend fun KnolusUnion.ScopeType.runDirect(
    knolusContext: KnolusContext,
    parameters: Map<String, Any?> = emptyMap(),
    init: KnolusContext.() -> Unit = {},
): KorneaResult<ScopeResult> {
    parameters.forEach { (k, v) -> knolusContext[k] = v as? KnolusTypedValue ?: return@forEach }

    knolusContext.init()

    return KorneaResult.success(ScopeResult.LineMap(lines.mapWith { union ->
        when (union) {
            is KnolusUnion.Action<*> -> union.run(knolusContext).doOnFailure { return it.cast() }
            is KnolusUnion.VariableValue<*> -> union.value.let { value ->
                if (value is KnolusUnion.Action<*>) value.run(knolusContext).doOnFailure { return it.cast() }
                else value
            }
            is KnolusUnion.ReturnStatement -> union.value.let { value ->
                if (value is KnolusTypedValue.UnsureValue<KnolusTypedValue> && value.needsEvaluation(knolusContext))
                    return value.evaluate(knolusContext).map(ScopeResult::Returned)
                return KorneaResult.success(ScopeResult.Returned(value))
            }
            else -> null
        }
    }))
}