package org.abimon.knolus

import kotlin.reflect.KProperty

object UNINITIALIZED_VALUE

class OneTimeMutable<T> {
    private var _value: Any? = UNINITIALIZED_VALUE
    @Suppress("UNCHECKED_CAST")
    var value: T
        get() = _value as? T ?: throw IllegalStateException("Value not initialised")
        set(value) {
            if (_value === UNINITIALIZED_VALUE) {
                _value = value
            } else {
                throw IllegalStateException("Value already initialised")
            }
        }

    val isInitialised: Boolean
        get() = _value !== UNINITIALIZED_VALUE
}

public inline operator fun <T> OneTimeMutable<T>.getValue(thisRef: Any?, property: KProperty<*>): T = value
public inline operator fun <T> OneTimeMutable<T>.setValue(thisRef: Any?, property: KProperty<*>, value: T) {
    this.value = value
}

public inline fun <reified T> oneTimeMutable(): OneTimeMutable<T> = OneTimeMutable()