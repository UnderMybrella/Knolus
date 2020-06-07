package org.abimon.knolus

@ExperimentalUnsignedTypes
class KnolusFunction<T>(vararg val parameters: Pair<String, VariableValue?>, val variadicSupported: Boolean = false, val func: suspend (context: KnolusContext, parameters: Map<String, VariableValue>) -> T) {
    suspend fun suspendInvoke(context: KnolusContext, parameters: Map<String, VariableValue>) = func(context, parameters)
}

class KnolusFunctionBuilder<T> {
    val parameters: MutableList<Pair<String, VariableValue?>> =
        ArrayList()
    var variadicSupported = false
    lateinit var func: suspend (context: KnolusContext, parameters: Map<String, VariableValue>) -> T

    fun addParameter(name: String, default: VariableValue? = null): KnolusFunctionBuilder<T> {
        parameters.add(Pair(name.sanitiseFunctionIdentifier(), default))

        return this
    }

    fun addFlag(name: String, default: Boolean = false): KnolusFunctionBuilder<T> = addParameter(name,
        VariableValue.BooleanType(default)
    )

    fun setFunction(func: suspend (context: KnolusContext, parameters: Map<String, VariableValue>) -> T): KnolusFunctionBuilder<T> {
        this.func = func

        return this
    }

    fun build() = KnolusFunction(
        *parameters.toTypedArray(),
        variadicSupported = variadicSupported,
        func = func
    )
}

fun <T> KnolusFunctionBuilder<T>.setFunction(parameterName: String, func: suspend (context: KnolusContext, parameter: VariableValue) -> T): KnolusFunctionBuilder<T> {
    addParameter(parameterName)

    return setFunction { context: KnolusContext, parameters: Map<String, VariableValue> ->
        val param = parameters.getValue(parameterName.sanitiseFunctionIdentifier())

        func(context, param)
    }
}

fun <T> KnolusFunctionBuilder<T?>.setFunctionWithoutReturn(parameterName: String, func: suspend (context: KnolusContext, parameter: VariableValue) -> Unit): KnolusFunctionBuilder<T?> {
    addParameter(parameterName)

    return setFunction { context: KnolusContext, parameters: Map<String, VariableValue> ->
        val param = parameters.getValue(parameterName.sanitiseFunctionIdentifier())

        func(context, param)

        null
    }
}

fun <T, P> KnolusFunctionBuilder<T>.setFunction(parameterSpec: ParameterSpec<P>, func: suspend (context: KnolusContext, parameter: P) -> T): KnolusFunctionBuilder<T> {
    addParameter(parameterSpec.name)

    return setFunction { context: KnolusContext, parameters: Map<String, VariableValue> ->
        val param = parameters.getValue(context, parameterSpec)

        func(context, param)
    }
}

fun <T, P> KnolusFunctionBuilder<T?>.setFunctionWithoutReturn(parameterSpec: ParameterSpec<P>, func: suspend (context: KnolusContext, parameter: P) -> Unit): KnolusFunctionBuilder<T?> {
    addParameter(parameterSpec.name)

    return setFunction { context: KnolusContext, parameters: Map<String, VariableValue> ->
        val param = parameters.getValue(context, parameterSpec)

        func(context, param)

        null
    }
}

fun <T> KnolusFunctionBuilder<T>.setFunction(firstParameterName: String, secondParameterName: String, func: suspend (context: KnolusContext, firstParameter: VariableValue, secondParameter: VariableValue) -> T): KnolusFunctionBuilder<T> {
    addParameter(firstParameterName)
    addParameter(secondParameterName)

    return setFunction { context: KnolusContext, parameters: Map<String, VariableValue> ->
        val firstParam = parameters.getValue(firstParameterName.sanitiseFunctionIdentifier())
        val secondParam = parameters.getValue(secondParameterName.sanitiseFunctionIdentifier())

        func(context, firstParam, secondParam)
    }
}

fun <T> KnolusFunctionBuilder<T?>.setFunctionWithoutReturn(firstParameterName: String, secondParameterName: String, func: suspend (context: KnolusContext, firstParameter: VariableValue, secondParameter: VariableValue) -> Unit): KnolusFunctionBuilder<T?> {
    addParameter(firstParameterName)
    addParameter(secondParameterName)

    return setFunction { context: KnolusContext, parameters: Map<String, VariableValue> ->
        val firstParam = parameters.getValue(firstParameterName.sanitiseFunctionIdentifier())
        val secondParam = parameters.getValue(secondParameterName.sanitiseFunctionIdentifier())

        func(context, firstParam, secondParam)

        null
    }
}

