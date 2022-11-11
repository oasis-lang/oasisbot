class Module(val name: String) {
    val exports = mutableMapOf<String, Any?>()

    fun register(name: String, value: Any?) {
        if(exports.containsKey(name))
            throw RuntimeError("$name is already exported in ${this.name}")
        exports[name] = value
    }

    fun get(name: String): Any? {
        if (exports.containsKey(name)) {
            return exports[name]
        }
        throw RuntimeError("${this.name} does not export '${name}'.")
    }

    override fun toString(): String {
        return "module $name"
    }
}

interface NativeModule {
    fun toModule(): Module
}