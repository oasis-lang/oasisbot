import java.util.*
import kotlin.concurrent.thread
import kotlin.math.pow

class CallFrame(
    val function: OasisCallable,
    val callSite: Int,
    var returnValue: Any? = null
)

class Interpreter : Expression.Visitor<Any?>, Statement.Visitor<Boolean> {
    var environment = Environment()
    val callStack = Stack<CallFrame>()

    init {
        // Push a dummy frame to the call stack so that we can always peek at the top frame.
        resetCallStack()
    }

    fun resetState() {
        while (environment.enclosing != null) {
            environment = environment.enclosing!!
        }
        resetCallStack()
    }

    fun resetCallStack() {
        callStack.clear()
        callStack.push(
            CallFrame(
                object : OasisCallable {
                    override fun arity(): Int {
                        return 0
                    }

                    override fun name(): String {
                        return "<script>"
                    }

                    override fun call(interpreter: Interpreter, arguments: List<Any?>) {
                        // nothing to do
                    }
                },
                callSite = 0
            )
        )
    }

    fun eval(expression: Expression): Any? {
        return expression.accept(this)
    }

    fun execute(statement: Statement): Boolean {
        return statement.accept(this)
    }

    fun execute(statementList: StatementList): Boolean {
        environment = Environment(environment)
        for (statement in statementList.stmts) {
            if (execute(statement)) {
                environment = environment.enclosing!!
                return true
            }
        }
        environment = environment.enclosing!!
        return false
    }

    fun call(function: OasisCallable, line: Int, arguments: List<Any?>): Any? {
        callStack.push(CallFrame(function, line))
        try {
            function.call(this, arguments)
        } catch (e: RuntimeError) {
            throw OasisError(line, e.message!!)
        }
        val frame = callStack.pop()
        return frame.returnValue
    }

    override fun visitNumberLiteral(node: NumberLiteral): Any {
        return node.number
    }

    override fun visitStringLiteral(node: StringLiteral): Any {
        return node.string
    }

    override fun visitNilLiteral(node: NilLiteral): Any? {
        return null
    }

    override fun visitBoolLiteral(node: BoolLiteral): Any {
        return node.bool
    }

    override fun visitFunctionLiteral(node: FunctionLiteral): Any {
        return OasisFunction(
            name = node.name,
            parameters = node.args,
            returnType = node.returnType,
            closure = Environment(environment),
            body = node.body
        )
    }

    override fun visitObjectLiteral(node: ObjectLiteral): Any {
        val body = mutableMapOf<String, Any?>()
        for (pair in node.properties) {
            body[pair.key] = eval(pair.value)
        }
        return Prototype(
            if (node.parent != null) eval(node.parent) as Prototype else null,
            body
        )
    }

    override fun visitListLiteral(node: ListLiteral): Any {
        return node.elements.map { eval(it) }
    }

    override fun visitDictLiteral(node: DictLiteral): Any {
        val body = mutableMapOf<Any?, Any?>()
        for (pair in node.elements) {
            body[eval(pair.key)] = eval(pair.value)
        }
        return body
    }

    override fun visitVariableReference(node: VariableReference): Any? {
        // FOR DEBUGGING
        if (node.name == "debug") {
            return Stream()
        }
        if(node.name == "debugPrint") {
            return object : OasisCallable {
                override fun arity(): Int {
                    return 1
                }

                override fun name(): String {
                    return "debugPrint"
                }

                override fun call(interpreter: Interpreter, arguments: List<Any?>) {
                    println(arguments[0])
                }
            }
        }
        try {
            return environment.get(node.name)
        } catch (e: RuntimeError) {
            throw OasisError(node.line, e.message!!)
        }
    }

    override fun visitTupleExpression(node: TupleExpression): Any? {
        return Tuple(*node.items.map { eval(it) }.toTypedArray())
    }

    override fun visitPropertyAccess(node: PropertyAccess): Any? {
        return when (val obj = eval(node.obj)) {
            is Prototype -> obj.get(node.name)
            // TODO: Add support for primitives with operations
            else -> throw OasisError(node.line, "Cannot access property of non-object")
        }
    }

