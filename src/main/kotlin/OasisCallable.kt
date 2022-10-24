interface OasisCallable {
    fun arity(): Int
    fun name(): String
    fun call(interpreter: Interpreter, arguments: List<Any?>)
}