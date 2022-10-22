interface Constraint {
    fun fits(item: Any?): Boolean
    fun isEqual(other: Constraint): Boolean
}

object NumericConstraint : Constraint {
    override fun fits(item: Any?): Boolean =
        item is Double

    override fun isEqual(other: Constraint): Boolean =
        other is NumericConstraint
}

object StringConstraint : Constraint {
    override fun fits(item: Any?): Boolean =
        item is String

    override fun isEqual(other: Constraint): Boolean =
        other is StringConstraint
}

object BooleanConstraint : Constraint {
    override fun fits(item: Any?): Boolean =
        item is Boolean

    override fun isEqual(other: Constraint): Boolean =
        other is BooleanConstraint
}

object NilConstraint : Constraint {
    override fun fits(item: Any?): Boolean =
        item == null


    override fun isEqual(other: Constraint): Boolean =
        other is NilConstraint
}

object AnyConstraint : Constraint {
    override fun fits(item: Any?): Boolean =
        true

    override fun isEqual(other: Constraint): Boolean =
        other is AnyConstraint
}

object ObjectConstraint : Constraint {
    override fun fits(item: Any?): Boolean =
        item is Map<*, *>

    override fun isEqual(other: Constraint): Boolean =
        other is ObjectConstraint
}

class ListConstraint(val constraint: Constraint) : Constraint {
    override fun fits(item: Any?): Boolean =
        item is List<*> && item.all { constraint.fits(it) }

    override fun isEqual(other: Constraint): Boolean =
        other is ListConstraint && constraint.isEqual(other.constraint)
}

class DictConstraint(val keyConstraint: Constraint, val valueConstraint: Constraint) : Constraint {
    override fun fits(item: Any?): Boolean =
        item is Map<*, *> && item.all { keyConstraint.fits(it.key) && valueConstraint.fits(it.value) }

    override fun isEqual(other: Constraint): Boolean =
        other is DictConstraint && keyConstraint.isEqual(other.keyConstraint) && valueConstraint.isEqual(other.valueConstraint)
}

class TupleConstraint(val constraints: List<Constraint>) : Constraint {
    override fun fits(item: Any?): Boolean =
        item is List<*> && item.size == constraints.size && item.zip(constraints).all { it.second.fits(it.first) }

    override fun isEqual(other: Constraint): Boolean =
        other is TupleConstraint && constraints.zip(other.constraints).all { it.first.isEqual(it.second) }
}

class FunctionConstraint(val args: List<Constraint>, val returnType: Constraint) : Constraint {
    override fun fits(item: Any?): Boolean = false
        // item is Function<*> && item.parameters.size == args.size && item.parameters.zip(args).all { it.second.fits(it.first.type) }

    override fun isEqual(other: Constraint): Boolean =
        other is FunctionConstraint && args.zip(other.args).all { it.first.isEqual(it.second) } && returnType.isEqual(other.returnType)
}

class NamedConstraint(val name: String) : Constraint {
    override fun fits(item: Any?): Boolean =
        false

    override fun isEqual(other: Constraint): Boolean =
        other is NamedConstraint && name == other.name
}

class UnionConstraint(val left: Constraint, val right: Constraint) : Constraint {
    override fun fits(item: Any?): Boolean =
        left.fits(item) || right.fits(item)

    override fun isEqual(other: Constraint): Boolean =
        other is UnionConstraint && left.isEqual(other.left) && right.isEqual(other.right)
}

class AndConstraint(val left: Constraint, val right: Constraint) : Constraint {
    override fun fits(item: Any?): Boolean =
        left.fits(item) && right.fits(item)

    override fun isEqual(other: Constraint): Boolean =
        other is AndConstraint && left.isEqual(other.left) && right.isEqual(other.right)
}