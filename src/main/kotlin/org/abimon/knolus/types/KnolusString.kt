package org.abimon.knolus.types

import org.abimon.knolus.KnolusResult
import org.abimon.knolus.context.KnolusContext
import org.abimon.knolus.toFormattedBoolean
import org.abimon.knolus.toIntBaseN
import org.abimon.knolus.toIntOrNullBaseN

inline class KnolusString(val string: String) : KnolusTypedValue {
    companion object TypeInfo : KnolusTypedValue.TypeInfo<KnolusString> {
        override val typeHierarchicalNames: Array<String> = arrayOf("String", "Object")

        override fun isInstance(value: KnolusTypedValue): Boolean = value is KnolusString
    }

    override val typeInfo: KnolusTypedValue.TypeInfo<KnolusString>
        get() = TypeInfo

    override suspend fun <T> asString(context: KnolusContext<T>): KnolusResult<String> = KnolusResult.success(string)
    override suspend fun <T> asNumber(context: KnolusContext<T>): KnolusResult<Number> =
        if (string.contains('.')) KnolusResult.successOrEmpty(string.toDoubleOrNull()) else KnolusResult.successOrEmpty(string.toIntOrNullBaseN())

    override suspend fun <T> asBoolean(context: KnolusContext<T>): KnolusResult<Boolean> = KnolusResult.success(string.toFormattedBoolean())
}