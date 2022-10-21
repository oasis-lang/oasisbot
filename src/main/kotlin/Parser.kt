import java.text.ParseException

class Parser(private val tokens: List<Token>) {
    var current = 0

    private fun isAtEnd(): Boolean {
        return peek() == TokenType.Eof
    }

    private fun raise(message: String) {
        throw ParseException(tokens[current].line)
    }

    private fun peek(): TokenType {
        return if (isAtEnd()) TokenType.Eof else tokens[current].tokenType
    }

    private fun peek(type: TokenType): Boolean {
        return peek() == type
    }

    fun eat(type: TokenType): Token {
        if (peek(type)) {
            return tokens[current++]
        }
        raise("Unexpected token ${peek().name} - expected $type")

        // we *have* to return, even though this will never execute :)
        return Token(TokenType.Eof, "", null, -1)
    }

    fun eat(): Token {
        return tokens[current++]
    }


}