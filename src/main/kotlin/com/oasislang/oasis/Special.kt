package com.oasislang.oasis

enum class SpecialType {
    BREAK,
    CONTINUE,
}

class Special(val type: SpecialType) : Exception() {
    override fun fillInStackTrace(): Throwable {
        return this
    }
}
