package dev.brella.knolus.modules.functionregistry

import dev.brella.knolus.*
import dev.brella.knolus.context.KnolusContext
import dev.brella.knolus.types.KnolusTypedValue
import dev.brella.kornea.errors.common.KorneaResult


fun <T, P0> KnolusFunctionBuilder<T>.setMemberFunctionWithContext(typeSpec: ParameterSpec<*, P0>, func: suspend (context: KnolusContext, self: P0) -> T): KnolusFunctionBuilder<T> {
    addParameter(typeSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec).get()

        func(context, self)
    }
}

fun <T, P0> KnolusFunctionBuilder<T?>.setMemberFunctionWithContextWithoutReturn(typeSpec: ParameterSpec<*, P0>, func: suspend (context: KnolusContext, self: P0) -> Unit): KnolusFunctionBuilder<T?> {
    addParameter(typeSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec).get()

        func(context, self)

        null
    }
}

fun <T, P0, P1> KnolusFunctionBuilder<T>.setMemberFunctionWithContext(typeSpec: ParameterSpec<*, P0>, parameterSpec: ParameterSpec<*, P1>, func: suspend (context: KnolusContext, self: P0, parameter: P1) -> T): KnolusFunctionBuilder<T> {
    addParameter(typeSpec)
    addParameter(parameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec).get()
        val param = parameters.getValue(context, parameterSpec).get()

        func(context, self, param)
    }
}

fun <T, P0, P1> KnolusFunctionBuilder<T?>.setMemberFunctionWithContextWithoutReturn(typeSpec: ParameterSpec<*, P0>, parameterSpec: ParameterSpec<*, P1>, func: suspend (context: KnolusContext, self: P0, parameter: P1) -> Unit): KnolusFunctionBuilder<T?> {
    addParameter(typeSpec)
    addParameter(parameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec).get()
        val param = parameters.getValue(context, parameterSpec).get()

        func(context, self, param)

        null
    }
}

fun <T, P0, P1, P2> KnolusFunctionBuilder<T>.setMemberFunctionWithContext(typeSpec: ParameterSpec<*, P0>, firstParameterSpec: ParameterSpec<*, P1>, secondParameterSpec: ParameterSpec<*, P2>, func: suspend (context: KnolusContext, self: P0, firstParameter: P1, secondParameter: P2) -> T): KnolusFunctionBuilder<T> {
    addParameter(typeSpec)
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec).get()
        val firstParam = parameters.getValue(context, firstParameterSpec).get()
        val secondParam = parameters.getValue(context, secondParameterSpec).get()

        func(context, self, firstParam, secondParam)
    }
}

fun <T, P0, P1, P2> KnolusFunctionBuilder<T?>.setMemberFunctionWithContextWithoutReturn(typeSpec: ParameterSpec<*, P0>, firstParameterSpec: ParameterSpec<*, P1>, secondParameterSpec: ParameterSpec<*, P2>, func: suspend (context: KnolusContext, self: P0, firstParameter: P1, secondParameter: P2) -> Unit): KnolusFunctionBuilder<T?> {
    addParameter(typeSpec)
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec).get()
        val firstParam = parameters.getValue(context, firstParameterSpec).get()
        val secondParam = parameters.getValue(context, secondParameterSpec).get()

        func(context, self, firstParam, secondParam)

        null
    }
}

fun <T, P0, P1, P2, P3> KnolusFunctionBuilder<T>.setMemberFunctionWithContext(typeSpec: ParameterSpec<*, P0>, firstParameterSpec: ParameterSpec<*, P1>, secondParameterSpec: ParameterSpec<*, P2>, thirdParameterSpec: ParameterSpec<*, P3>, func: suspend (context: KnolusContext, self: P0, firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> T): KnolusFunctionBuilder<T> {
    addParameter(typeSpec)
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)
    addParameter(thirdParameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec).get()
        val firstParam = parameters.getValue(context, firstParameterSpec).get()
        val secondParam = parameters.getValue(context, secondParameterSpec).get()
        val thirdParam = parameters.getValue(context, thirdParameterSpec).get()

        func(context, self, firstParam, secondParam, thirdParam)
    }
}

fun <T, P0, P1, P2, P3> KnolusFunctionBuilder<T?>.setMemberFunctionWithContextWithoutReturn(typeSpec: ParameterSpec<*, P0>, firstParameterSpec: ParameterSpec<*, P1>, secondParameterSpec: ParameterSpec<*, P2>, thirdParameterSpec: ParameterSpec<*, P3>, func: suspend (context: KnolusContext, self: P0, firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> Unit): KnolusFunctionBuilder<T?> {
    addParameter(typeSpec)
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)
    addParameter(thirdParameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec).get()
        val firstParam = parameters.getValue(context, firstParameterSpec).get()
        val secondParam = parameters.getValue(context, secondParameterSpec).get()
        val thirdParam = parameters.getValue(context, thirdParameterSpec).get()

        func(context, self, firstParam, secondParam, thirdParam)

        null
    }
}

