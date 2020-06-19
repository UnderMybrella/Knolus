package org.abimon.knolus.modules.functionregistry

import org.abimon.knolus.*
import org.abimon.knolus.context.KnolusContext
import org.abimon.knolus.types.KnolusTypedValue
import org.abimon.kornea.errors.common.KorneaResult

fun <R, C: KnolusContext<out R>, P> KnolusContext<R>.registerMultiFunction(
    functionName: String,
    parameterSpecs: Array<ParameterSpec<*, P, in R, C>>,
    func: suspend (context: C, parameter: P) -> KnolusTypedValue,
) = parameterSpecs.forEach { parameterSpec ->
    register(
        functionName,
        functionBuilder<R, C>()
            .setFunction(parameterSpec, func)
            .build()
    )
}

fun <R, C: KnolusContext<out R>, P> KnolusContext<R>.registerMultiFunctionWithoutReturn(
    functionName: String,
    parameterSpecs: Array<ParameterSpec<*, P, in R, C>>,
    func: suspend (context: C, parameter: P) -> Unit,
) = parameterSpecs.forEach { parameterSpec ->
    register(
        functionName,
        functionBuilder<R, C>()
            .setFunctionWithoutReturn(parameterSpec, func)
            .build()
    )
}

fun <R, C: KnolusContext<out R>, P1, P2> KnolusContext<R>.registerMultiFunction(
    functionName: String,
    firstParameterSpecs: Array<ParameterSpec<*, P1, in R, C>>,
    secondParameterSpecs: Array<ParameterSpec<*, P2, in R, C>>,
    func: suspend (context: C, firstParameter: P1, secondParameter: P2) -> KnolusTypedValue,
) = firstParameterSpecs.forEach { firstParameterSpec ->
    secondParameterSpecs.forEach { secondParameterSpec ->
        register(
            functionName,
            functionBuilder<R, C>()
                .setFunction(firstParameterSpec, secondParameterSpec, func)
                .build()
        )
    }
}

fun <R, C: KnolusContext<out R>, P1, P2> KnolusContext<R>.registerMultiFunctionWithoutReturn(
    functionName: String,
    firstParameterSpecs: Array<ParameterSpec<*, P1, in R, C>>,
    secondParameterSpecs: Array<ParameterSpec<*, P2, in R, C>>,
    func: suspend (context: C, firstParameter: P1, secondParameter: P2) -> Unit,
) = firstParameterSpecs.forEach { firstParameterSpec ->
    secondParameterSpecs.forEach { secondParameterSpec ->
        register(
            functionName,
            functionBuilder<R, C>()
                .setFunctionWithoutReturn(firstParameterSpec, secondParameterSpec, func)
                .build()
        )
    }
}

fun <R, C: KnolusContext<out R>, P1, P2, P3> KnolusContext<R>.registerMultiFunction(
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
                    .setFunction(firstParameterSpec, secondParameterSpec, thirdParameterSpec, func)
                    .build()
            )
        }
    }
}

fun <R, C: KnolusContext<out R>, P1, P2, P3> KnolusContext<R>.registerMultiFunctionWithoutReturn(
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
                    .setFunctionWithoutReturn(firstParameterSpec, secondParameterSpec, thirdParameterSpec, func)
                    .build()
            )
        }
    }
}

/** Result */

fun <R, C: KnolusContext<out R>, P> KnolusContext<R>.registerMultiResultFunction(
    functionName: String,
    parameterSpecs: Array<ParameterSpec<*, P, in R, C>>,
    func: suspend (context: C, parameter: KorneaResult<P>) -> KnolusTypedValue,
) = parameterSpecs.forEach { parameterSpec ->
    register(
        functionName,
        functionBuilder<R, C>()
            .setResultFunction(parameterSpec, func)
            .build()
    )
}

fun <R, C: KnolusContext<out R>, P> KnolusContext<R>.registerMultiResultFunctionWithoutReturn(
    functionName: String,
    parameterSpecs: Array<ParameterSpec<*, P, in R, C>>,
    func: suspend (context: C, parameter: KorneaResult<P>) -> Unit,
) = parameterSpecs.forEach { parameterSpec ->
    register(
        functionName,
        functionBuilder<R, C>()
            .setResultFunctionWithoutReturn(parameterSpec, func)
            .build()
    )
}

fun <R, C: KnolusContext<out R>, P1, P2> KnolusContext<R>.registerMultiResultFunction(
    functionName: String,
    firstParameterSpecs: Array<ParameterSpec<*, P1, in R, C>>,
    secondParameterSpecs: Array<ParameterSpec<*, P2, in R, C>>,
    func: suspend (context: C, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>) -> KnolusTypedValue,
) = firstParameterSpecs.forEach { firstParameterSpec ->
    secondParameterSpecs.forEach { secondParameterSpec ->
        register(
            functionName,
            functionBuilder<R, C>()
                .setResultFunction(firstParameterSpec, secondParameterSpec, func)
                .build()
        )
    }
}

fun <R, C: KnolusContext<out R>, P1, P2> KnolusContext<R>.registerMultiResultFunctionWithoutReturn(
    functionName: String,
    firstParameterSpecs: Array<ParameterSpec<*, P1, in R, C>>,
    secondParameterSpecs: Array<ParameterSpec<*, P2, in R, C>>,
    func: suspend (context: C, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>) -> Unit,
) = firstParameterSpecs.forEach { firstParameterSpec ->
    secondParameterSpecs.forEach { secondParameterSpec ->
        register(
            functionName,
            functionBuilder<R, C>()
                .setResultFunctionWithoutReturn(firstParameterSpec, secondParameterSpec, func)
                .build()
        )
    }
}

fun <R, C: KnolusContext<out R>, P1, P2, P3> KnolusContext<R>.registerMultiResultFunction(
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
                    .setResultFunction(firstParameterSpec, secondParameterSpec, thirdParameterSpec, func)
                    .build()
            )
        }
    }
}

fun <R, C: KnolusContext<out R>, P1, P2, P3> KnolusContext<R>.registerMultiResultFunctionWithoutReturn(
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
                    .setResultFunctionWithoutReturn(firstParameterSpec, secondParameterSpec, thirdParameterSpec, func)
                    .build()
            )
        }
    }
}