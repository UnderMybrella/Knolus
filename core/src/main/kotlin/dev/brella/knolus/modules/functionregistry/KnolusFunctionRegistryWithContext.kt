package dev.brella.knolus.modules.functionregistry

import dev.brella.knolus.KnolusFunction
import dev.brella.knolus.KnolusFunctionBuilder
import dev.brella.knolus.ParameterSpec
import dev.brella.knolus.context.KnolusContext
import dev.brella.knolus.getValue
import dev.brella.knolus.types.KnolusTypedValue
import dev.brella.kornea.errors.common.KorneaResult

fun <T> KnolusFunctionBuilder<T>.setFunctionWithContext(func: suspend (context: KnolusContext) -> T): KnolusFunctionBuilder<T> {
    return setFunction { context: KnolusContext, _: Map<String, KnolusTypedValue> -> func(context) }
}

fun <T> KnolusFunctionBuilder<T?>.setFunctionWithContextWithoutReturn(func: suspend (context: KnolusContext) -> Unit): KnolusFunctionBuilder<T?> {
    return setFunction { context: KnolusContext, _: Map<String, KnolusTypedValue> ->
        func(context)

        null
    }
}

fun <T, P> KnolusFunctionBuilder<T>.setFunctionWithContext(parameterSpec: ParameterSpec<*, P>, func: suspend (context: KnolusContext, parameter: P) -> T): KnolusFunctionBuilder<T> {
    addParameter(parameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val param = parameters.getValue(context, parameterSpec).get()

        func(context, param)
    }
}

fun <T, P> KnolusFunctionBuilder<T?>.setFunctionWithContextWithoutReturn(parameterSpec: ParameterSpec<*, P>, func: suspend (context: KnolusContext, parameter: P) -> Unit): KnolusFunctionBuilder<T?> {
    addParameter(parameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val param = parameters.getValue(context, parameterSpec).get()

        func(context, param)

        null
    }
}

fun <T, P1, P2> KnolusFunctionBuilder<T>.setFunctionWithContext(
    firstParameterSpec: ParameterSpec<*, P1>,
    secondParameterSpec: ParameterSpec<*, P2>,
    func: suspend (context: KnolusContext, firstParameter: P1, secondParameter: P2) -> T
): KnolusFunctionBuilder<T> {
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val firstParam = parameters.getValue(context, firstParameterSpec).get()
        val secondParam = parameters.getValue(context, secondParameterSpec).get()

        func(context, firstParam, secondParam)
    }
}

fun <T, P1, P2> KnolusFunctionBuilder<T?>.setFunctionWithContextWithoutReturn(
    firstParameterSpec: ParameterSpec<*, P1>,
    secondParameterSpec: ParameterSpec<*, P2>,
    func: suspend (context: KnolusContext, firstParameter: P1, secondParameter: P2) -> Unit
): KnolusFunctionBuilder<T?> {
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val firstParam = parameters.getValue(context, firstParameterSpec).get()
        val secondParam = parameters.getValue(context, secondParameterSpec).get()

        func(context, firstParam, secondParam)

        null
    }
}

fun <T, P1, P2, P3> KnolusFunctionBuilder<T>.setFunctionWithContext(
    firstParameterSpec: ParameterSpec<*, P1>,
    secondParameterSpec: ParameterSpec<*, P2>,
    thirdParameterSpec: ParameterSpec<*, P3>,
    func: suspend (context: KnolusContext, firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> T
): KnolusFunctionBuilder<T> {
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)
    addParameter(thirdParameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val firstParam = parameters.getValue(context, firstParameterSpec).get()
        val secondParam = parameters.getValue(context, secondParameterSpec).get()
        val thirdParam = parameters.getValue(context, thirdParameterSpec).get()

        func(context, firstParam, secondParam, thirdParam)
    }
}

fun <T, P1, P2, P3> KnolusFunctionBuilder<T?>.setFunctionWithContextWithoutReturn(
    firstParameterSpec: ParameterSpec<*, P1>,
    secondParameterSpec: ParameterSpec<*, P2>,
    thirdParameterSpec: ParameterSpec<*, P3>,
    func: suspend (context: KnolusContext, firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> Unit
): KnolusFunctionBuilder<T?> {
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)
    addParameter(thirdParameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val firstParam = parameters.getValue(context, firstParameterSpec).get()
        val secondParam = parameters.getValue(context, secondParameterSpec).get()
        val thirdParam = parameters.getValue(context, thirdParameterSpec).get()

        func(context, firstParam, secondParam, thirdParam)

        null
    }
}

fun KnolusContext.registerFunctionWithContext(
    functionName: String,
    func: suspend (context: KnolusContext) -> KnolusTypedValue,
): KorneaResult<KnolusFunction<KnolusTypedValue?>> = register(
    functionName,
    functionBuilder()
        .setFunctionWithContext(func)
        .build()
)

