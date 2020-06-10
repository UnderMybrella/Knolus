package org.abimon.knolus.types

import org.abimon.knolus.*
import org.abimon.knolus.context.KnolusContext

@ExperimentalUnsignedTypes
data class KnolusLazyFunctionCall(val name: String, val parameters: Array<KnolusUnion.FunctionParameterType>) :
    KnolusTypedValue.RuntimeValue, KnolusUnion.Action<KnolusTypedValue> {
    companion object TypeInfo : KnolusTypedValue.TypeInfo<KnolusLazyFunctionCall> {
        override val typeHierarchicalNames: Array<String> = arrayOf("FunctionCall", "Object")

        override fun isInstance(value: KnolusTypedValue): Boolean = value is KnolusLazyFunctionCall
    }

    override val typeInfo: KnolusTypedValue.TypeInfo<KnolusLazyFunctionCall>
        get() = TypeInfo

    override suspend fun evaluate(context: KnolusContext): KnolusTypedValue =
        run(context).getOrElse(KnolusConstants.Undefined)

    override suspend fun asString(context: KnolusContext): String = evaluate(context).asString(context)
    override suspend fun asNumber(context: KnolusContext): Number = evaluate(context).asNumber(context)
    override suspend fun asBoolean(context: KnolusContext): Boolean = evaluate(context).asBoolean(context)

    override suspend fun run(context: KnolusContext): KnolusResult<KnolusTypedValue> {
        val evaledParams = parameters.mapToArray { funcParam ->
            if (funcParam.parameter is KnolusTypedValue.UnsureValue && funcParam.parameter.needsEvaluation(context))
                KnolusUnion.FunctionParameterType(funcParam.name, funcParam.parameter.evaluate(context))
            else
                funcParam
        }
        return context.invokeFunction(name, evaledParams)
    }
}