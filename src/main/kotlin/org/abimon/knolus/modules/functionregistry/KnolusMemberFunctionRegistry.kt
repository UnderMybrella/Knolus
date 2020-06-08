package org.abimon.knolus.modules.functionregistry

import org.abimon.knolus.*
import org.abimon.knolus.types.KnolusTypedValue
import kotlin.collections.getValue

fun <T, P0> KnolusFunctionBuilder<T>.setMemberFunction(typeSpec: ParameterSpec<*, P0>, func: suspend (context: KnolusContext, self: P0) -> T): KnolusFunctionBuilder<T> {
    addParameter(typeSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec)

        func(context, self)
    }
}

fun <T, P0> KnolusFunctionBuilder<T?>.setMemberFunctionWithoutReturn(typeSpec: ParameterSpec<*, P0>, func: suspend (context: KnolusContext, self: P0) -> Unit): KnolusFunctionBuilder<T?> {
    addParameter(typeSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec)

        func(context, self)

        null
    }
}

fun <T, P0, P1> KnolusFunctionBuilder<T>.setMemberFunction(typeSpec: ParameterSpec<*, P0>, parameterSpec: ParameterSpec<*, P1>, func: suspend (context: KnolusContext, self: P0, parameter: P1) -> T): KnolusFunctionBuilder<T> {
    addParameter(typeSpec)
    addParameter(parameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec)
        val param = parameters.getValue(context, parameterSpec)

        func(context, self, param)
    }
}

fun <T, P0, P1> KnolusFunctionBuilder<T?>.setMemberFunctionWithoutReturn(typeSpec: ParameterSpec<*, P0>, parameterSpec: ParameterSpec<*, P1>, func: suspend (context: KnolusContext, self: P0, parameter: P1) -> Unit): KnolusFunctionBuilder<T?> {
    addParameter(typeSpec)
    addParameter(parameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec)
        val param = parameters.getValue(context, parameterSpec)

        func(context, self, param)

        null
    }
}

fun <T, P0, P1, P2> KnolusFunctionBuilder<T>.setMemberFunction(typeSpec: ParameterSpec<*, P0>, firstParameterSpec: ParameterSpec<*, P1>, secondParameterSpec: ParameterSpec<*, P2>, func: suspend (context: KnolusContext, self: P0, firstParameter: P1, secondParameter: P2) -> T): KnolusFunctionBuilder<T> {
    addParameter(typeSpec)
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec)
        val firstParam = parameters.getValue(context, firstParameterSpec)
        val secondParam = parameters.getValue(context, secondParameterSpec)

        func(context, self, firstParam, secondParam)
    }
}

fun <T, P0, P1, P2> KnolusFunctionBuilder<T?>.setMemberFunctionWithoutReturn(typeSpec: ParameterSpec<*, P0>, firstParameterSpec: ParameterSpec<*, P1>, secondParameterSpec: ParameterSpec<*, P2>, func: suspend (context: KnolusContext, self: P0, firstParameter: P1, secondParameter: P2) -> Unit): KnolusFunctionBuilder<T?> {
    addParameter(typeSpec)
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec)
        val firstParam = parameters.getValue(context, firstParameterSpec)
        val secondParam = parameters.getValue(context, secondParameterSpec)

        func(context, self, firstParam, secondParam)

        null
    }
}

fun <T, P0, P1, P2, P3> KnolusFunctionBuilder<T>.setMemberFunction(typeSpec: ParameterSpec<*, P0>, firstParameterSpec: ParameterSpec<*, P1>, secondParameterSpec: ParameterSpec<*, P2>, thirdParameterSpec: ParameterSpec<*, P3>, func: suspend (context: KnolusContext, self: P0, firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> T): KnolusFunctionBuilder<T> {
    addParameter(typeSpec)
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)
    addParameter(thirdParameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec)
        val firstParam = parameters.getValue(context, firstParameterSpec)
        val secondParam = parameters.getValue(context, secondParameterSpec)
        val thirdParam = parameters.getValue(context, thirdParameterSpec)

        func(context, self, firstParam, secondParam, thirdParam)
    }
}

