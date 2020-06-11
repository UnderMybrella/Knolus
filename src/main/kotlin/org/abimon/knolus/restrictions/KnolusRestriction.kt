package org.abimon.knolus.restrictions

import org.abimon.knolus.*
import org.abimon.knolus.context.KnolusContext
import org.abimon.knolus.types.KnolusTypedValue

@ExperimentalUnsignedTypes
interface KnolusRestriction<T> {
    companion object {
        val SUCCESS: KnolusResult<StaticSuccess> = KnolusResult.success()
    }

    fun canGetVariable(context: KnolusContext<T>, variableName: String): KnolusResult<T>
    fun canAskParentForVariable(child: KnolusContext<T>, parent: KnolusContext<T>, variableName: String): KnolusResult<T>

    fun canSetVariable(
        context: KnolusContext<T>,
        variableName: String,
        variableValue: KnolusTypedValue,
        attemptedParentalSet: KnolusResult<KnolusTypedValue?>?,
    ): KnolusResult<T>

    fun <FT> canRegisterFunction(
        context: KnolusContext<T>,
        functionName: String,
        function: KnolusFunction<FT, T, *>,
        attemptedParentalRegister: KnolusResult<KnolusFunction<FT, T, *>>?,
    ): KnolusResult<T>

    fun canAskForFunction(
        context: KnolusContext<T>,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
    ): KnolusResult<T>

    fun canAskParentForFunction(
        child: KnolusContext<T>,
        parent: KnolusContext<T>,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
    ): KnolusResult<T>

    fun canTryFunction(
        context: KnolusContext<T>,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
        function: KnolusFunction<KnolusTypedValue?, T, *>,
    ): KnolusResult<T>

    fun canRunFunction(
        context: KnolusContext<T>,
        function: KnolusFunction<KnolusTypedValue?, T, *>,
        parameters: Map<String, KnolusTypedValue>,
    ): KnolusResult<T>

    fun canAskForMemberFunction(
        context: KnolusContext<T>,
        member: KnolusTypedValue,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
    ): KnolusResult<T>

    fun canAskForMemberPropertyGetter(context: KnolusContext<T>, member: KnolusTypedValue, propertyName: String): KnolusResult<T>
    fun canAskForOperatorFunction(
        context: KnolusContext<T>,
        operator: ExpressionOperator,
        a: KnolusTypedValue,
        b: KnolusTypedValue,
    ): KnolusResult<T>

    fun createSubroutineRestrictions(
        currentContext: KnolusContext<T>,
        function: KnolusFunction<KnolusTypedValue?, T, *>,
        parameters: Map<String, KnolusTypedValue>,
    ): KnolusResult<KnolusRestriction<T>>
}

@ExperimentalUnsignedTypes
fun <R> KnolusContext<R>.canAskAsParentForVariable(child: KnolusContext<R>, variableName: String): KnolusResult<R> =
    child.restrictions.canAskParentForVariable(child, this, variableName)

@ExperimentalUnsignedTypes
fun <R> KnolusContext<R>.canAskAsParentForFunction(
    child: KnolusContext<R>,
    functionName: String,
    functionParameters: Array<KnolusUnion.FunctionParameterType>,
): KnolusResult<R> = child.restrictions.canAskParentForFunction(child, this, functionName, functionParameters)