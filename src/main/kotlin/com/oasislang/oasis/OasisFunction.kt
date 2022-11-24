package com.oasislang.oasis

class OasisFunction(
    val name: String,
    val parameters: List<Pair<String, Constraint>>,
    val returnType: Constraint,
    val closure: Environment,
    val body: Statement
) : OasisCallable() {
    override fun arity(): Int {
        return parameters.size
    }

    override fun name(): String {
        return name
    }

    override fun call(interpreter: Interpreter, arguments: List<Any?>) {
        for (i in parameters.indices) {
            if (!parameters[i].second.fits(arguments[i])) {
                throw RuntimeError("Argument ${i + 1} to '${name}' must be ${parameters[i].second}, got ${arguments[i]}")
            }
            closure.define(parameters[i].first, arguments[i])
        }
        val previous = interpreter.environment
        interpreter.environment = closure
        interpreter.execute(body)
        if (!returnType.fits(interpreter.callStack.peek().returnValue)) {
            throw RuntimeError("Function '$name' returned ${interpreter.callStack.peek().returnValue} which does not fit the return type ${returnType}.")
        }
        interpreter.environment = previous
    }
}

class PartialFunc(val fn: OasisCallable, val partialArgs: List<Any?>, val line: Int): OasisCallable() {
    override fun arity(): Int {
        return fn.arity() - partialArgs.size
    }

    override fun name(): String {
        return "partial ${fn.name()}(${partialArgs.joinToString(", ")})"
    }

    override fun call(interpreter: Interpreter, arguments: List<Any?>) {
        interpreter.callStack.peek().returnValue = interpreter.call(fn, line, partialArgs.plus(arguments))
    }
}