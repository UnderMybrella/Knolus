package dev.brella.knolus.restrictions

import dev.brella.knolus.*
import dev.brella.knolus.context.KnolusContext
import dev.brella.knolus.types.KnolusTypedValue
import dev.brella.kornea.errors.common.KorneaResult

interface KnolusBaseRestriction<T> : KnolusRestriction<T> {
    @Suppress("UNCHECKED_CAST")
    object Instance : KnolusBaseRestriction<Any?> {
        inline operator fun <T> invoke(): KnolusBaseRestriction<T> = this as KnolusBaseRestriction<T>
    }

    override fun canGetVariable(context: KnolusContext, variableName: String): KorneaResult<T> = KorneaResult.empty()
    override fun canAskParentForVariable(
        child: KnolusContext,
        parent: KnolusContext,
        variableName: String,
    ): KorneaResult<T> = KorneaResult.empty()

    override fun canSetVariable(
        context: KnolusContext,
        variableName: String,
        variableValue: KnolusTypedValue,
        attemptedParentalSet: KorneaResult<KnolusTypedValue?>?,
    ): KorneaResult<T> = KorneaResult.empty()

    override fun <FT> canRegisterFunction(
        context: KnolusContext,
        functionName: String,
        function: KnolusFunction<FT>,
        attemptedParentalRegister: KorneaResult<KnolusFunction<FT>>?,
    ): KorneaResult<T> = KorneaResult.empty()

    override fun canAskForFunction(
        context: KnolusContext,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
    ): KorneaResult<T> = KorneaResult.empty()

    override fun canAskParentForFunction(
        child: KnolusContext,
        parent: KnolusContext,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
    ): KorneaResult<T> = KorneaResult.empty()

    override fun canTryFunction(
        context: KnolusContext,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
        function: KnolusFunction<KnolusTypedValue?>,
    ): KorneaResult<T> = KorneaResult.empty()

    override fun canRunFunction(
        context: KnolusContext,
        function: KnolusFunction<KnolusTypedValue?>,
        parameters: Map<String, KnolusTypedValue>,
    ): KorneaResult<T> = KorneaResult.empty()

    override fun canAskForMemberFunction(
        context: KnolusContext,
        member: KnolusTypedValue,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
    ): KorneaResult<T> = KorneaResult.empty()

    override fun canAskForMemberPropertyGetter(
        context: KnolusContext,
        member: KnolusTypedValue,
        propertyName: String,
    ): KorneaResult<T> = KorneaResult.empty()

    override fun canAskForOperatorFunction(
        context: KnolusContext,
        operator: ExpressionOperator,
        a: KnolusTypedValue,
        b: KnolusTypedValue,
    ): KorneaResult<T> = KorneaResult.empty()

    override fun canAskForCastingOperatorFunction(
        context: KnolusContext,
        self: KnolusTypedValue,
        castingTo: KnolusTypedValue.TypeInfo<*>
    ): KorneaResult<T> = KorneaResult.empty()

    override fun createSubroutineRestrictions(
        currentContext: KnolusContext,
        function: KnolusFunction<KnolusTypedValue?>,
        parameters: Map<String, KnolusTypedValue>
    ): KorneaResult<KnolusBaseRestriction<T>> = KorneaResult.success(this)
}