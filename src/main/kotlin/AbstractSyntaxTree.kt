abstract class Expression(open val line: Int) {
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
        fun visitImportExpression(expr: ImportExpression): T
    }

    abstract fun <T> accept(visitor: Visitor<T>): T
}

abstract class Statement(open val line: Int) {
    interface Visitor<T> {
        fun visitImportStatement(node: ImportStatement): T
        fun visitMatchStatement(node: MatchStatement): T
        fun visitIteratorStatement(node: IteratorStatement): T
        fun visitForStatement(node: ForStatement): T
        fun visitWhileStatement(node: WhileStatement): T
        fun visitIfStatement(node: IfStatement): T
        fun visitContinueStatement(node: ContinueStatement): T
        fun visitBreakStatement(node: BreakStatement): T
        fun visitSpawnStatement(node: SpawnStatement): T
        fun visitDeclarationStatement(node: DeclarationStatement): T
        fun visitExportStatement(node: ExportStatement): T
        fun visitSendStatement(node: SendStatement): T
        fun visitReturnStatement(node: ReturnStatement): T
        fun visitExpressionStmt(node: ExpressionStatement): T
        fun visitStatementList(node: StatementList): T
    }

    abstract fun <T> accept(visitor: Visitor<T>): T
}

class NumberLiteral(override val line: Int, val number: Double) : Expression(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitNumberLiteral(this)
    }

    override fun toString(): String {
        return number.toString()
    }
}

class StringLiteral(override val line: Int, val string: String) : Expression(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitStringLiteral(this)
    }

    override fun toString(): String {
        return "\"$string\""
    }
}

class NilLiteral(override val line: Int) : Expression(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitNilLiteral(this)
    }

    override fun toString(): String {
        return "nil"
    }
}

class BoolLiteral(override val line: Int, val bool: Boolean) : Expression(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitBoolLiteral(this)
    }

    override fun toString(): String {
        return bool.toString()
    }
}

class FunctionLiteral(
    override val line: Int,
    val args: List<Pair<String, Constraint>>,
    val returnType: Constraint,
    val body: Statement,
    var name: String
) : Expression(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitFunctionLiteral(this)
    }

    override fun toString(): String {
        return "fn $name(${args.map { "${it.first}: ${it.second}" }.joinToString(", ")}): $returnType { $body }"
    }
}

class ObjectLiteral(override val line: Int, val properties: Map<String, Expression>, val parent: Expression?) :
    Expression(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitObjectLiteral(this)
    }

    override fun toString(): String {
        return "object${if (parent != null) " : $parent" else ""} { " +
                properties.map { "${it.key} = ${it.value}" }.joinToString(" ") +
                " }"
    }
}

class ListLiteral(override val line: Int, val elements: List<Expression>) : Expression(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitListLiteral(this)
    }

    override fun toString(): String {
        return elements.joinToString(", ", "[", "]")
    }
}

class DictLiteral(override val line: Int, val elements: Map<Expression, Expression>) : Expression(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitDictLiteral(this)
    }

    override fun toString(): String {
        return "dict { ${
            elements.map { "${if (it.key is StringLiteral) "\"${(it.key as StringLiteral).string}\"" else "(${it.key})"} : ${it.value}" }
                .joinToString(", ")
        } }"
    }
}

class VariableReference(override val line: Int, val name: String) : Expression(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitVariableReference(this)
    }

    override fun toString(): String {
        return name
    }
}

class TupleExpression(override val line: Int, val items: List<Expression>) : Expression(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitTupleExpression(this)
    }

    override fun toString(): String {
        return "(${items.joinToString(", ")})"
    }
}

class PropertyAccess(override val line: Int, val obj: Expression, val name: String) : Expression(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitPropertyAccess(this)
    }

    override fun toString(): String {
        return "$obj:$name"
    }
}

class IndexOperator(override val line: Int, val obj: Expression, val index: Expression) : Expression(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitIndexOperator(this)
    }

    override fun toString(): String {
        return "$obj[$index]"
    }
}

