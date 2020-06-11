package org.abimon.knolus.types

import org.abimon.knolus.KnolusResult
import org.abimon.knolus.context.KnolusContext
import org.abimon.knolus.flatMap
import org.abimon.knolus.getOrElse

data class KnolusPropertyReference(val variableName: String, val propertyName: String) : KnolusTypedValue.RuntimeValue<KnolusTypedValue> {
    companion object TypeInfo: KnolusTypedValue.TypeInfo<KnolusPropertyReference> {
        override val typeHierarchicalNames: Array<String> = arrayOf("MemberVariableReference", "Reference", "Object")

        override fun isInstance(value: KnolusTypedValue): Boolean = value is KnolusPropertyReference
    }

    override val typeInfo: KnolusTypedValue.TypeInfo<KnolusPropertyReference>
        get() = TypeInfo

    @ExperimentalUnsignedTypes
    override suspend fun <T> evaluate(context: KnolusContext<T>): KnolusResult<KnolusTypedValue> = context[variableName].flatMap { member ->
        context.invokeMemberPropertyGetter(member, propertyName)
    }

    override suspend fun <T> asString(context: KnolusContext<T>): KnolusResult<String> = evaluate(context).flatMap { it.asString(context) }
    override suspend fun <T> asNumber(context: KnolusContext<T>): KnolusResult<Number> = evaluate(context).flatMap { it.asNumber(context) }
    override suspend fun <T> asBoolean(context: KnolusContext<T>): KnolusResult<Boolean> = evaluate(context).flatMap { it.asBoolean(context) }
}