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

    override fun canGetVariable(context: KnolusContext<T>, variableName: String): KorneaResult<T> = KorneaResult.empty()
    override fun canAskParentForVariable(
        child: KnolusContext<T>,
        parent: KnolusContext<T>,
        variableName: String,
    ): KorneaResult<T> = KorneaResult.empty()

    override fun canSetVariable(
        context: KnolusContext<T>,
        variableName: String,
        variableValue: KnolusTypedValue,
        attemptedParentalSet: KorneaResult<KnolusTypedValue?>?,
    ): KorneaResult<T> = KorneaResult.empty()

    override fun <FT> canRegisterFunction(
        context: KnolusContext<T>,
        functionName: String,
        function: KnolusFunction<FT, T, *>,
        attemptedParentalRegister: KorneaResult<KnolusFunction<FT, T, *>>?,
    ): KorneaResult<T> = KorneaResult.empty()

    override fun canAskForFunction(
        context: KnolusContext<T>,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
    ): KorneaResult<T> = KorneaResult.empty()

    override fun canAskParentForFunction(
        child: KnolusContext<T>,
        parent: KnolusContext<T>,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
    ): KorneaResult<T> = KorneaResult.empty()

    override fun canTryFunction(
        context: KnolusContext<T>,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
        function: KnolusFunction<KnolusTypedValue?, T, *>,
    ): KorneaResult<T> = KorneaResult.empty()

    override fun canRunFunction(
        context: KnolusContext<T>,
        function: KnolusFunction<KnolusTypedValue?, T, *>,
        parameters: Map<String, KnolusTypedValue>,
    ): KorneaResult<T> = KorneaResult.empty()

    override fun canAskForMemberFunction(
        context: KnolusContext<T>,
        member: KnolusTypedValue,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
    ): KorneaResult<T> = KorneaResult.empty()

    override fun canAskForMemberPropertyGetter(
        context: KnolusContext<T>,
        member: KnolusTypedValue,
        propertyName: String,
    ): KorneaResult<T> = KorneaResult.empty()

    override fun canAskForOperatorFunction(
        context: KnolusContext<T>,
        operator: ExpressionOperator,
        a: KnolusTypedValue,
        b: KnolusTypedValue,
    ): KorneaResult<T> = KorneaResult.empty()

    override fun canAskForCastingOperatorFunction(
        context: KnolusContext<T>,
        self: KnolusTypedValue,
        castingTo: KnolusTypedValue.TypeInfo<*>
    ): KorneaResult<T> = KorneaResult.empty()

    override fun createSubroutineRestrictions(
        currentContext: KnolusContext<T>,
        function: KnolusFunction<KnolusTypedValue?, T, *>,
        parameters: Map<String, KnolusTypedValue>
    ): KorneaResult<KnolusBaseRestriction<T>> = KorneaResult.success(this)
}