package org.abimon.knolus.restrictions

import org.abimon.knolus.*
import org.abimon.knolus.context.KnolusContext
import org.abimon.knolus.types.KnolusTypedValue

interface KnolusBasePermissiveRestrictions : KnolusRestrictions {
    object INSTANCE: KnolusBasePermissiveRestrictions

    override fun canGetVariable(context: KnolusContext, variableName: String): Boolean = true
    override fun canAskParentForVariable(
        child: KnolusContext,
        parent: KnolusContext,
        variableName: String,
    ): Boolean = true

    override fun canSetVariable(
        context: KnolusContext,
        variableName: String,
        variableValue: KnolusTypedValue,
        attemptedParentalSet: Boolean,
    ): Boolean = true

    override fun <T> canRegisterFunction(
        context: KnolusContext,
        functionName: String,
        function: KnolusFunction<T>,
        attemptedParentalRegister: KnolusResult<KnolusFunction<KnolusTypedValue?>>?,
    ): Boolean = true

    override fun canAskForFunction(
        context: KnolusContext,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
    ): Boolean = true

    override fun canAskParentForFunction(
        child: KnolusContext,
        parent: KnolusContext,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
    ): Boolean = true

    override fun canTryFunction(
        context: KnolusContext,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
        function: KnolusFunction<KnolusTypedValue?>,
    ): Boolean = true

    override fun canRunFunction(
        context: KnolusContext,
        function: KnolusFunction<KnolusTypedValue?>,
        parameters: Map<String, KnolusTypedValue>,
    ): Boolean = true

    override fun canAskForMemberFunction(
        context: KnolusContext,
        member: KnolusTypedValue,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
    ): Boolean = true

    override fun canAskForMemberPropertyGetter(
        context: KnolusContext,
        member: KnolusTypedValue,
        propertyName: String,
    ): Boolean = true

    override fun canAskForOperatorFunction(
        context: KnolusContext,
        operator: ExpressionOperator,
        a: KnolusTypedValue,
        b: KnolusTypedValue,
    ): Boolean = true

    override fun createSubroutineRestrictions(
        currentContext: KnolusContext,
        function: KnolusFunction<KnolusTypedValue?>,
        parameters: Map<String, KnolusTypedValue>,
    ): KnolusRestrictions = this
}