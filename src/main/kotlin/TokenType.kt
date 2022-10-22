enum class TokenType {
    LeftParen, RightParen, LeftBrace, RightBrace,
    Comma, Colon, Minus, Plus, Slash, Star,
    Bang, BangEqual, Equal, EqualEqual, Greater, GreaterEqual, Less, LessEqual,
    Identifier, String, Number, Ampersand, AmpersandAmpersand, Pipe, PipePipe,
    Arrow, Question, LeftBracket, RightBracket,

    // Keywords
    And, Or, Nil, True, False, Let, Const, Type, Object, Fn, If, Unless, Else, While, Until, For,
    Match, Send, Recv, Not, Item, In, Break, Continue, Return, Import, Export, New, Dict,
    NumType, StringType, BoolType, ListType, ObjectType, TupleType,

    Eof
}