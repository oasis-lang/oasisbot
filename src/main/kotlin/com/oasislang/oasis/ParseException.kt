package com.oasislang.oasis

fun constructMessage(line: Int, message: String) =
    "(line $line): Parse com.oasislang.oasis.error: $message"

class ParseException(val line: Int, val msg: String) : Throwable() {
    override fun toString(): String {
        return "\n" + constructMessage(line, msg)
    }
    override fun fillInStackTrace(): Throwable {
        return this
    }
}