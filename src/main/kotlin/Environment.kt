class Environment(val enclosing: Environment? = null) {
    private val variables = mutableMapOf<String, Any?>()
    private val constants = mutableListOf<String>()

    fun define(name: String, value: Any?, isConstant: Boolean = false) {
        variables[name] = value
        if (isConstant) constants.add(name)
    }

    fun get(name: String): Any? {
        if (variables.containsKey(name)) {
            return variables[name]
        }

        if (enclosing != null) return enclosing.get(name)

        throw RuntimeError("Undefined variable '${name}'.")
    }

    fun assign(name: String, value: Any?) {
        if (constants.contains(name)) throw RuntimeError("Cannot assign to constant '${name}'.")
        if (variables.containsKey(name)) {
            variables[name] = value
            return
        }

        if (enclosing != null) {
            enclosing.assign(name, value)
            return
        }

        throw RuntimeError("Undefined variable '${name}'.")
    }

    override fun toString(): String {
        return variables.entries.joinToString(", ", "{", "}") {
            "${it.key} = ${it.value}"
        }
    }
}