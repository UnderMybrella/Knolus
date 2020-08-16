@file:Suppress("EXPERIMENTAL_API_USAGE")

package dev.brella.knolus.modules.functionregistry

import dev.brella.knolus.*
import dev.brella.knolus.context.KnolusContext
import dev.brella.knolus.types.KnolusTypedValue
import dev.brella.kornea.annotations.AvailableSince
import dev.brella.kornea.errors.common.KorneaResult

//<T(.*?)> KnolusFunctionBuilder<(T\??)>
//<T, R$1> KnolusFunctionBuilder<$2, R>

inline fun functionBuilder() = KnolusFunctionBuilder<KnolusTypedValue?>()
inline fun <T: KnolusTypedValue?> typedFunctionBuilder() = KnolusFunctionBuilder<T>()

fun <T> KnolusFunctionBuilder<T>.setFunction(func: suspend () -> T): KnolusFunctionBuilder<T> {
    return setFunction { context: KnolusContext, _: Map<String, KnolusTypedValue> -> func() }
}

fun <T> KnolusFunctionBuilder<T?>.setFunctionWithoutReturn(func: suspend () -> Unit): KnolusFunctionBuilder<T?> {
    return setFunction { context: KnolusContext, _: Map<String, KnolusTypedValue> ->
        func()

        null
    }
}

fun <T, P> KnolusFunctionBuilder<T>.setFunction(parameterSpec: ParameterSpec<*, P>, func: suspend (parameter: P) -> T): KnolusFunctionBuilder<T> {
    addParameter(parameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val param = parameters.getValue(context, parameterSpec).get()

        func(param)
    }
}

fun <T, P> KnolusFunctionBuilder<T?>.setFunctionWithoutReturn(parameterSpec: ParameterSpec<*, P>, func: suspend (parameter: P) -> Unit): KnolusFunctionBuilder<T?> {
    addParameter(parameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val param = parameters.getValue(context, parameterSpec).get()

        func(param)

        null
    }
}

fun <T, P1, P2> KnolusFunctionBuilder<T>.setFunction(
    firstParameterSpec: ParameterSpec<*, P1>,
    secondParameterSpec: ParameterSpec<*, P2>,
    func: suspend (firstParameter: P1, secondParameter: P2) -> T
): KnolusFunctionBuilder<T> {
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val firstParam = parameters.getValue(context, firstParameterSpec).get()
        val secondParam = parameters.getValue(context, secondParameterSpec).get()

        func(firstParam, secondParam)
    }
}

fun <T, P1, P2> KnolusFunctionBuilder<T?>.setFunctionWithoutReturn(
    firstParameterSpec: ParameterSpec<*, P1>,
    secondParameterSpec: ParameterSpec<*, P2>,
    func: suspend (firstParameter: P1, secondParameter: P2) -> Unit
): KnolusFunctionBuilder<T?> {
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val firstParam = parameters.getValue(context, firstParameterSpec).get()
        val secondParam = parameters.getValue(context, secondParameterSpec).get()

        func(firstParam, secondParam)

        null
    }
}

fun <T, P1, P2, P3> KnolusFunctionBuilder<T>.setFunction(
    firstParameterSpec: ParameterSpec<*, P1>,
    secondParameterSpec: ParameterSpec<*, P2>,
    thirdParameterSpec: ParameterSpec<*, P3>,
    func: suspend (firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> T
): KnolusFunctionBuilder<T> {
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)
    addParameter(thirdParameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val firstParam = parameters.getValue(context, firstParameterSpec).get()
        val secondParam = parameters.getValue(context, secondParameterSpec).get()
        val thirdParam = parameters.getValue(context, thirdParameterSpec).get()

        func(firstParam, secondParam, thirdParam)
    }
}

fun <T, P1, P2, P3> KnolusFunctionBuilder<T?>.setFunctionWithoutReturn(
    firstParameterSpec: ParameterSpec<*, P1>,
    secondParameterSpec: ParameterSpec<*, P2>,
    thirdParameterSpec: ParameterSpec<*, P3>,
    func: suspend (firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> Unit
): KnolusFunctionBuilder<T?> {
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)
    addParameter(thirdParameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val firstParam = parameters.getValue(context, firstParameterSpec).get()
        val secondParam = parameters.getValue(context, secondParameterSpec).get()
        val thirdParam = parameters.getValue(context, thirdParameterSpec).get()

        func(firstParam, secondParam, thirdParam)

        null
    }
}

