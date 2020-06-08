package org.abimon.knolus.types

import org.abimon.knolus.KnolusContext

inline class KnolusBoolean(val boolean: Boolean) : KnolusTypedValue {
    companion object TypeInfo: KnolusTypedValue.TypeInfo<KnolusBoolean> {
        override val typeHierarchicalNames: Array<String> = arrayOf("Boolean", "Object")
    }

    override val typeInfo: KnolusTypedValue.TypeInfo<KnolusBoolean>
        get() = TypeInfo

    override suspend fun asString(context: KnolusContext): String = boolean.toString()
    override suspend fun asNumber(context: KnolusContext): Number = if (boolean) 1 else 0
    override suspend fun asBoolean(context: KnolusContext): Boolean = boolean
}