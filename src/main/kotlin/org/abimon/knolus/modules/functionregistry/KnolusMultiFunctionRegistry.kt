package org.abimon.knolus.modules.functionregistry

import org.abimon.knolus.*
import org.abimon.knolus.types.KnolusTypedValue

fun <P> KnolusContext.registerMultiFunction(
    functionName: String,
    parameterSpecs: Array<ParameterSpec<*, P>>,
    func: suspend (context: KnolusContext, parameter: P) -> KnolusTypedValue,
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
    func: suspend (context: KnolusContext, parameter: P) -> Unit,
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
    func: suspend (context: KnolusContext, firstParameter: P1, secondParameter: P2) -> KnolusTypedValue,
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
    func: suspend (context: KnolusContext, firstParameter: P1, secondParameter: P2) -> Unit,
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
    func: suspend (context: KnolusContext, firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> KnolusTypedValue,
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
    func: suspend (context: KnolusContext, firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> Unit,
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