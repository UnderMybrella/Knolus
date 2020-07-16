package dev.brella.knolus

import dev.brella.knolus.types.KnolusTypedValue
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.successInline

inline fun <T> KorneaResult.Companion.foldingMutableListOf(list: MutableList<T> = ArrayList()): KorneaResult<MutableList<T>> = success(list)

inline fun <E : KnolusTypedValue, T : KnolusTypedValue.RuntimeValue<E>> KorneaResult.Companion.successVar(value: T): KorneaResult<KnolusUnion.VariableValue<T>> = successInline(KnolusUnion.VariableValue.Lazy(value))
inline fun <T: KnolusTypedValue> KorneaResult.Companion.successVar(value: T): KorneaResult<KnolusUnion.VariableValue<T>> = successInline(KnolusUnion.VariableValue.Stable(value))