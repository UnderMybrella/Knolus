package org.abimon.knolus

import org.abimon.knolus.types.KnolusTypedValue
import org.abimon.kornea.errors.common.KorneaResult
import org.abimon.kornea.errors.common.successInline

inline fun <T> KorneaResult.Companion.foldingMutableListOf(list: MutableList<T> = ArrayList()): KorneaResult<MutableList<T>> = success(list)

inline fun <E : KnolusTypedValue, T : KnolusTypedValue.RuntimeValue<E>> KorneaResult.Companion.successVar(value: T): KorneaResult<KnolusUnion.VariableValue<T>> = successInline(KnolusUnion.VariableValue.Lazy(value))
inline fun <T: KnolusTypedValue> KorneaResult.Companion.successVar(value: T): KorneaResult<KnolusUnion.VariableValue<T>> = successInline(KnolusUnion.VariableValue.Stable(value))