fun <P0> KnolusContext.registerMemberFunctionWithContext(
    typeSpec: ParameterSpec<*, P0>,
    functionName: String,
    func: suspend (context: KnolusContext, self: P0) -> KnolusTypedValue,
) = register(
    typeSpec.getMemberFunctionName(functionName),
    functionBuilder()
        .setMemberFunctionWithContext(typeSpec, func)
        .build()
)

fun <P0> KnolusContext.registerMemberFunctionWithContextWithoutReturn(
    typeSpec: ParameterSpec<*, P0>,
    functionName: String,
    func: suspend (context: KnolusContext, self: P0) -> Unit,
) = register(
    typeSpec.getMemberFunctionName(functionName),
    functionBuilder()
        .setMemberFunctionWithContextWithoutReturn(typeSpec, func)
        .build()
)

fun <P0, P1> KnolusContext.registerMemberFunctionWithContext(
    typeSpec: ParameterSpec<*, P0>,
    functionName: String,
    parameterSpec: ParameterSpec<*, P1>,
    func: suspend (context: KnolusContext, self: P0, parameter: P1) -> KnolusTypedValue,
) = register(
    typeSpec.getMemberFunctionName(functionName),
    functionBuilder()
        .setMemberFunctionWithContext(typeSpec, parameterSpec, func)
        .build()
)

fun <P0, P1> KnolusContext.registerMemberFunctionWithContextWithoutReturn(
    typeSpec: ParameterSpec<*, P0>,
    functionName: String,
    parameterSpec: ParameterSpec<*, P1>,
    func: suspend (context: KnolusContext, self: P0, parameter: P1) -> Unit,
) = register(
    typeSpec.getMemberFunctionName(functionName),
    functionBuilder()
        .setMemberFunctionWithContextWithoutReturn(typeSpec, parameterSpec, func)
        .build()
)

fun <P0, P1, P2> KnolusContext.registerMemberFunctionWithContext(
    typeSpec: ParameterSpec<*, P0>,
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1>,
    secondParameterSpec: ParameterSpec<*, P2>,
    func: suspend (context: KnolusContext, self: P0, firstParameter: P1, secondParameter: P2) -> KnolusTypedValue,
) = register(
    typeSpec.getMemberFunctionName(functionName),
    functionBuilder()
        .setMemberFunctionWithContext(typeSpec, firstParameterSpec, secondParameterSpec, func)
        .build()
)

fun <P0, P1, P2> KnolusContext.registerMemberFunctionWithContextWithoutReturn(
    typeSpec: ParameterSpec<*, P0>,
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1>,
    secondParameterSpec: ParameterSpec<*, P2>,
    func: suspend (context: KnolusContext, self: P0, firstParameter: P1, secondParameter: P2) -> Unit,
) = register(
    typeSpec.getMemberFunctionName(functionName),
    functionBuilder()
        .setMemberFunctionWithContextWithoutReturn(typeSpec, firstParameterSpec, secondParameterSpec, func)
        .build()
)

/** Result Functions */

fun <T, P0, P1> KnolusFunctionBuilder<T>.setMemberResultFunctionWithContext(typeSpec: ParameterSpec<*, P0>, parameterSpec: ParameterSpec<*, P1>, func: suspend (context: KnolusContext, self: P0, parameter: KorneaResult<P1>) -> T): KnolusFunctionBuilder<T> {
    addParameter(typeSpec)
    addParameter(parameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec).get()
        val param = parameters.getValue(context, parameterSpec)

        func(context, self, param)
    }
}

fun <T, P0, P1> KnolusFunctionBuilder<T?>.setMemberResultFunctionWithContextWithoutReturn(typeSpec: ParameterSpec<*, P0>, parameterSpec: ParameterSpec<*, P1>, func: suspend (context: KnolusContext, self: P0, parameter: KorneaResult<P1>) -> Unit): KnolusFunctionBuilder<T?> {
    addParameter(typeSpec)
    addParameter(parameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec).get()
        val param = parameters.getValue(context, parameterSpec)

        func(context, self, param)

        null
    }
}

fun <T, P0, P1, P2> KnolusFunctionBuilder<T>.setMemberResultFunctionWithContext(typeSpec: ParameterSpec<*, P0>, firstParameterSpec: ParameterSpec<*, P1>, secondParameterSpec: ParameterSpec<*, P2>, func: suspend (context: KnolusContext, self: P0, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>) -> T): KnolusFunctionBuilder<T> {
    addParameter(typeSpec)
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec).get()
        val firstParam = parameters.getValue(context, firstParameterSpec)
        val secondParam = parameters.getValue(context, secondParameterSpec)

        func(context, self, firstParam, secondParam)
    }
}

