package dev.briiqn.jason.callstack

import dev.briiqn.jason.interpreter.objects.Value

data class StackFrame(
    val functionName: String,
    val position: Position,
    val localVars: MutableMap<String, Value>,
    val caller: Position? = null
)