package dev.brella.knolus.modules.functionregistry

import dev.brella.knolus.*
import dev.brella.knolus.context.KnolusContext
import dev.brella.knolus.types.KnolusTypedValue
import dev.brella.kornea.errors.common.KorneaResult

fun <P> KnolusContext.registerMultiFunctionWithContext(
    functionName: String,
    parameterSpecs: Array<ParameterSpec<*, P>>,
    func: suspend (context: KnolusContext, parameter: P) -> KnolusTypedValue,
) = parameterSpecs.forEach { parameterSpec ->
    register(
        functionName,
        functionBuilder()
            .setFunctionWithContext(parameterSpec, func)
            .build()
    )
}

fun <P> KnolusContext.registerMultiFunctionWithContextWithoutReturn(
    functionName: String,
    parameterSpecs: Array<ParameterSpec<*, P>>,
    func: suspend (context: KnolusContext, parameter: P) -> Unit,
) = parameterSpecs.forEach { parameterSpec ->
    register(
        functionName,
        functionBuilder()
            .setFunctionWithContextWithoutReturn(parameterSpec, func)
            .build()
    )
}

fun <P1, P2> KnolusContext.registerMultiFunctionWithContext(
    functionName: String,
    firstParameterSpecs: Array<ParameterSpec<*, P1>>,
    secondParameterSpecs: Array<ParameterSpec<*, P2>>,
    func: suspend (context: KnolusContext, firstParameter: P1, secondParameter: P2) -> KnolusTypedValue,
) = firstParameterSpecs.forEach { firstParameterSpec ->
    secondParameterSpecs.forEach { secondParameterSpec ->
        register(
            functionName,
            functionBuilder()
                .setFunctionWithContext(firstParameterSpec, secondParameterSpec, func)
                .build()
        )
    }
}

fun <P1, P2> KnolusContext.registerMultiFunctionWithContextWithoutReturn(
    functionName: String,
    firstParameterSpecs: Array<ParameterSpec<*, P1>>,
    secondParameterSpecs: Array<ParameterSpec<*, P2>>,
    func: suspend (context: KnolusContext, firstParameter: P1, secondParameter: P2) -> Unit,
) = firstParameterSpecs.forEach { firstParameterSpec ->
    secondParameterSpecs.forEach { secondParameterSpec ->
        register(
            functionName,
            functionBuilder()
                .setFunctionWithContextWithoutReturn(firstParameterSpec, secondParameterSpec, func)
                .build()
        )
    }
}

