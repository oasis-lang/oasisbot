class Scanner(private val source: String) {
    private var start = 0
    var current = 0
    private var line = 1

    var tokens = mutableListOf<Token>()

    private var keywords = mapOf(
        "and" to TokenType.And,
        "or" to TokenType.Or,
        "nil" to TokenType.Nil,
        "true" to TokenType.True,
        "false" to TokenType.False,
        "let" to TokenType.Let,
        "const" to TokenType.Const,
        "type" to TokenType.Type,
        "object" to TokenType.Object,
        "fn" to TokenType.Fn,
        "if" to TokenType.If,
        "unless" to TokenType.Unless,
        "else" to TokenType.Else,
        "while" to TokenType.While,
        "until" to TokenType.Until,
        "for" to TokenType.For,
        "match" to TokenType.Match,
        "send" to TokenType.Send,
        "recv" to TokenType.Recv,
        "not" to TokenType.Not,
        "item" to TokenType.Item,
        "in" to TokenType.In,
        "break" to TokenType.Break,
        "continue" to TokenType.Continue,
        "return" to TokenType.Return,
        "import" to TokenType.Import,
        "export" to TokenType.Export,
        "new" to TokenType.New,
        "dict" to TokenType.Dict,
        "num" to TokenType.NumType,
        "string" to TokenType.StringType,
        "bool" to TokenType.BoolType,
        "list" to TokenType.ListType,
        "tuple" to TokenType.TupleType,
        "spawn" to TokenType.Spawn,
        "to" to TokenType.To,
        "then" to TokenType.Then
    )

    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            start = current
            scanToken()
        }
        tokens.add(Token(TokenType.Eof, "", null, line))
        return tokens
    }

    private fun isAtEnd(): Boolean {
        return current >= source.length
    }

    private fun advance(): Char {
        current++
        return source[current - 1]
    }

    private fun addToken(type: TokenType, literal: Any? = null) {
        val text = source.substring(start, current)
        tokens.add(Token(type, text, literal, line))
    }

    private fun match(expected: Char): Boolean {
        if (isAtEnd()) return false
        if (source[current] != expected) return false
        current++
        return true
    }

    private fun peek(): Char {
        if (isAtEnd()) return '\u0000'
        return source[current]
    }

    private fun peekNext(): Char {
        if (current + 1 >= source.length) return '\u0000'
        return source[current + 1]
    }

    private fun scanToken() {
        when (val c = advance()) {
            '(' -> addToken(TokenType.LeftParen)
            ')' -> addToken(TokenType.RightParen)
            '[' -> addToken(TokenType.LeftBracket)
            ']' -> addToken(TokenType.RightBracket)
            '{' -> addToken(TokenType.LeftBrace)
            '}' -> addToken(TokenType.RightBrace)
            ':' -> if (match('=')) addToken(TokenType.ColonEqual) else addToken(TokenType.Colon)
            '&' -> if (match('&')) addToken(TokenType.AmpersandAmpersand) else addToken(TokenType.Ampersand)
            '|' -> if (match('|')) addToken(TokenType.PipePipe) else addToken(TokenType.Pipe)
            ',' -> addToken(TokenType.Comma)
            '-' -> addToken(TokenType.Minus)
            '+' -> addToken(TokenType.Plus)
            '*' -> addToken(TokenType.Star)
            '!' -> addToken(if (match('=')) TokenType.BangEqual else TokenType.Bang)
            '=' -> addToken(if (match('=')) TokenType.EqualEqual else if (match('>')) TokenType.Arrow else TokenType.Equal)
            '<' -> addToken(if (match('=')) TokenType.LessEqual else TokenType.Less)
            '>' -> addToken(if (match('=')) TokenType.GreaterEqual else TokenType.Greater)
            '?' -> addToken(TokenType.Question)
            '/' -> if (match('/')) {
                while (peek() != '/' && !isAtEnd()) advance()
            } else {
                addToken(TokenType.Slash)
            }

            ' ', '\r', '\t', '\n' -> {}
            '"' -> string()
            else -> {
                if (isDigit(c)) {
                    number()
                } else if (isAlpha(c)) {
                    identifier()
                } else {
                    error(line, "Unexpected character.")
                }
            }
        }
    }

    private fun isDigit(c: Char): Boolean {
        return c in '0'..'9'
    }

    private fun isAlpha(c: Char): Boolean {
        return c in 'a'..'z' || c in 'A'..'Z' || c == '_'
    }

    private fun number() {
        while (isDigit(peek())) advance()
        if (peek() == '.' && isDigit(peekNext())) {
            advance()
            while (isDigit(peek())) advance()
        }
        addToken(TokenType.Number, source.substring(start, current).toDouble())
    }

    private fun string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++
            advance()
        }
        if (isAtEnd()) {
            error(line, "Unterminated string.")
            return
        }
        advance()
        var value = source.substring(start + 1, current - 1)

        value = value.replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\t", "\t")
            .replace("\\\\", "\\")
            .replace("\\\"", "\"")

        addToken(TokenType.String, value)
    }

    private fun isAlphaNumeric(c: Char): Boolean {
        return isAlpha(c) || isDigit(c)
    }

    private fun identifier() {
        while (isAlphaNumeric(peek())) advance()
        val text = source.substring(start, current)
        val type = keywords[text] ?: TokenType.Identifier
        addToken(type)
    }
}