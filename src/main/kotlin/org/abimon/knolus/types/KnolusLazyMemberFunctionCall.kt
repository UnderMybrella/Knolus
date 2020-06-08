package org.abimon.knolus.types

import org.abimon.knolus.KnolusContext
import org.abimon.knolus.KnolusUnion
import org.abimon.knolus.mapToArray

@ExperimentalUnsignedTypes
data class KnolusLazyMemberFunctionCall(
    val variableName: String,
    val functionName: String,
    val parameters: Array<KnolusUnion.FunctionParameterType>,
) : KnolusTypedValue.RuntimeValue, KnolusUnion.Action {
    companion object TypeInfo: KnolusTypedValue.TypeInfo<KnolusLazyMemberFunctionCall> {
        override val typeHierarchicalNames: Array<String> = arrayOf("FunctionCall", "Reference", "Object")

        override fun isInstance(value: KnolusTypedValue): Boolean = value is KnolusLazyMemberFunctionCall
    }

    override val typeInfo: KnolusTypedValue.TypeInfo<KnolusLazyMemberFunctionCall>
        get() = TypeInfo

    override suspend fun evaluate(context: KnolusContext): KnolusTypedValue {
        val member = context[variableName] ?: return KnolusConstants.Undefined

        val evaledParams = parameters.mapToArray { funcParam ->
            if (funcParam.parameter is KnolusTypedValue.UnsureValue && funcParam.parameter.needsEvaluation(context))
                KnolusUnion.FunctionParameterType(funcParam.name, funcParam.parameter.evaluate(context))
            else
                funcParam
        }

        return context.invokeMemberFunction(member, functionName, evaledParams) ?: KnolusConstants.Undefined
    }

    override suspend fun asString(context: KnolusContext): String = evaluate(context).asString(context)
    override suspend fun asNumber(context: KnolusContext): Number = evaluate(context).asNumber(context)
    override suspend fun asBoolean(context: KnolusContext): Boolean = evaluate(context)
        .asBoolean(context)

    override suspend fun run(context: KnolusContext) {
        evaluate(context)
    }
}