fun <C : KnolusContext> KnolusContext.registerFunction(
    functionName: String,
    func: suspend () -> KnolusTypedValue?,
): KorneaResult<KnolusFunction<KnolusTypedValue?>> = register(
    functionName,
    functionBuilder()
        .setFunction(func)
        .build()
)

fun <C: KnolusContext> KnolusContext.registerFunctionWithoutReturn(
    functionName: String,
    func: suspend () -> Unit,
) = register(
    functionName,
    functionBuilder()
        .setFunctionWithoutReturn(func)
        .build()
)

fun <P> KnolusContext.registerFunction(
    functionName: String,
    parameterSpec: ParameterSpec<*, P>,
    func: suspend (parameter: P) -> KnolusTypedValue,
) = register(
    functionName,
    functionBuilder()
        .setFunction(parameterSpec, func)
        .build()
)

fun <P> KnolusContext.registerFunctionWithoutReturn(
    functionName: String,
    parameterSpec: ParameterSpec<*, P>,
    func: suspend (parameter: P) -> Unit,
) = register(
    functionName,
    functionBuilder()
        .setFunctionWithoutReturn(parameterSpec, func)
        .build()
)

fun <P1, P2> KnolusContext.registerFunction(
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1>,
    secondParameterSpec: ParameterSpec<*, P2>,
    func: suspend (firstParameter: P1, secondParameter: P2) -> KnolusTypedValue,
) = register(
    functionName,
    functionBuilder()
        .setFunction(firstParameterSpec, secondParameterSpec, func)
        .build()
)

fun <P1, P2> KnolusContext.registerFunctionWithoutReturn(
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1>,
    secondParameterSpec: ParameterSpec<*, P2>,
    func: suspend (firstParameter: P1, secondParameter: P2) -> Unit,
) = register(
    functionName,
    functionBuilder()
        .setFunctionWithoutReturn(firstParameterSpec, secondParameterSpec, func)
        .build()
)