fun <T, P0, P1, P2, P3> KnolusFunctionBuilder<T?>.setMemberFunctionWithoutReturn(typeSpec: ParameterSpec<*, P0>, firstParameterSpec: ParameterSpec<*, P1>, secondParameterSpec: ParameterSpec<*, P2>, thirdParameterSpec: ParameterSpec<*, P3>, func: suspend (context: KnolusContext, self: P0, firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> Unit): KnolusFunctionBuilder<T?> {
    addParameter(typeSpec)
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)
    addParameter(thirdParameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec)
        val firstParam = parameters.getValue(context, firstParameterSpec)
        val secondParam = parameters.getValue(context, secondParameterSpec)
        val thirdParam = parameters.getValue(context, thirdParameterSpec)

        func(context, self, firstParam, secondParam, thirdParam)

        null
    }
}

fun <P0> KnolusContext.registerMemberFunction(
    typeSpec: ParameterSpec<*, P0>,
    functionName: String,
    func: suspend (context: KnolusContext, self: P0) -> KnolusTypedValue,
) = register(
    typeSpec.getMemberFunctionName(functionName),
    functionBuilder()
        .setMemberFunction(typeSpec, func)
        .build()
)

fun <P0> KnolusContext.registerMemberFunctionWithoutReturn(
    typeSpec: ParameterSpec<*, P0>,
    functionName: String,
    func: suspend (context: KnolusContext, self: P0) -> Unit,
) = register(
    typeSpec.getMemberFunctionName(functionName),
    functionBuilder()
        .setMemberFunctionWithoutReturn(typeSpec, func)
        .build()
)

fun <P0, P1> KnolusContext.registerMemberFunction(
    typeSpec: ParameterSpec<*, P0>,
    functionName: String,
    parameterSpec: ParameterSpec<*, P1>,
    func: suspend (context: KnolusContext, self: P0, parameter: P1) -> KnolusTypedValue,
) = register(
    typeSpec.getMemberFunctionName(functionName),
    functionBuilder()
        .setMemberFunction(typeSpec, parameterSpec, func)
        .build()
)

fun <P0, P1> KnolusContext.registerMemberFunctionWithoutReturn(
    typeSpec: ParameterSpec<*, P0>,
    functionName: String,
    parameterSpec: ParameterSpec<*, P1>,
    func: suspend (context: KnolusContext, self: P0, parameter: P1) -> Unit,
) = register(
    typeSpec.getMemberFunctionName(functionName),
    functionBuilder()
        .setMemberFunctionWithoutReturn(typeSpec, parameterSpec, func)
        .build()
)

fun <P0, P1, P2> KnolusContext.registerMemberFunction(
    typeSpec: ParameterSpec<*, P0>,
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1>,
    secondParameterSpec: ParameterSpec<*, P2>,
    func: suspend (context: KnolusContext, self: P0, firstParameter: P1, secondParameter: P2) -> KnolusTypedValue,
) = register(
    typeSpec.getMemberFunctionName(functionName),
    functionBuilder()
        .setMemberFunction(typeSpec, firstParameterSpec, secondParameterSpec, func)
        .build()
)

fun <P0, P1, P2> KnolusContext.registerMemberFunctionWithoutReturn(
    typeSpec: ParameterSpec<*, P0>,
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1>,
    secondParameterSpec: ParameterSpec<*, P2>,
    func: suspend (context: KnolusContext, self: P0, firstParameter: P1, secondParameter: P2) -> Unit,
) = register(
    typeSpec.getMemberFunctionName(functionName),
    functionBuilder()
        .setMemberFunctionWithoutReturn(typeSpec, firstParameterSpec, secondParameterSpec, func)
        .build()
)