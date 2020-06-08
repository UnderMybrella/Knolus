package org.abimon.knolus.types

import org.abimon.knolus.*

data class KnolusLazyString(val components: Array<KnolusUnion.StringComponent>): KnolusTypedValue.RuntimeValue {
    companion object TypeInfo: KnolusTypedValue.TypeInfo<KnolusLazyString> {
        override val typeHierarchicalNames: Array<String> = arrayOf("LazyString", "Object")
    }

    override val typeInfo: KnolusTypedValue.TypeInfo<KnolusLazyString>
        get() = TypeInfo

    override suspend fun evaluate(context: KnolusContext): KnolusString =
        KnolusString(
            components.mapNotNull { component ->
                when (component) {
                    is KnolusUnion.StringComponent.RawText -> component.text
                    is KnolusUnion.StringComponent.VariableReference -> context[component.variableName]?.asString(
                        context)
                }
            }.joinToString("")
        )

    override suspend fun asString(context: KnolusContext): String = evaluate(context).string
    override suspend fun asNumber(context: KnolusContext): Number {
        val str = evaluate(context).string
        if (str.contains('.'))
            return str.toDouble()
        return str.toIntBaseN()
    }

    override suspend fun asBoolean(context: KnolusContext): Boolean = asString(context).toFormattedBoolean()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KnolusLazyString

        if (!components.contentEquals(other.components)) return false

        return true
    }

    override fun hashCode(): Int {
        return components.contentHashCode()
    }
}