class Environment(val enclosing: Environment? = null) {
    private val variables = mutableMapOf<String, Any?>()
    private val constants = mutableListOf<String>()

    private val cache = mutableMapOf<String, Int>()

    fun define(name: String, value: Any?, isConstant: Boolean = false) {
        variables[name] = value
        cache[name] = 0
        if (isConstant) constants.add(name)
    }

    fun fetchFromCache(name: String): Any? {
        var env = this
        for(i in 0 until cache[name]!!) {
            env = env.enclosing!!
        }
        return env.get(name)
    }

    fun get(name: String): Any? {
        if (variables.containsKey(name)) {
            return variables[name]
        }

        if (name in cache) {
            return fetchFromCache(name)
        } else if(enclosing != null && enclosing.cache.containsKey(name)) {
            cache[name] = enclosing.cache[name]!! + 1
            return fetchFromCache(name)
        } else {
            var env = this
            var i = 0
            while (env.enclosing != null) {
                env = env.enclosing!!
                i++
                if (env.variables.containsKey(name)) {
                    cache[name] = i
                    return env.variables[name]
                }
            }
        }

        throw RuntimeError("Undefined variable '${name}'.")
    }

    fun assign(name: String, value: Any?) {
        if (variables.containsKey(name) && !constants.contains(name)) {
            variables[name] = value
            return
        }

        if (name in cache) {
            var env = this
            for (i in 0 until cache[name]!!) {
                env = env.enclosing!!
            }
            env.assign(name, value)
            return
        } else if(enclosing != null && enclosing.cache.containsKey(name)) {
            cache[name] = enclosing.cache[name]!! + 1
        } else {
            var env = this
            var i = 0
            while (env.enclosing != null) {
                env = env.enclosing!!
                i++
                if (env.variables.containsKey(name)) {
                    cache[name] = i
                    env.assign(name, value)
                    return
                }
            }
        }

        throw RuntimeError("Undefined variable '${name}'.")
    }

    override fun toString(): String {
        return variables.entries.joinToString(", ", "{", "}") {
            "${it.key} = ${it.value}" + if (it.key in constants) " (constant)" else ""
        } + " cache: " + cache.entries.joinToString(", ", "{", "}") { "${it.key} = ${it.value}" } +
                " enclosing: " + (enclosing?.toString() ?: "null")
    }
}