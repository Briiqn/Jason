package dev.briiqn.jason.interpreter.objects

import dev.briiqn.jason.exception.InterpreterException
import dev.briiqn.jason.callstack.Position

sealed class Type {
    object Integer : Type()
    object Float : Type()
    object String : Type()
    object Boolean : Type()
    object Void : Type()
    data class Array(val elementType: Type) : Type()
    data class Function(val paramTypes: List<Type>, val returnType: Type) : Type()
    data class Struct(val fields: Map<kotlin.String, Type>) : Type()
    data class JavaObject(val clazz: Class<*>) : Type()

    companion object {
        fun fromString(type: kotlin.String): Type {
            // Handle array types
            if (type.startsWith("array<") && type.endsWith(">")) {
                val elementTypeStr = type.substring(6, type.length - 1)
                return Array(fromString(elementTypeStr))
            }

            return when (type) {
                "int" -> Integer
                "float" -> Float
                "string" -> String
                "bool" -> Boolean
                "void" -> Void
                else -> {
                    try {
                        System.out.println(type + "YES YES")
                        val javaClass = Class.forName(type)
                        JavaObject(javaClass)
                    } catch (e: ClassNotFoundException) {
                        throw InterpreterException("Unknown type: $type", Position.unknown())
                    }
                }

            }
        }
    }
    }