fun <T, P1, P2> KnolusFunctionBuilder<T>.setFunction(firstParameterSpec: ParameterSpec<P1>, secondParameterSpec: ParameterSpec<P2>, func: suspend (context: KnolusContext, firstParameter: P1, secondParameter: P2) -> T): KnolusFunctionBuilder<T> {
    addParameter(firstParameterSpec.name)
    addParameter(secondParameterSpec.name)

    return setFunction { context: KnolusContext, parameters: Map<String, VariableValue> ->
        val firstParam = parameters.getValue(context, firstParameterSpec)
        val secondParam = parameters.getValue(context, secondParameterSpec)

        func(context, firstParam, secondParam)
    }
}

fun <T, P1, P2> KnolusFunctionBuilder<T?>.setFunctionWithoutReturn(firstParameterSpec: ParameterSpec<P1>, secondParameterSpec: ParameterSpec<P2>, func: suspend (context: KnolusContext, firstParameter: P1, secondParameter: P2) -> Unit): KnolusFunctionBuilder<T?> {
    addParameter(firstParameterSpec.name)
    addParameter(secondParameterSpec.name)

    return setFunction { context: KnolusContext, parameters: Map<String, VariableValue> ->
        val firstParam = parameters.getValue(context, firstParameterSpec)
        val secondParam = parameters.getValue(context, secondParameterSpec)

        func(context, firstParam, secondParam)

        null
    }
}

fun <T> KnolusFunctionBuilder<T>.setFunction(firstParameterName: String, secondParameterName: String, thirdParameterName: String, func: suspend (context: KnolusContext, firstParameter: VariableValue, secondParameter: VariableValue, thirdParameter: VariableValue) -> T): KnolusFunctionBuilder<T> {
    addParameter(firstParameterName)
    addParameter(secondParameterName)
    addParameter(thirdParameterName)

    return setFunction { context: KnolusContext, parameters: Map<String, VariableValue> ->
        val firstParam = parameters.getValue(firstParameterName.sanitiseFunctionIdentifier())
        val secondParam = parameters.getValue(secondParameterName.sanitiseFunctionIdentifier())
        val thirdParam = parameters.getValue(thirdParameterName.sanitiseFunctionIdentifier())

        func(context, firstParam, secondParam, thirdParam)
    }
}

fun <T> KnolusFunctionBuilder<T?>.setFunctionWithoutReturn(firstParameterName: String, secondParameterName: String, thirdParameterName: String, func: suspend (context: KnolusContext, firstParameter: VariableValue, secondParameter: VariableValue, thirdParameter: VariableValue) -> Unit): KnolusFunctionBuilder<T?> {
    addParameter(firstParameterName)
    addParameter(secondParameterName)
    addParameter(thirdParameterName)

    return setFunction { context: KnolusContext, parameters: Map<String, VariableValue> ->
        val firstParam = parameters.getValue(firstParameterName.sanitiseFunctionIdentifier())
        val secondParam = parameters.getValue(secondParameterName.sanitiseFunctionIdentifier())
        val thirdParam = parameters.getValue(thirdParameterName.sanitiseFunctionIdentifier())

        func(context, firstParam, secondParam, thirdParam)

        null
    }
}

fun <T, P1, P2, P3> KnolusFunctionBuilder<T>.setFunction(firstParameterSpec: ParameterSpec<P1>, secondParameterSpec: ParameterSpec<P2>, thirdParameterSpec: ParameterSpec<P3>, func: suspend (context: KnolusContext, firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> T): KnolusFunctionBuilder<T> {
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)
    addParameter(thirdParameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, VariableValue> ->
        val firstParam = parameters.getValue(context, firstParameterSpec)
        val secondParam = parameters.getValue(context, secondParameterSpec)
        val thirdParam = parameters.getValue(context, thirdParameterSpec)

        func(context, firstParam, secondParam, thirdParam)
    }
}

fun <T, P1, P2, P3> KnolusFunctionBuilder<T?>.setFunctionWithoutReturn(firstParameterSpec: ParameterSpec<P1>, secondParameterSpec: ParameterSpec<P2>, thirdParameterSpec: ParameterSpec<P3>, func: suspend (context: KnolusContext, firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> Unit): KnolusFunctionBuilder<T?> {
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)
    addParameter(thirdParameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, VariableValue> ->
        val firstParam = parameters.getValue(context, firstParameterSpec)
        val secondParam = parameters.getValue(context, secondParameterSpec)
        val thirdParam = parameters.getValue(context, thirdParameterSpec)

        func(context, firstParam, secondParam, thirdParam)

        null
    }
}