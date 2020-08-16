package dev.brella.knolus.restrictions

import dev.brella.knolus.*
import dev.brella.knolus.context.KnolusContext
import dev.brella.knolus.types.KnolusTypedValue
import dev.brella.kornea.annotations.AvailableSince
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.StaticSuccess
import dev.brella.kornea.errors.common.success

@ExperimentalUnsignedTypes
interface KnolusRestriction<T> {
    companion object {
        val SUCCESS: KorneaResult<StaticSuccess> = KorneaResult.success()
    }

    fun canGetVariable(context: KnolusContext, variableName: String): KorneaResult<T>
    fun canAskParentForVariable(child: KnolusContext, parent: KnolusContext, variableName: String): KorneaResult<T>

    fun canSetVariable(
        context: KnolusContext,
        variableName: String,
        variableValue: KnolusTypedValue,
        attemptedParentalSet: KorneaResult<KnolusTypedValue?>?,
    ): KorneaResult<T>

    fun <FT> canRegisterFunction(
        context: KnolusContext,
        functionName: String,
        function: KnolusFunction<FT>,
        attemptedParentalRegister: KorneaResult<KnolusFunction<FT>>?,
    ): KorneaResult<T>

    fun canAskForFunction(
        context: KnolusContext,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
    ): KorneaResult<T>

    fun canAskParentForFunction(
        child: KnolusContext,
        parent: KnolusContext,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
    ): KorneaResult<T>

    fun canTryFunction(
        context: KnolusContext,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
        function: KnolusFunction<KnolusTypedValue?>,
    ): KorneaResult<T>

    fun canRunFunction(
        context: KnolusContext,
        function: KnolusFunction<KnolusTypedValue?>,
        parameters: Map<String, KnolusTypedValue>,
    ): KorneaResult<T>

    fun canAskForMemberFunction(
        context: KnolusContext,
        member: KnolusTypedValue,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
    ): KorneaResult<T>

    fun canAskForMemberPropertyGetter(context: KnolusContext, member: KnolusTypedValue, propertyName: String): KorneaResult<T>
    fun canAskForOperatorFunction(
        context: KnolusContext,
        operator: ExpressionOperator,
        a: KnolusTypedValue,
        b: KnolusTypedValue,
    ): KorneaResult<T>

    @AvailableSince(Knolus.VERSION_1_4_0)
    fun canAskForCastingOperatorFunction(
        context: KnolusContext,
        self: KnolusTypedValue,
        castingTo: KnolusTypedValue.TypeInfo<*>,
    ): KorneaResult<T>

    fun createSubroutineRestrictions(
        currentContext: KnolusContext,
        function: KnolusFunction<KnolusTypedValue?>,
        parameters: Map<String, KnolusTypedValue>,
    ): KorneaResult<KnolusRestriction<T>>
}

@ExperimentalUnsignedTypes
fun KnolusContext.canAskAsParentForVariable(child: KnolusContext, variableName: String): KorneaResult<Any?> =
    child.restrictions.canAskParentForVariable(child, this, variableName)

@ExperimentalUnsignedTypes
fun KnolusContext.canAskAsParentForFunction(
    child: KnolusContext,
    functionName: String,
    functionParameters: Array<KnolusUnion.FunctionParameterType>,
): KorneaResult<Any?> = child.restrictions.canAskParentForFunction(child, this, functionName, functionParameters)