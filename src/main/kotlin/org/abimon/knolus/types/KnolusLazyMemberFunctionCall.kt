package org.abimon.knolus.types

import org.abimon.knolus.*
import org.abimon.knolus.context.KnolusContext

@ExperimentalUnsignedTypes
data class KnolusLazyMemberFunctionCall(
    val variableName: String,
    val functionName: String,
    val parameters: Array<KnolusUnion.FunctionParameterType>,
) : KnolusTypedValue.RuntimeValue<KnolusTypedValue>, KnolusUnion.Action<KnolusTypedValue> {
    companion object TypeInfo : KnolusTypedValue.TypeInfo<KnolusLazyMemberFunctionCall> {
        override val typeHierarchicalNames: Array<String> = arrayOf("FunctionCall", "Reference", "Object")

        override fun isInstance(value: KnolusTypedValue): Boolean = value is KnolusLazyMemberFunctionCall
    }

    override val typeInfo: KnolusTypedValue.TypeInfo<KnolusLazyMemberFunctionCall>
        get() = TypeInfo

    override suspend fun <T> evaluate(context: KnolusContext<T>): KnolusResult<KnolusTypedValue> =
        run(context)

    override suspend fun <T> run(context: KnolusContext<T>): KnolusResult<KnolusTypedValue> =
        context[variableName].switchIfEmpty {
            KnolusResult.Error(KnolusContext.UNDECLARED_VARIABLE, "No such variable by name of $variableName")
        }.flatMap { member ->
            parameters.fold(KnolusResult.foldingMutableListOf<KnolusUnion.FunctionParameterType>()) { acc, funcParam ->
                acc.flatMapOrSelf { list ->
                    if (funcParam.parameter is KnolusTypedValue.UnsureValue<*> && funcParam.parameter.needsEvaluation(context))
                        funcParam.parameter.evaluate(context).map { value -> list.withElement(KnolusUnion.FunctionParameterType(funcParam.name, value)) }
                    else
                        KnolusResult.success(list.withElement(funcParam))
                }
            }.flatMap { params -> context.invokeMemberFunction(member, functionName, params.toTypedArray()) }
        }
}