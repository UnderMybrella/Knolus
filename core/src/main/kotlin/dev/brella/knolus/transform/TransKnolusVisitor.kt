package dev.brella.knolus.transform

import dev.brella.knolus.*
import dev.brella.knolus.types.*
import dev.brella.kornea.annotations.AvailableSince
import dev.brella.kornea.errors.common.*
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.misc.ParseCancellationException
import org.antlr.v4.runtime.tree.ParseTreeVisitor
import dev.brella.kornea.toolkit.common.switchIfNull

@AvailableSince(Knolus.VERSION_1_2_0)
@ExperimentalUnsignedTypes
/**
 * A version of [KnolusVisitor] that's capable of handling arbitrary parser input
 */
open class TransKnolusVisitor(val restrictions: KnolusTransVisitorRestrictions<*>, val parser: Recognizer<*, *>, val blueprint: ParserBlueprint<ParserRuleContext>) : TransKnolusParserVisitor {
    companion object {
        const val NO_VALID_VARIABLE_VALUE = 0x1200
        const val NO_VALID_NUMBER_TYPE = 0x1201
        const val NO_VALID_EXPRESSION_OPERATION = 0x1202
        const val NO_VALID_CHAR_VALUE = 0x1203
        const val NO_VALID_LINE_STATEMENT = 0x1204

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

    constructor(restrictions: KnolusTransVisitorRestrictions<*>, parser: Parser) : this(restrictions, parser, ReflectiveParserBlueprint(parser))

    override fun visitScope(ctx: ScopeBlueprint): KorneaResult<KnolusUnion.ScopeType> {
        if (restrictions.canVisitScope(ctx) !is KorneaResult.Success<*>)
            return KorneaResult.errorAsIllegalState(SCOPE_VISIT_DENIED, "Restriction denied scope visit")

        val lines = ctx.getLines(blueprint)

        if (lines.isEmpty()) return KorneaResult.empty()

        return lines.foldResults(this::visitLine)
            .filter(List<KnolusUnion>::isNotEmpty)
            .map { list -> KnolusUnion.ScopeType(list.toTypedArray()) }
            .flatMap { scope ->
                if (restrictions.shouldTakeScope(ctx, scope) is KorneaResult.Success<*>) KorneaResult.success(scope, null)
                else KorneaResult.errorAsIllegalState(SCOPE_RESULT_DENIED, "Restriction denied scope result")
            }
    }

    override fun visitLine(ctx: LineBlueprint): KorneaResult<KnolusUnion> {
//        if (restrictions.canVisitVariableDeclaration(ctx) !is KorneaResult.Success<*>)
//            return KorneaResult.errorAsIllegalState(VARIABLE_DECL_VISIT_DENIED, "Restriction denied variable declaration visit")

        val lineStatement =
            ctx.getFunctionCall(blueprint)?.let(this::visitFunctionCall)
            ?: ctx.getFunctionDeclaration(blueprint)?.let(this::visitDeclareFunction)
            ?: ctx.getMemberFunctionCall(blueprint)?.let(this::visitMemberFunctionCall)
            ?: ctx.getVariableAssignment(blueprint)?.let(this::visitSetVariableValue)
            ?: ctx.getVariableDeclaration(blueprint)?.let(this::visitDeclareVariable)
            ?: KorneaResult.errorAsIllegalState(
                NO_VALID_LINE_STATEMENT,
                "No valid variable value in \"${ctx.matchingText()}\" (${ctx.toString(parser)})"
            )

        return lineStatement
//            .flatMapOrSelf { value ->
//            restrictions.shouldTakeVariableValue(ctx, value)
//                .doOnFailure { KorneaResult.WithErrorDetails.asIllegalState(VARIABLE_VALUE_RESULT_DENIED, "Restriction denied variable value result", it) }
//                .takeUnless { it is KorneaResult.Success<*> }
//                ?.cast()
//        }
    }

    override fun visitDeclareFunction(ctx: DeclareFunctionBlueprint): KorneaResult<KnolusUnion.FunctionDeclaration> = KorneaResult.thrown(NotImplementedError("Function declarations are not yet implemented"))
    override fun visitDeclareFunctionBody(ctx: DeclareFunctionBodyBlueprint): KorneaResult<KnolusUnion.ScopeType> = KorneaResult.thrown(NotImplementedError("Function declarations are not yet implemented"))

    override fun visitDeclareVariable(ctx: DeclareVariableBlueprint): KorneaResult<KnolusUnion.DeclareVariableAction> {
        if (restrictions.canVisitVariableDeclaration(ctx) !is KorneaResult.Success<*>)
            return KorneaResult.errorAsIllegalState(VARIABLE_DECL_VISIT_DENIED, "Restriction denied variable declaration visit")

        return ctx.getVariableValue(blueprint)?.let(this::visitVariableValue)
            .switchIfNull { KorneaResult.successVar(KnolusConstants.Undefined) }
            .map { variableValue ->
                KnolusUnion.DeclareVariableAction(
                    ctx.getVariableNameToken(blueprint).text,
                    variableValue.value,
                    ctx.isGlobal(blueprint)
                )
            }.flatMapOrSelf { variableDeclaration ->
                restrictions.shouldTakeVariableDeclaration(ctx, variableDeclaration)
                    .doOnFailure { KorneaResult.WithErrorDetails.asIllegalState(VARIABLE_DECL_RESULT_DENIED, "Restriction denied variable declaration result", it) }
                    .takeUnless { it is KorneaResult.Success<*> }
                    ?.cast()
            }
    }

    override fun visitSetVariableValue(ctx: AssignVariableBlueprint): KorneaResult<KnolusUnion.AssignVariableAction> {
        if (restrictions.canVisitVariableAssignment(ctx) !is KorneaResult.Success<*>)
            return KorneaResult.errorAsIllegalState(VARIABLE_ASSIGN_VISIT_DENIED, "Restriction denied variable assign visit")

        return visitVariableValue(ctx.getVariableValue(blueprint))
            .map { variableValue ->
                KnolusUnion.AssignVariableAction(
                    ctx.getVariableNameToken(blueprint).text,
                    variableValue.value,
                    ctx.isGlobal(blueprint)
                )
            }.flatMapOrSelf { variableAssign ->
                restrictions.shouldTakeVariableAssignment(ctx, variableAssign)
                    .doOnFailure { KorneaResult.WithErrorDetails.asIllegalState(VARIABLE_ASSIGN_RESULT_DENIED, "Restriction denied variable assign result", it) }
                    .takeUnless { it is KorneaResult.Success<*> }
                    ?.cast()
            }
    }

    override fun visitVariableValue(ctx: VariableValueBlueprint): KorneaResult<KnolusUnion.VariableValue<KnolusTypedValue>> {
//        if (ctx == null) return KorneaResult.empty()

        if (restrictions.canVisitVariableValue(ctx) !is KorneaResult.Success<*>)
            return KorneaResult.errorAsIllegalState(VARIABLE_VALUE_VISIT_DENIED, "Restriction denied variable value visit")

        val variableValue =
            (if (ctx.getNullToken(blueprint) != null) KorneaResult.successVar(KnolusConstants.Null) else null)
            ?: ctx.getQuotedCharacter(blueprint)?.let(this::visitQuotedCharacter)
            ?: ctx.getQuotedString(blueprint)?.let(this::visitQuotedString)
            ?: ctx.getPlainString(blueprint)?.let(this::visitPlainString)
            ?: ctx.getNumber(blueprint)?.let(this::visitNumber)
            ?: ctx.getVariableReference(blueprint)?.let(this::visitVariableReference)
            ?: ctx.getMemberVariableReference(blueprint)?.let(this::visitMemberVariableReference)
            ?: ctx.getFunctionCall(blueprint)?.let(this::visitFunctionCall)
            ?: ctx.getMemberFunctionCall(blueprint)?.let(this::visitMemberFunctionCall)
            ?: ctx.getExpression(blueprint)?.let(this::visitExpression)
            ?: ctx.getBoolean(blueprint)?.let(this::visitBool)
            ?: ctx.getArray(blueprint)?.let(this::visitArray)
            ?: KorneaResult.errorAsIllegalState(
                NO_VALID_VARIABLE_VALUE,
                "No valid variable value in \"${ctx.matchingText()}\" (${ctx.toString(parser)})"
            )

        return variableValue.flatMapOrSelf { value ->
            restrictions.shouldTakeVariableValue(ctx, value)
                .doOnFailure { KorneaResult.WithErrorDetails.asIllegalState(VARIABLE_VALUE_RESULT_DENIED, "Restriction denied variable value result", it) }
                .takeUnless { it is KorneaResult.Success<*> }
                ?.cast()
        }
    }

    override fun visitStringValue(ctx: StringValueBlueprint): KorneaResult<KnolusUnion.VariableValue<KnolusTypedValue>> {
        if (restrictions.canVisitStringValue(ctx) !is KorneaResult.Success<*>)
            return KorneaResult.errorAsIllegalState(STRING_VALUE_VISIT_DENIED, "Restriction denied string value visit")

        val variableValue =
            ctx.getQuotedCharacter(blueprint)?.let(this::visitQuotedCharacter)
            ?: ctx.getQuotedString(blueprint)?.let(this::visitQuotedString)
            ?: ctx.getPlainString(blueprint)?.let(this::visitPlainString)
            ?: ctx.getVariableReference(blueprint)?.let(this::visitVariableReference)
            ?: ctx.getMemberVariableReference(blueprint)?.let(this::visitMemberVariableReference)
            ?: ctx.getFunctionCall(blueprint)?.let(this::visitFunctionCall)
            ?: ctx.getMemberFunctionCall(blueprint)?.let(this::visitMemberFunctionCall)
            ?: KorneaResult.errorAsIllegalState(
                NO_VALID_VARIABLE_VALUE,
                "No valid variable value in \"${ctx.matchingText()}\" (${ctx.toString(parser)})"
            )

        return variableValue.flatMapOrSelf { value ->
            restrictions.shouldTakeStringValue(ctx, value)
                .doOnFailure { KorneaResult.WithErrorDetails.asIllegalState(STRING_VALUE_RESULT_DENIED, "Restriction denied string value result", it) }
                .takeUnless { it is KorneaResult.Success<*> }
                ?.cast()
        }
    }

    override fun visitBool(ctx: BooleanBlueprint): KorneaResult<KnolusUnion.VariableValue<KnolusBoolean>> {
        if (restrictions.canVisitBoolean(ctx) !is KorneaResult.Success<*>)
            return KorneaResult.errorAsIllegalState(BOOLEAN_VISIT_DENIED, "Restriction denied boolean visit")

        val value = KorneaResult.successVar(KnolusBoolean(ctx.parseBoolean(blueprint)))

        return value.flatMapOrSelf { bool ->
            restrictions.shouldTakeBoolean(ctx, bool)
                .doOnFailure { KorneaResult.WithErrorDetails.asIllegalState(BOOLEAN_RESULT_DENIED, "Restriction denied boolean result", it) }
                .takeUnless { it is KorneaResult.Success<*> }
                ?.cast()
        }
    }

    override fun visitQuotedString(ctx: QuotedStringBlueprint): KorneaResult<KnolusUnion.VariableValue<KnolusLazyString>> {
        if (restrictions.canVisitQuotedString(ctx) !is KorneaResult.Success<*>)
            return KorneaResult.errorAsIllegalState(QUOTED_STRING_VISIT_DENIED, "Restriction denied quoted string visit")

        val components: MutableList<KnolusUnion.StringComponent> = ArrayList()
        val builder = StringBuilder()

        ctx.getComponents(blueprint).forEach { node ->
            when (node) {
                is KnolusTokenBlueprint -> {
                    when (node.getTokenType(blueprint)) {
                        KnolusTokenBlueprint.TokenType.ESCAPES -> {
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

                        KnolusTokenBlueprint.TokenType.STRING_CHARACTERS -> builder.append(node.text)
                        KnolusTokenBlueprint.TokenType.STRING_WHITESPACE -> builder.append(node.text)
                        KnolusTokenBlueprint.TokenType.QUOTED_STRING_LINE_BREAK -> builder.append('\n')
                    }
                }
                is QuotedStringVariableReferenceBlueprint -> {
                    if (builder.isNotEmpty()) {
                        components.add(KnolusUnion.StringComponent.RawText(builder.toString()))
                        builder.clear()
                    }

                    components.add(KnolusUnion.StringComponent.VariableReference(node.getVariableNameToken(blueprint).text))
                }
                else -> println("$node is unaccounted for!")
            }
        }

        if (builder.isNotEmpty()) components.add(KnolusUnion.StringComponent.RawText(builder.toString()))
        val lazyComponents = components.toTypedArray()
        val lazyStr = KnolusLazyString(lazyComponents)

        return KorneaResult.successVar(lazyStr).flatMapOrSelf { string ->
            restrictions.shouldTakeQuotedString(ctx, string)
                .doOnFailure { KorneaResult.WithErrorDetails.asIllegalState(QUOTED_STRING_RESULT_DENIED, "Restriction denied quoted string result", it) }
                .takeUnless { it is KorneaResult.Success<*> }
                ?.cast()
        }
    }

    override fun visitPlainString(ctx: PlainStringBlueprint): KorneaResult<KnolusUnion.VariableValue<KnolusString>> {
        if (restrictions.canVisitPlainString(ctx) !is KorneaResult.Success<*>)
            return KorneaResult.errorAsIllegalState(PLAIN_STRING_VISIT_DENIED, "Restriction denied plain string visit")

        val builder = StringBuilder()

        ctx.getTokens(blueprint).forEach { node ->
            when (node.getTokenType(blueprint)) {
                KnolusTokenBlueprint.TokenType.ESCAPES -> {
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

                KnolusTokenBlueprint.TokenType.PLAIN_CHARACTERS -> builder.append(node.text)
            }
        }

        return KorneaResult.successVar(KnolusString(builder.toString())).flatMapOrSelf { string ->
            restrictions.shouldTakePlainString(ctx, string)
                .doOnFailure { KorneaResult.WithErrorDetails.asIllegalState(PLAIN_STRING_RESULT_DENIED, "Restriction denied plain string result", it) }
                .takeUnless { it is KorneaResult.Success<*> }
                ?.cast()
        }
    }

    override fun visitQuotedCharacter(ctx: QuotedCharacterBlueprint): KorneaResult<KnolusUnion.VariableValue<KnolusChar>> {
        if (restrictions.canVisitQuotedCharacter(ctx) !is KorneaResult.Success<*>)
            return KorneaResult.errorAsIllegalState(QUOTED_CHARACTER_VISIT_DENIED, "Restriction denied quoted character visit")

        val node = ctx.getCharacterToken(blueprint)
        val charValue = when (node.getTokenType(blueprint)) {
            KnolusTokenBlueprint.TokenType.CHARACTER_ESCAPES -> {
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
            }
            KnolusTokenBlueprint.TokenType.QUOTED_CHARACTERS -> KorneaResult.successVar(KnolusChar(node.text[0]))
            KnolusTokenBlueprint.TokenType.QUOTED_CHARACTER_LINE_BREAK -> KorneaResult.successVar(KnolusChar('\n'))
            else -> KorneaResult.errorAsIllegalState(NO_VALID_CHAR_VALUE, "No valid char value in \"${ctx.matchingText()}\" (${ctx.toString(parser)})")
        }

        return charValue.flatMapOrSelf { char ->
            restrictions.shouldTakeQuotedCharacter(ctx, char)
                .doOnFailure { KorneaResult.WithErrorDetails.asIllegalState(QUOTED_CHARACTER_RESULT_DENIED, "Restriction denied quoted character result", it) }
                .takeUnless { it is KorneaResult.Success<*> }
                ?.cast()
        }
    }

    override fun visitVariableReference(ctx: VariableReferenceBlueprint): KorneaResult<KnolusUnion.VariableValue<KnolusVariableReference>> {
        if (restrictions.canVisitVariableReference(ctx) !is KorneaResult.Success<*>)
            return KorneaResult.errorAsIllegalState(VARIABLE_REF_VISIT_DENIED, "Restriction denied variable reference visit")

        return KorneaResult.successVar(KnolusVariableReference(ctx.getVariableNameToken(blueprint).text.removePrefix("$")))
            .flatMapOrSelf { ref ->
                restrictions.shouldTakeVariableReference(ctx, ref)
                    .doOnFailure { KorneaResult.WithErrorDetails.asIllegalState(VARIABLE_REF_RESULT_DENIED, "Restriction denied variable reference result", it) }
                    .takeUnless { it is KorneaResult.Success<*> }
                    ?.cast()
            }
    }

    override fun visitMemberVariableReference(ctx: MemberVariableReferenceBlueprint): KorneaResult<KnolusUnion.VariableValue<KnolusPropertyReference>> {
        if (restrictions.canVisitMemberVariableReference(ctx) !is KorneaResult.Success<*>)
            return KorneaResult.errorAsIllegalState(
                MEMBER_VARIABLE_REF_VISIT_DENIED,
                "Restriction denied member variable reference visit"
            )

        return KorneaResult.successVar(
            KnolusPropertyReference(
                ctx.getMemberNameToken(blueprint).text.removeSuffix("."),
                ctx.getVariableNameToken(blueprint).text
            )
        ).flatMap { ref ->
            if (restrictions.shouldTakeMemberVariableReference(ctx, ref) is KorneaResult.Success<*>) KorneaResult.success(ref, null)
            else KorneaResult.errorAsIllegalState(
                MEMBER_VARIABLE_REF_RESULT_DENIED,
                "Restriction denied member variable reference result"
            )
        }
    }

    override fun visitFunctionCall(ctx: FunctionCallBlueprint): KorneaResult<KnolusUnion.VariableValue<KnolusLazyFunctionCall>> {
        if (restrictions.canVisitFunctionCall(ctx) !is KorneaResult.Success<*>)
            return KorneaResult.errorAsIllegalState(FUNCTION_CALL_VISIT_DENIED, "Restriction denied function call visit")

        val functionName = ctx.getFunctionNameToken(blueprint).text.removeSuffix("(")
        return ctx.getFunctionParameters(blueprint)
            .foldResults(this::visitFunctionCallParameter)
            .flatMap { params -> KorneaResult.successVar(KnolusLazyFunctionCall(functionName, params.toTypedArray())) }
            .flatMapOrSelf { func ->
                restrictions.shouldTakeFunctionCall(ctx, func)
                    .doOnFailure { KorneaResult.WithErrorDetails.asIllegalState(FUNCTION_CALL_RESULT_DENIED, "Restriction denied function call result", it) }
                    .takeUnless { it is KorneaResult.Success<*> }
                    ?.cast()
            }
    }

    override fun visitMemberFunctionCall(ctx: MemberFunctionCallBlueprint): KorneaResult<KnolusUnion.VariableValue<KnolusLazyMemberFunctionCall>> {
        if (restrictions.canVisitMemberFunctionCall(ctx) !is KorneaResult.Success<*>)
            return KorneaResult.errorAsIllegalState(
                MEMBER_FUNCTION_CALL_VISIT_DENIED,
                "Restriction denied member function call visit"
            )

        return visitFunctionCall(ctx.getFunctionCall(blueprint))
            .flatMap { functionCallType ->
                val memberName = ctx.getMemberNameToken(blueprint).text.removeSuffix(".")
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

    override fun visitFunctionCallParameter(ctx: FunctionCallParameterBlueprint): KorneaResult<KnolusUnion.FunctionParameterType> {
        if (restrictions.canVisitFunctionCallParameter(ctx) !is KorneaResult.Success<*>)
            return KorneaResult.errorAsIllegalState(
                FUNCTION_CALL_PARAM_VISIT_DENIED,
                "Restriction denied function call parameter visit"
            )

        return visitVariableValue(ctx.getParameterValue(blueprint))
            .flatMap { value ->
                KorneaResult.success(
                    KnolusUnion.FunctionParameterType(
                        ctx.getParameterNameToken(blueprint)?.text?.removeSuffix("="),
                        value.value
                    ), null
                )
            }.flatMap { param ->
                if (restrictions.shouldTakeFunctionCallParameter(ctx, param) is KorneaResult.Success<*>) KorneaResult.success(param, null)
                else KorneaResult.errorAsIllegalState(
                    FUNCTION_CALL_PARAM_RESULT_DENIED,
                    "Restriction denied function call parameter result"
                )
            }
    }

    override fun visitNumber(ctx: NumberBlueprint): KorneaResult<KnolusUnion.VariableValue<KnolusNumericalType>> {
        if (restrictions.canVisitNumber(ctx) !is KorneaResult.Success<*>)
            return KorneaResult.errorAsIllegalState(NUMBER_VISIT_DENIED, "Restriction denied number visit")

        val number =
            ctx.getWholeNumber(blueprint)?.let(this::visitWholeNumber)
            ?: ctx.getDecimalNumber(blueprint)?.let(this::visitDecimalNumber)
            ?: KorneaResult.errorAsIllegalArgument(NO_VALID_NUMBER_TYPE, "No valid number type in \"${ctx.matchingText()}\" (${ctx.toString(parser)})")

        return number.flatMapOrSelf { num ->
            restrictions.shouldTakeNumber(ctx, num)
                .doOnFailure { KorneaResult.WithErrorDetails.asIllegalState(NUMBER_RESULT_DENIED, "Restriction denied number result", it) }
                .takeUnless { it is KorneaResult.Success<*> }
                ?.cast()
        }
    }

    override fun visitWholeNumber(ctx: WholeNumberBlueprint): KorneaResult<KnolusUnion.VariableValue<KnolusNumericalType>> {
        if (restrictions.canVisitWholeNumber(ctx) !is KorneaResult.Success<*>)
            return KorneaResult.errorAsIllegalState(WHOLE_NUMBER_VISIT_DENIED, "Restriction denied whole number visit")

        val token = ctx.getInteger(blueprint)
        val text = token.text.trim()
        val long = text.toLongOrNullBaseN()
        if (long == null) {
            val (numStr, base) = text.stripBase()
            if (numStr.isValidInBase(base)) {
                val bigInt = numStr.toBigIntOrNull(base) ?:
                             return KorneaResult.errorAsIllegalArgument(KnolusVisitor.NUMBER_FORMAT_ERROR, "$text was not a valid numerical string")

                return KorneaResult.successVar(KnolusBigInt(bigInt)).flatMap { num ->
                    if (restrictions.shouldTakeWholeNumber(ctx, num) is KorneaResult.Success<*>) KorneaResult.success(num, null)
                    else KorneaResult.errorAsIllegalArgument(KnolusVisitor.WHOLE_NUMBER_RESULT_DENIED, "Restriction denied whole number result")
                }
            } else {
                return KorneaResult.errorAsIllegalArgument(KnolusVisitor.NUMBER_FORMAT_ERROR, "$text was not a valid numerical string")
            }
        }

        return KorneaResult.successVar(if (long < Int.MAX_VALUE && long > Int.MIN_VALUE) KnolusInt(long.toInt()) else KnolusLong(long)).flatMap { num ->
            if (restrictions.shouldTakeWholeNumber(ctx, num) is KorneaResult.Success<*>) KorneaResult.success(num, null)
            else KorneaResult.errorAsIllegalArgument(KnolusVisitor.WHOLE_NUMBER_RESULT_DENIED, "Restriction denied whole number result")
        }
    }

    override fun visitDecimalNumber(ctx: DecimalNumberBlueprint): KorneaResult<KnolusUnion.VariableValue<KnolusDouble>> {
        if (restrictions.canVisitDecimalNumber(ctx) !is KorneaResult.Success<*>)
            return KorneaResult.errorAsIllegalState(DECIMAL_NUMBER_VISIT_DENIED, "Restriction denied decimal number visit")

        val token = ctx.getDecimalNumber(blueprint)
        val double = token.text.trim().toDoubleOrNull() ?: return KorneaResult.errorAsIllegalArgument(NUMBER_FORMAT_ERROR, "${token.text} was not a valid double string")

        return KorneaResult.successVar(KnolusDouble(double)).flatMapOrSelf { num ->
            restrictions.shouldTakeDecimalNumber(ctx, num)
                .doOnFailure { KorneaResult.WithErrorDetails.asIllegalState(DECIMAL_NUMBER_RESULT_DENIED, "Restriction denied decimal number result", it) }
                .takeUnless { it is KorneaResult.Success<*> }
                ?.cast()
        }
    }

    override fun visitExpression(ctx: ExpressionBlueprint): KorneaResult<KnolusUnion.VariableValue<KnolusLazyExpression>> {
        if (restrictions.canVisitExpression(ctx) !is KorneaResult.Success<*>)
            return KorneaResult.errorAsIllegalState(EXPRESSION_VISIT_DENIED, "Restriction denied expression visit")

        val starting = ctx.getStartingValue(blueprint)?.let(this::visitVariableValue)?.getOrBreak { return it.asType() }
                       ?: ctx.getStaticStartingValue(blueprint)

        return ctx.getZippedExpression(blueprint).foldResults { (op, value) -> visitVariableValue(value).map { v -> Pair(op, v.value) } }
            .flatMap { ops -> KorneaResult.successVar(KnolusLazyExpression(starting.value, ops.toTypedArray())) }
            .flatMapOrSelf { expr ->
                restrictions.shouldTakeExpression(ctx, expr)
                    .doOnFailure { KorneaResult.WithErrorDetails.asIllegalState(EXPRESSION_RESULT_DENIED, "Restriction denied expression result", it) }
                    .takeUnless { it is KorneaResult.Success<*> }
                    ?.cast()
            }
    }

    override fun visitArray(ctx: ArrayBlueprint): KorneaResult<KnolusUnion.VariableValue<KnolusArray<out KnolusTypedValue>>> {
        if (restrictions.canVisitArray(ctx) !is KorneaResult.Success<*>)
            return KorneaResult.errorAsIllegalState(ARRAY_VISIT_DENIED, "Restriction denied array visit")

        return visitArrayContents(ctx.getArrayContents(blueprint))
            .flatMap { array -> KorneaResult.successVar(KnolusArray.of(array.inner)) }
            .flatMapOrSelf { array ->
                restrictions.shouldTakeArray(ctx, array)
                    .doOnFailure { KorneaResult.WithErrorDetails.asIllegalState(ARRAY_RESULT_DENIED, "Restriction denied array result", it) }
                    .takeUnless { it is KorneaResult.Success<*> }
                    ?.cast()
            }
    }

    override fun visitArrayContents(ctx: ArrayContentsBlueprint): KorneaResult<KnolusUnion.ArrayContents> {
        if (restrictions.canVisitArrayContents(ctx) !is KorneaResult.Success<*>)
            return KorneaResult.errorAsIllegalState(ARRAY_CONTENTS_VISIT_DENIED, "Restriction denied array contents visit")

        return ctx.getArrayElements(blueprint).foldResults { element ->
            visit(element).filterToInstance<KnolusUnion.VariableValue<KnolusTypedValue>>().map(KnolusUnion.VariableValue<KnolusTypedValue>::value)
        }.map { list ->
            KnolusUnion.ArrayContents(list.toTypedArray())
        }.flatMapOrSelf { array ->
            restrictions.shouldTakeArrayContents(ctx, array)
                .doOnFailure { KorneaResult.WithErrorDetails.asIllegalState(ARRAY_CONTENTS_RESULT_DENIED, "Restriction denied array contents result", it) }
                .takeUnless { it is KorneaResult.Success<*> }
                ?.cast()
        }
    }
}

@Suppress("UnnecessaryVariable")
@ExperimentalUnsignedTypes
//Restrictions here are relaxed due to Java/Kotlin interop
fun parseKnolusTransScope(text: String, restrictions: KnolusTransVisitorRestrictions<*>, lexerInit: (CharStream?) -> Lexer, parserInit: (TokenStream?) -> Parser): KorneaResult<KnolusUnion.ScopeType> {
    try {
        val charStream = CharStreams.fromString(text)
        val lexer = lexerInit(charStream)
        lexer.removeErrorListeners()
        lexer.addErrorListener(ThrowingErrorListener)

        val tokens = CommonTokenStream(lexer)
        val parser = parserInit(tokens)
        parser.removeErrorListeners()
        parser.addErrorListener(ThrowingErrorListener)

        val blueprint = ReflectiveParserBlueprint(parser)

        val visitor = TransKnolusVisitor(restrictions, parser, blueprint)
        val union = visitor.visitScope(blueprint.parseScope())
            .filter { scope -> scope.lines.isNotEmpty() }
        return union
    } catch (pce: ParseCancellationException) {
        return KorneaResult.thrown(pce)
    }
}

@Suppress("UnnecessaryVariable")
@ExperimentalUnsignedTypes
//Restrictions here on nullability are relaxed due to Java/Kotlin interop
fun <L: Lexer, P : Parser, R, V : ParseTreeVisitor<KorneaResult<R>>> parseKnolusTransRule(
    text: String,
    restrictions: KnolusTransVisitorRestrictions<*>,
    lexerInit: (CharStream?) -> L,
    parserInit: (TokenStream?) -> P,
    visitorInit: (restrictions: KnolusTransVisitorRestrictions<*>, parser: P, delegate: TransKnolusParserVisitor) -> V,
    visit: (parser: P, visitor: V) -> KorneaResult<R>
): KorneaResult<KnolusParserResult<R, L, CommonTokenStream, P, V>> =
    parseKnolusTransRule(text, restrictions, lexerInit, parserInit, ::TransKnolusVisitor, visitorInit, visit)

@Suppress("UnnecessaryVariable")
@ExperimentalUnsignedTypes
//Restrictions here on nullability are relaxed due to Java/Kotlin interop
fun <L: Lexer, P : Parser, R, V : ParseTreeVisitor<KorneaResult<R>>, D: TransKnolusParserVisitor> parseKnolusTransRule(
    text: String,
    restrictions: KnolusTransVisitorRestrictions<*>,
    lexerInit: (CharStream?) -> L,
    parserInit: (TokenStream?) -> P,
    delegateInit: (restrictions: KnolusTransVisitorRestrictions<*>, parser: P, ParserBlueprint<ParserRuleContext>) -> D,
    visitorInit: (restrictions: KnolusTransVisitorRestrictions<*>, parser: P, delegate: D) -> V,
    visit: (parser: P, visitor: V) -> KorneaResult<R>
): KorneaResult<KnolusParserResult<R, L, CommonTokenStream, P, V>> {
    try {
        val charStream = CharStreams.fromString(text)
        val lexer = lexerInit(charStream)
        lexer.removeErrorListeners()
        lexer.addErrorListener(ThrowingErrorListener)

        val tokens = CommonTokenStream(lexer)
        val parser = parserInit(tokens)
        parser.removeErrorListeners()
        parser.addErrorListener(ThrowingErrorListener)

        val blueprint = ReflectiveParserBlueprint(parser)

        val delegate = delegateInit(restrictions, parser, blueprint)
        val visitor = visitorInit(restrictions, parser, delegate)

        return visit(parser, visitor).map { union -> KnolusParserResult(union, lexer, tokens, parser, visitor) }
    } catch (pce: ParseCancellationException) {
        return KorneaResult.thrown(pce)
    }
}

@Suppress("UnnecessaryVariable")
@ExperimentalUnsignedTypes
fun <L: Lexer, T: BufferedTokenStream, P : Parser, R, V : ParseTreeVisitor<KorneaResult<R>>> parseKnolusTransRuleWithState(
    text: String,
    state: KnolusParserState<R, L, T, P, V>,
    visit: (parser: P, visitor: V) -> KorneaResult<R>
): KorneaResult<R> {
    try {
        val charStream = CharStreams.fromString(text)
        state.lexer.inputStream = charStream
        state.tokenStream.tokenSource = state.lexer
        state.parser.inputStream = state.tokenStream

        return visit(state.parser, state.visitor)
    } catch (pce: ParseCancellationException) {
        return KorneaResult.thrown(pce)
    }
}