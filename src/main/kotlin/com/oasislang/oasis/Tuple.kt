package com.oasislang.oasis

class Tuple(vararg val elements: Any?) : Iterable<Any?> {
    override fun iterator(): Iterator<Any?> {
        return elements.iterator()
    }

    override fun toString(): String {
        return elements.joinToString(", ", "(", ")")
    }

    fun get(index: Int): Any? {
        return elements[index]
    }
}