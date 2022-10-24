class OasisError : Exception {
    constructor(line: Int, error: String) : super("(line $line): $error")
    constructor(message: String) : super(message)
}

class RuntimeError(message: String) : Exception(message)