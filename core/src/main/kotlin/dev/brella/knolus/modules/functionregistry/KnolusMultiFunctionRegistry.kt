package dev.brella.knolus.modules.functionregistry

import dev.brella.knolus.*
import dev.brella.knolus.context.KnolusContext
import dev.brella.knolus.types.KnolusTypedValue
import dev.brella.kornea.errors.common.KorneaResult

fun <P> KnolusContext.registerMultiFunction(
    functionName: String,
    parameterSpecs: Array<ParameterSpec<*, P>>,
    func: suspend (parameter: P) -> KnolusTypedValue,
) = parameterSpecs.forEach { parameterSpec ->
    register(
        functionName,
        functionBuilder()
            .setFunction(parameterSpec, func)
            .build()
    )
}

fun <P> KnolusContext.registerMultiFunctionWithoutReturn(
    functionName: String,
    parameterSpecs: Array<ParameterSpec<*, P>>,
    func: suspend (parameter: P) -> Unit,
) = parameterSpecs.forEach { parameterSpec ->
    register(
        functionName,
        functionBuilder()
            .setFunctionWithoutReturn(parameterSpec, func)
            .build()
    )
}

fun <P1, P2> KnolusContext.registerMultiFunction(
    functionName: String,
    firstParameterSpecs: Array<ParameterSpec<*, P1>>,
    secondParameterSpecs: Array<ParameterSpec<*, P2>>,
    func: suspend (firstParameter: P1, secondParameter: P2) -> KnolusTypedValue,
) = firstParameterSpecs.forEach { firstParameterSpec ->
    secondParameterSpecs.forEach { secondParameterSpec ->
        register(
            functionName,
            functionBuilder()
                .setFunction(firstParameterSpec, secondParameterSpec, func)
                .build()
        )
    }
}

fun <P1, P2> KnolusContext.registerMultiFunctionWithoutReturn(
    functionName: String,
    firstParameterSpecs: Array<ParameterSpec<*, P1>>,
    secondParameterSpecs: Array<ParameterSpec<*, P2>>,
    func: suspend (firstParameter: P1, secondParameter: P2) -> Unit,
) = firstParameterSpecs.forEach { firstParameterSpec ->
    secondParameterSpecs.forEach { secondParameterSpec ->
        register(
            functionName,
            functionBuilder()
                .setFunctionWithoutReturn(firstParameterSpec, secondParameterSpec, func)
                .build()
        )
    }
}

