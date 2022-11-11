package StandardLibrary

import NativeFunc
import RuntimeError
import Module
import NativeModule
import Prototype
import java.io.File
import java.nio.charset.Charset

object Io : NativeModule {
    private var print = NativeFunc("print", 1) { _, args ->
        println(args[0])
    }

    private var input = NativeFunc("input", 0) { _, _ ->
        readLine()!!
    }

    private var inputInt = NativeFunc("inputInt", 0) { _, _ ->
        try {
            readLine()?.toInt()?.toDouble()!!
        } catch (e: NumberFormatException) {
            throw RuntimeError("Input was not an integer.")
        }
    }

    private var inputDouble = NativeFunc("inputDouble", 0) { _, _ ->
        try {
            readLine()?.toDouble()!!
        } catch (e: NumberFormatException) {
            throw RuntimeError("Input was not a double.")
        }
    }

    private var open = NativeFunc("open", 1) { _, args ->
        val result = Prototype(null, mutableMapOf(
            "__file" to File(args[0] as String)
        ))
        result.body.putAll(mutableMapOf(
            "read" to NativeFunc("read", 0) { _, _ ->
                (result.get("__file") as File).readText(Charset.defaultCharset())
            },
            "write" to NativeFunc("write", 1) { _, args ->
                (result.get("__file") as File).writeText(args[0] as String, Charset.defaultCharset())
            },
            "exists" to NativeFunc("exists", 0) { _, _ ->
                (result.get("__file") as File).exists()
            }
        ))
        result
    }

    override fun toModule(): Module {
        val module = Module("io")
        module.register("print", print)
        module.register("input", input)
        module.register("inputInt", inputInt)
        module.register("inputDouble", inputDouble)
        module.register("open", open)
        return module
    }
}