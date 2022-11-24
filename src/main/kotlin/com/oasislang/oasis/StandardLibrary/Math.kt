package com.oasislang.oasis.StandardLibrary

import com.oasislang.oasis.Module
import com.oasislang.oasis.NativeFunc
import com.oasislang.oasis.NativeModule
import java.lang.Math
import kotlin.math.*
import kotlin.random.Random

object Math : NativeModule {
    private var sin = NativeFunc("sin", 1) { _, args ->
        sin(args[0] as Double)
    }

    private var cos = NativeFunc("cos", 1) { _, args ->
        cos(args[0] as Double)
    }

    private var tan = NativeFunc("tan", 1) { _, args ->
        tan(args[0] as Double)
    }

    private var asin = NativeFunc("asin", 1) { _, args ->
        asin(args[0] as Double)
    }

    private var acos = NativeFunc("acos", 1) { _, args ->
        acos(args[0] as Double)
    }

    private var atan = NativeFunc("atan", 1) { _, args ->
        atan(args[0] as Double)
    }

    private var atan2 = NativeFunc("atan2", 2) { _, args ->
        atan2(args[0] as Double, args[1] as Double)
    }

    private var sinh = NativeFunc("sinh", 1) { _, args ->
        sinh(args[0] as Double)
    }

    private var cosh = NativeFunc("cosh", 1) { _, args ->
        cosh(args[0] as Double)
    }

    private var tanh = NativeFunc("tanh", 1) { _, args ->
        tanh(args[0] as Double)
    }

    private var asinh = NativeFunc("asinh", 1) { _, args ->
        asinh(args[0] as Double)
    }

    private var acosh = NativeFunc("acosh", 1) { _, args ->
        acosh(args[0] as Double)
    }

    private var atanh = NativeFunc("atanh", 1) { _, args ->
        atanh(args[0] as Double)
    }

    private var exp = NativeFunc("exp", 1) { _, args ->
        exp(args[0] as Double)
    }

    private var expm1 = NativeFunc("expm1", 1) { _, args ->
        expm1(args[0] as Double)
    }

    private var ln = NativeFunc("ln", 1) { _, args ->
        ln(args[0] as Double)
    }

    private var ln1p = NativeFunc("ln1p", 1) { _, args ->
        ln1p(args[0] as Double)
    }

    private var log2 = NativeFunc("log2", 1) { _, args ->
        log2(args[0] as Double)
    }

    private var log10 = NativeFunc("log10", 1) { _, args ->
        log10(args[0] as Double)
    }

    private var log = NativeFunc("log", 2) { _, args ->
        log(args[0] as Double, args[1] as Double)
    }

    private var sqrt = NativeFunc("sqrt", 1) { _, args ->
        sqrt(args[0] as Double)
    }

    private var cbrt = NativeFunc("cbrt", 1) { _, args ->
        Math.cbrt(args[0] as Double)
    }

    private var hypot = NativeFunc("hypot", 2) { _, args ->
        hypot(args[0] as Double, args[1] as Double)
    }

    private var pow = NativeFunc("pow", 2) { _, args ->
        (args[0] as Double).pow(args[1] as Double)
    }

    private var ceil = NativeFunc("ceil", 1) { _, args ->
        ceil(args[0] as Double)
    }

    private var floor = NativeFunc("floor", 1) { _, args ->
        floor(args[0] as Double)
    }

    private var round = NativeFunc("round", 1) { _, args ->
        round(args[0] as Double)
    }

    private var abs = NativeFunc("abs", 1) { _, args ->
        abs(args[0] as Double)
    }

    private var sign = NativeFunc("sign", 1) { _, args ->
        sign(args[0] as Double)
    }

    private var max = NativeFunc("max", 2) { _, args ->
        max(args[0] as Double, args[1] as Double)
    }

    private var min = NativeFunc("min", 2) { _, args ->
        min(args[0] as Double, args[1] as Double)
    }

    private var random = NativeFunc("random", 0) { _, _ ->
        Random.nextDouble()
    }

    private var randomInt = NativeFunc("randomInt", 1) { _, args ->
        Random.nextInt((args[0] as Double).toInt()).toDouble()
    }

    private var randomIntRange = NativeFunc("randomIntRange", 2) { _, args ->
        Random.nextInt((args[0] as Double).toInt(), (args[1] as Double).toInt()).toDouble()
    }

    private var randomRange = NativeFunc("randomRange", 2) { _, args ->
        Random.nextDouble((args[0] as Double), (args[1] as Double))
    }

    private var toDegrees = NativeFunc("toDegrees", 1) { _, args ->
        Math.toDegrees(args[0] as Double)
    }

    private var toRadians = NativeFunc("toRadians", 1) { _, args ->
        Math.toRadians(args[0] as Double)
    }

    private var nextDown = NativeFunc("nextDown", 1) { _, args ->
        (args[0] as Double).nextDown()
    }

    private var nextUp = NativeFunc("nextUp", 1) { _, args ->
        (args[0] as Double).nextUp()
    }

    private var ulp = NativeFunc("ulp", 1) { _, args ->
        (args[0] as Double).ulp
    }

    private var signum = NativeFunc("signum", 1) { _, args ->
        (args[0] as Double).sign
    }

    private var isNaN = NativeFunc("isNaN", 1) { _, args ->
        (args[0] as Double).isNaN()
    }

    private var isInfinite = NativeFunc("isInfinite", 1) { _, args ->
        (args[0] as Double).isInfinite()
    }

    private var isFinite = NativeFunc("isFinite", 1) { _, args ->
        (args[0] as Double).isFinite()
    }

    override fun toModule(): Module {
        return Module("math").apply {
            register("sin", sin)
            register("cos", cos)
            register("tan", tan)
            register("asin", asin)
            register("acos", acos)
            register("atan", atan)
            register("atan2", atan2)
            register("sinh", sinh)
            register("cosh", cosh)
            register("tanh", tanh)
            register("asinh", asinh)
            register("acosh", acosh)
            register("atanh", atanh)
            register("exp", exp)
            register("expm1", expm1)
            register("ln", ln)
            register("ln1p", ln1p)
            register("log2", log2)
            register("log10", log10)
            register("log", log)
            register("sqrt", sqrt)
            register("cbrt", cbrt)
            register("hypot", hypot)
            register("pow", pow)
            register("ceil", ceil)
            register("floor", floor)
            register("round", round)
            register("abs", abs)
            register("sign", sign)
            register("max", max)
            register("min", min)
            register("random", random)
            register("randomInt", randomInt)
            register("randomIntRange", randomIntRange)
            register("randomRange", randomRange)
            register("toDegrees", toDegrees)
            register("toRadians", toRadians)
            register("nextDown", nextDown)
            register("nextUp", nextUp)
            register("ulp", ulp)
            register("signum", signum)
            register("isNaN", isNaN)
            register("isInfinite", isInfinite)
            register("isFinite", isFinite)
        }
    }
}