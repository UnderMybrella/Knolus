package org.abimon.knolus.types

import org.abimon.knolus.context.KnolusContext

sealed class KnolusArray<T : KnolusTypedValue>(open val array: Array<T>) : KnolusTypedValue {
    companion object TypeInfo: KnolusTypedValue.TypeInfo<KnolusArray<*>> {
        override val typeHierarchicalNames: Array<String> = arrayOf("Array", "Object")

        fun of(array: Array<KnolusTypedValue>): KnolusArray<KnolusTypedValue> {
            var unsure: Boolean = false
            array.forEach { value ->
                if (value is KnolusTypedValue.RuntimeValue) return RuntimeArray(array)
                if (!unsure && value is KnolusTypedValue.UnsureValue) unsure = true
            }

            if (unsure) return UnsureArray(array)
            return StableArray(array)
        }
        fun <T: KnolusTypedValue> ofStable(array: Array<T>): KnolusArray<T> = StableArray(array)
        fun <T: KnolusTypedValue.UnsureValue> ofUnsure(array: Array<T>): KnolusArray<T> = UnsureArray(array)

        suspend fun KnolusArray<*>.evaluateOrSelf(context: KnolusContext): KnolusArray<*> = when (this) {
            is RuntimeArray<*> -> evaluate(context)
            is UnsureArray<*> -> if (needsEvaluation(context)) evaluate(context) else this
            else -> this
        }

        override fun isInstance(value: KnolusTypedValue): Boolean = value is KnolusArray<*>
    }

    private data class StableArray<T: KnolusTypedValue>(override val array: Array<T>): KnolusArray<T>(array) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as StableArray<*>

            if (!array.contentEquals(other.array)) return false

            return true
        }

        override fun hashCode(): Int {
            return array.contentHashCode()
        }

        init {
            require(array.none { t -> t is KnolusTypedValue.UnsureValue || t is KnolusTypedValue.RuntimeValue })
        }
    }

    private data class UnsureArray<T: KnolusTypedValue>(override val array: Array<T>): KnolusArray<T>(array), KnolusTypedValue.UnsureValue {
        override suspend fun needsEvaluation(context: KnolusContext): Boolean = array.any { t -> t is KnolusTypedValue.UnsureValue && t.needsEvaluation(context) }
        override suspend fun evaluate(context: KnolusContext): KnolusArray<KnolusTypedValue> = of(Array(array.size) { array[it].evaluateOrSelf(context) })

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            if (!super.equals(other)) return false

            other as UnsureArray<*>

            if (!array.contentEquals(other.array)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + array.contentHashCode()
            return result
        }
    }

    private data class RuntimeArray<T: KnolusTypedValue>(override val array: Array<T>): KnolusArray<T>(array), KnolusTypedValue.RuntimeValue {
        override suspend fun evaluate(context: KnolusContext): KnolusArray<KnolusTypedValue> = of(Array(array.size) { array[it].evaluateOrSelf(context) })
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as RuntimeArray<*>

            if (!array.contentEquals(other.array)) return false

            return true
        }

        override fun hashCode(): Int {
            return array.contentHashCode()
        }
    }

    override val typeInfo: KnolusTypedValue.TypeInfo<KnolusArray<*>>
        get() = TypeInfo

    override suspend fun asBoolean(context: KnolusContext): Boolean =
        array.isNotEmpty()

    override suspend fun asNumber(context: KnolusContext): Number =
        array.size

    override suspend fun asString(context: KnolusContext): String =
        evaluateOrSelf(context)
            .array
            .map { t -> t.asString(context) }
            .joinToString(prefix = "arrayOf(", postfix = ")") { "\"$it\"" }
}