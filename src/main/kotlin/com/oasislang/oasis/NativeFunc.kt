package com.oasislang.oasis

class NativeFunc<T>(val name: String, val args: Int, val fnc: (Interpreter, List<Any?>) -> T) : OasisCallable() {
    override fun arity(): Int {
        return args
    }

    override fun name(): String {
        return name
    }

    override fun call(interpreter: Interpreter, arguments: List<Any?>) {
        interpreter.callStack.peek().returnValue = fnc(interpreter, arguments)
    }
}