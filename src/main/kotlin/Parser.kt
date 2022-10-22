import TokenType.*

sealed class Either<out A, out B>
class L<A>(val value: A) : Either<A, Nothing>()
class R<B>(val value: B) : Either<Nothing, B>()

class Parser(private val tokens: List<Token>) {
    private var current = 0

    private fun isAtEnd(): Boolean {
        return tokens[current].tokenType == Eof
    }

    private fun raise(message: String) {
        throw ParseException(tokens[current].line, message)
    }

    private fun peek(): TokenType {
        return if (isAtEnd()) Eof else tokens[current].tokenType
    }

    private fun peek(type: TokenType): Boolean {
        return peek() == type
    }

    private fun eat(type: TokenType): Token {
        if (peek(type)) {
            return tokens[current++]
        }
        raise("Unexpected token ${peek().name} - expected $type")

        // we *have* to return, even though this will never execute :)
        return Token(Eof, "", null, -1)
    }

    private fun eat(): Token {
        return tokens[current++]
    }

    private fun body(): Statement {
        eat(LeftBrace)
        val stmts = mutableListOf<Statement>()
        while (!peek(RightBrace)) {
            stmts.add(statement())
        }
        eat(RightBrace)
        return StatementList(stmts)
    }

    private fun statement(): Statement {
        return ExpressionStatement(NilLiteral())
    }

    private fun constraint(): Constraint {
        var constraint = when(peek()) {
            NumType -> {
                eat(NumType)
                NumericConstraint
            }
            BoolType -> {
                eat(BoolType)
                BooleanConstraint
            }
            StringType -> {
                eat(StringType)
                StringConstraint
            }
            ListType -> {
                eat(ListType)
                eat(LeftBracket)
                val constraint = constraint()
                eat(RightBracket)
                ListConstraint(constraint)
            }
            Dict -> {
                eat(Dict)
                eat(LeftBracket)
                val keyConstraint = constraint()
                eat(Colon)
                val valueConstraint = constraint()
                eat(RightBracket)
                DictConstraint(keyConstraint, valueConstraint)
            }
            TupleType -> {
                eat(TupleType)
                eat(LeftBracket)
                val constraints = mutableListOf<Constraint>()
                while (!peek(RightBracket)) {
                    constraints.add(constraint())
                    if (peek(Comma)) {
                        eat(Comma)
                    }
                }
                eat(RightBracket)
                TupleConstraint(constraints)
            }
            Fn -> {
                eat(Fn)
                val parameters = mutableListOf<Constraint>()
                if (peek(LeftBracket)) {
                    eat(LeftBracket)
                    if (!peek(RightBracket)) {
                        parameters.add(constraint())
                        while (peek(Comma)) {
                            eat(Comma)
                            parameters.add(constraint())
                        }
                    }
                    eat(RightBracket)
                }
                val returnType = if (peek(Colon)) {
                    eat(Colon)
                    constraint()
                } else {
                    AnyConstraint
                }
                FunctionConstraint(parameters, returnType)
            }
            Object -> {
                eat(Object)
                ObjectConstraint
            }
            else -> {
               if (peek(Identifier)) {
                   val name = eat(Identifier).lexeme
                   NamedConstraint(name)
               } else {
                   raise("Unexpected token ${peek().name} - expected constraint")
                   NilConstraint
               }
            }
        }

        while (peek(Ampersand) || peek(Pipe)) {
            val operator = eat()
            val right = constraint()
            constraint = if (operator.tokenType == Ampersand) {
                AndConstraint(constraint, right)
            } else {
                UnionConstraint(constraint, right)
            }
        }

        return constraint
    }