fun KnolusContext.registerFunctionWithContextWithoutReturn(
    functionName: String,
    func: suspend (context: KnolusContext) -> Unit,
) = register(
    functionName,
    functionBuilder()
        .setFunctionWithContextWithoutReturn(func)
        .build()
)

fun <P> KnolusContext.registerFunctionWithContext(
    functionName: String,
    parameterSpec: ParameterSpec<*, P>,
    func: suspend (context: KnolusContext, parameter: P) -> KnolusTypedValue,
) = register(
    functionName,
    functionBuilder()
        .setFunctionWithContext(parameterSpec, func)
        .build()
)

fun <P> KnolusContext.registerFunctionWithContextWithoutReturn(
    functionName: String,
    parameterSpec: ParameterSpec<*, P>,
    func: suspend (context: KnolusContext, parameter: P) -> Unit,
) = register(
    functionName,
    functionBuilder()
        .setFunctionWithContextWithoutReturn(parameterSpec, func)
        .build()
)

fun <P1, P2> KnolusContext.registerFunctionWithContext(
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1>,
    secondParameterSpec: ParameterSpec<*, P2>,
    func: suspend (context: KnolusContext, firstParameter: P1, secondParameter: P2) -> KnolusTypedValue,
) = register(
    functionName,
    functionBuilder()
        .setFunctionWithContext(firstParameterSpec, secondParameterSpec, func)
        .build()
)

fun <P1, P2> KnolusContext.registerFunctionWithContextWithoutReturn(
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1>,
    secondParameterSpec: ParameterSpec<*, P2>,
    func: suspend (context: KnolusContext, firstParameter: P1, secondParameter: P2) -> Unit,
) = register(
    functionName,
    functionBuilder()
        .setFunctionWithContextWithoutReturn(firstParameterSpec, secondParameterSpec, func)
        .build()
)

fun <P1, P2, P3> KnolusContext.registerFunctionWithContext(
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1>,
    secondParameterSpec: ParameterSpec<*, P2>,
    thirdParameterSpec: ParameterSpec<*, P3>,
    func: suspend (context: KnolusContext, firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> KnolusTypedValue,
) = register(
    functionName,
    functionBuilder()
        .setFunctionWithContext(firstParameterSpec, secondParameterSpec, thirdParameterSpec, func)
        .build()
)

fun <P1, P2, P3> KnolusContext.registerFunctionWithContextWithoutReturn(
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1>,
    secondParameterSpec: ParameterSpec<*, P2>,
    thirdParameterSpec: ParameterSpec<*, P3>,
    func: suspend (context: KnolusContext, firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> Unit,
) = register(
    functionName,
    functionBuilder()
        .setFunctionWithContextWithoutReturn(firstParameterSpec, secondParameterSpec, thirdParameterSpec, func)
        .build()
)

fun <R> KnolusContext.registerUntypedFunctionWithContext(
    functionName: String,
    func: suspend (context: KnolusContext) -> KnolusTypedValue,
): KorneaResult<KnolusFunction<KnolusTypedValue?>> = register(
    functionName,
    functionBuilder()
        .setFunctionWithContext(func)
        .build()
)

fun <R> KnolusContext.registerUntypedFunctionWithContextWithoutReturn(
    functionName: String,
    func: suspend (context: KnolusContext) -> Unit,
) = register(
    functionName,
    functionBuilder()
        .setFunctionWithContextWithoutReturn(func)
        .build()
)

/** Getter */

fun <P0> KnolusContext.registerMemberPropertyGetterWithContext(
    typeSpec: ParameterSpec<*, P0>,
    propertyName: String,
    func: suspend (context: KnolusContext, self: P0) -> KnolusTypedValue,
) = register(
    typeSpec.getMemberPropertyGetterName(propertyName),
    functionBuilder()
        .setFunctionWithContext(typeSpec, func)
        .build()
)

fun <T, P> KnolusFunctionBuilder<T>.setResultFunctionWithContext(parameterSpec: ParameterSpec<*, P>, func: suspend(context: KnolusContext, parameter: KorneaResult<P>) -> T): KnolusFunctionBuilder<T> {
    addParameter(parameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val param = parameters.getValue(context, parameterSpec)

        func(context, param)
    }
}

fun <T, P> KnolusFunctionBuilder<T?>.setResultFunctionWithContextWithoutReturn(
    parameterSpec: ParameterSpec<*, P>,
    func: suspend(context: KnolusContext, parameter: KorneaResult<P>) -> Unit
): KnolusFunctionBuilder<T?> {
    addParameter(parameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val param = parameters.getValue(context, parameterSpec)

        func(context, param)

        null
    }
}

fun <T, P1, P2> KnolusFunctionBuilder<T>.setResultFunctionWithContext(
    firstParameterSpec: ParameterSpec<*, P1>,
    secondParameterSpec: ParameterSpec<*, P2>,
    func: suspend(context: KnolusContext, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>) -> T
): KnolusFunctionBuilder<T> {
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val firstParam = parameters.getValue(context, firstParameterSpec)
        val secondParam = parameters.getValue(context, secondParameterSpec)

        func(context, firstParam, secondParam)
    }
}

fun <T, P1, P2> KnolusFunctionBuilder<T?>.setResultFunctionWithContextWithoutReturn(
    firstParameterSpec: ParameterSpec<*, P1>,
    secondParameterSpec: ParameterSpec<*, P2>,
    func: suspend(context: KnolusContext, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>) -> Unit
): KnolusFunctionBuilder<T?> {
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val firstParam = parameters.getValue(context, firstParameterSpec)
        val secondParam = parameters.getValue(context, secondParameterSpec)

        func(context, firstParam, secondParam)

        null
    }
}

