class Prototype(val parent: Prototype?, val body: MutableMap<String, Any?>) : Iterable<Any?> {
    fun get(name: String): Any? {
        return body[name] ?: parent?.get(name)
    }

    fun set(name: String, value: Any?) {
        body[name] = value
    }

    fun has(name: String): Boolean {
        return body.containsKey(name) || parent?.has(name) ?: false
    }

    override fun iterator(): Iterator<Any?> {
        if (!has("__iter") || get("__iter") !is OasisCallable) {
            throw RuntimeError("Object is missing __iter, or __iter is not a function.")
        } else {
            if(!has("__hasNext") || get("__hasNext") !is OasisCallable)
                throw RuntimeError("Object is missing __hasNext, or __hasNext is not a function.")

            return object : Iterator<Any?> {
                override fun hasNext(): Boolean {
                    return interpreter.call(get("__hasNext") as OasisCallable, 0, listOf()) as? Boolean
                        ?: throw RuntimeError("Object's __hasNext did not return a boolean.")
                }

                override fun next(): Any? {
                    return interpreter.call(get("__iter") as OasisCallable, 0, listOf())
                }
            }
        }
    }

    override fun toString(): String {
        return "object { ${body.map { "${it.key} = ${it.value}" }.joinToString(", ")} }"
    }
}