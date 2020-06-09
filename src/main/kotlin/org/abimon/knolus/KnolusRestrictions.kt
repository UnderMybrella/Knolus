package org.abimon.knolus

import org.abimon.knolus.types.KnolusTypedValue

@ExperimentalUnsignedTypes
interface KnolusRestrictions {
    object Permissive : KnolusRestrictions {
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
            attemptedParentalRegister: KnolusResult<Array<KnolusFunction<KnolusTypedValue?>>>?,
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

    object Lockdown : KnolusRestrictions {
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
            attemptedParentalRegister: KnolusResult<Array<KnolusFunction<KnolusTypedValue?>>>?,
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

    fun canGetVariable(context: KnolusContext, variableName: String): Boolean
    fun canAskParentForVariable(child: KnolusContext, parent: KnolusContext, variableName: String): Boolean

    fun canSetVariable(
        context: KnolusContext,
        variableName: String,
        variableValue: KnolusTypedValue,
        attemptedParentalSet: Boolean,
    ): Boolean

    fun <T> canRegisterFunction(
        context: KnolusContext,
        functionName: String,
        function: KnolusFunction<T>,
        attemptedParentalRegister: KnolusResult<Array<KnolusFunction<KnolusTypedValue?>>>?,
    ): Boolean

    fun canAskForFunction(
        context: KnolusContext,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
    ): Boolean

    fun canAskParentForFunction(
        child: KnolusContext,
        parent: KnolusContext,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
    ): Boolean

    fun canTryFunction(
        context: KnolusContext,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
        function: KnolusFunction<KnolusTypedValue?>,
    ): Boolean

    fun canRunFunction(
        context: KnolusContext,
        function: KnolusFunction<KnolusTypedValue?>,
        parameters: Map<String, KnolusTypedValue>,
    ): Boolean

    fun canAskForMemberFunction(
        context: KnolusContext,
        member: KnolusTypedValue,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
    ): Boolean

    fun canAskForMemberPropertyGetter(context: KnolusContext, member: KnolusTypedValue, propertyName: String): Boolean
    fun canAskForOperatorFunction(
        context: KnolusContext,
        operator: ExpressionOperator,
        a: KnolusTypedValue,
        b: KnolusTypedValue,
    ): Boolean

    fun createSubroutineRestrictions(
        currentContext: KnolusContext,
        function: KnolusFunction<KnolusTypedValue?>,
        parameters: Map<String, KnolusTypedValue>,
    ): KnolusRestrictions
}

@ExperimentalUnsignedTypes
fun KnolusContext.canAskAsParentForVariable(child: KnolusContext, variableName: String): Boolean =
    child.restrictions.canAskParentForVariable(child, this, variableName)

@ExperimentalUnsignedTypes
fun KnolusContext.canAskAsParentForFunction(
    child: KnolusContext,
    functionName: String,
    functionParameters: Array<KnolusUnion.FunctionParameterType>,
): Boolean = child.restrictions.canAskParentForFunction(child, this, functionName, functionParameters)