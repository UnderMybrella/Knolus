package org.abimon.knolus.modules.functionregistry

import org.abimon.knolus.context.KnolusContext
import org.abimon.knolus.ParameterSpec
import org.abimon.knolus.types.KnolusTypedValue
import org.abimon.kornea.errors.common.KorneaResult

fun <R, C: KnolusContext<out R>, P0, P1> KnolusContext<R>.registerMultiMemberFunction(
    typeSpec: ParameterSpec<*, P0, in R, C>,
    functionName: String,
    parameterSpecs: Array<ParameterSpec<*, P1, in R, C>>,
    func: suspend (context: C, self: P0, parameter: P1) -> KnolusTypedValue,
) = parameterSpecs.forEach { parameterSpec ->
    register(
        typeSpec.getMemberFunctionName(functionName),
        functionBuilder<R, C>()
            .setMemberFunction(typeSpec, parameterSpec, func)
            .build()
    )
}

fun <R, C: KnolusContext<out R>, P0, P1> KnolusContext<R>.registerMultiMemberFunctionWithoutReturn(
    typeSpec: ParameterSpec<*, P0, in R, C>,
    functionName: String,
    parameterSpecs: Array<ParameterSpec<*, P1, in R, C>>,
    func: suspend (context: C, self: P0, parameter: P1) -> Unit,
) = parameterSpecs.forEach { parameterSpec ->
    register(
        typeSpec.getMemberFunctionName(functionName),
        functionBuilder<R, C>()
            .setMemberFunctionWithoutReturn(typeSpec, parameterSpec, func)
            .build()
    )
}

fun <R, C: KnolusContext<out R>, P0, P1, P2> KnolusContext<R>.registerMultiMemberFunction(
    typeSpec: ParameterSpec<*, P0, in R, C>,
    functionName: String,
    firstParameterSpecs: Array<ParameterSpec<*, P1, in R, C>>,
    secondParameterSpecs: Array<ParameterSpec<*, P2, in R, C>>,
    func: suspend (context: C, self: P0, firstParameter: P1, secondParameter: P2) -> KnolusTypedValue,
) = firstParameterSpecs.forEach { firstParameterSpec ->
    secondParameterSpecs.forEach { secondParameterSpec ->
        register(
            typeSpec.getMemberFunctionName(functionName),
            functionBuilder<R, C>()
                .setMemberFunction(typeSpec, firstParameterSpec, secondParameterSpec, func)
                .build()
        )
    }
}

fun <R, C: KnolusContext<out R>, P0, P1, P2> KnolusContext<R>.registerMultiMemberFunctionWithoutReturn(
    typeSpec: ParameterSpec<*, P0, in R, C>,
    functionName: String,
    firstParameterSpecs: Array<ParameterSpec<*, P1, in R, C>>,
    secondParameterSpecs: Array<ParameterSpec<*, P2, in R, C>>,
    func: suspend (context: C, self: P0, firstParameter: P1, secondParameter: P2) -> Unit,
) = firstParameterSpecs.forEach { firstParameterSpec ->
    secondParameterSpecs.forEach { secondParameterSpec ->
        register(
            typeSpec.getMemberFunctionName(functionName),
            functionBuilder<R, C>()
                .setMemberFunctionWithoutReturn(typeSpec, firstParameterSpec, secondParameterSpec, func)
                .build()
        )
    }
}

/** Results */
fun <R, C: KnolusContext<out R>, P0, P1> KnolusContext<R>.registerMultiMemberOperatorFunction(
    typeSpec: ParameterSpec<*, P0, in R, C>,
    functionName: String,
    parameterSpecs: Array<ParameterSpec<*, P1, in R, C>>,
    func: suspend (context: C, self: P0, parameter: KorneaResult<P1>) -> KnolusTypedValue,
) = parameterSpecs.forEach { parameterSpec ->
    register(
        typeSpec.getMemberFunctionName(functionName),
        functionBuilder<R, C>()
            .setMemberResultFunction(typeSpec, parameterSpec, func)
            .build()
    )
}

fun <R, C: KnolusContext<out R>, P0, P1> KnolusContext<R>.registerMultiMemberOperatorFunctionWithoutReturn(
    typeSpec: ParameterSpec<*, P0, in R, C>,
    functionName: String,
    parameterSpecs: Array<ParameterSpec<*, P1, in R, C>>,
    func: suspend (context: C, self: P0, parameter: KorneaResult<P1>) -> Unit,
) = parameterSpecs.forEach { parameterSpec ->
    register(
        typeSpec.getMemberFunctionName(functionName),
        functionBuilder<R, C>()
            .setMemberResultFunctionWithoutReturn(typeSpec, parameterSpec, func)
            .build()
    )
}

fun <R, C: KnolusContext<out R>, P0, P1, P2> KnolusContext<R>.registerMultiMemberOperatorFunction(
    typeSpec: ParameterSpec<*, P0, in R, C>,
    functionName: String,
    firstParameterSpecs: Array<ParameterSpec<*, P1, in R, C>>,
    secondParameterSpecs: Array<ParameterSpec<*, P2, in R, C>>,
    func: suspend (context: C, self: P0, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>) -> KnolusTypedValue,
) = firstParameterSpecs.forEach { firstParameterSpec ->
    secondParameterSpecs.forEach { secondParameterSpec ->
        register(
            typeSpec.getMemberFunctionName(functionName),
            functionBuilder<R, C>()
                .setMemberResultFunction(typeSpec, firstParameterSpec, secondParameterSpec, func)
                .build()
        )
    }
}

fun <R, C: KnolusContext<out R>, P0, P1, P2> KnolusContext<R>.registerMultiMemberOperatorFunctionWithoutReturn(
    typeSpec: ParameterSpec<*, P0, in R, C>,
    functionName: String,
    firstParameterSpecs: Array<ParameterSpec<*, P1, in R, C>>,
    secondParameterSpecs: Array<ParameterSpec<*, P2, in R, C>>,
    func: suspend (context: C, self: P0, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>) -> Unit,
) = firstParameterSpecs.forEach { firstParameterSpec ->
    secondParameterSpecs.forEach { secondParameterSpec ->
        register(
            typeSpec.getMemberFunctionName(functionName),
            functionBuilder<R, C>()
                .setMemberResultFunctionWithoutReturn(typeSpec, firstParameterSpec, secondParameterSpec, func)
                .build()
        )
    }
}