    override fun visitIndexOperator(node: IndexOperator): Any? {
        val obj = eval(node.obj)
        val index = eval(node.index)
        return when (obj) {
            is List<*> -> obj[(index as Double).toInt()]
            is Map<*, *> -> obj[index]
            is Tuple -> obj.get((index as Double).toInt())
            else -> throw OasisError(node.line, "Cannot index non-indexable object")
        }
    }

    override fun visitFunctionCall(node: FunctionCall): Any? {
        val callee = eval(node.fn)
        val arguments = node.arguments.map { eval(it) }
        if (callee !is OasisCallable) {
            throw OasisError(node.line, "Cannot call non-callable object")
        }
        if (callee.arity() != arguments.size) {
            throw OasisError(
                node.line,
                "Expected ${callee.arity()} arguments but got ${arguments.size} at function ${callee.name()}"
            )
        }
        return call(callee, node.line, arguments)
    }

    private fun isTruthy(obj: Any?): Boolean {
        if (obj == null) return false
        if (obj is Boolean) return obj
        if (obj is Number) return obj != 0.0
        if (obj is String) return obj.isNotEmpty()
        if (obj is List<*>) return obj.isNotEmpty()
        if (obj is Map<*, *>) return obj.isNotEmpty()
        return true
    }

    override fun visitBinOp(node: BinOp): Any? {
        when (node.op.lexeme) {
            "+", "-", "*", "/", "%", "^" -> {
                val left = eval(node.lhs)
                val right = eval(node.rhs)
                if (left is Number && right is Number) {
                    return when (node.op.lexeme) {
                        "+" -> left.toDouble() + right.toDouble()
                        "-" -> left.toDouble() - right.toDouble()
                        "*" -> left.toDouble() * right.toDouble()
                        "/" -> left.toDouble() / right.toDouble()
                        "%" -> left.toDouble() % right.toDouble()
                        "^" -> left.toDouble().pow(right.toDouble())
                        else -> throw OasisError(node.line, "Invalid operator")
                    }
                } else {
                    throw OasisError(node.line, "Invalid operands for operator")
                }
            }
            "==", "!=", "<", ">", "<=", ">=" -> {
                val left = eval(node.lhs)
                val right = eval(node.rhs)
                return when (node.op.lexeme) {
                    "==" -> left == right
                    "!=" -> left != right
                    else -> if (left is Number && right is Number) {
                        when (node.op.lexeme) {
                            "<" -> left.toDouble() < right.toDouble()
                            ">" -> left.toDouble() > right.toDouble()
                            "<=" -> left.toDouble() <= right.toDouble()
                            ">=" -> left.toDouble() >= right.toDouble()
                            else -> throw OasisError(node.line, "Invalid operator")
                        }
                    } else {
                        throw OasisError(node.line, "Invalid operands for operator")
                    }
                }
            }
            "and", "or", "&&", "||" -> {
                val left = eval(node.lhs)
                if (node.op.lexeme in listOf("and", "&&") && !isTruthy(left)) return false
                if (node.op.lexeme in listOf("or", "||") && isTruthy(left)) return true
                return eval(node.rhs)
            }
            "=" -> {
                when (node.lhs) {
                    is VariableReference -> {
                        val value = eval(node.rhs)
                        try {
                            environment.assign(node.lhs.name, value)
                        } catch (e: RuntimeError) {
                            throw OasisError(node.line, e.message!!)
                        }
                        return value
                    }
                    is PropertyAccess -> {
                        val obj = eval(node.lhs.obj)
                        if (obj is Prototype) {
                            val value = eval(node.rhs)
                            obj.set(node.lhs.name, value)
                            return value
                        } else {
                            throw OasisError(node.line, "Cannot assign to non-object")
                        }
                    }
                    is IndexOperator -> {
                        val obj = eval(node.lhs.obj)
                        val index = eval(node.lhs.index)
                        if (obj is List<*>) {
                            if (index is Int) {
                                val objList = (obj as? List<Any?> ?: throw OasisError(
                                    node.line,
                                    "Cannot assign to non-object"
                                )).toMutableList()
                                val value = eval(node.rhs)
                                objList[index] = value
                                return value
                            } else {
                                throw OasisError(node.line, "Index must be an integer")
                            }
                        } else if (obj is Map<*, *>) {
                            val objMap = (obj as? Map<Any?, Any?> ?: throw OasisError(
                                node.line,
                                "Invalid map"
                            )).toMutableMap()
                            val value = eval(node.rhs)
                            objMap[index] = value
                            return value
                        } else {
                            throw OasisError(node.line, "Cannot index non-indexable object")
                        }
                    }
                    else -> throw OasisError(node.line, "Invalid assignment target")
                }
            }
            else -> throw OasisError(node.line, "Invalid operator")
        }
    }