fun <P1, P2, P3> KnolusContext.registerMultiFunction(
    functionName: String,
    firstParameterSpecs: Array<ParameterSpec<*, P1>>,
    secondParameterSpecs: Array<ParameterSpec<*, P2>>,
    thirdParameterSpecs: Array<ParameterSpec<*, P3>>,
    func: suspend (firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> KnolusTypedValue,
) = firstParameterSpecs.forEach { firstParameterSpec ->
    secondParameterSpecs.forEach { secondParameterSpec ->
        thirdParameterSpecs.forEach { thirdParameterSpec ->
            register(
                functionName,
                functionBuilder()
                    .setFunction(firstParameterSpec, secondParameterSpec, thirdParameterSpec, func)
                    .build()
            )
        }
    }
}

fun <P1, P2, P3> KnolusContext.registerMultiFunctionWithoutReturn(
    functionName: String,
    firstParameterSpecs: Array<ParameterSpec<*, P1>>,
    secondParameterSpecs: Array<ParameterSpec<*, P2>>,
    thirdParameterSpecs: Array<ParameterSpec<*, P3>>,
    func: suspend (firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> Unit,
) = firstParameterSpecs.forEach { firstParameterSpec ->
    secondParameterSpecs.forEach { secondParameterSpec ->
        thirdParameterSpecs.forEach { thirdParameterSpec ->
            register(
                functionName,
                functionBuilder()
                    .setFunctionWithoutReturn(firstParameterSpec, secondParameterSpec, thirdParameterSpec, func)
                    .build()
            )
        }
    }
}

/** Result */

fun <P> KnolusContext.registerMultiResultFunction(
    functionName: String,
    parameterSpecs: Array<ParameterSpec<*, P>>,
    func: suspend (parameter: KorneaResult<P>) -> KnolusTypedValue,
) = parameterSpecs.forEach { parameterSpec ->
    register(
        functionName,
        functionBuilder()
            .setResultFunction(parameterSpec, func)
            .build()
    )
}

fun <P> KnolusContext.registerMultiResultFunctionWithoutReturn(
    functionName: String,
    parameterSpecs: Array<ParameterSpec<*, P>>,
    func: suspend (parameter: KorneaResult<P>) -> Unit,
) = parameterSpecs.forEach { parameterSpec ->
    register(
        functionName,
        functionBuilder()
            .setResultFunctionWithoutReturn(parameterSpec, func)
            .build()
    )
}

fun <P1, P2> KnolusContext.registerMultiResultFunction(
    functionName: String,
    firstParameterSpecs: Array<ParameterSpec<*, P1>>,
    secondParameterSpecs: Array<ParameterSpec<*, P2>>,
    func: suspend (firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>) -> KnolusTypedValue,
) = firstParameterSpecs.forEach { firstParameterSpec ->
    secondParameterSpecs.forEach { secondParameterSpec ->
        register(
            functionName,
            functionBuilder()
                .setResultFunction(firstParameterSpec, secondParameterSpec, func)
                .build()
        )
    }
}

fun <P1, P2> KnolusContext.registerMultiResultFunctionWithoutReturn(
    functionName: String,
    firstParameterSpecs: Array<ParameterSpec<*, P1>>,
    secondParameterSpecs: Array<ParameterSpec<*, P2>>,
    func: suspend (firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>) -> Unit,
) = firstParameterSpecs.forEach { firstParameterSpec ->
    secondParameterSpecs.forEach { secondParameterSpec ->
        register(
            functionName,
            functionBuilder()
                .setResultFunctionWithoutReturn(firstParameterSpec, secondParameterSpec, func)
                .build()
        )
    }
}

fun <P1, P2, P3> KnolusContext.registerMultiResultFunction(
    functionName: String,
    firstParameterSpecs: Array<ParameterSpec<*, P1>>,
    secondParameterSpecs: Array<ParameterSpec<*, P2>>,
    thirdParameterSpecs: Array<ParameterSpec<*, P3>>,
    func: suspend (firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>, thirdParameter: KorneaResult<P3>) -> KnolusTypedValue,
) = firstParameterSpecs.forEach { firstParameterSpec ->
    secondParameterSpecs.forEach { secondParameterSpec ->
        thirdParameterSpecs.forEach { thirdParameterSpec ->
            register(
                functionName,
                functionBuilder()
                    .setResultFunction(firstParameterSpec, secondParameterSpec, thirdParameterSpec, func)
                    .build()
            )
        }
    }
}

fun <P1, P2, P3> KnolusContext.registerMultiResultFunctionWithoutReturn(
    functionName: String,
    firstParameterSpecs: Array<ParameterSpec<*, P1>>,
    secondParameterSpecs: Array<ParameterSpec<*, P2>>,
    thirdParameterSpecs: Array<ParameterSpec<*, P3>>,
    func: suspend (firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>, thirdParameter: KorneaResult<P3>) -> Unit,
) = firstParameterSpecs.forEach { firstParameterSpec ->
    secondParameterSpecs.forEach { secondParameterSpec ->
        thirdParameterSpecs.forEach { thirdParameterSpec ->
            register(
                functionName,
                functionBuilder()
                    .setResultFunctionWithoutReturn(firstParameterSpec, secondParameterSpec, thirdParameterSpec, func)
                    .build()
            )
        }
    }
}