fun <T, P0, P1, P2> KnolusFunctionBuilder<T?>.setMemberResultFunctionWithContextWithoutReturn(typeSpec: ParameterSpec<*, P0>, firstParameterSpec: ParameterSpec<*, P1>, secondParameterSpec: ParameterSpec<*, P2>, func: suspend (context: KnolusContext, self: P0, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>) -> Unit): KnolusFunctionBuilder<T?> {
    addParameter(typeSpec)
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec).get()
        val firstParam = parameters.getValue(context, firstParameterSpec)
        val secondParam = parameters.getValue(context, secondParameterSpec)

        func(context, self, firstParam, secondParam)

        null
    }
}

fun <T, P0, P1, P2, P3> KnolusFunctionBuilder<T>.setMemberResultFunctionWithContext(typeSpec: ParameterSpec<*, P0>, firstParameterSpec: ParameterSpec<*, P1>, secondParameterSpec: ParameterSpec<*, P2>, thirdParameterSpec: ParameterSpec<*, P3>, func: suspend (context: KnolusContext, self: P0, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>, thirdParameter: KorneaResult<P3>) -> T): KnolusFunctionBuilder<T> {
    addParameter(typeSpec)
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)
    addParameter(thirdParameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec).get()
        val firstParam = parameters.getValue(context, firstParameterSpec)
        val secondParam = parameters.getValue(context, secondParameterSpec)
        val thirdParam = parameters.getValue(context, thirdParameterSpec)

        func(context, self, firstParam, secondParam, thirdParam)
    }
}

fun <T, P0, P1, P2, P3> KnolusFunctionBuilder<T?>.setMemberResultFunctionWithContextWithoutReturn(typeSpec: ParameterSpec<*, P0>, firstParameterSpec: ParameterSpec<*, P1>, secondParameterSpec: ParameterSpec<*, P2>, thirdParameterSpec: ParameterSpec<*, P3>, func: suspend (context: KnolusContext, self: P0, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>, thirdParameter: KorneaResult<P3>) -> Unit): KnolusFunctionBuilder<T?> {
    addParameter(typeSpec)
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)
    addParameter(thirdParameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec).get()
        val firstParam = parameters.getValue(context, firstParameterSpec)
        val secondParam = parameters.getValue(context, secondParameterSpec)
        val thirdParam = parameters.getValue(context, thirdParameterSpec)

        func(context, self, firstParam, secondParam, thirdParam)

        null
    }
}

fun <P0, P1> KnolusContext.registerMemberResultFunctionWithContext(
    typeSpec: ParameterSpec<*, P0>,
    functionName: String,
    parameterSpec: ParameterSpec<*, P1>,
    func: suspend (context: KnolusContext, self: P0, parameter: KorneaResult<P1>) -> KnolusTypedValue,
) = register(
    typeSpec.getMemberFunctionName(functionName),
    functionBuilder()
        .setMemberResultFunctionWithContext(typeSpec, parameterSpec, func)
        .build()
)

fun <P0, P1> KnolusContext.registerMemberResultFunctionWithContextWithoutReturn(
    typeSpec: ParameterSpec<*, P0>,
    functionName: String,
    parameterSpec: ParameterSpec<*, P1>,
    func: suspend (context: KnolusContext, self: P0, parameter: KorneaResult<P1>) -> Unit,
) = register(
    typeSpec.getMemberFunctionName(functionName),
    functionBuilder()
        .setMemberResultFunctionWithContextWithoutReturn(typeSpec, parameterSpec, func)
        .build()
)

fun <P0, P1, P2> KnolusContext.registerMemberResultFunctionWithContext(
    typeSpec: ParameterSpec<*, P0>,
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1>,
    secondParameterSpec: ParameterSpec<*, P2>,
    func: suspend (context: KnolusContext, self: P0, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>) -> KnolusTypedValue,
) = register(
    typeSpec.getMemberFunctionName(functionName),
    functionBuilder()
        .setMemberResultFunctionWithContext(typeSpec, firstParameterSpec, secondParameterSpec, func)
        .build()
)

fun <P0, P1, P2> KnolusContext.registerMemberResultFunctionWithContextWithoutReturn(
    typeSpec: ParameterSpec<*, P0>,
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1>,
    secondParameterSpec: ParameterSpec<*, P2>,
    func: suspend (context: KnolusContext, self: P0, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>) -> Unit,
) = register(
    typeSpec.getMemberFunctionName(functionName),
    functionBuilder()
        .setMemberResultFunctionWithContextWithoutReturn(typeSpec, firstParameterSpec, secondParameterSpec, func)
        .build()
)