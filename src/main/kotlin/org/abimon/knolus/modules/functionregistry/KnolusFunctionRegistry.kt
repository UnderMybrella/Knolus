package org.abimon.knolus.modules.functionregistry

import org.abimon.knolus.*
import org.abimon.knolus.types.KnolusTypedValue

inline fun functionBuilder() = KnolusFunctionBuilder<KnolusTypedValue?>()

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