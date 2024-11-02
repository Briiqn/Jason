package dev.briiqn.jason.interpreter

import dev.briiqn.jason.exception.InterpreterException
import dev.briiqn.jason.callstack.Position
import dev.briiqn.jason.interpreter.objects.Type
import dev.briiqn.jason.interpreter.objects.Value
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.net.HttpURLConnection
import java.net.URL

class StandardLibrary() {
    private val functions = mutableMapOf<String, (List<Value>) -> Value>()
    private val outputStream =System.out
    init {
        functions["print"] = { args ->
            require(args.size == 1) { "print() takes exactly one argument" }
            outputStream.write(args[0].value.toString().toByteArray())
            Value(Type.Integer, Unit)
        }

        functions["println"] = { args ->
            require(args.size == 1) { "println() takes exactly one argument" }
            outputStream.write((args[0].value.toString() + "\n").toByteArray())
            Value(Type.Integer, Unit)
        }
        functions["download"] = { args ->
            require(args.size == 1) { "download() takes exactly one argument: URL" }
            require(args[0].type == Type.String) { "Argument must be a URL string" }

            val url = args[0].value as String

            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000



                val content = connection.inputStream.use { input ->
                    BufferedReader(InputStreamReader(input)).use { reader ->
                        reader.readText()
                    }
                }

                Value(Type.String, content)
            } catch (e: Exception) {
                throw InterpreterException("Download failed: ${e.message}", Position.unknown())
            }
        }
        functions["invoke"] = { args ->
            require(args.size >= 2) { "invoke() requires at least two arguments: class name and method name" }
            require(args[0].type == Type.String) { "First argument must be a class name string" }
            require(args[1].type == Type.String) { "Second argument must be a method name string" }

            val className = args[0].value as String
            val methodName = args[1].value as String
            val methodArgs = args.drop(2)

            invokeJavaMethod(className, methodName, methodArgs)
        }

        functions["abs"] = { args ->
            require(args.size == 1) { "abs() takes exactly one argument" }
            when (args[0].type) {
                Type.Integer -> Value(Type.Integer, abs(args[0].value as Int))
                Type.Float -> Value(Type.Float, abs(args[0].value as Double))
                else -> throw InterpreterException("abs() requires numeric argument", Position.unknown())
            }
        }

        functions["sqrt"] = { args ->
            require(args.size == 1) { "sqrt() takes exactly one argument" }
            Value(Type.Float, sqrt(args[0].value.toString().toDouble()))
        }

        functions["sin"] = { args ->
            require(args.size == 1) { "sin() takes exactly one argument" }
            Value(Type.Float, sin(args[0].value.toString().toDouble()))
        }

        functions["cos"] = { args ->
            require(args.size == 1) { "cos() takes exactly one argument" }
            Value(Type.Float, cos(args[0].value.toString().toDouble()))
        }

        functions["tan"] = { args ->
            require(args.size == 1) { "tan() takes exactly one argument" }
            Value(Type.Float, tan(args[0].value.toString().toDouble()))
        }

    }
 // this is kinda cheating since these are written in kotlin but soon i'll write them in jason :)
    private fun abs(value: Int): Int = if (value < 0) -value else value
    private fun abs(value: Double): Double = if (value < 0) -value else value

    private fun sqrt(value: Double): Double {
        if (value < 0) throw InterpreterException("Cannot compute square root of a negative number", Position.unknown())
        var x = value
        var y = 1.0
        val epsilon = 0.00001
        while (abs(x - y) >= epsilon) {
            x = (x + y) / 2
            y = value / x
        }
        return x
    }

    private fun sin(value: Double): Double {
        val radians = value % (2 * Math.PI)
        var term = radians
        var sum = term
        var n = 1
        while (abs(term) > 0.00001) {
            term *= -radians * radians / ((2 * n) * (2 * n + 1))
            sum += term
            n++
        }
        return sum
    }

    private fun cos(value: Double): Double {
        val radians = value % (2 * Math.PI)
        var term = 1.0
        var sum = term
        var n = 0
        while (abs(term) > 0.00001) {
            term *= -radians * radians / ((2 * n + 1) * (2 * n + 2))
            sum += term
            n++
        }
        return sum
    }

    private fun tan(value: Double): Double {
        val cosValue = cos(value)
        if (cosValue == 0.0) throw InterpreterException("Cannot compute tangent for angles where cosine is zero",
            Position.unknown())
        return sin(value) / cosValue
    }

    private fun invokeJavaMethod(className: String, methodName: String, methodArgs: List<Value>): Value {
        try {
            val clazz = Class.forName(className)
            val paramTypes = methodArgs.map { arg -> when (arg.type) {
                Type.Integer -> Int::class.java
                Type.Float -> Double::class.java
                Type.String -> String::class.java
                else -> throw InterpreterException("Unsupported argument type: ${arg.type}", Position.unknown())
            }}.toTypedArray()

            val method: Method = clazz.getMethod(methodName, *paramTypes)
            val convertedArgs = methodArgs.map { arg -> arg.value }.toTypedArray()
            val isStatic = Modifier.isStatic(method.modifiers)

            val result = if (isStatic) {
                method.invoke(null, *convertedArgs)
            } else {
                val instance = clazz.getDeclaredConstructor().newInstance()
                method.invoke(instance, *convertedArgs)
            }
            return when (result) {
                is Int -> Value(Type.Integer, result)
                is Double -> Value(Type.Float, result)
                is String -> Value(Type.String, result)
                else -> throw InterpreterException("Unsupported return type: ${result?.javaClass?.name}", Position.unknown())
            }
        } catch (e: Exception) {
            throw InterpreterException("Error invoking method: ${e.message}", Position.unknown())
        }
    }

    fun hasFunction(name: String): Boolean = name in functions

    fun call(name: String, args: List<Value>): Value =
        functions[name]?.invoke(args) ?: throw InterpreterException("Unknown standard library function: $name",
            Position.unknown())
}
