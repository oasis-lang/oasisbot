package com.oasislang.oasis

import com.oasislang.oasis.TokenType.*
import com.oasislang.oasis.TokenType.Number

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

    private fun peekNext(): TokenType {
        return if (current + 1 >= tokens.size) Eof else tokens[current + 1].tokenType
    }

    private fun peekNext(type: TokenType): Boolean {
        return peekNext() == type
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

    fun parse(): StatementList {
        val statements = mutableListOf<Statement>()
        while (!isAtEnd()) {
            statements.add(statement())
        }
        return StatementList(0, statements)
    }

    private fun body(): Statement {
        val line = eat(LeftBrace).line
        val stmts = mutableListOf<Statement>()
        while (!peek(RightBrace)) {
            stmts.add(statement())
        }
        eat(RightBrace)
        return StatementList(line, stmts)
    }

    private fun whileStatement(): Statement {
        val token = eat()
        val type = token.tokenType
        val line = token.line
        val condition = expression()
        val body = statement()
        return WhileStatement(
            line,
            when (type) {
                While -> condition
                Until -> UnaryOp(Token(Not, "not", null, line), condition)
                else -> NilLiteral(0)
            },
            body
        )
    }

    private fun ifStatement(): Statement {
        val token = eat()
        val type = token.tokenType
        val line = token.line
        val condition = expression()
        val thenBranch = statement()
        val elseBranch = if (peek(Else)) {
            eat(Else)
            statement()
        } else {
            null
        }
        return IfStatement(
            line,
            when (type) {
                If -> condition
                Unless -> UnaryOp(Token(Not, "not", null, line), condition)
                else -> NilLiteral(0)
            }, thenBranch, elseBranch
        )
    }

    private fun forStatement(): Statement {
        val line = eat(For).line
        if (peek(Item)) {
            eat(Item)
            val name = eat(Identifier).lexeme
            eat(In)
            val iterable = expression()
            val body = statement()
            return IteratorStatement(line, name, iterable, body)
        } else {
            val initializer = statement()
            eat(Pipe)
            val condition = expression()
            eat(Pipe)
            val increment = statement()
            val body = statement()
            return ForStatement(line, initializer, condition, increment, body)
        }
    }

    private fun matchStatement(): Statement {
        val line = eat(Match).line
        val expr = expression()
        eat(LeftBrace)
        val cases = mutableListOf<Pair<Expression, Statement>>()
        while (!peek(RightBrace) && !peek(Else)) {
            val pattern = expression()
            eat(Then)
            val body = statement()
            cases.add(Pair(pattern, body))
        }
        val elseBranch = if (peek(Else)) {
            eat(Else)
            eat(ColonEqual)
            statement()
        } else {
            null
        }
        eat(RightBrace)
        return MatchStatement(line, expr, cases, elseBranch)
    }

    fun statement(): Statement {
        when (peek()) {
            If, Unless -> return ifStatement()
            While, Until -> return whileStatement()
            For -> return forStatement()
            Match -> return matchStatement()
            Send -> {
                val line = eat(Send).line
                val value = expression()
                eat(To)
                val channel = expression()
                return SendStatement(line, value, channel)
            }
            Export -> {
                val line = eat(Export).line
                return if (peek(LeftBrace)) {
                    eat(LeftBrace)
                    val names = mutableListOf<String>()
                    names.add(eat(Identifier).lexeme)
                    while (peek(Comma)) {
                        eat(Comma)
                        names.add(eat(Identifier).lexeme)
                    }
                    eat(RightBrace)
                    ExportStatement(line, names)
                } else {
                    ExportStatement(line, listOf(eat(Identifier).lexeme))
                }
            }
            Fn -> {
                val line = eat(Fn).line
                val const = peek(Const)
                if (const) {
                    eat(Const)
                }
                val name = eat(Identifier).lexeme
                val func = fn() as FunctionLiteral
                func.name = name
                return DeclarationStatement(line, name, func, const)
            }
            Let -> {
                val line = eat(Let).line
                val const = peek(Const)
                if (const) {
                    eat(Const)
                }
                val name = eat(Identifier).lexeme
                eat(Equal)
                val value = expression()
                return DeclarationStatement(line, name, value, const)
            }
            Identifier -> {
                return if (peekNext(ColonEqual)) {
                    val token = eat(Identifier)
                    val name = token.lexeme
                    val line = token.line
                    eat(ColonEqual)
                    val value = expression()
                    DeclarationStatement(line, name, value, false)
                } else
                    ExpressionStatement(tokens[current].line, expression())
            }
            Spawn -> {
                val line = eat(Spawn).line
                val value = expression()
                return SpawnStatement(line, value)
            }
            Return -> {
                val line = eat(Return).line
                val value = expression()
                return ReturnStatement(line, value)
            }
            Break -> {
                return BreakStatement(eat(Break).line)
            }
            Continue -> {
                return ContinueStatement(eat(Continue).line)
            }
            Import -> {
                val line = eat(Import).line
                val path = eat(TokenType.String).literal as String
                return ImportStatement(line, path)
            }
            LeftBrace -> return body()
            Type -> { userType(); return EmptyStatement(tokens[current].line) }
            else -> return ExpressionStatement(tokens[current].line, expression())
        }
    }

    private fun userType(): Constraint {
        eat(Type)
        val name = eat(Identifier).lexeme
        val constraints = mutableMapOf<String, Constraint>()
        eat(LeftBrace)
        while (!peek(RightBrace)) {
            val property = eat(Identifier).lexeme
            if (peek(Colon)) {
                eat(Colon)
                constraints[property] = constraint()
            } else {
                constraints[property] = AnyConstraint
            }
            if (!peek(RightBrace)) {
                eat(Comma)
            }
        }
        eat(RightBrace)
        Constraints[name] = UserConstraint(constraints)
        return Constraints[name]!!
    }
    private fun constraint(): Constraint {
        var constraint = when (peek()) {
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
            Nil -> {
                eat(Nil)
                NilConstraint
            }
            Not, Bang -> {
                eat(peek())
                NotConstraint(constraint())
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
        val args = mutableListOf<Pair<String, Constraint>>()

        val line = tokens[current].line

        if (peek(LeftParen)) {
            eat(LeftParen)
            if (!peek(RightParen)) {
                fun parseArgument() {
                    val name = eat(Identifier).lexeme
                    if (peek(Colon)) {
                        eat(Colon)
                        args.add(Pair(name, constraint()))
                    } else {
                        args.add(Pair(name, AnyConstraint))
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

        when (peek()) {
            LeftBrace -> return FunctionLiteral(tokens[current].line, args, returnType, body(), "<anonymous>")
            Arrow -> {
                return FunctionLiteral(
                    line,
                    args,
                    returnType,
                    ReturnStatement(eat(Arrow).line, expression()),
                    "<anonymous>"
                )
            }
            else -> raise("Unexpected token ${peek().name} - expected { or =>")
        }

        // never reached
        return NilLiteral(0)
    }

    private fun objectExpr(): Expression {
        val line = tokens[current].line
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
                eat(Equal)
                fields[name] = expression()
            }

            parseField()
            while (peek(Comma)) {
                eat(Comma)
                parseField()
            }
        }
        eat(RightBrace)
        return ObjectLiteral(line, fields, parent)
    }

    private fun list(): Expression {
        val elements = mutableListOf<Expression>()
        val line = eat(LeftBracket).line
        if (!peek(RightBracket)) {
            elements.add(expression())
            while (peek(Comma)) {
                eat(Comma)
                elements.add(expression())
            }
        }
        eat(RightBracket)
        return ListLiteral(line, elements)
    }

    private fun dict(): Expression {
        val line = eat(Dict).line
        val elements = mutableMapOf<Expression, Expression>()
        eat(LeftBrace)
        if (!peek(RightBrace)) {
            fun parseElement() {
                val key = if (peek(LeftParen)) {
                    eat(LeftParen)
                    val key = expression()
                    eat(RightParen)
                    key
                } else {
                    val token = eat(TokenType.String)
                    StringLiteral(token.line, token.literal as String)
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
        return DictLiteral(line, elements)
    }

    private fun atom(): Expression {
        return when (peek()) {
            Number -> NumberLiteral(tokens[current].line, eat().literal as Double)
            TokenType.String -> StringLiteral(tokens[current].line, eat().literal as String)
            LeftBracket -> list()
            Dict -> dict()
            Identifier -> VariableReference(tokens[current].line, eat().lexeme)
            LeftParen -> {
                val line = eat(LeftParen).line
                val item = expression()
                if (peek(Comma)) {
                    val items = mutableListOf(item)
                    while (!peek(RightParen)) {
                        eat(Comma)
                        items.add(expression())
                    }
                    eat(RightParen)
                    return TupleExpression(line, items)
                }
                eat(RightParen)
                item
            }
            Fn -> {
                eat(Fn); fn()
            }
            Object -> {
                eat(Object); objectExpr()
            }
            Nil -> NilLiteral(eat().line)
            True -> BoolLiteral(eat(True).line, true)
            False -> BoolLiteral(eat(False).line, false)
            Import -> {
                val line = eat(Import).line
                val file = eat(TokenType.String).literal as String

                return ImportExpression(line, file)
            }
            else -> {
                raise("Unexpected token ${peek().name} - Invalid expression")

                // never going to reach
                NilLiteral(0)
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
        when (peek()) {
            Colon -> {
                val line = eat(Colon).line
                return PropertyAccess(line, lhs, eat(Identifier).lexeme)
            }
            LeftBracket -> {
                val line = eat(LeftBracket).line
                val index = expression()
                eat(RightBracket)
                return IndexOperator(line, lhs, index)
            }
            LeftParen -> {
                val line = eat(LeftParen).line
                val parameters = mutableListOf<Expression>()
                if (!peek(RightParen)) {
                    parameters.add(expression())
                    while (peek(Comma)) {
                        eat(Comma)
                        parameters.add(expression())
                    }
                }
                eat(RightParen)
                val fcall = FunctionCall(line, lhs, parameters)
                if (peek(Arrow)) {
                    val line = eat(Arrow).line
                    // Syntactic sugar for f(..., fn { })
                    fcall.arguments.add(
                        FunctionLiteral(
                            line,
                            listOf(),
                            NilConstraint,
                            body(),
                            "<anonymous>"
                        )
                    )
                }
                return fcall
            }
            Arrow -> {
                val line = eat(Arrow).line
                // Syntactic sugar for f(fn { })
                return FunctionCall(
                    line, lhs, mutableListOf(
                        FunctionLiteral(
                            line,
                            listOf(),
                            NilConstraint,
                            body(),
                            "<anonymous>"
                        )
                    )
                )
            }
            else -> return lhs
        }
    }

    private fun termPrefix(): Expression {
        return when (peek()) {
            Minus, Not, Bang, New, Recv -> UnaryOp(eat(), termPrefix())
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

    private fun expression(): Expression {
        val items = mutableListOf<Either<Token, Expression>>(R(term()))
        while (seeBinop()) {
            items.add(L(eat()))
            items.add(R(term()))
        }
        return if (items.count() == 1)
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

    private fun priorityOf(type: Token): Int {
        for ((i, p) in precedence.withIndex()) {
            if (type.tokenType in p) return i
        }
        return -1
    }

    private fun priorityOfOption(type: Either<Token, Expression>): Int {
        return when (type) {
            is L -> priorityOf(type.value)
            is R -> -1
        }
    }

    private fun parsePrecedence(items: List<Either<Token, Expression>>): Expression {
        val mutableItems = items.toMutableList()

        // parse items into an AST with precedence
        var i = 1
        while (i < mutableItems.count()) {
            // step to the right to find the highest priority operator
            while (i < mutableItems.count() - 2 && priorityOfOption(mutableItems[i]) < priorityOfOption(mutableItems[i + 2])) {
                i += 2
            }

            // parse the operator and its operands
            val rhs = (mutableItems.removeAt(i + 1) as R).value
            val op = (mutableItems.removeAt(i) as L).value
            val lhs = (mutableItems[i - 1] as R).value
            mutableItems[i - 1] = R(BinOp(lhs, op, rhs))

            // step down the slope
            if (i > 1) {
                i -= 2
            }
        }

        assert(mutableItems.count() == 1)
        return (mutableItems[0] as R).value
    }
}