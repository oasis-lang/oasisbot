interface Constraint {
    fun fits(item: Any?): Boolean
    fun isEqual(other: Constraint): Boolean
}

object NumericConstraint : Constraint {
    override fun fits(item: Any?): Boolean =
        item is Double
                || item is NumberLiteral // AST type checking

    override fun isEqual(other: Constraint): Boolean =
        other is NumericConstraint

    override fun toString(): String {
        return "num"
    }
}

object StringConstraint : Constraint {
    override fun fits(item: Any?): Boolean =
        item is String
                || item is StringLiteral // AST type checking

    override fun isEqual(other: Constraint): Boolean =
        other is StringConstraint

    override fun toString(): String {
        return "string"
    }
}

object BooleanConstraint : Constraint {
    override fun fits(item: Any?): Boolean =
        item is Boolean
                || item is BoolLiteral // AST type checking

    override fun isEqual(other: Constraint): Boolean =
        other is BooleanConstraint

    override fun toString(): String {
        return "bool"
    }
}

object NilConstraint : Constraint {
    override fun fits(item: Any?): Boolean =
        item == null
                || item is NilLiteral // AST type checking

    override fun isEqual(other: Constraint): Boolean =
        other is NilConstraint

    override fun toString(): String {
        return "nil"
    }
}

object AnyConstraint : Constraint {
    override fun fits(item: Any?): Boolean =
        true

    override fun isEqual(other: Constraint): Boolean =
        other is AnyConstraint

    override fun toString(): String {
        return "any"
    }
}

object ObjectConstraint : Constraint {
    override fun fits(item: Any?): Boolean =
        item is Map<*, *>
                || item is ObjectLiteral // AST type checking

    override fun isEqual(other: Constraint): Boolean =
        other is ObjectConstraint

    override fun toString(): String {
        return "object"
    }
}

class ListConstraint(val constraint: Constraint) : Constraint {
    override fun fits(item: Any?): Boolean =
        item is List<*> && item.all { constraint.fits(it) }
                || item is ListLiteral && item.elements.all { constraint.fits(it) } // AST type checking

    override fun isEqual(other: Constraint): Boolean =
        other is ListConstraint && constraint.isEqual(other.constraint)

    override fun toString(): String =
        "list[$constraint]"
}

class DictConstraint(val keyConstraint: Constraint, val valueConstraint: Constraint) : Constraint {
    override fun fits(item: Any?): Boolean =
        item is Map<*, *> && item.all { keyConstraint.fits(it.key) && valueConstraint.fits(it.value) }
                || item is DictLiteral && item.elements.all { keyConstraint.fits(it.key) && valueConstraint.fits(it.value) } // AST type checking

    override fun isEqual(other: Constraint): Boolean =
        other is DictConstraint && keyConstraint.isEqual(other.keyConstraint) && valueConstraint.isEqual(other.valueConstraint)

    override fun toString(): String {
        return "dict[$keyConstraint : $valueConstraint]"
    }
}

class TupleConstraint(val constraints: List<Constraint>) : Constraint {
    override fun fits(item: Any?): Boolean =
        item is List<*> && item.size == constraints.size && item.zip(constraints).all { it.second.fits(it.first) }
                || (item is TupleExpression
                && item.items.size == constraints.size
                && item.items.zip(constraints).all { it.second.fits(it.first) }) // AST type checking

    override fun isEqual(other: Constraint): Boolean =
        other is TupleConstraint && constraints.zip(other.constraints).all { it.first.isEqual(it.second) }

    override fun toString(): String {
        return constraints.joinToString(", ", "tuple[", "]")
    }
}

class FunctionConstraint(val args: List<Constraint>, val returnType: Constraint) : Constraint {
    override fun fits(item: Any?): Boolean =
        item is OasisCallable && item.arity() == args.size
                && if (item is OasisFunction)
            returnType.isEqual(item.returnType)
                    && args.zip(item.parameters).all { it.first.isEqual(it.second.second) }
        else true

    override fun isEqual(other: Constraint): Boolean =
        other is FunctionConstraint && args.zip(other.args).all { it.first.isEqual(it.second) } && returnType.isEqual(
            other.returnType
        )

    override fun toString(): String {
        return "fn[${args.joinToString(", ")}]: $returnType"
    }
}

class NamedConstraint(val name: String) : Constraint {
    override fun fits(item: Any?): Boolean =
        false

    override fun isEqual(other: Constraint): Boolean =
        other is NamedConstraint && name == other.name

    override fun toString(): String {
        return name
    }
}

class UnionConstraint(val left: Constraint, val right: Constraint) : Constraint {
    override fun fits(item: Any?): Boolean =
        left.fits(item) || right.fits(item)

    override fun isEqual(other: Constraint): Boolean =
        other is UnionConstraint && left.isEqual(other.left) && right.isEqual(other.right)

    override fun toString(): String {
        return "$left | $right"
    }
}

class AndConstraint(val left: Constraint, val right: Constraint) : Constraint {
    override fun fits(item: Any?): Boolean =
        left.fits(item) && right.fits(item)

    override fun isEqual(other: Constraint): Boolean =
        other is AndConstraint && left.isEqual(other.left) && right.isEqual(other.right)

    override fun toString(): String {
        return "$left & $right"
    }
}