class FunctionCall(override val line: Int, val fn: Expression, val arguments: MutableList<Expression>) :
    Expression(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitFunctionCall(this)
    }

    override fun toString(): String {
        return "$fn(${arguments.joinToString(", ")})"
    }
}

class BinOp(val lhs: Expression, val op: Token, val rhs: Expression) : Expression(op.line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitBinOp(this)
    }

    override fun toString(): String {
        return "$lhs ${op.lexeme} $rhs"
    }
}

class UnaryOp(val op: Token, val rhs: Expression) : Expression(op.line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitUnaryOp(this)
    }

    override fun toString(): String {
        return "${op.lexeme} $rhs)"
    }
}

class ImportExpression(override val line: Int, val path: String) : Expression(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitImportExpression(this)
    }

    override fun toString(): String {
        return "import $path"
    }
}

class ImportStatement(override val line: Int, val path: String) : Statement(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitImportStatement(this)
    }

    override fun toString(): String {
        return "import $path"
    }
}

class MatchStatement(
    override val line: Int,
    val expr: Expression,
    val cases: List<Pair<Expression, Statement>>,
    val elseBranch: Statement?
) : Statement(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitMatchStatement(this)
    }

    override fun toString(): String {
        return "match $expr { " +
                cases.joinToString(" ") { "${it.first} := ${it.second}" } +
                (if (elseBranch != null) " else := $elseBranch " else "") +
                "}"
    }
}

class IteratorStatement(override val line: Int, val name: String, val iterator: Expression, val body: Statement) :
    Statement(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitIteratorStatement(this)
    }

    override fun toString(): String {
        return "for item $name in $iterator { $body }"
    }
}

class ForStatement(
    override val line: Int,
    val initializer: Statement,
    val condition: Expression,
    val increment: Statement,
    val body: Statement
) : Statement(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitForStatement(this)
    }

    override fun toString(): String {
        return "for $initializer | $condition | $increment { $body }"
    }
}

class WhileStatement(override val line: Int, val condition: Expression, val body: Statement) : Statement(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitWhileStatement(this)
    }

    override fun toString(): String {
        return "while $condition { $body }"
    }
}

class IfStatement(
    override val line: Int,
    val condition: Expression,
    val thenBranch: Statement,
    val elseBranch: Statement?
) : Statement(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitIfStatement(this)
    }

    override fun toString(): String {
        return "if $condition { $thenBranch }" +
                if (elseBranch != null) " else { $elseBranch }" else ""
    }
}

class ContinueStatement(override val line: Int) : Statement(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitContinueStatement(this)
    }

    override fun toString(): String {
        return "continue"
    }
}

class BreakStatement(override val line: Int) : Statement(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitBreakStatement(this)
    }

    override fun toString(): String {
        return "break"
    }
}

class SpawnStatement(override val line: Int, val expr: Expression) : Statement(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitSpawnStatement(this)
    }

    override fun toString(): String {
        return "spawn $expr"
    }
}

class DeclarationStatement(override val line: Int, val name: String, val value: Expression, val const: Boolean) :
    Statement(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitDeclarationStatement(this)
    }

    override fun toString(): String {
        return "let${if (const) " const" else ""} $name = $value"
    }
}

class ExportStatement(override val line: Int, val names: List<String>) : Statement(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitExportStatement(this)
    }

    override fun toString(): String {
        return "export { ${names.joinToString(", ")} }"
    }
}

class SendStatement(override val line: Int, val value: Expression, val receiver: Expression) : Statement(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitSendStatement(this)
    }

    override fun toString(): String {
        return "send $value => $receiver"
    }
}

class ReturnStatement(override val line: Int, val value: Expression) : Statement(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitReturnStatement(this)
    }

    override fun toString(): String {
        return "return $value"
    }
}

class ExpressionStatement(override val line: Int, val expr: Expression) : Statement(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitExpressionStmt(this)
    }

    override fun toString(): String {
        return expr.toString()
    }
}

class StatementList(override val line: Int, val stmts: List<Statement>) : Statement(line) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitStatementList(this)
    }

    override fun toString(): String {
        return stmts.joinToString(" ")
    }
}