    override fun visitUnaryOp(node: UnaryOp): Any? {
        val right = eval(node.rhs)
        return when (node.op.lexeme) {
            "-" -> -(right as Double)
            "not", "!" -> !isTruthy(right)
            "recv" -> {
                val stream = eval(node.rhs)
                if (stream !is Stream) throw OasisError(node.line, "Cannot receive from non-stream")
                return stream.receive()
            }
            else -> throw OasisError(node.line, "Invalid operator")
        }
    }

    override fun visitImportExpression(expr: ImportExpression): Any? {
        TODO("Not yet implemented")
    }

    override fun visitImportStatement(node: ImportStatement): Boolean {
        TODO("Not yet implemented")
    }

    override fun visitMatchStatement(node: MatchStatement): Boolean {
        val target = eval(node.expr)
        for (case in node.cases) {
            if (eval(case.first) == target) {
                return execute(case.second)
            }
        }
        if (node.elseBranch != null) {
            return execute(node.elseBranch)
        }
        return false
    }

    override fun visitIteratorStatement(node: IteratorStatement): Boolean {
        TODO("Not yet implemented")
    }

    override fun visitForStatement(node: ForStatement): Boolean {
        if (execute(node.initializer)) return true
        while (isTruthy(eval(node.condition))) {
            if (execute(node.body)) return true
            if (execute(node.increment)) return true
        }
        return false
    }

    override fun visitWhileStatement(node: WhileStatement): Boolean {
        TODO("Not yet implemented")
    }

    override fun visitIfStatement(node: IfStatement): Boolean {
        TODO("Not yet implemented")
    }

    override fun visitContinueStatement(node: ContinueStatement): Boolean {
        TODO("Not yet implemented")
    }

    override fun visitBreakStatement(node: BreakStatement): Boolean {
        TODO("Not yet implemented")
    }

    override fun visitSpawnStatement(node: SpawnStatement): Boolean {
        if (node.expr !is FunctionCall) {
            throw OasisError(node.line, "Spawn statement must be a function call")
        }
        thread { eval(node.expr) }
        return false
    }

    override fun visitDeclarationStatement(node: DeclarationStatement): Boolean {
        try {
            environment.define(node.name, eval(node.value))
        } catch (e: RuntimeError) {
            throw OasisError(node.line, e.message!!)
        }
        return false
    }

    override fun visitExportStatement(node: ExportStatement): Boolean {
        TODO("Not yet implemented")
    }

    override fun visitSendStatement(node: SendStatement): Boolean {
        val stream = eval(node.receiver)
        if (stream !is Stream) {
            throw OasisError(node.line, "Cannot send to non-stream")
        }
        stream.send(eval(node.value))
        return false
    }

    override fun visitReturnStatement(node: ReturnStatement): Boolean {
        if (callStack.isEmpty()) {
            throw OasisError(node.line, "Cannot return outside of function")
        }

        callStack.peek().returnValue = eval(node.value)
        return true
    }

    override fun visitExpressionStmt(node: ExpressionStatement): Boolean {
        eval(node.expr)
        return false
    }

    override fun visitStatementList(node: StatementList): Boolean {
        return execute(node)
    }

}