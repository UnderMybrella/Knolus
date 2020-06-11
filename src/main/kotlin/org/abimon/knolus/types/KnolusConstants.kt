package org.abimon.knolus.types

import org.abimon.knolus.KnolusResult
import org.abimon.knolus.context.KnolusContext

sealed class KnolusConstants {
    object Null : KnolusTypedValue, KnolusTypedValue.TypeInfo<Null> {
        override val typeHierarchicalNames: Array<String> = arrayOf("Null", "Constant", "Object")
        override val typeInfo: KnolusTypedValue.TypeInfo<Null>
            get() = this

        override suspend fun <T> asNumber(context: KnolusContext<T>): KnolusResult<Number> = if (context.nullTypeCoercible) KnolusResult.success(0) else KnolusResult.nullEmpty()
        override suspend fun <T> asString(context: KnolusContext<T>): KnolusResult<String> = if (context.nullTypeCoercible) KnolusResult.success("[null]") else KnolusResult.nullEmpty()
        override suspend fun <T> asBoolean(context: KnolusContext<T>): KnolusResult<Boolean> = if (context.nullTypeCoercible) KnolusResult.success(false) else KnolusResult.nullEmpty()

        override fun isInstance(value: KnolusTypedValue): Boolean = value === Null
    }

    object Undefined : KnolusTypedValue, KnolusTypedValue.TypeInfo<Undefined> {
        override val typeHierarchicalNames: Array<String> = arrayOf("Undefined", "Constant", "Object")
        override val typeInfo: KnolusTypedValue.TypeInfo<Undefined>
            get() = this

        override suspend fun <T> asNumber(context: KnolusContext<T>): KnolusResult<Number> = if (context.undefinedTypeCoercible) KnolusResult.success(0) else KnolusResult.undefinedEmpty()
        override suspend fun <T> asString(context: KnolusContext<T>): KnolusResult<String> = if (context.undefinedTypeCoercible) KnolusResult.success("[undefined]") else KnolusResult.undefinedEmpty()
        override suspend fun <T> asBoolean(context: KnolusContext<T>): KnolusResult<Boolean> = if (context.undefinedTypeCoercible) KnolusResult.success(false) else KnolusResult.undefinedEmpty()

        override fun isInstance(value: KnolusTypedValue): Boolean = value === Undefined
    }
}