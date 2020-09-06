package dev.brella.knolus.types

import dev.brella.knolus.context.KnolusContext
import dev.brella.kornea.errors.common.KorneaResult
import java.math.BigInteger

interface KnolusNumericalType : KnolusTypedValue {
    companion object TypeInfo: KnolusTypedValue.TypeInfo<KnolusNumericalType> {
        override val typeHierarchicalNames: Array<String> = arrayOf("Number", "Object")

        override fun isInstance(instance: Any?): Boolean = instance is KnolusNumericalType
        override fun asInstance(instance: Any?): KnolusNumericalType = instance as KnolusNumericalType
        override fun asInstanceSafe(instance: Any?): KnolusNumericalType? = instance as? KnolusNumericalType
    }

    val number: Number

    override suspend fun <R: KnolusTypedValue, I : KnolusTypedValue.TypeInfo<R>> asTypeImpl(context: KnolusContext, typeInfo: I): KorneaResult<R> =
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

inline class KnolusLong(override val number: Long) : KnolusNumericalType {
    companion object TypeInfo : KnolusTypedValue.TypeInfo<KnolusLong> {
        override val typeHierarchicalNames: Array<String> = arrayOf("Long", "Number", "Object")

        override fun isInstance(instance: Any?): Boolean = instance is KnolusLong
        override fun asInstance(instance: Any?): KnolusLong = instance as KnolusLong
        override fun asInstanceSafe(instance: Any?): KnolusLong? = instance as? KnolusLong
    }

    override val typeInfo: KnolusTypedValue.TypeInfo<KnolusLong>
        get() = TypeInfo
}

inline class KnolusBigInt(override val number: BigInteger): KnolusNumericalType {
    companion object TypeInfo : KnolusTypedValue.TypeInfo<KnolusBigInt> {
        override val typeHierarchicalNames: Array<String> = arrayOf("BigInt", "Number", "Object")

        override fun isInstance(instance: Any?): Boolean = instance is KnolusBigInt
        override fun asInstance(instance: Any?): KnolusBigInt = instance as KnolusBigInt
        override fun asInstanceSafe(instance: Any?): KnolusBigInt? = instance as? KnolusBigInt
    }

    override val typeInfo: KnolusTypedValue.TypeInfo<KnolusBigInt>
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

    override suspend fun <R: KnolusTypedValue, I : KnolusTypedValue.TypeInfo<R>> asTypeImpl(context: KnolusContext, typeInfo: I): KorneaResult<R> =
        when (typeInfo) {
            KnolusString -> typeInfo.asResult(KnolusString(char.toString()))
            KnolusBoolean -> typeInfo.asResult(KnolusBoolean(char != '\u0000'))
            KnolusNumericalType -> typeInfo.asResult(this)

            else -> KorneaResult.empty()
//            else -> KorneaResult.empty()
        }
}