fun <T, P1, P2, P3> KnolusFunctionBuilder<T>.setResultFunctionWithContext(
    firstParameterSpec: ParameterSpec<*, P1>,
    secondParameterSpec: ParameterSpec<*, P2>,
    thirdParameterSpec: ParameterSpec<*, P3>,
    func: suspend(context: KnolusContext, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>, thirdParameter: KorneaResult<P3>) -> T
): KnolusFunctionBuilder<T> {
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)
    addParameter(thirdParameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val firstParam = parameters.getValue(context, firstParameterSpec)
        val secondParam = parameters.getValue(context, secondParameterSpec)
        val thirdParam = parameters.getValue(context, thirdParameterSpec)

        func(context, firstParam, secondParam, thirdParam)
    }
}

fun <T, P1, P2, P3> KnolusFunctionBuilder<T?>.setResultFunctionWithContextWithoutReturn(
    firstParameterSpec: ParameterSpec<*, P1>,
    secondParameterSpec: ParameterSpec<*, P2>,
    thirdParameterSpec: ParameterSpec<*, P3>,
    func: suspend(context: KnolusContext, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>, thirdParameter: KorneaResult<P3>) -> Unit
): KnolusFunctionBuilder<T?> {
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)
    addParameter(thirdParameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val firstParam = parameters.getValue(context, firstParameterSpec)
        val secondParam = parameters.getValue(context, secondParameterSpec)
        val thirdParam = parameters.getValue(context, thirdParameterSpec)

        func(context, firstParam, secondParam, thirdParam)

        null
    }
}

fun <P> KnolusContext.registerResultFunctionWithContext(
    functionName: String,
    parameterSpec: ParameterSpec<*, P>,
    func: suspend(context: KnolusContext, parameter: KorneaResult<P>) -> KnolusTypedValue,
) = register(
    functionName,
    functionBuilder()
        .setResultFunctionWithContext(parameterSpec, func)
        .build()
)

fun <P> KnolusContext.registerResultFunctionWithContextWithoutReturn(
    functionName: String,
    parameterSpec: ParameterSpec<*, P>,
    func: suspend(context: KnolusContext, parameter: KorneaResult<P>) -> Unit,
) = register(
    functionName,
    functionBuilder()
        .setResultFunctionWithContextWithoutReturn(parameterSpec, func)
        .build()
)

fun <P1, P2> KnolusContext.registerResultFunctionWithContext(
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1>,
    secondParameterSpec: ParameterSpec<*, P2>,
    func: suspend(context: KnolusContext, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>) -> KnolusTypedValue,
) = register(
    functionName,
    functionBuilder()
        .setResultFunctionWithContext(firstParameterSpec, secondParameterSpec, func)
        .build()
)

fun <P1, P2> KnolusContext.registerResultFunctionWithContextWithoutReturn(
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1>,
    secondParameterSpec: ParameterSpec<*, P2>,
    func: suspend(context: KnolusContext, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>) -> Unit,
) = register(
    functionName,
    functionBuilder()
        .setResultFunctionWithContextWithoutReturn(firstParameterSpec, secondParameterSpec, func)
        .build()
)

fun <P1, P2, P3> KnolusContext.registerResultFunctionWithContext(
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1>,
    secondParameterSpec: ParameterSpec<*, P2>,
    thirdParameterSpec: ParameterSpec<*, P3>,
    func: suspend(context: KnolusContext, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>, thirdParameter: KorneaResult<P3>) -> KnolusTypedValue,
) = register(
    functionName,
    functionBuilder()
        .setResultFunctionWithContext(firstParameterSpec, secondParameterSpec, thirdParameterSpec, func)
        .build()
)

fun <P1, P2, P3> KnolusContext.registerResultFunctionWithContextWithoutReturn(
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1>,
    secondParameterSpec: ParameterSpec<*, P2>,
    thirdParameterSpec: ParameterSpec<*, P3>,
    func: suspend(context: KnolusContext, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>, thirdParameter: KorneaResult<P3>) -> Unit,
) = register(
    functionName,
    functionBuilder()
        .setResultFunctionWithContextWithoutReturn(firstParameterSpec, secondParameterSpec, thirdParameterSpec, func)
        .build()
)