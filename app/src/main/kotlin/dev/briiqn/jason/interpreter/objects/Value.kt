package dev.briiqn.jason.interpreter.objects

import dev.briiqn.jason.callstack.Position
import dev.briiqn.jason.exception.InterpreterException

data class Value(val type: Type, val value: Any?) {
    fun toJavaObject(): Any? {
        if (value == null) {
            return null
        }

        return when (type) {
            is Type.Integer -> value as Int
            is Type.Float -> value as Double
            is Type.String -> value as String
            is Type.Boolean -> value as Boolean
            is Type.Void -> null
            is Type.JavaObject -> value
            is Type.Array -> {
                val array = value as List<*>
                array.map { (it as Value).toJavaObject() }.toTypedArray()
            }
            is Type.Struct -> {
                val struct = value as Map<String, Value>
                struct.mapValues { it.value.toJavaObject() }
            }
            is Type.Function -> throw InterpreterException(
                "Cannot convert function to Java object",
                Position.unknown()
            )
        }
    }

    companion object {
        fun fromJavaObject(obj: Any?): Value {
            if (obj == null) {
                return Value(Type.Void, null)
            }

            return when (obj) {
                is Int -> Value(Type.Integer, obj)
                is Double -> Value(Type.Float, obj)
                is String -> Value(Type.String, obj)
                is Boolean -> Value(Type.Boolean, obj)
                is Array<*> -> {
                    val elementType = obj.firstOrNull()?.let { fromJavaObject(it).type } ?: Type.Void
                    Value(Type.Array(elementType), obj.map { fromJavaObject(it) }.toList())
                }
                is List<*> -> {
                    val elementType = obj.firstOrNull()?.let { fromJavaObject(it).type } ?: Type.Void
                    Value(Type.Array(elementType), obj.map { fromJavaObject(it) }.toList())
                }
                is Map<*, *> -> {
                    // Convert the map entries to a proper format for Struct type
                    val convertedFields = obj.entries.associate { (key, value) ->
                        val fieldName = key.toString()
                        val fieldValue = fromJavaObject(value)
                        fieldName to fieldValue
                    }

                    // Create the struct type with field types
                    val structType = Type.Struct(convertedFields.mapValues { it.value.type })

                    // Return the Value with the proper struct type and converted fields
                    Value(structType, convertedFields)
                }
                else -> Value(Type.JavaObject(obj.javaClass), obj)
            }
        }
    }

    override fun toString(): String {
        return when (type) {
            is Type.Void -> "null"
            is Type.Array -> (value as List<*>).joinToString(", ", "[", "]")
            is Type.Struct -> (value as Map<*, *>).entries.joinToString(", ", "{", "}") { "${it.key}: ${it.value}" }
            else -> value.toString()
        }
    }
}