fun <P1, P2, P3> KnolusContext.registerFunction(
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1>,
    secondParameterSpec: ParameterSpec<*, P2>,
    thirdParameterSpec: ParameterSpec<*, P3>,
    func: suspend (firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> KnolusTypedValue,
) = register(
    functionName,
    functionBuilder()
        .setFunction(firstParameterSpec, secondParameterSpec, thirdParameterSpec, func)
        .build()
)

fun <P1, P2, P3> KnolusContext.registerFunctionWithoutReturn(
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1>,
    secondParameterSpec: ParameterSpec<*, P2>,
    thirdParameterSpec: ParameterSpec<*, P3>,
    func: suspend (firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> Unit,
) = register(
    functionName,
    functionBuilder()
        .setFunctionWithoutReturn(firstParameterSpec, secondParameterSpec, thirdParameterSpec, func)
        .build()
)

fun KnolusContext.registerUntypedFunction(
    functionName: String,
    func: suspend (context: KnolusContext) -> KnolusTypedValue,
): KorneaResult<KnolusFunction<KnolusTypedValue?>> = register(
    functionName,
    functionBuilder()
        .setFunctionWithContext(func)
        .build()
)

fun KnolusContext.registerUntypedFunctionWithoutReturn(
    functionName: String,
    func: suspend (context: KnolusContext) -> Unit,
) = register(
    functionName,
    functionBuilder()
        .setFunctionWithContextWithoutReturn(func)
        .build()
)

/** Getter */

fun <P0> KnolusContext.registerMemberPropertyGetter(
    typeSpec: ParameterSpec<*, P0>,
    propertyName: String,
    func: suspend (self: P0) -> KnolusTypedValue,
) = register(
    typeSpec.getMemberPropertyGetterName(propertyName),
    functionBuilder()
        .setFunction(typeSpec, func)
        .build()
)

/** Operator */

fun <P0, P1> KnolusContext.registerOperatorFunction(
    typeSpec: ParameterSpec<*, P0>,
    operator: ExpressionOperator,
    parameterSpec: ParameterSpec<*, P1>,
    func: suspend (a: P0, b: P1) -> KnolusTypedValue,
) = register(
    typeSpec.getMemberOperatorName(operator),
    functionBuilder()
        .setOperatorFunction(typeSpec, parameterSpec, func)
        .build()
)

fun <P0, P1> KnolusContext.registerMultiOperatorFunction(
    typeSpec: ParameterSpec<*, P0>,
    operator: ExpressionOperator,
    parameterSpecs: Array<ParameterSpec<*, P1>>,
    func: suspend (a: P0, b: P1) -> KnolusTypedValue,
) = parameterSpecs.forEach { parameterSpec ->
    register(
        typeSpec.getMemberOperatorName(operator),
        functionBuilder()
            .setOperatorFunction(typeSpec, parameterSpec, func)
            .build()
    )
}

@AvailableSince(Knolus.VERSION_1_4_0)
fun <P0, R0: KnolusTypedValue?, T: KnolusTypedValue.TypeInfo<R0>> KnolusContext.registerCastingOperatorFunction(
    typeSpec: ParameterSpec<*, P0>,
    castingTo: T,
    func: suspend (self: P0) -> R0,
) = register(
    typeSpec.getMemberCastingOperatorName(castingTo),
    typedFunctionBuilder<R0>()
        .setMemberFunction(typeSpec, func)
        .build()
)

/** Result Functions */
fun <T, P> KnolusFunctionBuilder<T>.setResultFunction(parameterSpec: ParameterSpec<*, P>, func: suspend (parameter: KorneaResult<P>) -> T): KnolusFunctionBuilder<T> {
    addParameter(parameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val param = parameters.getValue(context, parameterSpec)

        func(param)
    }
}

fun <T, P> KnolusFunctionBuilder<T?>.setResultFunctionWithoutReturn(
    parameterSpec: ParameterSpec<*, P>,
    func: suspend (parameter: KorneaResult<P>) -> Unit
): KnolusFunctionBuilder<T?> {
    addParameter(parameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val param = parameters.getValue(context, parameterSpec)

        func(param)

        null
    }
}

fun <T, P1, P2> KnolusFunctionBuilder<T>.setResultFunction(
    firstParameterSpec: ParameterSpec<*, P1>,
    secondParameterSpec: ParameterSpec<*, P2>,
    func: suspend (firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>) -> T
): KnolusFunctionBuilder<T> {
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val firstParam = parameters.getValue(context, firstParameterSpec)
        val secondParam = parameters.getValue(context, secondParameterSpec)

        func(firstParam, secondParam)
    }
}

fun <T, P1, P2> KnolusFunctionBuilder<T?>.setResultFunctionWithoutReturn(
    firstParameterSpec: ParameterSpec<*, P1>,
    secondParameterSpec: ParameterSpec<*, P2>,
    func: suspend (firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>) -> Unit
): KnolusFunctionBuilder<T?> {
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val firstParam = parameters.getValue(context, firstParameterSpec)
        val secondParam = parameters.getValue(context, secondParameterSpec)

        func(firstParam, secondParam)

        null
    }
}

fun <T, P1, P2, P3> KnolusFunctionBuilder<T>.setResultFunction(
    firstParameterSpec: ParameterSpec<*, P1>,
    secondParameterSpec: ParameterSpec<*, P2>,
    thirdParameterSpec: ParameterSpec<*, P3>,
    func: suspend (firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>, thirdParameter: KorneaResult<P3>) -> T
): KnolusFunctionBuilder<T> {
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)
    addParameter(thirdParameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val firstParam = parameters.getValue(context, firstParameterSpec)
        val secondParam = parameters.getValue(context, secondParameterSpec)
        val thirdParam = parameters.getValue(context, thirdParameterSpec)

        func(firstParam, secondParam, thirdParam)
    }
}

