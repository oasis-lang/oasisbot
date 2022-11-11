abstract class OasisCallable {
    abstract fun arity(): Int
    abstract fun name(): String
    abstract fun call(interpreter: Interpreter, arguments: List<Any?>)

    override fun toString(): String {
        return "<fn ${name()}>"
    }
}