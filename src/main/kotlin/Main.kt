var hadError = false

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
    try {
        throw HighestException(interpreter, oasisError)
    } catch (e: HighestException) {
        hadError = true
        println(e.localizedMessage)
        e.printStackTrace()
    }
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
            if (result != null) {
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
    val interpreter = Interpreter()
    while (true) {
        print(">> ")
        val line = readLine() ?: break
        if (line.isBlank() || line.isEmpty()) continue
        run(line, interpreter)
        if (hadError) interpreter.resetState()
        hadError = false
    }
}