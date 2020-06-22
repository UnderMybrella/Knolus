package org.abimon.knolus.restrictions

import org.abimon.knolus.*
import org.abimon.knolus.context.KnolusContext
import org.abimon.knolus.types.KnolusTypedValue
import org.abimon.kornea.errors.common.KorneaResult
import org.abimon.kornea.errors.common.StaticSuccess
import org.abimon.kornea.errors.common.success

@ExperimentalUnsignedTypes
interface KnolusRestriction<T> {
    companion object {
        val SUCCESS: KorneaResult<StaticSuccess> = KorneaResult.success()
    }

    fun canGetVariable(context: KnolusContext<T>, variableName: String): KorneaResult<T>
    fun canAskParentForVariable(child: KnolusContext<T>, parent: KnolusContext<T>, variableName: String): KorneaResult<T>

    fun canSetVariable(
        context: KnolusContext<T>,
        variableName: String,
        variableValue: KnolusTypedValue,
        attemptedParentalSet: KorneaResult<KnolusTypedValue?>?,
    ): KorneaResult<T>

    fun <FT> canRegisterFunction(
        context: KnolusContext<T>,
        functionName: String,
        function: KnolusFunction<FT, T, *>,
        attemptedParentalRegister: KorneaResult<KnolusFunction<FT, T, *>>?,
    ): KorneaResult<T>

    fun canAskForFunction(
        context: KnolusContext<T>,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
    ): KorneaResult<T>

    fun canAskParentForFunction(
        child: KnolusContext<T>,
        parent: KnolusContext<T>,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
    ): KorneaResult<T>

    fun canTryFunction(
        context: KnolusContext<T>,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
        function: KnolusFunction<KnolusTypedValue?, T, *>,
    ): KorneaResult<T>

    fun canRunFunction(
        context: KnolusContext<T>,
        function: KnolusFunction<KnolusTypedValue?, T, *>,
        parameters: Map<String, KnolusTypedValue>,
    ): KorneaResult<T>

    fun canAskForMemberFunction(
        context: KnolusContext<T>,
        member: KnolusTypedValue,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
    ): KorneaResult<T>

    fun canAskForMemberPropertyGetter(context: KnolusContext<T>, member: KnolusTypedValue, propertyName: String): KorneaResult<T>
    fun canAskForOperatorFunction(
        context: KnolusContext<T>,
        operator: ExpressionOperator,
        a: KnolusTypedValue,
        b: KnolusTypedValue,
    ): KorneaResult<T>

    fun createSubroutineRestrictions(
        currentContext: KnolusContext<T>,
        function: KnolusFunction<KnolusTypedValue?, T, *>,
        parameters: Map<String, KnolusTypedValue>,
    ): KorneaResult<KnolusRestriction<T>>
}

@ExperimentalUnsignedTypes
fun <R> KnolusContext<R>.canAskAsParentForVariable(child: KnolusContext<R>, variableName: String): KorneaResult<R> =
    child.restrictions.canAskParentForVariable(child, this, variableName)

@ExperimentalUnsignedTypes
fun <R> KnolusContext<R>.canAskAsParentForFunction(
    child: KnolusContext<R>,
    functionName: String,
    functionParameters: Array<KnolusUnion.FunctionParameterType>,
): KorneaResult<R> = child.restrictions.canAskParentForFunction(child, this, functionName, functionParameters)