fun <P1, P2, P3> KnolusContext.registerMultiFunctionWithContext(
    functionName: String,
    firstParameterSpecs: Array<ParameterSpec<*, P1>>,
    secondParameterSpecs: Array<ParameterSpec<*, P2>>,
    thirdParameterSpecs: Array<ParameterSpec<*, P3>>,
    func: suspend (context: KnolusContext, firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> KnolusTypedValue,
) = firstParameterSpecs.forEach { firstParameterSpec ->
    secondParameterSpecs.forEach { secondParameterSpec ->
        thirdParameterSpecs.forEach { thirdParameterSpec ->
            register(
                functionName,
                functionBuilder()
                    .setFunctionWithContext(firstParameterSpec, secondParameterSpec, thirdParameterSpec, func)
                    .build()
            )
        }
    }
}

fun <P1, P2, P3> KnolusContext.registerMultiFunctionWithContextWithoutReturn(
    functionName: String,
    firstParameterSpecs: Array<ParameterSpec<*, P1>>,
    secondParameterSpecs: Array<ParameterSpec<*, P2>>,
    thirdParameterSpecs: Array<ParameterSpec<*, P3>>,
    func: suspend (context: KnolusContext, firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> Unit,
) = firstParameterSpecs.forEach { firstParameterSpec ->
    secondParameterSpecs.forEach { secondParameterSpec ->
        thirdParameterSpecs.forEach { thirdParameterSpec ->
            register(
                functionName,
                functionBuilder()
                    .setFunctionWithContextWithoutReturn(firstParameterSpec, secondParameterSpec, thirdParameterSpec, func)
                    .build()
            )
        }
    }
}

/** Result */

fun <P> KnolusContext.registerMultiResultFunctionWithContext(
    functionName: String,
    parameterSpecs: Array<ParameterSpec<*, P>>,
    func: suspend (context: KnolusContext, parameter: KorneaResult<P>) -> KnolusTypedValue,
) = parameterSpecs.forEach { parameterSpec ->
    register(
        functionName,
        functionBuilder()
            .setResultFunctionWithContext(parameterSpec, func)
            .build()
    )
}

fun <P> KnolusContext.registerMultiResultFunctionWithContextWithoutReturn(
    functionName: String,
    parameterSpecs: Array<ParameterSpec<*, P>>,
    func: suspend (context: KnolusContext, parameter: KorneaResult<P>) -> Unit,
) = parameterSpecs.forEach { parameterSpec ->
    register(
        functionName,
        functionBuilder()
            .setResultFunctionWithContextWithoutReturn(parameterSpec, func)
            .build()
    )
}

fun <P1, P2> KnolusContext.registerMultiResultFunctionWithContext(
    functionName: String,
    firstParameterSpecs: Array<ParameterSpec<*, P1>>,
    secondParameterSpecs: Array<ParameterSpec<*, P2>>,
    func: suspend (context: KnolusContext, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>) -> KnolusTypedValue,
) = firstParameterSpecs.forEach { firstParameterSpec ->
    secondParameterSpecs.forEach { secondParameterSpec ->
        register(
            functionName,
            functionBuilder()
                .setResultFunctionWithContext(firstParameterSpec, secondParameterSpec, func)
                .build()
        )
    }
}

fun <P1, P2> KnolusContext.registerMultiResultFunctionWithContextWithoutReturn(
    functionName: String,
    firstParameterSpecs: Array<ParameterSpec<*, P1>>,
    secondParameterSpecs: Array<ParameterSpec<*, P2>>,
    func: suspend (context: KnolusContext, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>) -> Unit,
) = firstParameterSpecs.forEach { firstParameterSpec ->
    secondParameterSpecs.forEach { secondParameterSpec ->
        register(
            functionName,
            functionBuilder()
                .setResultFunctionWithContextWithoutReturn(firstParameterSpec, secondParameterSpec, func)
                .build()
        )
    }
}

fun <P1, P2, P3> KnolusContext.registerMultiResultFunctionWithContext(
    functionName: String,
    firstParameterSpecs: Array<ParameterSpec<*, P1>>,
    secondParameterSpecs: Array<ParameterSpec<*, P2>>,
    thirdParameterSpecs: Array<ParameterSpec<*, P3>>,
    func: suspend (context: KnolusContext, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>, thirdParameter: KorneaResult<P3>) -> KnolusTypedValue,
) = firstParameterSpecs.forEach { firstParameterSpec ->
    secondParameterSpecs.forEach { secondParameterSpec ->
        thirdParameterSpecs.forEach { thirdParameterSpec ->
            register(
                functionName,
                functionBuilder()
                    .setResultFunctionWithContext(firstParameterSpec, secondParameterSpec, thirdParameterSpec, func)
                    .build()
            )
        }
    }
}

fun <P1, P2, P3> KnolusContext.registerMultiResultFunctionWithContextWithoutReturn(
    functionName: String,
    firstParameterSpecs: Array<ParameterSpec<*, P1>>,
    secondParameterSpecs: Array<ParameterSpec<*, P2>>,
    thirdParameterSpecs: Array<ParameterSpec<*, P3>>,
    func: suspend (context: KnolusContext, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>, thirdParameter: KorneaResult<P3>) -> Unit,
) = firstParameterSpecs.forEach { firstParameterSpec ->
    secondParameterSpecs.forEach { secondParameterSpec ->
        thirdParameterSpecs.forEach { thirdParameterSpec ->
            register(
                functionName,
                functionBuilder()
                    .setResultFunctionWithContextWithoutReturn(firstParameterSpec, secondParameterSpec, thirdParameterSpec, func)
                    .build()
            )
        }
    }
}