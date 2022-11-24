package com.oasislang.oasis

class Stream : Iterable<Any?> {
    val queue = ArrayDeque<Any?>()

    fun send(item: Any?) {
        val len = queue.size
        queue.add(item)
        while (queue.size == len) {
            // wait for the item to be received
        }
    }

    fun receive(): Any? {
        while (queue.isEmpty()) {
            // wait for the item to be sent
        }
        return queue.removeFirst()
    }

    override fun iterator(): Iterator<Any?> {
        return object : Iterator<Any?> {
            override fun hasNext(): Boolean {
                return true
            }

            override fun next(): Any? {
                return receive()
            }
        }
    }
}