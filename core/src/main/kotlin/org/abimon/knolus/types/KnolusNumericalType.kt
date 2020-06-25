package org.abimon.knolus.types

import org.abimon.knolus.context.KnolusContext
import org.abimon.kornea.errors.common.KorneaResult

interface KnolusNumericalType : KnolusTypedValue {
    companion object TypeInfo: KnolusTypedValue.TypeInfo<KnolusNumericalType> {
        override val typeHierarchicalNames: Array<String> = arrayOf("Number", "Object")

        override fun isInstance(instance: Any?): Boolean = instance is KnolusNumericalType
        override fun asInstance(instance: Any?): KnolusNumericalType = instance as KnolusNumericalType
        override fun asInstanceSafe(instance: Any?): KnolusNumericalType? = instance as? KnolusNumericalType
    }

    val number: Number

    override suspend fun <T, R: KnolusTypedValue, I : KnolusTypedValue.TypeInfo<R>> asTypeImpl(context: KnolusContext<T>, typeInfo: I): KorneaResult<R> =
        when (typeInfo) {
            KnolusNumericalType -> typeInfo.asResult(this)
            KnolusString -> typeInfo.asResult(KnolusString(number.toString()))
            KnolusBoolean -> typeInfo.asResult(KnolusBoolean(number != 0))

            else -> KorneaResult.empty()
        }
}

inline class KnolusInt(override val number: Int) : KnolusNumericalType {
    companion object TypeInfo : KnolusTypedValue.TypeInfo<KnolusInt> {
        override val typeHierarchicalNames: Array<String> = arrayOf("Int", "Number", "Object")

        override fun isInstance(instance: Any?): Boolean = instance is KnolusInt
        override fun asInstance(instance: Any?): KnolusInt = instance as KnolusInt
        override fun asInstanceSafe(instance: Any?): KnolusInt? = instance as? KnolusInt
    }

    override val typeInfo: KnolusTypedValue.TypeInfo<KnolusInt>
        get() = TypeInfo
}

inline class KnolusDouble(override val number: Double) : KnolusNumericalType {
    companion object TypeInfo : KnolusTypedValue.TypeInfo<KnolusDouble> {
        override val typeHierarchicalNames: Array<String> = arrayOf("Double", "Number", "Object")

        override fun isInstance(instance: Any?): Boolean = instance is KnolusDouble
        override fun asInstance(instance: Any?): KnolusDouble = instance as KnolusDouble
        override fun asInstanceSafe(instance: Any?): KnolusDouble? = instance as? KnolusDouble
    }

    override val typeInfo: KnolusTypedValue.TypeInfo<KnolusDouble>
        get() = TypeInfo
}

inline class KnolusChar(val char: Char) : KnolusNumericalType {
    companion object TypeInfo : KnolusTypedValue.TypeInfo<KnolusChar> {
        override val typeHierarchicalNames: Array<String> = arrayOf("Char", "Number", "Object")

        override fun isInstance(instance: Any?): Boolean = instance is KnolusChar
        override fun asInstance(instance: Any?): KnolusChar = instance as KnolusChar
        override fun asInstanceSafe(instance: Any?): KnolusChar? = instance as? KnolusChar
    }

    override val number: Number
        get() = char.toInt()

    override val typeInfo: KnolusTypedValue.TypeInfo<KnolusChar>
        get() = TypeInfo

    override suspend fun <T, R: KnolusTypedValue, I : KnolusTypedValue.TypeInfo<R>> asTypeImpl(context: KnolusContext<T>, typeInfo: I): KorneaResult<R> =
        when (typeInfo) {
            KnolusString -> typeInfo.asResult(KnolusString(char.toString()))
            KnolusBoolean -> typeInfo.asResult(KnolusBoolean(char != '\u0000'))
            KnolusNumericalType -> typeInfo.asResult(this)

            else -> KorneaResult.empty()
//            else -> KorneaResult.empty()
        }
}