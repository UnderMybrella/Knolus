package org.abimon.knolus.restrictions

import org.abimon.knolus.*
import org.abimon.knolus.context.KnolusContext
import org.abimon.knolus.types.KnolusTypedValue

interface KnolusBaseLockdownRestrictions : KnolusRestrictions {
    object INSTANCE: KnolusBaseLockdownRestrictions

    override fun canGetVariable(context: KnolusContext, variableName: String): Boolean = false
    override fun canAskParentForVariable(
        child: KnolusContext,
        parent: KnolusContext,
        variableName: String,
    ): Boolean = false

    override fun canSetVariable(
        context: KnolusContext,
        variableName: String,
        variableValue: KnolusTypedValue,
        attemptedParentalSet: Boolean,
    ): Boolean = false

    override fun <T> canRegisterFunction(
        context: KnolusContext,
        functionName: String,
        function: KnolusFunction<T>,
        attemptedParentalRegister: KnolusResult<KnolusFunction<KnolusTypedValue?>>?,
    ): Boolean = false

    override fun canAskForFunction(
        context: KnolusContext,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
    ): Boolean = false

    override fun canAskParentForFunction(
        child: KnolusContext,
        parent: KnolusContext,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
    ): Boolean = false

    override fun canTryFunction(
        context: KnolusContext,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
        function: KnolusFunction<KnolusTypedValue?>,
    ): Boolean = false

    override fun canRunFunction(
        context: KnolusContext,
        function: KnolusFunction<KnolusTypedValue?>,
        parameters: Map<String, KnolusTypedValue>,
    ): Boolean = false

    override fun canAskForMemberFunction(
        context: KnolusContext,
        member: KnolusTypedValue,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
    ): Boolean = false

    override fun canAskForMemberPropertyGetter(
        context: KnolusContext,
        member: KnolusTypedValue,
        propertyName: String,
    ): Boolean = false

    override fun canAskForOperatorFunction(
        context: KnolusContext,
        operator: ExpressionOperator,
        a: KnolusTypedValue,
        b: KnolusTypedValue,
    ): Boolean = false

    override fun createSubroutineRestrictions(
        currentContext: KnolusContext,
        function: KnolusFunction<KnolusTypedValue?>,
        parameters: Map<String, KnolusTypedValue>,
    ): KnolusRestrictions = this
}