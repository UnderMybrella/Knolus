package dev.brella.knolus.restrictions

import dev.brella.knolus.*
import dev.brella.knolus.context.KnolusContext
import dev.brella.knolus.types.KnolusTypedValue
import org.abimon.kornea.errors.common.*

@ExperimentalUnsignedTypes
class CompoundKnolusRestriction<R>(val restrictions: List<KnolusRestriction<R>>, val startingValue: () -> KorneaResult<R>, val emptyResult: (empty: KorneaResult.Empty) -> KorneaResult<R>) : KnolusRestriction<R> {
    companion object {
        fun <R> of(vararg restrictions: KnolusRestriction<R>, emptyResult: (empty: KorneaResult.Empty) -> KorneaResult<R>) =
            CompoundKnolusRestriction(restrictions.toList(), KorneaResult.Companion::empty, emptyResult)

        fun fromPermissive(vararg restrictions: KnolusRestriction<StaticSuccess>) =
            CompoundKnolusRestriction(restrictions.toList(), KorneaResult.Companion::empty) { KorneaResult.success() }

        fun <R> fromRestrictive(vararg restrictions: KnolusRestriction<R>) =
            CompoundKnolusRestriction(restrictions.toList(), KorneaResult.Companion::empty) { KorneaResult.empty() }
    }

    constructor(restrictions: Array<KnolusRestriction<R>>, emptyResult: (empty: KorneaResult.Empty) -> KorneaResult<R>) : this(restrictions.toList(), KorneaResult.Companion::empty, emptyResult)

    override fun canGetVariable(context: KnolusContext<R>, variableName: String): KorneaResult<R> =
        restrictions.fold(startingValue()) { acc, r ->
            acc.switchIfEmpty { r.canGetVariable(context, variableName) }
        }.switchIfEmpty(emptyResult)

    override fun canAskParentForVariable(child: KnolusContext<R>, parent: KnolusContext<R>, variableName: String): KorneaResult<R> =
        restrictions.fold(startingValue()) { acc, r ->
            acc.switchIfEmpty { r.canAskParentForVariable(child, parent, variableName) }
        }.switchIfEmpty(emptyResult)

    override fun canSetVariable(
        context: KnolusContext<R>,
        variableName: String,
        variableValue: KnolusTypedValue,
        attemptedParentalSet: KorneaResult<KnolusTypedValue?>?,
    ): KorneaResult<R> = restrictions.fold(startingValue()) { acc, r ->
        acc.switchIfEmpty { r.canSetVariable(context, variableName, variableValue, attemptedParentalSet) }
    }.switchIfEmpty(emptyResult)

    override fun <T> canRegisterFunction(
        context: KnolusContext<R>,
        functionName: String,
        function: KnolusFunction<T, R, *>,
        attemptedParentalRegister: KorneaResult<KnolusFunction<T, R, *>>?,
    ): KorneaResult<R> = restrictions.fold(startingValue()) { acc, r ->
        acc.switchIfEmpty { r.canRegisterFunction(context, functionName, function, attemptedParentalRegister) }
    }.switchIfEmpty(emptyResult)

    override fun canAskForFunction(
        context: KnolusContext<R>,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
    ): KorneaResult<R> =
        restrictions.fold(startingValue()) { acc, r ->
            acc.switchIfEmpty { r.canAskForFunction(context, functionName, functionParameters) }
        }.switchIfEmpty(emptyResult)

    override fun canAskParentForFunction(
        child: KnolusContext<R>,
        parent: KnolusContext<R>,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
    ): KorneaResult<R> = restrictions.fold(startingValue()) { acc, r ->
        acc.switchIfEmpty { r.canAskParentForFunction(child, parent, functionName, functionParameters) }
    }.switchIfEmpty(emptyResult)

    override fun canTryFunction(
        context: KnolusContext<R>,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
        function: KnolusFunction<KnolusTypedValue?, R, *>,
    ): KorneaResult<R> = restrictions.fold(startingValue()) { acc, r ->
        acc.switchIfEmpty { r.canTryFunction(context, functionName, functionParameters, function) }
    }.switchIfEmpty(emptyResult)

    override fun canRunFunction(
        context: KnolusContext<R>,
        function: KnolusFunction<KnolusTypedValue?, R, *>,
        parameters: Map<String, KnolusTypedValue>,
    ): KorneaResult<R> = restrictions.fold(startingValue()) { acc, r ->
        acc.switchIfEmpty { r.canRunFunction(context, function, parameters) }
    }.switchIfEmpty(emptyResult)

    override fun canAskForMemberFunction(
        context: KnolusContext<R>,
        member: KnolusTypedValue,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
    ): KorneaResult<R> = restrictions.fold(startingValue()) { acc, r ->
        acc.switchIfEmpty { r.canAskForMemberFunction(context, member, functionName, functionParameters) }
    }.switchIfEmpty(emptyResult)

    override fun canAskForMemberPropertyGetter(
        context: KnolusContext<R>,
        member: KnolusTypedValue,
        propertyName: String,
    ): KorneaResult<R> =
        restrictions.fold(startingValue()) { acc, r ->
            acc.switchIfEmpty { r.canAskForMemberPropertyGetter(context, member, propertyName) }
        }.switchIfEmpty(emptyResult)

    override fun canAskForOperatorFunction(
        context: KnolusContext<R>,
        operator: ExpressionOperator,
        a: KnolusTypedValue,
        b: KnolusTypedValue,
    ): KorneaResult<R> =
        restrictions.fold(startingValue()) { acc, r ->
            acc.switchIfEmpty { r.canAskForOperatorFunction(context, operator, a, b) }
        }.switchIfEmpty(emptyResult)

    override fun canAskForCastingOperatorFunction(
        context: KnolusContext<R>,
        self: KnolusTypedValue,
        castingTo: KnolusTypedValue.TypeInfo<*>
    ): KorneaResult<R> =
        restrictions.fold(startingValue()) { acc, r ->
            acc.switchIfEmpty { r.canAskForCastingOperatorFunction(context, self, castingTo) }
        }.switchIfEmpty(emptyResult)

    override fun createSubroutineRestrictions(
        currentContext: KnolusContext<R>,
        function: KnolusFunction<KnolusTypedValue?, R, *>,
        parameters: Map<String, KnolusTypedValue>,
    ): KorneaResult<KnolusRestriction<R>> =
        restrictions.fold(KorneaResult.success(mutableListOf<KnolusRestriction<R>>())) { acc, r ->
            acc.flatMap { list -> r.createSubroutineRestrictions(currentContext, function, parameters).map(list::withElement) }
        }.map { list -> CompoundKnolusRestriction(list, startingValue, emptyResult) }
}