abstract class Expression {
    interface Visitor<T> {
        fun visitNumberLiteral(node: NumberLiteral): T
        fun visitStringLiteral(node: StringLiteral): T
        fun visitNilLiteral(node: NilLiteral): T
        fun visitBoolLiteral(node: BoolLiteral): T
        fun visitFunctionLiteral(node: FunctionLiteral): T
        fun visitObjectLiteral(node: ObjectLiteral): T
        fun visitListLiteral(node: ListLiteral): T
        fun visitDictLiteral(node: DictLiteral): T
        fun visitVariableReference(node: VariableReference): T
        fun visitTupleExpression(node: TupleExpression): T
        fun visitPropertyAccess(node: PropertyAccess): T
        fun visitIndexOperator(node: IndexOperator): T
        fun visitFunctionCall(node: FunctionCall): T
        fun visitBinOp(node: BinOp): T
        fun visitUnaryOp(node: UnaryOp): T
    }

    abstract fun <T> accept(visitor: Visitor<T>): T
}

abstract class Statement {
    interface Visitor<T> {
        fun visitReturnStatement(node: ReturnStatement): T
        fun visitExpressionStmt(node: ExpressionStatement): T
        fun visitStatementList(node: StatementList): T
    }

    abstract fun <T> accept(visitor: Visitor<T>): T
}

class NumberLiteral(val number: Double) : Expression() {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitNumberLiteral(this)
    }

    override fun toString(): String {
        return number.toString()
    }
}

class StringLiteral(val string: String) : Expression() {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitStringLiteral(this)
    }
}

class NilLiteral : Expression() {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitNilLiteral(this)
    }
}

class BoolLiteral(val bool: Boolean) : Expression() {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitBoolLiteral(this)
    }
}

class FunctionLiteral(val args: Map<String, Constraint>, val returnType: Constraint, val body: Statement, val name: String) : Expression() {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitFunctionLiteral(this)
    }
}

class ObjectLiteral(val properties: Map<String, Expression>, val parent: Expression?) : Expression() {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitObjectLiteral(this)
    }
}

class ListLiteral(val elements: List<Expression>) : Expression() {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitListLiteral(this)
    }
}

class DictLiteral(val elements: Map<Expression, Expression>) : Expression() {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitDictLiteral(this)
    }
}

class VariableReference(val name: String) : Expression() {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitVariableReference(this)
    }
}

class TupleExpression(val items: List<Expression>) : Expression() {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitTupleExpression(this)
    }
}

class PropertyAccess(val obj: Expression, val name: String) : Expression() {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitPropertyAccess(this)
    }
}

class IndexOperator(val obj: Expression, val index: Expression) : Expression() {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitIndexOperator(this)
    }
}

class FunctionCall(val fn: Expression, val arguments: MutableList<Expression>) : Expression() {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitFunctionCall(this)
    }
}

class BinOp(val lhs: Expression, val op: TokenType, val rhs: Expression) : Expression() {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitBinOp(this)
    }

    override fun toString(): String {
        return "BinOp($lhs, $op, $rhs)"
    }
}

class UnaryOp(val op: TokenType, val rhs: Expression) : Expression() {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitUnaryOp(this)
    }

    override fun toString(): String {
        return "UnaryOp($op, $rhs)"
    }
}

class ReturnStatement(val value: Expression) : Statement() {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitReturnStatement(this)
    }
}

class ExpressionStatement(val expr: Expression) : Statement() {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitExpressionStmt(this)
    }
}

class StatementList(val stmts: List<Statement>) : Statement() {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitStatementList(this)
    }
}