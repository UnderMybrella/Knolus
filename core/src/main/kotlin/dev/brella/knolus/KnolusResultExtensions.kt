package dev.brella.knolus

import dev.brella.knolus.types.KnolusTypedValue
import dev.brella.kornea.errors.common.KorneaResult

inline fun <E : KnolusTypedValue, T : KnolusTypedValue.RuntimeValue<E>> KorneaResult.Companion.successVar(value: T): KorneaResult<KnolusUnion.VariableValue<T>> = success(KnolusUnion.VariableValue.Lazy(value), null)
inline fun <T: KnolusTypedValue> KorneaResult.Companion.successVar(value: T): KorneaResult<KnolusUnion.VariableValue<T>> = success(KnolusUnion.VariableValue.Stable(value), null)