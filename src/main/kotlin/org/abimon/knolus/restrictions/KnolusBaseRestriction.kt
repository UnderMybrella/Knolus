package org.abimon.knolus.restrictions

import org.abimon.knolus.*
import org.abimon.knolus.context.KnolusContext
import org.abimon.knolus.types.KnolusTypedValue

interface KnolusBaseRestriction<T> : KnolusRestriction<T> {
    @Suppress("UNCHECKED_CAST")
    object Instance: KnolusBaseRestriction<Any?> {
        inline operator fun <T> invoke(): KnolusBaseRestriction<T> = this as KnolusBaseRestriction<T>
    }

    override fun canGetVariable(context: KnolusContext<T>, variableName: String): KnolusResult<T> = KnolusResult.empty()
    override fun canAskParentForVariable(
        child: KnolusContext<T>,
        parent: KnolusContext<T>,
        variableName: String,
    ): KnolusResult<T> = KnolusResult.empty()

    override fun canSetVariable(
        context: KnolusContext<T>,
        variableName: String,
        variableValue: KnolusTypedValue,
        attemptedParentalSet: KnolusResult<KnolusTypedValue?>?,
    ): KnolusResult<T> = KnolusResult.empty()

    override fun <FT> canRegisterFunction(
        context: KnolusContext<T>,
        functionName: String,
        function: KnolusFunction<FT, T, *>,
        attemptedParentalRegister: KnolusResult<KnolusFunction<FT, T, *>>?,
    ): KnolusResult<T> = KnolusResult.empty()

    override fun canAskForFunction(
        context: KnolusContext<T>,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
    ): KnolusResult<T> = KnolusResult.empty()

    override fun canAskParentForFunction(
        child: KnolusContext<T>,
        parent: KnolusContext<T>,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
    ): KnolusResult<T> = KnolusResult.empty()

    override fun canTryFunction(
        context: KnolusContext<T>,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
        function: KnolusFunction<KnolusTypedValue?, T, *>,
    ): KnolusResult<T> = KnolusResult.empty()

    override fun canRunFunction(
        context: KnolusContext<T>,
        function: KnolusFunction<KnolusTypedValue?, T, *>,
        parameters: Map<String, KnolusTypedValue>,
    ): KnolusResult<T> = KnolusResult.empty()

    override fun canAskForMemberFunction(
        context: KnolusContext<T>,
        member: KnolusTypedValue,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
    ): KnolusResult<T> = KnolusResult.empty()

    override fun canAskForMemberPropertyGetter(
        context: KnolusContext<T>,
        member: KnolusTypedValue,
        propertyName: String,
    ): KnolusResult<T> = KnolusResult.empty()

    override fun canAskForOperatorFunction(
        context: KnolusContext<T>,
        operator: ExpressionOperator,
        a: KnolusTypedValue,
        b: KnolusTypedValue,
    ): KnolusResult<T> = KnolusResult.empty()

    override fun createSubroutineRestrictions(
        currentContext: KnolusContext<T>,
        function: KnolusFunction<KnolusTypedValue?, T, *>,
        parameters: Map<String, KnolusTypedValue>
    ): KnolusResult<KnolusBaseRestriction<T>> = KnolusResult.success(this)
}