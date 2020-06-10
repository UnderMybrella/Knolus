package org.abimon.knolus.types

import org.abimon.knolus.context.KnolusContext

/**
 * A reference to a variable defined by either the environment, or the user.
 * Note: These are 'freestanding' variables, not member properties.
 */
data class KnolusVariableReference(val variableName: String) : KnolusTypedValue.RuntimeValue {
    companion object TypeInfo: KnolusTypedValue.TypeInfo<KnolusVariableReference> {
        override val typeHierarchicalNames: Array<String> = arrayOf("VariableReference", "Reference", "Object")
        override fun isInstance(value: KnolusTypedValue): Boolean = value is KnolusVariableReference
    }

    override val typeInfo: KnolusTypedValue.TypeInfo<KnolusVariableReference>
        get() = TypeInfo

    @ExperimentalUnsignedTypes
    override suspend fun evaluate(context: KnolusContext): KnolusTypedValue =
        context[variableName] ?: KnolusConstants.Null

    override suspend fun asString(context: KnolusContext): String = evaluate(context).asString(context)
    override suspend fun asNumber(context: KnolusContext): Number = evaluate(context).asNumber(context)
    override suspend fun asBoolean(context: KnolusContext): Boolean = evaluate(context).asBoolean(context)
}