    private fun fn(): Expression {
        val args = mutableMapOf<String, Constraint>()

        if(peek(LeftParen)) {
            eat(LeftParen)
            if (!peek(RightParen)){
                fun parseArgument() {
                    val name = eat(Identifier).lexeme
                    if (peek(Colon)) {
                        eat(Colon)
                        args[name] = constraint()
                    } else {
                        args[name] = AnyConstraint
                    }
                }

                parseArgument()
                while (peek(Comma)) {
                    eat(Comma)
                    parseArgument()
                }
            }
            eat(RightParen)
        }

        val returnType = if (peek(Colon)) {
            eat(Colon)
            constraint()
        } else {
            AnyConstraint
        }

        when(peek()) {
            LeftBrace -> return FunctionLiteral(args, returnType, body(), "<anonymous>")
            Arrow -> {
                eat(Arrow)
                return FunctionLiteral(args, returnType, ReturnStatement(expression()), "<anonymous>")
            }
            else -> raise("Unexpected token ${peek().name} - expected { or =>")
        }

        // never reached
        return NilLiteral()
    }

    private fun objectExpr(): Expression {
        val parent = if (peek(Colon)) {
            eat(Colon)
            expression()
        } else {
            null
        }
        val fields = mutableMapOf<String, Expression>()
        eat(LeftBrace)
        if (!peek(RightBrace)) {
            fun parseField() {
                val name = eat(Identifier).lexeme
                eat(Colon)
                fields[name] = expression()
            }

            parseField()
            while (peek(Comma)) {
                eat(Comma)
                parseField()
            }
        }
        eat(RightBrace)
        return ObjectLiteral(fields, parent)
    }

    private fun list(): Expression {
        val elements = mutableListOf<Expression>()
        eat(LeftBracket)
        if (!peek(RightBracket)) {
            elements.add(expression())
            while (peek(Comma)) {
                eat(Comma)
                elements.add(expression())
            }
        }
        eat(RightBracket)
        return ListLiteral(elements)
    }

    private fun dict(): Expression {
        eat(Dict)
        val elements = mutableMapOf<Expression, Expression>()
        eat(LeftBrace)
        if (!peek(RightBrace)) {
            fun parseElement() {
                val key = if(peek(LeftParen)) {
                    eat(LeftParen)
                    val key = expression()
                    eat(RightParen)
                    key
                } else {
                    StringLiteral(eat(TokenType.String).literal as String)
                }
                eat(Colon)
                val value = expression()
                elements[key] = value
            }

            parseElement()
            while (peek(Comma)) {
                eat(Comma)
                parseElement()
            }
        }
        eat(RightBrace)
        return DictLiteral(elements)
    }

    private fun atom(): Expression {
        return when(peek()) {
            Number -> NumberLiteral(eat().literal as Double)
            TokenType.String -> StringLiteral(eat().literal as String)
            LeftBracket -> list()
            Dict -> dict()
            Identifier -> VariableReference(eat().lexeme)
            LeftParen -> {
                eat(LeftParen)
                val item = expression()
                if(peek(Comma)) {
                    val items = mutableListOf(item)
                    while(!peek(RightParen)) {
                        eat(Comma)
                        items.add(expression())
                    }
                    return TupleExpression(items)
                }
                item
            }
            Fn -> { eat(Fn); fn() }
            Object -> { eat(Object); objectExpr() }
            Nil -> { eat(Nil); NilLiteral() }
            True -> BoolLiteral(true)
            False -> BoolLiteral(false)
            Import -> {
                eat(Import)
                //val file = eat(TokenType.String).literal as String

                // TODO - Imports

                NilLiteral()
            }
            else -> {
                raise("Invalid expression")

                // never going to reach
                NilLiteral()
            }
        }
    }

    private fun seeTermSuffix(): Boolean {
        return peek() in listOf(
            Colon,
            LeftBracket,
            LeftParen,
            Arrow
        )
    }

