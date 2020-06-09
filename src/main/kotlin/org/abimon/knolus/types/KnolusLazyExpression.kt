package org.abimon.knolus.types

import org.abimon.knolus.ExpressionOperator
import org.abimon.knolus.KnolusContext
import org.abimon.knolus.KnolusUnion
import org.abimon.knolus.getOrElse

data class KnolusLazyExpression(
    val startValue: KnolusTypedValue,
    val ops: Array<Pair<ExpressionOperator, KnolusTypedValue>>,
) : KnolusTypedValue.RuntimeValue {
    companion object TypeInfo : KnolusTypedValue.TypeInfo<KnolusLazyExpression> {
        override val typeHierarchicalNames: Array<String> = arrayOf("Expression", "Object")

        override fun isInstance(value: KnolusTypedValue): Boolean = value is KnolusLazyExpression
    }

    override val typeInfo: KnolusTypedValue.TypeInfo<KnolusLazyExpression>
        get() = TypeInfo

    override suspend fun asNumber(context: KnolusContext): Number =
        evaluate(context).asNumber(context)

    override suspend fun asString(context: KnolusContext): String =
        evaluate(context).asString(context)

    override suspend fun asBoolean(context: KnolusContext): Boolean =
        evaluate(context).asBoolean(context)

    override suspend fun evaluate(context: KnolusContext): KnolusTypedValue {
        var value: KnolusTypedValue =
            if (this.startValue is KnolusTypedValue.UnsureValue && this.startValue.needsEvaluation(context))
                this.startValue.evaluate(context)
            else
                this.startValue

        val remainingOps: MutableList<Pair<ExpressionOperator, KnolusTypedValue>> = ArrayList(this.ops.size)
        remainingOps.addAll(this.ops.map { op ->
            if (op.second is KnolusTypedValue.UnsureValue && (op.second as KnolusTypedValue.UnsureValue).needsEvaluation(
                    context)
            )
                Pair(op.first, (op.second as KnolusTypedValue.UnsureValue).evaluate(context))
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

        return value
    }
}