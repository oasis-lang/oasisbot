fun constructMessage(line: Int, message: String) =
    "(line $line): Parse error: $message"

class ParseException : Exception {
    constructor(line: Int) :
            super()

    constructor(line: Int, message: String) :
            super(constructMessage(line, message))

    constructor(line: Int, message: String, cause: Throwable) :
            super(constructMessage(line, message), cause)

    constructor(cause: Throwable) :
            super(cause)

    constructor(line: Int, message: String, cause: Throwable, enableSuppression: Boolean, writableStackTrace: Boolean) :
            super(constructMessage(line, message), cause, enableSuppression, writableStackTrace)
}