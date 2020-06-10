package org.abimon.knolus.modules.functionregistry

import org.abimon.knolus.*
import org.abimon.knolus.context.KnolusContext
import org.abimon.knolus.types.KnolusTypedValue

inline fun functionBuilder() = KnolusFunctionBuilder<KnolusTypedValue?>()

fun <T> KnolusFunctionBuilder<T>.setFunction(func: suspend (context: KnolusContext) -> T): KnolusFunctionBuilder<T> {
    return setFunction { context: KnolusContext, _: Map<String, KnolusTypedValue> ->
        func(context)
    }
}

fun <T> KnolusFunctionBuilder<T?>.setFunctionWithoutReturn(func: suspend (context: KnolusContext) -> Unit): KnolusFunctionBuilder<T?> {
    return setFunction { context: KnolusContext, _: Map<String, KnolusTypedValue> ->
        func(context)

        null
    }
}

fun <T, P> KnolusFunctionBuilder<T>.setFunction(parameterSpec: ParameterSpec<*, P>, func: suspend (context: KnolusContext, parameter: P) -> T): KnolusFunctionBuilder<T> {
    addParameter(parameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val param = parameters.getValue(context, parameterSpec)

        func(context, param)
    }
}

fun <T, P> KnolusFunctionBuilder<T?>.setFunctionWithoutReturn(parameterSpec: ParameterSpec<*, P>, func: suspend (context: KnolusContext, parameter: P) -> Unit): KnolusFunctionBuilder<T?> {
    addParameter(parameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val param = parameters.getValue(context, parameterSpec)

        func(context, param)

        null
    }
}

fun <T, P1, P2> KnolusFunctionBuilder<T>.setFunction(firstParameterSpec: ParameterSpec<*, P1>, secondParameterSpec: ParameterSpec<*, P2>, func: suspend (context: KnolusContext, firstParameter: P1, secondParameter: P2) -> T): KnolusFunctionBuilder<T> {
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val firstParam = parameters.getValue(context, firstParameterSpec)
        val secondParam = parameters.getValue(context, secondParameterSpec)

        func(context, firstParam, secondParam)
    }
}

fun <T, P1, P2> KnolusFunctionBuilder<T?>.setFunctionWithoutReturn(firstParameterSpec: ParameterSpec<*, P1>, secondParameterSpec: ParameterSpec<*, P2>, func: suspend (context: KnolusContext, firstParameter: P1, secondParameter: P2) -> Unit): KnolusFunctionBuilder<T?> {
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val firstParam = parameters.getValue(context, firstParameterSpec)
        val secondParam = parameters.getValue(context, secondParameterSpec)

        func(context, firstParam, secondParam)

        null
    }
}

fun <T, P1, P2, P3> KnolusFunctionBuilder<T>.setFunction(firstParameterSpec: ParameterSpec<*, P1>, secondParameterSpec: ParameterSpec<*, P2>, thirdParameterSpec: ParameterSpec<*, P3>, func: suspend (context: KnolusContext, firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> T): KnolusFunctionBuilder<T> {
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

fun <T, P1, P2, P3> KnolusFunctionBuilder<T?>.setFunctionWithoutReturn(firstParameterSpec: ParameterSpec<*, P1>, secondParameterSpec: ParameterSpec<*, P2>, thirdParameterSpec: ParameterSpec<*, P3>, func: suspend (context: KnolusContext, firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> Unit): KnolusFunctionBuilder<T?> {
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

fun KnolusContext.registerFunction(
    functionName: String,
    func: suspend (context: KnolusContext) -> KnolusTypedValue,
) = register(
    functionName,
    functionBuilder()
        .setFunction(func)
        .build()
)

fun KnolusContext.registerFunctionWithoutReturn(
    functionName: String,
    func: suspend (context: KnolusContext) -> Unit,
) = register(
    functionName,
    functionBuilder()
        .setFunctionWithoutReturn(func)
        .build()
)

fun <P> KnolusContext.registerFunction(
    functionName: String,
    parameterSpec: ParameterSpec<*, P>,
    func: suspend (context: KnolusContext, parameter: P) -> KnolusTypedValue,
) = register(
    functionName,
    functionBuilder()
        .setFunction(parameterSpec, func)
        .build()
)

fun <P> KnolusContext.registerFunctionWithoutReturn(
    functionName: String,
    parameterSpec: ParameterSpec<*, P>,
    func: suspend (context: KnolusContext, parameter: P) -> Unit,
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
    func: suspend (context: KnolusContext, firstParameter: P1, secondParameter: P2) -> KnolusTypedValue,
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
    func: suspend (context: KnolusContext, firstParameter: P1, secondParameter: P2) -> Unit,
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
    func: suspend (context: KnolusContext, firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> KnolusTypedValue,
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
    func: suspend (context: KnolusContext, firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> Unit,
) = register(
    functionName,
    functionBuilder()
        .setFunctionWithoutReturn(firstParameterSpec, secondParameterSpec, thirdParameterSpec, func)
        .build()
)


/** Getter */

fun <P0> KnolusContext.registerMemberPropertyGetter(
    typeSpec: ParameterSpec<*, P0>,
    propertyName: String,
    func: suspend (context: KnolusContext, self: P0) -> KnolusTypedValue,
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
    func: suspend (context: KnolusContext, a: P0, b: P1) -> KnolusTypedValue,
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
    func: suspend (context: KnolusContext, a: P0, b: P1) -> KnolusTypedValue,
) = parameterSpecs.forEach { parameterSpec ->
    register(
        typeSpec.getMemberOperatorName(operator),
        functionBuilder()
            .setOperatorFunction(typeSpec, parameterSpec, func)
            .build()
    )
}