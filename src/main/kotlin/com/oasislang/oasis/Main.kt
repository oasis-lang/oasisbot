package com.oasislang.oasis

import java.nio.file.Files
import java.nio.file.Path

var hadError = false

var interpreter: Interpreter = Interpreter()

fun main(args: Array<String>) {
    if (args.isEmpty()) runPrompt()
    else {
        val program = Files.readString(Path.of(args[1]))
        runProgram(program, interpreter)
    }
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

fun runProgram(source: String, interpreter: Interpreter) {
    val scanner = Scanner(source)
    val tokens = scanner.scanTokens()

    if (hadError) {
        return
    }

    val parser = Parser(tokens)
    val statements = parser.parse()

    if (hadError) {
        return
    }

    try {
        interpreter.execute(statements)
    } catch (e: OasisError) {
        handleError(interpreter, e)
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