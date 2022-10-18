var hadError = false

fun main(args: Array<String>) {
    runPrompt()
}

fun error(line: Int, message: String) {
    report(line, "", message)
}

fun report(line: Int, where: String, message: String) {
    System.err.println("(line $line) Error$where: $message")
    hadError = true
}

fun run(line: String) {
    val scanner = Scanner(line)
    val tokens = scanner.scanTokens()

    for (token in tokens) {
        println(token)
    }
}

fun runPrompt() {
    while (true) {
        print(">> ")
        val line = readLine() ?: break
        run(line)
        hadError = false
    }
}