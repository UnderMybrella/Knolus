package dev.brella.knolus.modules.functionregistry

import dev.brella.knolus.*
import dev.brella.knolus.context.KnolusContext
import dev.brella.knolus.types.KnolusTypedValue
import org.abimon.kornea.errors.common.KorneaResult

fun <R, C: KnolusContext<out R>, P> KnolusContext<R>.registerMultiFunctionWithContext(
    functionName: String,
    parameterSpecs: Array<ParameterSpec<*, P, in R, C>>,
    func: suspend (context: C, parameter: P) -> KnolusTypedValue,
) = parameterSpecs.forEach { parameterSpec ->
    register(
        functionName,
        functionBuilder<R, C>()
            .setFunctionWithContext(parameterSpec, func)
            .build()
    )
}

fun <R, C: KnolusContext<out R>, P> KnolusContext<R>.registerMultiFunctionWithContextWithoutReturn(
    functionName: String,
    parameterSpecs: Array<ParameterSpec<*, P, in R, C>>,
    func: suspend (context: C, parameter: P) -> Unit,
) = parameterSpecs.forEach { parameterSpec ->
    register(
        functionName,
        functionBuilder<R, C>()
            .setFunctionWithContextWithoutReturn(parameterSpec, func)
            .build()
    )
}

fun <R, C: KnolusContext<out R>, P1, P2> KnolusContext<R>.registerMultiFunctionWithContext(
    functionName: String,
    firstParameterSpecs: Array<ParameterSpec<*, P1, in R, C>>,
    secondParameterSpecs: Array<ParameterSpec<*, P2, in R, C>>,
    func: suspend (context: C, firstParameter: P1, secondParameter: P2) -> KnolusTypedValue,
) = firstParameterSpecs.forEach { firstParameterSpec ->
    secondParameterSpecs.forEach { secondParameterSpec ->
        register(
            functionName,
            functionBuilder<R, C>()
                .setFunctionWithContext(firstParameterSpec, secondParameterSpec, func)
                .build()
        )
    }
}

fun <R, C: KnolusContext<out R>, P1, P2> KnolusContext<R>.registerMultiFunctionWithContextWithoutReturn(
    functionName: String,
    firstParameterSpecs: Array<ParameterSpec<*, P1, in R, C>>,
    secondParameterSpecs: Array<ParameterSpec<*, P2, in R, C>>,
    func: suspend (context: C, firstParameter: P1, secondParameter: P2) -> Unit,
) = firstParameterSpecs.forEach { firstParameterSpec ->
    secondParameterSpecs.forEach { secondParameterSpec ->
        register(
            functionName,
            functionBuilder<R, C>()
                .setFunctionWithContextWithoutReturn(firstParameterSpec, secondParameterSpec, func)
                .build()
        )
    }
}

