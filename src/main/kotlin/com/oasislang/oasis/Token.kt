package com.oasislang.oasis

class Token(val tokenType: TokenType, val lexeme: String, val literal: Any?, val line: Int) {
    override fun toString(): String {
        return "$tokenType $lexeme $literal"
    }
}
