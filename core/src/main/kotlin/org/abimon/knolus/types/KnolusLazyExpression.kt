package org.abimon.knolus.types

import org.abimon.knolus.*
import org.abimon.knolus.context.KnolusContext
import org.abimon.kornea.errors.common.*

data class KnolusLazyExpression(
    val startValue: KnolusTypedValue,
    val ops: Array<Pair<ExpressionOperator, KnolusTypedValue>>,
) : KnolusTypedValue.RuntimeValue<KnolusTypedValue> {
    companion object TypeInfo : KnolusTypedValue.TypeInfo<KnolusLazyExpression> {
        override val typeHierarchicalNames: Array<String> = arrayOf("Expression", "Object")

        override fun isInstance(instance: Any?): Boolean = instance is KnolusLazyExpression
        override fun asInstance(instance: Any?): KnolusLazyExpression = instance as KnolusLazyExpression
        override fun asInstanceSafe(instance: Any?): KnolusLazyExpression? = instance as? KnolusLazyExpression
    }

    override val typeInfo: KnolusTypedValue.TypeInfo<KnolusLazyExpression>
        get() = TypeInfo

    override suspend fun <T> asString(context: KnolusContext<T>): KorneaResult<String> = evaluate(context).flatMap { it.asString(context) }
    override suspend fun <T> asNumber(context: KnolusContext<T>): KorneaResult<Number> = evaluate(context).flatMap { it.asNumber(context) }
    override suspend fun <T> asBoolean(context: KnolusContext<T>): KorneaResult<Boolean> = evaluate(context).flatMap { it.asBoolean(context) }

    override suspend fun <T> evaluate(context: KnolusContext<T>): KorneaResult<KnolusTypedValue> {
        var value: KnolusTypedValue =
            if (this.startValue is KnolusTypedValue.UnsureValue<*> && this.startValue.needsEvaluation(context)) {
                this.startValue.evaluate(context).getOrBreak { return it.asType() }
            } else {
                this.startValue
            }

        val remainingOps: MutableList<Pair<ExpressionOperator, KnolusTypedValue>> = ArrayList(this.ops.size)
        remainingOps.addAll(this.ops.map { op ->
            if (op.second is KnolusTypedValue.UnsureValue<*> && (op.second as KnolusTypedValue.UnsureValue<*>).needsEvaluation(context))
                Pair(op.first, (op.second as KnolusTypedValue.UnsureValue<*>).evaluate(context).getOrBreak { return it.cast() })
            else
                op
        })

        suspend fun handleOperations(vararg operators: ExpressionOperator) {
            val ops = remainingOps.toTypedArray()
            remainingOps.clear()

            ops.forEach { pair ->
                if (pair.first !in operators) remainingOps.add(pair)
                else {
                    val op: ExpressionOperator?
                    val first: KnolusTypedValue
                    if (remainingOps.isEmpty()) {
                        op = null
                        first = value
                    } else {
                        val lastPair = remainingOps.removeAt(remainingOps.lastIndex)
                        op = lastPair.first
                        first = lastPair.second
                    }

                    val second = pair.second

                    val result = context.invokeOperator(pair.first, first, second).getOrElse(KnolusConstants.Undefined)
                    if (op == null)
                        value = result
                    else if (remainingOps.isEmpty())
                        value = context.invokeOperator(op, value, result).getOrElse(KnolusConstants.Undefined)
                    else
                        remainingOps.add(Pair(op, result))
                }
            }
        }

        //Parenthesis, already handled by flattening

        //Exponentials
        handleOperations(ExpressionOperator.EXPONENTIAL)

        //Division, Multiplication, Remainder
        handleOperations(
            ExpressionOperator.DIVIDE,
            ExpressionOperator.MULTIPLY
        )

        //Addition, Subtraction
        handleOperations(
            ExpressionOperator.PLUS,
            ExpressionOperator.MINUS
        )

        //Shift Left, Shift Right

        //Comparisons: less than and greater than

        //Comparisons: equal and not equal

        //Bitwise AND

        //Bitwise XOR

        //Bitwise OR

        //Logical AND

        //Logical OR

        //Ternary / Elvis

        return KorneaResult.successInline(value)
    }
}