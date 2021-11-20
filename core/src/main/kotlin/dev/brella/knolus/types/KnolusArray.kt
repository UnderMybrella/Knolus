package dev.brella.knolus.types

import dev.brella.knolus.context.KnolusContext
import dev.brella.kornea.errors.common.*

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

        suspend fun <T> KnolusArray<*>.evaluateOrSelf(context: KnolusContext): KorneaResult<KnolusArray<*>> =
            when (this) {
                is RuntimeArray<*> -> evaluate(context)
                is UnsureArray<*> -> if (needsEvaluation(context)) evaluate(context) else KorneaResult.success(this)
                else -> KorneaResult.success(this)
            }

        override fun isInstance(instance: Any?): Boolean = instance is KnolusArray<*>
        override fun asInstance(instance: Any?): KnolusArray<*> = instance as KnolusArray<*>
        override fun asInstanceSafe(instance: Any?): KnolusArray<*>? = instance as? KnolusArray<*>
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
        override suspend fun needsEvaluation(context: KnolusContext): Boolean =
            array.any { t -> t is KnolusTypedValue.UnsureValue<*> && t.needsEvaluation(context) }

        override suspend fun evaluate(context: KnolusContext): KorneaResult<KnolusArray<KnolusTypedValue>> {
            val initial: KorneaResult<Array<KnolusTypedValue?>> = KorneaResult.success(arrayOfNulls(array.size))

            return array.indices.fold(initial) { acc, i ->
                acc.flatMap { evalArray ->
                    val element = this.array[i]

                    if (element is KnolusTypedValue.UnsureValue<*>) {
                        element.evaluateOrSelf(context)
                            .map { value ->
                                evalArray[i] = value
                                evalArray
                            }
                    } else {
                        evalArray[i] = element

                        KorneaResult.success(evalArray)
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
        override suspend fun evaluate(context: KnolusContext): KorneaResult<KnolusArray<KnolusTypedValue>> {
            val initial: KorneaResult<Array<KnolusTypedValue?>> = KorneaResult.success(arrayOfNulls(array.size))

            return array.indices.fold(initial) { acc, i ->
                acc.flatMap { evalArray ->
                    val element = this.array[i]

                    if (element is KnolusTypedValue.UnsureValue<*>) {
                        element.evaluateOrSelf(context)
                            .map { value ->
                                evalArray[i] = value
                                evalArray
                            }
                    } else {
                        evalArray[i] = element

                        KorneaResult.success(evalArray)
                    }
                }
            }.map { evalArray -> of(evalArray.requireNoNulls()) }
        }

//        override suspend fun <T, R: KnolusTypedValue, I : KnolusTypedValue.TypeInfo<R>> asTypeImpl(context: KnolusContext<T>, typeInfo: I): KorneaResult<R> =
//            super<KnolusTypedValue.RuntimeValue>.asTypeImpl(context, typeInfo)

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

//    override suspend fun <T, R, I : KnolusTypedValue.TypeInfo<R>> asType(context: KnolusContext<T>, typeInfo: I): KorneaResult<R> = KorneaResult.empty()
}