    private fun termSuffix(lhs: Expression): Expression {
        when(peek()) {
            Colon -> {
                eat(Colon)
                return PropertyAccess(lhs, eat(TokenType.String).literal as String)
            }
            LeftBracket -> {
                eat(LeftBracket)
                val index = expression()
                eat(RightBracket)
                return IndexOperator(lhs, index)
            }
            LeftParen -> {
                eat(LeftParen)
                val parameters = mutableListOf<Expression>()
                if (!peek(RightParen)) {
                    parameters.add(expression())
                    while (peek(Comma)) {
                        eat(Comma)
                        parameters.add(expression())
                    }
                }
                val fcall = FunctionCall(lhs, parameters)
                if (peek(Arrow)) {
                    // Syntactic sugar for f(..., fn { })
                    fcall.arguments.add(FunctionLiteral(
                        mapOf(),
                        NilConstraint,
                        body(),
                        "<anonymous>"
                    ))
                }
                return fcall
            }
            Arrow -> {
                eat(Arrow)
                // Syntactic sugar for f(fn { })
                return FunctionCall(lhs, mutableListOf(FunctionLiteral(
                    mapOf(),
                    NilConstraint,
                    body(),
                    "<anonymous>"
                )))
            }
            else -> return lhs
        }
    }

    private fun termPrefix(): Expression {
        return when(peek()) {
            Minus -> { eat(); UnaryOp(Minus, termPrefix()) }
            Not, Bang -> { eat(); UnaryOp(Not, termPrefix()) }
            New -> { eat(); UnaryOp(New, termPrefix()) }
            Recv -> { eat(); UnaryOp(Recv, termPrefix()) }
            else -> atom()
        }
    }

    private fun term(): Expression {
        var item = termPrefix()
        while (seeTermSuffix())
            item = termSuffix(item)
        return item
    }

    private fun seeBinop(): Boolean {
        return peek() in listOf(
            Plus, Minus, Star, Slash,
            Equal, EqualEqual, BangEqual,
            Less, Greater, LessEqual, GreaterEqual,
            PipePipe, Or, AmpersandAmpersand, And,
            Question
        )
    }

    fun expression(): Expression {
        val items = mutableListOf<Either<TokenType, Expression>>(R(term()))
        while(seeBinop()) {
            items.add(L(eat().tokenType))
            items.add(R(term()))
        }
        return if(items.count() == 1)
            (items[0] as R).value
        else
            parsePrecedence(items)
    }

    private val precedence = listOf(
        listOf(Or, AmpersandAmpersand, And),
        listOf(PipePipe),
        listOf(Equal, EqualEqual, BangEqual),
        listOf(Less, Greater, LessEqual, GreaterEqual),
        listOf(Plus, Minus),
        listOf(Star, Slash),
        listOf(Question)
    )

    private fun priorityOf(type: TokenType): Int {
        for((i, p) in precedence.withIndex()) {
            if(type in p) return i
        }
        return -1
    }

    private fun priorityOfOption(type: Either<TokenType, Expression>): Int {
        return when(type) {
            is L -> priorityOf(type.value)
            is R -> -1
        }
    }

    private fun parsePrecedence(items: List<Either<TokenType, Expression>>): Expression {
        val mutableItems = items.toMutableList()

        // parse items into an AST with precedence
        for (level in precedence) {
            var i = 1
            while (i < mutableItems.count()) {
                // step to the right to find the highest priority operator
                while (i < mutableItems.count() - 2 && priorityOfOption(mutableItems[i]) < priorityOfOption(mutableItems[i + 2])) {
                    i += 2
                }

                // parse the operator and its operands
                val op  = (mutableItems[i]     as L).value
                val lhs = (mutableItems[i - 1] as R).value
                val rhs = (mutableItems[i + 1] as R).value
                mutableItems[i] = R(BinOp(lhs, op, rhs))
                mutableItems.removeAt(i + 1)
                mutableItems.removeAt(i - 1)

                // step down the slope
                if (i > 1) {
                    i -= 2
                }
            }
        }

        assert(mutableItems.count() == 1)
        return (mutableItems[0] as R).value
    }
}
