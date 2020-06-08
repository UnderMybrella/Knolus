package org.abimon.knolus.modules.functionregistry

import org.abimon.knolus.KnolusContext
import org.abimon.knolus.ParameterSpec
import org.abimon.knolus.setMemberFunction
import org.abimon.knolus.setMemberFunctionWithoutReturn
import org.abimon.knolus.types.KnolusTypedValue

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