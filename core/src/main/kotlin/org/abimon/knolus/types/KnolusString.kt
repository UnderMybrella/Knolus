package org.abimon.knolus.types

import org.abimon.knolus.context.KnolusContext
import org.abimon.knolus.toFormattedBoolean
import org.abimon.knolus.toIntOrNullBaseN
import org.abimon.kornea.errors.common.KorneaResult

inline class KnolusString(val string: String) : KnolusTypedValue {
    companion object TypeInfo : KnolusTypedValue.TypeInfo<KnolusString> {
        override val typeHierarchicalNames: Array<String> = arrayOf("String", "Object")

        override fun isInstance(instance: Any?): Boolean = instance is KnolusString
        override fun asInstance(instance: Any?): KnolusString = instance as KnolusString
        override fun asInstanceSafe(instance: Any?): KnolusString? = instance as? KnolusString
    }

    override val typeInfo: KnolusTypedValue.TypeInfo<KnolusString>
        get() = TypeInfo

    override suspend fun <T> asString(context: KnolusContext<T>): KorneaResult<String> = KorneaResult.success(string)
    override suspend fun <T> asNumber(context: KnolusContext<T>): KorneaResult<Number> =
        if (string.contains('.')) KorneaResult.successOrEmpty(string.toDoubleOrNull()) else KorneaResult.successOrEmpty(string.toIntOrNullBaseN())

    override suspend fun <T> asBoolean(context: KnolusContext<T>): KorneaResult<Boolean> = KorneaResult.success(string.toFormattedBoolean())
}