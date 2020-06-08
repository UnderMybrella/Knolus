package org.abimon.knolus.modules.functionregistry

import org.abimon.knolus.KnolusContext
import org.abimon.knolus.ParameterSpec
import org.abimon.knolus.setMemberFunction
import org.abimon.knolus.setMemberFunctionWithoutReturn
import org.abimon.knolus.types.KnolusTypedValue

fun <P0, P1> KnolusContext.registerMultiMemberFunction(
    typeSpec: ParameterSpec<*, P0>,
    functionName: String,
    parameterSpecs: Array<ParameterSpec<*, P1>>,
    func: suspend (context: KnolusContext, self: P0, parameter: P1) -> KnolusTypedValue,
) = parameterSpecs.forEach { parameterSpec ->
    register(
        typeSpec.getMemberFunctionName(functionName),
        functionBuilder()
            .setMemberFunction(typeSpec, parameterSpec, func)
            .build()
    )
}

fun <P0, P1> KnolusContext.registerMultiMemberFunctionWithoutReturn(
    typeSpec: ParameterSpec<*, P0>,
    functionName: String,
    parameterSpecs: Array<ParameterSpec<*, P1>>,
    func: suspend (context: KnolusContext, self: P0, parameter: P1) -> Unit,
) = parameterSpecs.forEach { parameterSpec ->
    register(
        typeSpec.getMemberFunctionName(functionName),
        functionBuilder()
            .setMemberFunctionWithoutReturn(typeSpec, parameterSpec, func)
            .build()
    )
}

fun <P0, P1, P2> KnolusContext.registerMultiMemberFunction(
    typeSpec: ParameterSpec<*, P0>,
    functionName: String,
    firstParameterSpecs: Array<ParameterSpec<*, P1>>,
    secondParameterSpecs: Array<ParameterSpec<*, P2>>,
    func: suspend (context: KnolusContext, self: P0, firstParameter: P1, secondParameter: P2) -> KnolusTypedValue,
) = firstParameterSpecs.forEach { firstParameterSpec ->
    secondParameterSpecs.forEach { secondParameterSpec ->
        register(
            typeSpec.getMemberFunctionName(functionName),
            functionBuilder()
                .setMemberFunction(typeSpec, firstParameterSpec, secondParameterSpec, func)
                .build()
        )
    }
}

fun <P0, P1, P2> KnolusContext.registerMultiMemberFunctionWithoutReturn(
    typeSpec: ParameterSpec<*, P0>,
    functionName: String,
    firstParameterSpecs: Array<ParameterSpec<*, P1>>,
    secondParameterSpecs: Array<ParameterSpec<*, P2>>,
    func: suspend (context: KnolusContext, self: P0, firstParameter: P1, secondParameter: P2) -> Unit,
) = firstParameterSpecs.forEach { firstParameterSpec ->
    secondParameterSpecs.forEach { secondParameterSpec ->
        register(
            typeSpec.getMemberFunctionName(functionName),
            functionBuilder()
                .setMemberFunctionWithoutReturn(typeSpec, firstParameterSpec, secondParameterSpec, func)
                .build()
        )
    }
}