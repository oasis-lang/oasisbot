class Tuple(vararg val elements: Any?) {
    override fun toString(): String {
        return elements.joinToString(", ", "(", ")")
    }

    fun get(index: Int): Any? {
        return elements[index]
    }
}