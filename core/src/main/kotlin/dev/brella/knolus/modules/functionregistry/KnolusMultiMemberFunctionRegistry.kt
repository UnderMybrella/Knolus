package dev.brella.knolus.modules.functionregistry

import dev.brella.knolus.context.KnolusContext
import dev.brella.knolus.ParameterSpec
import dev.brella.knolus.types.KnolusTypedValue
import dev.brella.kornea.errors.common.KorneaResult

fun <P0, P1> KnolusContext.registerMultiMemberFunction(
    typeSpec: ParameterSpec<*, P0>,
    functionName: String,
    parameterSpecs: Array<ParameterSpec<*, P1>>,
    func: suspend (self: P0, parameter: P1) -> KnolusTypedValue,
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
    func: suspend (self: P0, parameter: P1) -> Unit,
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
    func: suspend (self: P0, firstParameter: P1, secondParameter: P2) -> KnolusTypedValue,
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
    func: suspend (self: P0, firstParameter: P1, secondParameter: P2) -> Unit,
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

/** Results */
fun <P0, P1> KnolusContext.registerMultiMemberOperatorFunction(
    typeSpec: ParameterSpec<*, P0>,
    functionName: String,
    parameterSpecs: Array<ParameterSpec<*, P1>>,
    func: suspend (self: P0, parameter: KorneaResult<P1>) -> KnolusTypedValue,
) = parameterSpecs.forEach { parameterSpec ->
    register(
        typeSpec.getMemberFunctionName(functionName),
        functionBuilder()
            .setMemberResultFunction(typeSpec, parameterSpec, func)
            .build()
    )
}

fun <P0, P1> KnolusContext.registerMultiMemberOperatorFunctionWithoutReturn(
    typeSpec: ParameterSpec<*, P0>,
    functionName: String,
    parameterSpecs: Array<ParameterSpec<*, P1>>,
    func: suspend (self: P0, parameter: KorneaResult<P1>) -> Unit,
) = parameterSpecs.forEach { parameterSpec ->
    register(
        typeSpec.getMemberFunctionName(functionName),
        functionBuilder()
            .setMemberResultFunctionWithoutReturn(typeSpec, parameterSpec, func)
            .build()
    )
}

fun <P0, P1, P2> KnolusContext.registerMultiMemberOperatorFunction(
    typeSpec: ParameterSpec<*, P0>,
    functionName: String,
    firstParameterSpecs: Array<ParameterSpec<*, P1>>,
    secondParameterSpecs: Array<ParameterSpec<*, P2>>,
    func: suspend (self: P0, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>) -> KnolusTypedValue,
) = firstParameterSpecs.forEach { firstParameterSpec ->
    secondParameterSpecs.forEach { secondParameterSpec ->
        register(
            typeSpec.getMemberFunctionName(functionName),
            functionBuilder()
                .setMemberResultFunction(typeSpec, firstParameterSpec, secondParameterSpec, func)
                .build()
        )
    }
}

fun <P0, P1, P2> KnolusContext.registerMultiMemberOperatorFunctionWithoutReturn(
    typeSpec: ParameterSpec<*, P0>,
    functionName: String,
    firstParameterSpecs: Array<ParameterSpec<*, P1>>,
    secondParameterSpecs: Array<ParameterSpec<*, P2>>,
    func: suspend (self: P0, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>) -> Unit,
) = firstParameterSpecs.forEach { firstParameterSpec ->
    secondParameterSpecs.forEach { secondParameterSpec ->
        register(
            typeSpec.getMemberFunctionName(functionName),
            functionBuilder()
                .setMemberResultFunctionWithoutReturn(typeSpec, firstParameterSpec, secondParameterSpec, func)
                .build()
        )
    }
}