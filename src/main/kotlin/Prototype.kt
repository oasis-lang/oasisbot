class Prototype(val parent: Prototype?, val body: MutableMap<String, Any?>) {
    fun get(name: String): Any? {
        return body[name] ?: parent?.get(name)
    }

    fun set(name: String, value: Any?) {
        body[name] = value
    }

    fun has(name: String): Boolean {
        return body.containsKey(name) || parent?.has(name) ?: false
    }
}