fun <R, C: KnolusContext<out R>, P1, P2, P3> KnolusContext<R>.registerMultiFunctionWithContext(
    functionName: String,
    firstParameterSpecs: Array<ParameterSpec<*, P1, in R, C>>,
    secondParameterSpecs: Array<ParameterSpec<*, P2, in R, C>>,
    thirdParameterSpecs: Array<ParameterSpec<*, P3, in R, C>>,
    func: suspend (context: C, firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> KnolusTypedValue,
) = firstParameterSpecs.forEach { firstParameterSpec ->
    secondParameterSpecs.forEach { secondParameterSpec ->
        thirdParameterSpecs.forEach { thirdParameterSpec ->
            register(
                functionName,
                functionBuilder<R, C>()
                    .setFunctionWithContext(firstParameterSpec, secondParameterSpec, thirdParameterSpec, func)
                    .build()
            )
        }
    }
}

fun <R, C: KnolusContext<out R>, P1, P2, P3> KnolusContext<R>.registerMultiFunctionWithContextWithoutReturn(
    functionName: String,
    firstParameterSpecs: Array<ParameterSpec<*, P1, in R, C>>,
    secondParameterSpecs: Array<ParameterSpec<*, P2, in R, C>>,
    thirdParameterSpecs: Array<ParameterSpec<*, P3, in R, C>>,
    func: suspend (context: C, firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> Unit,
) = firstParameterSpecs.forEach { firstParameterSpec ->
    secondParameterSpecs.forEach { secondParameterSpec ->
        thirdParameterSpecs.forEach { thirdParameterSpec ->
            register(
                functionName,
                functionBuilder<R, C>()
                    .setFunctionWithContextWithoutReturn(firstParameterSpec, secondParameterSpec, thirdParameterSpec, func)
                    .build()
            )
        }
    }
}

/** Result */

fun <R, C: KnolusContext<out R>, P> KnolusContext<R>.registerMultiResultFunctionWithContext(
    functionName: String,
    parameterSpecs: Array<ParameterSpec<*, P, in R, C>>,
    func: suspend (context: C, parameter: KorneaResult<P>) -> KnolusTypedValue,
) = parameterSpecs.forEach { parameterSpec ->
    register(
        functionName,
        functionBuilder<R, C>()
            .setResultFunctionWithContext(parameterSpec, func)
            .build()
    )
}

fun <R, C: KnolusContext<out R>, P> KnolusContext<R>.registerMultiResultFunctionWithContextWithoutReturn(
    functionName: String,
    parameterSpecs: Array<ParameterSpec<*, P, in R, C>>,
    func: suspend (context: C, parameter: KorneaResult<P>) -> Unit,
) = parameterSpecs.forEach { parameterSpec ->
    register(
        functionName,
        functionBuilder<R, C>()
            .setResultFunctionWithContextWithoutReturn(parameterSpec, func)
            .build()
    )
}

fun <R, C: KnolusContext<out R>, P1, P2> KnolusContext<R>.registerMultiResultFunctionWithContext(
    functionName: String,
    firstParameterSpecs: Array<ParameterSpec<*, P1, in R, C>>,
    secondParameterSpecs: Array<ParameterSpec<*, P2, in R, C>>,
    func: suspend (context: C, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>) -> KnolusTypedValue,
) = firstParameterSpecs.forEach { firstParameterSpec ->
    secondParameterSpecs.forEach { secondParameterSpec ->
        register(
            functionName,
            functionBuilder<R, C>()
                .setResultFunctionWithContext(firstParameterSpec, secondParameterSpec, func)
                .build()
        )
    }
}

fun <R, C: KnolusContext<out R>, P1, P2> KnolusContext<R>.registerMultiResultFunctionWithContextWithoutReturn(
    functionName: String,
    firstParameterSpecs: Array<ParameterSpec<*, P1, in R, C>>,
    secondParameterSpecs: Array<ParameterSpec<*, P2, in R, C>>,
    func: suspend (context: C, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>) -> Unit,
) = firstParameterSpecs.forEach { firstParameterSpec ->
    secondParameterSpecs.forEach { secondParameterSpec ->
        register(
            functionName,
            functionBuilder<R, C>()
                .setResultFunctionWithContextWithoutReturn(firstParameterSpec, secondParameterSpec, func)
                .build()
        )
    }
}

fun <R, C: KnolusContext<out R>, P1, P2, P3> KnolusContext<R>.registerMultiResultFunctionWithContext(
    functionName: String,
    firstParameterSpecs: Array<ParameterSpec<*, P1, in R, C>>,
    secondParameterSpecs: Array<ParameterSpec<*, P2, in R, C>>,
    thirdParameterSpecs: Array<ParameterSpec<*, P3, in R, C>>,
    func: suspend (context: C, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>, thirdParameter: KorneaResult<P3>) -> KnolusTypedValue,
) = firstParameterSpecs.forEach { firstParameterSpec ->
    secondParameterSpecs.forEach { secondParameterSpec ->
        thirdParameterSpecs.forEach { thirdParameterSpec ->
            register(
                functionName,
                functionBuilder<R, C>()
                    .setResultFunctionWithContext(firstParameterSpec, secondParameterSpec, thirdParameterSpec, func)
                    .build()
            )
        }
    }
}

fun <R, C: KnolusContext<out R>, P1, P2, P3> KnolusContext<R>.registerMultiResultFunctionWithContextWithoutReturn(
    functionName: String,
    firstParameterSpecs: Array<ParameterSpec<*, P1, in R, C>>,
    secondParameterSpecs: Array<ParameterSpec<*, P2, in R, C>>,
    thirdParameterSpecs: Array<ParameterSpec<*, P3, in R, C>>,
    func: suspend (context: C, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>, thirdParameter: KorneaResult<P3>) -> Unit,
) = firstParameterSpecs.forEach { firstParameterSpec ->
    secondParameterSpecs.forEach { secondParameterSpec ->
        thirdParameterSpecs.forEach { thirdParameterSpec ->
            register(
                functionName,
                functionBuilder<R, C>()
                    .setResultFunctionWithContextWithoutReturn(firstParameterSpec, secondParameterSpec, thirdParameterSpec, func)
                    .build()
            )
        }
    }
}