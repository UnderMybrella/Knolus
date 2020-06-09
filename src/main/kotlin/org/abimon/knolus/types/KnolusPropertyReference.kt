package org.abimon.knolus.types

import org.abimon.knolus.KnolusContext
import org.abimon.knolus.KnolusUnion
import org.abimon.knolus.getOrElse

data class KnolusPropertyReference(val variableName: String, val propertyName: String) : KnolusTypedValue.RuntimeValue {
    companion object TypeInfo: KnolusTypedValue.TypeInfo<KnolusPropertyReference> {
        override val typeHierarchicalNames: Array<String> = arrayOf("MemberVariableReference", "Reference", "Object")

        override fun isInstance(value: KnolusTypedValue): Boolean = value is KnolusPropertyReference
    }

    override val typeInfo: KnolusTypedValue.TypeInfo<KnolusPropertyReference>
        get() = TypeInfo

    @ExperimentalUnsignedTypes
    override suspend fun evaluate(context: KnolusContext): KnolusTypedValue {
        val member = context[variableName] ?: KnolusConstants.Null

        return context.invokeMemberPropertyGetter(member, propertyName).getOrElse(KnolusConstants.Undefined)
    }

    override suspend fun asString(context: KnolusContext): String = evaluate(context).asString(context)
    override suspend fun asNumber(context: KnolusContext): Number = evaluate(context).asNumber(context)
    override suspend fun asBoolean(context: KnolusContext): Boolean = evaluate(context).asBoolean(context)
}