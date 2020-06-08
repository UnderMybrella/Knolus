package org.abimon.knolus.types

import org.abimon.knolus.KnolusContext
import org.abimon.knolus.toFormattedBoolean
import org.abimon.knolus.toIntBaseN

inline class KnolusString(val string: String) : KnolusTypedValue {
    companion object TypeInfo : KnolusTypedValue.TypeInfo<KnolusString> {
        override val typeHierarchicalNames: Array<String> = arrayOf("String", "Object")

        override fun isInstance(value: KnolusTypedValue): Boolean = value is KnolusString
    }

    override val typeInfo: KnolusTypedValue.TypeInfo<KnolusString>
        get() = TypeInfo

    override suspend fun asString(context: KnolusContext): String = string
    override suspend fun asNumber(context: KnolusContext): Number =
        if (string.contains('.')) string.toDouble() else string.toIntBaseN()

    override suspend fun asBoolean(context: KnolusContext): Boolean = string.toFormattedBoolean()
}