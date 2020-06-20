package org.abimon.knolus.transform

import java.lang.reflect.Field
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0

interface TField<in S, out T> {
    operator fun get(instance: S): T
}

interface TMField<in S, T>: TField<S, T> {
    operator fun set(instance: S, value: T)
}

@Suppress("UNCHECKED_CAST")
inline class TransMemberField<in S, out T>(val backingField: Field): TField<S, T>, ReadOnlyProperty<S, T> {
    override operator fun get(instance: S): T = backingField[instance] as T
    override fun getValue(thisRef: S, property: KProperty<*>): T = get(thisRef)
}

@Suppress("UNCHECKED_CAST")
inline class TransMemberMutableField<in S, T>(val backingField: Field): TMField<S, T>, ReadWriteProperty<S, T> {
    override operator fun get(instance: S): T = backingField[instance] as T
    override operator fun set(instance: S, value: T) {
        backingField[instance] = value
    }

    override fun getValue(thisRef: S, property: KProperty<*>): T = get(thisRef)
    override fun setValue(thisRef: S, property: KProperty<*>, value: T) = set(thisRef, value)
}

inline fun <S: Any, T> Class<out S>.getTypedField(name: String): TransMemberField<S, T> = TransMemberField(getField(name))
inline fun <S: Any, T> Class<out S>.getTypedMutableField(name: String): TransMemberMutableField<S, T> = TransMemberMutableField(getField(name))

inline fun <T> Class<*>.getLooselyTypedField(name: String): TransMemberField<Any?, T> = TransMemberField(getField(name))
inline fun <T> Class<*>.getLooselyTypedMutableField(name: String): TransMemberMutableField<Any?, T> = TransMemberMutableField(getField(name))

inline fun <S: Any, T> field(ref: KProperty0<Class<out S>>, fieldName: String) = lazy { TransMemberField<S, T>(ref.get().getField(fieldName)) }
inline fun <S: Any, T> mutableField(ref: KProperty0<Class<out S>>, fieldName: String) = lazy { TransMemberMutableField<S, T>(ref.get().getField(fieldName)) }

inline fun <T> looseField(ref: KProperty0<Class<out Any?>>, fieldName: String) = lazy { TransMemberField<Any?, T>(ref.get().getField(fieldName)) }
inline fun <T> looseMutableField(ref: KProperty0<Class<out Any?>>, fieldName: String) = lazy { TransMemberMutableField<Any?, T>(ref.get().getField(fieldName)) }