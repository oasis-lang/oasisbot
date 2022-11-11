var hadError = false

var interpreter: Interpreter = Interpreter()
var code = mutableListOf<String>()

fun main(args: Array<String>) {
    runPrompt()
}

fun error(line: Int, message: String) {
    report(line, "", message)
}

fun report(line: Int, where: String, message: String) {
    System.err.println("(line $line) Error $where: $message")
    hadError = true
}

fun handleError(interpreter: Interpreter, oasisError: OasisError) {
    throw HighestException(interpreter, oasisError)
}

fun run(line: String, interpreter: Interpreter) {
    val scanner = Scanner(line)
    val tokens = scanner.scanTokens()

    if (hadError) {
        handleError(interpreter, OasisError(""))
        return
    }

    val parser = Parser(tokens)
    val expr = parser.statement()

    if (hadError) {
        handleError(interpreter, OasisError(""))
        return
    }

    try {
        if (expr is ExpressionStatement) {
            val result = interpreter.eval(expr.expr)
            if (result != null && result != Unit) {
                println(result)
            }
        } else {
            interpreter.execute(expr)
        }
    } catch (e: OasisError) {
        handleError(interpreter, e)
    }
}

fun runPrompt() {
    while (true) {
        print(">> ")
        val line = readLine() ?: break
        if (line.isBlank() || line.isEmpty()) continue
        run(line, interpreter)
        if (hadError) interpreter.resetState()
        hadError = false
    }
}