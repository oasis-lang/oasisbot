class HighestException(val interpreter: Interpreter, val error: OasisError) : Throwable() {
    override fun getLocalizedMessage(): String {
        return error.message ?: "Unknown error"
    }

    override fun printStackTrace() {
        val stack = interpreter.callStack
        val sb = StringBuilder()
        sb.appendLine("  at ${stack.peek().function.name()}(called at line: ${stack.peek().callSite})")
        for (i in stack.size - 2 downTo 0) {
            sb.appendLine("  at ${stack[i].function.name()}(called at line: ${stack[i].callSite})")
        }
        println(sb.toString())
    }
}