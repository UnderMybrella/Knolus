package org.abimon.knolus.types

import org.abimon.knolus.*
import org.abimon.knolus.context.KnolusContext
import org.abimon.knolus.types.KnolusArray.TypeInfo.evaluateOrSelf

sealed class KnolusArray<T : KnolusTypedValue>(open val array: Array<T>) : KnolusTypedValue {
    companion object TypeInfo : KnolusTypedValue.TypeInfo<KnolusArray<*>> {
        override val typeHierarchicalNames: Array<String> = arrayOf("Array", "Object")

        fun of(array: Array<KnolusTypedValue>): KnolusArray<KnolusTypedValue> {
            var unsure: Boolean = false
            array.forEach { value ->
                if (value is KnolusTypedValue.RuntimeValue<*>) return RuntimeArray(array)
                if (!unsure && value is KnolusTypedValue.UnsureValue<*>) unsure = true
            }

            if (unsure) return UnsureArray(array)
            return StableArray(array)
        }

        fun <T : KnolusTypedValue> ofStable(array: Array<T>): KnolusArray<T> = StableArray(array)
        fun <T : KnolusTypedValue.UnsureValue<*>> ofUnsure(array: Array<T>): KnolusArray<T> = UnsureArray(array)

        suspend fun <T> KnolusArray<*>.evaluateOrSelf(context: KnolusContext<T>): KnolusResult<KnolusArray<*>> =
            when (this) {
                is RuntimeArray<*> -> evaluate(context)
                is UnsureArray<*> -> if (needsEvaluation(context)) evaluate(context) else KnolusResult.knolusTyped(this)
                else -> KnolusResult.knolusTyped(this)
            }

        override fun isInstance(value: KnolusTypedValue): Boolean = value is KnolusArray<*>
    }

    private data class StableArray<T : KnolusTypedValue>(override val array: Array<T>) : KnolusArray<T>(array) {
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
            require(array.none { t -> t is KnolusTypedValue.UnsureValue<*> || t is KnolusTypedValue.RuntimeValue<*> })
        }
    }

    private data class UnsureArray<T : KnolusTypedValue>(override val array: Array<T>) :
        KnolusArray<T>(array), KnolusTypedValue.UnsureValue<KnolusArray<KnolusTypedValue>> {
        override suspend fun <T> needsEvaluation(context: KnolusContext<T>): Boolean =
            array.any { t -> t is KnolusTypedValue.UnsureValue<*> && t.needsEvaluation(context) }

        override suspend fun <T> evaluate(context: KnolusContext<T>): KnolusResult<KnolusArray<KnolusTypedValue>> {
            val initial: KnolusResult<Array<KnolusTypedValue?>> = KnolusResult.success(arrayOfNulls(array.size))

            return array.indices.fold(initial) { acc, i ->
                acc.flatMapOrSelf { evalArray ->
                    val element = this.array[i]

                    if (element is KnolusTypedValue.UnsureValue<*>) {
                        element.evaluateOrSelf(context)
                            .doOnSuccess { value -> evalArray[i] = value }
                            .cast()
                    } else {
                        evalArray[i] = element

                        null
                    }
                }
            }.map { evalArray -> of(evalArray.requireNoNulls()) }
        }

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

    private data class RuntimeArray<T : KnolusTypedValue>(override val array: Array<T>) : KnolusArray<T>(array),
        KnolusTypedValue.RuntimeValue<KnolusArray<KnolusTypedValue>> {
        override suspend fun <T> evaluate(context: KnolusContext<T>): KnolusResult<KnolusArray<KnolusTypedValue>> {
            val initial: KnolusResult<Array<KnolusTypedValue?>> = KnolusResult.success(arrayOfNulls(array.size))

            return array.indices.fold(initial) { acc, i ->
                acc.flatMapOrSelf { evalArray ->
                    val element = this.array[i]

                    if (element is KnolusTypedValue.UnsureValue<*>) {
                        element.evaluateOrSelf(context)
                            .doOnSuccess { value -> evalArray[i] = value }
                            .cast()
                    } else {
                        evalArray[i] = element

                        null
                    }
                }
            }.map { evalArray -> of(evalArray.requireNoNulls()) }
        }

        override suspend fun <T> asBoolean(context: KnolusContext<T>): KnolusResult<Boolean> = super<KnolusTypedValue.RuntimeValue>.asBoolean(context)

        override suspend fun <T> asNumber(context: KnolusContext<T>): KnolusResult<Number> = super<KnolusTypedValue.RuntimeValue>.asNumber(context)

        override suspend fun <T> asString(context: KnolusContext<T>): KnolusResult<String> = super<KnolusTypedValue.RuntimeValue>.asString(context)

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

    override suspend fun <T> asBoolean(context: KnolusContext<T>): KnolusResult<Boolean> =
        KnolusResult.success(array.isNotEmpty())

    override suspend fun <T> asNumber(context: KnolusContext<T>): KnolusResult<Number> =
        KnolusResult.success(array.size)

    override suspend fun <T> asString(context: KnolusContext<T>): KnolusResult<String> {
        val initial = KnolusResult.success(StringBuilder().apply {
            append("arrayOf(")
        })

        return array.indices.fold(initial) { acc, i ->
            acc.flatMapOrSelf { builder ->
                if (i > 0) builder.append(", ")

                array[i].asString(context)
                    .doOnSuccess(builder::append)
                    .cast()
            }
        }.map { builder ->
            builder.append(")")

            builder.toString()
        }
    }
}