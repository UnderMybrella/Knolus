package dev.brella.knolus.modules.functionregistry

import dev.brella.knolus.context.KnolusContext
import dev.brella.knolus.ParameterSpec
import dev.brella.knolus.types.KnolusTypedValue
import dev.brella.kornea.errors.common.KorneaResult

fun <R, C: KnolusContext<out R>, P0, P1> KnolusContext<R>.registerMultiMemberFunctionWithContext(
    typeSpec: ParameterSpec<*, P0, in R, C>,
    functionName: String,
    parameterSpecs: Array<ParameterSpec<*, P1, in R, C>>,
    func: suspend (context: C, self: P0, parameter: P1) -> KnolusTypedValue,
) = parameterSpecs.forEach { parameterSpec ->
    register(
        typeSpec.getMemberFunctionName(functionName),
        functionBuilder<R, C>()
            .setMemberFunctionWithContext(typeSpec, parameterSpec, func)
            .build()
    )
}

fun <R, C: KnolusContext<out R>, P0, P1> KnolusContext<R>.registerMultiMemberFunctionWithContextWithoutReturn(
    typeSpec: ParameterSpec<*, P0, in R, C>,
    functionName: String,
    parameterSpecs: Array<ParameterSpec<*, P1, in R, C>>,
    func: suspend (context: C, self: P0, parameter: P1) -> Unit,
) = parameterSpecs.forEach { parameterSpec ->
    register(
        typeSpec.getMemberFunctionName(functionName),
        functionBuilder<R, C>()
            .setMemberFunctionWithContextWithoutReturn(typeSpec, parameterSpec, func)
            .build()
    )
}

fun <R, C: KnolusContext<out R>, P0, P1, P2> KnolusContext<R>.registerMultiMemberFunctionWithContext(
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
                .setMemberFunctionWithContext(typeSpec, firstParameterSpec, secondParameterSpec, func)
                .build()
        )
    }
}

fun <R, C: KnolusContext<out R>, P0, P1, P2> KnolusContext<R>.registerMultiMemberFunctionWithContextWithoutReturn(
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
                .setMemberFunctionWithContextWithoutReturn(typeSpec, firstParameterSpec, secondParameterSpec, func)
                .build()
        )
    }
}

/** Results */
fun <R, C: KnolusContext<out R>, P0, P1> KnolusContext<R>.registerMultiMemberOperatorFunctionWithContext(
    typeSpec: ParameterSpec<*, P0, in R, C>,
    functionName: String,
    parameterSpecs: Array<ParameterSpec<*, P1, in R, C>>,
    func: suspend (context: C, self: P0, parameter: KorneaResult<P1>) -> KnolusTypedValue,
) = parameterSpecs.forEach { parameterSpec ->
    register(
        typeSpec.getMemberFunctionName(functionName),
        functionBuilder<R, C>()
            .setMemberResultFunctionWithContext(typeSpec, parameterSpec, func)
            .build()
    )
}

fun <R, C: KnolusContext<out R>, P0, P1> KnolusContext<R>.registerMultiMemberOperatorFunctionWithContextWithoutReturn(
    typeSpec: ParameterSpec<*, P0, in R, C>,
    functionName: String,
    parameterSpecs: Array<ParameterSpec<*, P1, in R, C>>,
    func: suspend (context: C, self: P0, parameter: KorneaResult<P1>) -> Unit,
) = parameterSpecs.forEach { parameterSpec ->
    register(
        typeSpec.getMemberFunctionName(functionName),
        functionBuilder<R, C>()
            .setMemberResultFunctionWithContextWithoutReturn(typeSpec, parameterSpec, func)
            .build()
    )
}

fun <R, C: KnolusContext<out R>, P0, P1, P2> KnolusContext<R>.registerMultiMemberOperatorFunctionWithContext(
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
                .setMemberResultFunctionWithContext(typeSpec, firstParameterSpec, secondParameterSpec, func)
                .build()
        )
    }
}

fun <R, C: KnolusContext<out R>, P0, P1, P2> KnolusContext<R>.registerMultiMemberOperatorFunctionWithContextWithoutReturn(
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
                .setMemberResultFunctionWithContextWithoutReturn(typeSpec, firstParameterSpec, secondParameterSpec, func)
                .build()
        )
    }
}