package org.abimon.knolus.types

import org.abimon.knolus.context.KnolusContext
import org.abimon.kornea.errors.common.KorneaResult

interface KnolusNumericalType : KnolusTypedValue {
    companion object TypeInfo: KnolusTypedValue.TypeInfo<KnolusNumericalType> {
        override val typeHierarchicalNames: Array<String> = arrayOf("Number", "Object")

        override fun isInstance(value: KnolusTypedValue): Boolean = value is KnolusNumericalType
    }

    val number: Number

    override suspend fun <T> asNumber(context: KnolusContext<T>): KorneaResult<Number> = KorneaResult.success(number)
    override suspend fun <T> asString(context: KnolusContext<T>): KorneaResult<String> = KorneaResult.success(number.toString())
    override suspend fun <T> asBoolean(context: KnolusContext<T>): KorneaResult<Boolean> = KorneaResult.success(number != 0)
}

inline class KnolusInt(override val number: Int) : KnolusNumericalType {
    companion object TypeInfo : KnolusTypedValue.TypeInfo<KnolusInt> {
        override val typeHierarchicalNames: Array<String> = arrayOf("Int", "Number", "Object")

        override fun isInstance(value: KnolusTypedValue): Boolean = value is KnolusInt
    }

    override val typeInfo: KnolusTypedValue.TypeInfo<KnolusInt>
        get() = TypeInfo
}

inline class KnolusDouble(override val number: Double) : KnolusNumericalType {
    companion object TypeInfo : KnolusTypedValue.TypeInfo<KnolusDouble> {
        override val typeHierarchicalNames: Array<String> = arrayOf("Double", "Number", "Object")

        override fun isInstance(value: KnolusTypedValue): Boolean = value is KnolusDouble
    }

    override val typeInfo: KnolusTypedValue.TypeInfo<KnolusDouble>
        get() = TypeInfo
}

inline class KnolusChar(val char: Char) : KnolusNumericalType {
    companion object TypeInfo : KnolusTypedValue.TypeInfo<KnolusChar> {
        override val typeHierarchicalNames: Array<String> = arrayOf("Char", "Number", "Object")

        override fun isInstance(value: KnolusTypedValue): Boolean = value is KnolusChar
    }

    override val number: Number
        get() = char.toInt()

    override val typeInfo: KnolusTypedValue.TypeInfo<KnolusChar>
        get() = TypeInfo

    override suspend fun <T> asString(context: KnolusContext<T>): KorneaResult<String> = KorneaResult.success(char.toString())
    override suspend fun <T> asBoolean(context: KnolusContext<T>): KorneaResult<Boolean> = KorneaResult.success(char != '\u0000')
}