fun <T, P1, P2, P3> KnolusFunctionBuilder<T?>.setResultFunctionWithoutReturn(
    firstParameterSpec: ParameterSpec<*, P1>,
    secondParameterSpec: ParameterSpec<*, P2>,
    thirdParameterSpec: ParameterSpec<*, P3>,
    func: suspend (firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>, thirdParameter: KorneaResult<P3>) -> Unit
): KnolusFunctionBuilder<T?> {
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)
    addParameter(thirdParameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val firstParam = parameters.getValue(context, firstParameterSpec)
        val secondParam = parameters.getValue(context, secondParameterSpec)
        val thirdParam = parameters.getValue(context, thirdParameterSpec)

        func(firstParam, secondParam, thirdParam)

        null
    }
}

fun <P> KnolusContext.registerResultFunction(
    functionName: String,
    parameterSpec: ParameterSpec<*, P>,
    func: suspend (parameter: KorneaResult<P>) -> KnolusTypedValue,
) = register(
    functionName,
    functionBuilder()
        .setResultFunction(parameterSpec, func)
        .build()
)

fun <P> KnolusContext.registerResultFunctionWithoutReturn(
    functionName: String,
    parameterSpec: ParameterSpec<*, P>,
    func: suspend (parameter: KorneaResult<P>) -> Unit,
) = register(
    functionName,
    functionBuilder()
        .setResultFunctionWithoutReturn(parameterSpec, func)
        .build()
)

fun <P1, P2> KnolusContext.registerResultFunction(
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1>,
    secondParameterSpec: ParameterSpec<*, P2>,
    func: suspend (firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>) -> KnolusTypedValue,
) = register(
    functionName,
    functionBuilder()
        .setResultFunction(firstParameterSpec, secondParameterSpec, func)
        .build()
)

fun <P1, P2> KnolusContext.registerResultFunctionWithoutReturn(
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1>,
    secondParameterSpec: ParameterSpec<*, P2>,
    func: suspend (firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>) -> Unit,
) = register(
    functionName,
    functionBuilder()
        .setResultFunctionWithoutReturn(firstParameterSpec, secondParameterSpec, func)
        .build()
)

fun <P1, P2, P3> KnolusContext.registerResultFunction(
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1>,
    secondParameterSpec: ParameterSpec<*, P2>,
    thirdParameterSpec: ParameterSpec<*, P3>,
    func: suspend (firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>, thirdParameter: KorneaResult<P3>) -> KnolusTypedValue,
) = register(
    functionName,
    functionBuilder()
        .setResultFunction(firstParameterSpec, secondParameterSpec, thirdParameterSpec, func)
        .build()
)

fun <P1, P2, P3> KnolusContext.registerResultFunctionWithoutReturn(
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1>,
    secondParameterSpec: ParameterSpec<*, P2>,
    thirdParameterSpec: ParameterSpec<*, P3>,
    func: suspend (firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>, thirdParameter: KorneaResult<P3>) -> Unit,
) = register(
    functionName,
    functionBuilder()
        .setResultFunctionWithoutReturn(firstParameterSpec, secondParameterSpec, thirdParameterSpec, func)
        .build()
)

/** Operator */

fun <P0, P1> KnolusContext.registerOperatorResultFunction(
    typeSpec: ParameterSpec<*, P0>,
    operator: ExpressionOperator,
    parameterSpec: ParameterSpec<*, P1>,
    func: suspend (a: KorneaResult<P0>, b: KorneaResult<P1>) -> KnolusTypedValue,
) = register(
    typeSpec.getMemberOperatorName(operator),
    functionBuilder()
        .setOperatorResultFunction(typeSpec, parameterSpec, func)
        .build()
)

fun <P0, P1> KnolusContext.registerMultiOperatorResultFunction(
    typeSpec: ParameterSpec<*, P0>,
    operator: ExpressionOperator,
    parameterSpecs: Array<ParameterSpec<*, P1>>,
    func: suspend (a: KorneaResult<P0>, b: KorneaResult<P1>) -> KnolusTypedValue,
) = parameterSpecs.forEach { parameterSpec ->
    register(
        typeSpec.getMemberOperatorName(operator),
        functionBuilder()
            .setOperatorResultFunction(typeSpec, parameterSpec, func)
            .build()
    )
}