package dev.briiqn.jason.interpreter
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import dev.briiqn.jason.interpreter.objects.Type
import dev.briiqn.jason.interpreter.objects.Value
import dev.briiqn.jason.exception.InterpreterException
import dev.briiqn.jason.callstack.Position
import dev.briiqn.jason.callstack.SourceMap
import dev.briiqn.jason.callstack.StackFrame
import dev.briiqn.jason.exception.BreakException
import dev.briiqn.jason.exception.ContinueException
import java.io.File
import java.lang.reflect.Constructor
import java.lang.reflect.Method




class Interpreter {
    private val functions = mutableMapOf<String, JsonNode>()
    private val variables = mutableMapOf<String, Value>()
    private val types = mutableMapOf<String, Type>()
    private val javaFunctions = mutableMapOf<String, Method>()
    private val javaTypes = mutableMapOf<String, Class<*>>()
    private val objectInstances = mutableMapOf<String, Any>()

    private val stdLib = StandardLibrary()
    private var currentFile: String? = null
    private var currentStackDepth = 0
    private val maxStackDepth = 10000
    private val executionStack = mutableListOf<StackFrame>()
    private val sourceMap = SourceMap()
    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class ScriptFunction(val name: String = "")
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class ScriptType(val name: String = "")
    @Target(AnnotationTarget.CONSTRUCTOR)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class ScriptConstructor(val name: String = "")
    private val javaConstructors = mutableMapOf<String, Constructor<*>>()


    private fun incrementStackDepth() {
        currentStackDepth++
        if (currentStackDepth > maxStackDepth) {
            throw InterpreterException(
                "Stack overflow: Maximum call depth of $maxStackDepth exceeded",
                Position(-1, -1, currentFile),
                "stack_overflow"
            )
        }
    }

    private fun decrementStackDepth() {
        currentStackDepth--
    }
    init {
        initializeStandardLibrary()
    }
    private fun pushStackFrame(frame: StackFrame) {
        if (executionStack.size >= maxStackDepth) {
            val stackTrace = buildStackTrace()
            throw InterpreterException(
                "Stack overflow: Maximum call depth of $maxStackDepth exceeded\n$stackTrace",
                frame.position,
                "stack_overflow"
            )
        }
        executionStack.add(frame)
    }

    private fun popStackFrame(): StackFrame {
        if (executionStack.isEmpty()) {
            throw InterpreterException(
                "Stack underflow: Attempting to pop from empty stack",
                Position(-1, -1, currentFile),
                "stack_underflow"
            )
        }
        return executionStack.removeAt(executionStack.size - 1)
    }
    private fun buildStackTrace(): String {
        return buildString {
            appendLine("Stack trace:")
            executionStack.asReversed().forEachIndexed { index, frame ->
                appendLine("  at ${frame.functionName} (${frame.position})")
                if (frame.caller != null) {
                    appendLine("    called from ${frame.caller}")
                }
            }
        }
    }

    private fun peekStackFrame(): StackFrame? {
        return executionStack.lastOrNull()
    }

    private fun initializeStandardLibrary() {
        types["int"] = Type.Integer
        types["float"] = Type.Float
        types["string"] = Type.String
        types["bool"] = Type.Boolean
        types["void"] = Type.Void
    }

    private fun getNodePosition(node: JsonNode): Position {
        return Position.fromNode(node, sourceMap)
    }

    fun executeFile(filePath: String) {
        sourceMap.loadFile(filePath)

        currentFile = filePath
        try {
            val mapper = ObjectMapper()
            val jsonNode = mapper.readTree(File(filePath))

            jsonNode.fields().forEach { (key, value) ->
                try {
                    when (value["type"]?.asText()) {
                        "function" -> registerFunction(value)
                        "type_def" -> defineType(value)
                    }
                } catch (e: Exception) {
                    handleError(e, value)
                }
            }

            jsonNode["main"]?.let { main ->
                println("Executing main function...")
                pushStackFrame(
                    StackFrame(
                    functionName = "main",
                    localVars = mutableMapOf(),
                    position = getNodePosition(main)
                )
                )
                try {
                    executeStatements(main)
                } finally {
                    popStackFrame()
                }
            } ?: throw InterpreterException(
                "No main function found in the file",
                Position(1, 1, filePath)
            )

        } catch (e: Exception) {
            handleError(e, null)
        }
    }
    private fun handleError(error: Exception, node: JsonNode?) {
        when (error) {
            is InterpreterException -> {
                System.err.println(error.getDetailedMessage())
                System.err.println(buildStackTrace())
            }
            else -> {
                val position = node?.let { getNodePosition(it) }
                    ?: Position.unknown()
                val nodeType = node?.get("type")?.asText()

                val exception = InterpreterException(
                    message = error.message ?: "Unknown error",
                    position = position,
                    nodeType = nodeType,
                    sourceMap = sourceMap,
                    cause = error
                )

                System.err.println(exception.getDetailedMessage())
                System.err.println(buildStackTrace())
            }
        }
    }



                fun executeStatements(statements: JsonNode) {
        if (statements.isArray) {
            statements.forEach { statement ->
                try {
                    when (statement["type"]?.asText()) {
                        "function" -> registerFunction(statement)
                        "type_def" -> defineType(statement)
                        else -> evaluate(statement)
                    }
                } catch (e: Exception) {
                    handleError(e, statement)
                }
            }
        } else {
            evaluate(statements)
        }
    }
    fun registerJavaType(clazz: Class<*>) {
        val scriptType = clazz.getAnnotation(ScriptType::class.java)
        if (scriptType != null) {
            val typeName = if (scriptType.name.isNotEmpty()) scriptType.name else clazz.simpleName
            javaTypes[typeName] = clazz
            types[typeName] = Type.JavaObject(clazz)
        }
    }

    fun registerJavaFunction(method: Method) {
        val scriptFunction = method.getAnnotation(ScriptFunction::class.java)
        if (scriptFunction != null) {
            val funcName = if (scriptFunction.name.isNotEmpty()) scriptFunction.name else method.name
            javaFunctions[funcName] = method
            method.isAccessible = true
        }
    }

    fun registerJavaClass(clazz: Class<*>) {
        println("Starting registration of class: ${clazz.name}")
        try {
            registerJavaType(clazz)
            println("Successfully registered type")

            clazz.constructors.forEach { constructor ->
                if (constructor.isAnnotationPresent(ScriptConstructor::class.java)) {
                    val scriptConstructor = constructor.getAnnotation(ScriptConstructor::class.java)
                    val constructorName = if (scriptConstructor.name.isNotEmpty()) {
                        scriptConstructor.name
                    } else {
                        "${clazz.simpleName}Constructor"
                    }
                    javaConstructors[constructorName] = constructor
                    constructor.isAccessible = true
                    println("Registered constructor: $constructorName")
                }
            }

            // Check if we need an instance
            val needsInstance = clazz.methods.any { method ->
                val hasAnnotation = method.isAnnotationPresent(ScriptFunction::class.java)
                val isNotStatic = !java.lang.reflect.Modifier.isStatic(method.modifiers)
                println("Method ${method.name}: hasAnnotation=$hasAnnotation, isNotStatic=$isNotStatic")
                hasAnnotation && isNotStatic
            }

            println("Class needs instance: $needsInstance")

            if (needsInstance) {
                try {
                    val constructor = clazz.constructors.find {
                        it.isAnnotationPresent(ScriptConstructor::class.java) &&
                                it.parameterCount == 0  // Only for no-arg constructors
                    } ?: clazz.getDeclaredConstructor()

                    constructor.isAccessible = true
                    val instance = constructor.newInstance()
                    objectInstances[clazz.name] = instance
                    println("Successfully created instance")
                } catch (e: Exception) {
                    println("Failed to create instance: ${e::class.simpleName} - ${e.message}")
                    // Instead of throwing, we could store a flag indicating this class needs explicit instantiation
                    clazz.constructors.firstOrNull()?.let { constructor ->
                        println("Class requires explicit instantiation with ${constructor.parameterCount} parameters")
                    }
                }
            }

            var methodCount = 0
            clazz.methods.forEach { method ->
                if (method.isAnnotationPresent(ScriptFunction::class.java)) {
                    registerJavaFunction(method)
                    methodCount++
                    println("Registered method: ${method.name}")
                }
            }
            println("Registered $methodCount methods")

        } catch (e: Exception) {
            println("Error during class registration: ${e::class.simpleName} - ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
    private fun callJavaFunction(
        funcName: String,
        args: JsonNode,
        callerPosition: Position
    ): Value {
        val method = javaFunctions[funcName] ?: throw InterpreterException(
            "Java function $funcName not found",
            callerPosition,
            "function_call"
        )

        val paramTypes = method.parameterTypes
        if (args.size() != paramTypes.size) {
            throw InterpreterException(
                "Function $funcName expects ${paramTypes.size} arguments, got ${args.size()}",
                callerPosition,
                "function_call"
            )
        }

        val javaArgs = Array(args.size()) { i ->
            val arg = evaluate(args[i]) ?: Value(Type.Void, null)
            arg.toJavaObject()
        }

        try {
            if (java.lang.reflect.Modifier.isStatic(method.modifiers)) {
                val result = method.invoke(null, *javaArgs)
                return Value.fromJavaObject(result)
            }

            val declaringClass = method.declaringClass
            val instance = objectInstances.entries
                .firstOrNull { (key, _) -> key.startsWith(declaringClass.name + "#") }
                ?.value ?: throw InterpreterException(
                "No instance found for ${declaringClass.name}",
                callerPosition,
                "instance_error"
            )

            val result = method.invoke(instance, *javaArgs)
            return Value.fromJavaObject(result)
        } catch (e: Exception) {
            val cause = e.cause ?: e
            throw InterpreterException(
                "Error calling Java function $funcName: ${cause.message}",
                callerPosition,
                "function_call",
                cause = cause
            )
        }
    }
    private fun executeWhileLoop(node: JsonNode, localVars: MutableMap<String, Value>): Value? {
        val conditionNode = node["condition"]
        var lastResult: Value? = null
        var iterationCount = 0
        val maxIterations = 32767

        while (true) {
            if (++iterationCount > maxIterations) {
                throw InterpreterException(
                    "Maximum while loop iterations ($maxIterations) exceeded - possible infinite loop",
                    getNodePosition(node),
                    "while_loop"
                )
            }

            val condition = evaluate(conditionNode)
                ?: throw InterpreterException("Condition Cannot be null", Position.unknown(),"general_failure",sourceMap)

            if (condition.type != Type.Boolean) {
                throw InterpreterException("While loop takes BOOLEAN", Position.unknown(),"general_failure",sourceMap)
            }

            if (!(condition.value as Boolean)) break

            try {
                lastResult = executeBody(node["body"], localVars)
            } catch (e: BreakException) {
                break
            } catch (e: ContinueException) {
                continue
            }
        }

        return lastResult
    }

    private fun registerFunction(node: JsonNode) {
        try {
            val name = node["name"]?.asText() ?: throw InterpreterException(
                "Function definition missing name",
                getNodePosition(node),
                "function"
            )
            println("Registering function: $name")
            functions[name] = node
        } catch (e: Exception) {
            throw InterpreterException(
                "Error registering function: ${e.message}",
                getNodePosition(node),
                "function",

            )
        }
    }
    fun registerInstance(instance: Any) {
        val className = instance.javaClass.name
        objectInstances[className] = instance
        objectInstances["${className}#default"] = instance

        objectInstances[instance.javaClass.simpleName] = instance

        println("Registered instance with keys:")
        println("- $className")
        println("- ${className}#default")
        println("- ${instance.javaClass.simpleName}")
    }

    fun clearInstances() {
        objectInstances.clear()
    }
    private fun defineType(node: JsonNode) {
        val position = getNodePosition(node)
        val name = node["name"]?.asText() ?: throw InterpreterException(
            "Type definition missing name",
            position,
            "type_def"
        )
        val typeDefKind = node["kind"]?.asText() ?: throw InterpreterException(
            "Type definition missing kind",
            position,
            "type_def"
        )

        val definedType = try {
            when (typeDefKind) {
                "struct" -> {
                    val fields = node["fields"]?.associate {
                        val fieldName = it["name"]?.asText() ?: throw InterpreterException(
                            "Struct field missing name",
                            getNodePosition(it),
                            "struct_field"
                        )
                        val fieldType = it["type"]?.asText() ?: throw InterpreterException(
                            "Struct field missing type",
                            getNodePosition(it),
                            "struct_field"
                        )
                        fieldName to Type.fromString(fieldType)
                    } ?: throw InterpreterException(
                        "Struct definition missing fields",
                        position,
                        "type_def"
                    )
                    Type.Struct(fields)
                }
                "array" -> {
                    val elementType = node["element_type"]?.asText() ?: throw InterpreterException(
                        "Array type definition missing element type",
                        position,
                        "type_def"
                    )
                    Type.Array(Type.fromString(elementType))
                }
                else -> throw InterpreterException(
                    "Unknown type definition kind: $typeDefKind",
                    position,
                    "type_def"
                )
            }
        } catch (e: Exception) {
            throw InterpreterException(
                "Error defining type: ${e.message}",
                position,
                "type_def",

            )
        }

        types[name] = definedType
    }
    private fun createNewInstance(node: JsonNode): Value {
        val className = node["class"].asText()
        val constructorName = node["constructor"]?.asText() ?: "${className}Constructor"
        println("Creating new instance of $className using constructor $constructorName")

        val args = node["arguments"]
        val javaClass = javaTypes[className] ?: throw InterpreterException(
            "Unknown class type: $className",
            getNodePosition(node),
            "type_error"
        )

        val evaluatedArgs = args.map { argNode ->
            println("Evaluating argument: $argNode")
            evaluate(argNode) ?: throw InterpreterException(
                "Constructor argument cannot be null",
                getNodePosition(argNode),
                "constructor_error"
            )
        }

        try {
            // Try to find the specifically named constructor first
            val constructor = javaConstructors[constructorName] ?: findMatchingConstructor(javaClass, evaluatedArgs)
            ?: throw InterpreterException(
                "No matching constructor found for $className with given arguments",
                getNodePosition(node),
                "constructor_error"
            )

            println("Found constructor: $constructor")
            val javaArgs = evaluatedArgs.map { it.toJavaObject() }.toTypedArray()
            val instance = constructor.newInstance(*javaArgs)
            val instanceKey = "${javaClass.name}#${System.identityHashCode(instance)}"
            objectInstances[instanceKey] = instance
            println("Stored instance with key: $instanceKey")

            return Value.fromJavaObject(instance)
        } catch (e: Exception) {
            throw InterpreterException(
                "Error creating instance of $className: ${e.message}",
                getNodePosition(node),
                "constructor_error",
                cause = e
            )
        }
    }


    private fun findMatchingConstructor(
        clazz: Class<*>,
        args: List<Value>
    ): Constructor<*>? {
        val constructors = clazz.constructors

        val exactMatch = constructors.firstOrNull { constructor ->
            if (constructor.parameterTypes.size != args.size) return@firstOrNull false

            args.zip(constructor.parameterTypes).all { (arg, paramType) ->
                isExactTypeMatch(arg, paramType)
            }
        }

        if (exactMatch != null) return exactMatch

        return constructors.firstOrNull { constructor ->
            if (constructor.parameterTypes.size != args.size) return@firstOrNull false

            args.zip(constructor.parameterTypes).all { (arg, paramType) ->
                isCompatibleType(arg, paramType)
            }
        }
    }

    private fun isExactTypeMatch(arg: Value, paramType: Class<*>): Boolean {
        return when (paramType) {
            String::class.java -> arg.type == Type.String
            Int::class.java, Integer::class.java -> arg.type == Type.Integer
            Double::class.java -> arg.type == Type.Float
            Boolean::class.java -> arg.type == Type.Boolean
            else -> false
        }
    }

    private fun isCompatibleType(arg: Value, paramType: Class<*>): Boolean {
        return when {
            paramType == Int::class.java || paramType == Integer::class.java ->
                arg.type == Type.Integer || arg.type == Type.Float
            paramType == Double::class.java || paramType == Float::class.java ->
                arg.type == Type.Float || arg.type == Type.Integer
            paramType == String::class.java ->
                true //  Lazy
            paramType == Boolean::class.java ->
                arg.type == Type.Boolean

            else -> false
        }
    }
    fun evaluate(node: JsonNode): Value? {
        if (node == null) return null

        val currentFrame = peekStackFrame()
        val localVars = currentFrame?.localVars ?: mutableMapOf()

        return when (val nodeType = node["type"]?.asText()) {
            "value" -> evaluateValue(node)
            "if" -> executeIfStatement(node, localVars)
            "switch" -> executeSwitchStatement(node, localVars)
            "variable" -> evaluateVariable(node, localVars)
            "operation" -> evaluateOperation(node, localVars)
            "new" -> createNewInstance(node)
            "call" -> {
                val funcName = node["name"].asText()

                val arguments = node["arguments"] ?: throw InterpreterException(
                    "No arguments field in function call",
                    getNodePosition(node),
                    "function_call"
                )

                when {
                    javaFunctions.containsKey(funcName) ->

                        callJavaFunction(funcName, arguments, getNodePosition(node))

                    stdLib.hasFunction(funcName) ->
                        callStdLibFunction(funcName, arguments, localVars)
                    functions.containsKey(funcName) ->
                        callFunction(functions[funcName]!!, arguments, getNodePosition(node))
                    else -> throw InterpreterException(
                        "Function $funcName not defined",
                        getNodePosition(node),
                        "undefined_function"
                    )
                }
            }
            "function" -> {
                registerFunction(node)
                null
            }
            "type_def" -> {
                defineType(node)
                null
            }
            "for_loop" -> executeForLoop(node, localVars)
            "while_loop" -> executeWhileLoop(node, localVars)
            null -> throw InterpreterException(
                    "Missing node type",
            getNodePosition(node),
            "missing_type",
            sourceMap
                )
            else -> throw InterpreterException(
                "Unknown node type: $nodeType",
                getNodePosition(node),
                nodeType,
                sourceMap
            )

        }
    }
    private fun createJavaObject(type: String, args: List<Value>): Value {
        val javaClass = javaTypes[type] ?: throw InterpreterException(
            "Unknown Java type: $type",
            Position.unknown(),
            "type_error"
        )

        try {
            val constructor = javaClass.constructors.firstOrNull { constructor ->
                constructor.parameterTypes.size == args.size &&
                        args.zip(constructor.parameterTypes).all { (arg, paramType) ->
                            paramType.isAssignableFrom(arg.toJavaObject()?.javaClass)
                        }
            } ?: throw InterpreterException(
                "No matching constructor found for type $type",
                Position.unknown(),
                "type_error"
            )

            val javaArgs = args.map { it.toJavaObject() }.toTypedArray()
            val instance = constructor.newInstance(*javaArgs)
            return Value.fromJavaObject(instance)
        } catch (e: Exception) {
            throw InterpreterException(
                "Error creating Java object of type $type: ${e.message}",
                Position.unknown(),
                "type_error",
                cause = e
            )
        }
    }

    private fun executeIfStatement(node: JsonNode, localVars: MutableMap<String, Value>): Value? {
        val conditionNode = node["condition"]
        val thenBody = node["then"]
        val elseIfBranches = node["elseif"]
        val elseBody = node["else"]

        val condition = evaluate(conditionNode)
            ?:throw InterpreterException("Condition Cannot be null", Position.unknown(),"general_failure",sourceMap)

        if (condition.type != Type.Boolean) {
            throw throw InterpreterException("If condition must take a BOOLEAN", Position.unknown(),"general_failure",sourceMap)
        }

        if (condition.value as Boolean) {
            return executeBody(thenBody, localVars)
        }

        if (elseIfBranches != null && elseIfBranches.isArray) {
            for (elseIfBranch in elseIfBranches) {
                val elseIfCondition = evaluate(elseIfBranch["condition"])
                    ?: throw InterpreterException("Condition Cannot be null", Position.unknown(),"general_failure",sourceMap)

                if (elseIfCondition.type != Type.Boolean) {
                    throw InterpreterException("Condition must take a BOOLEAN", Position.unknown(),"general_failure",sourceMap)
                }

                if (elseIfCondition.value as Boolean) {
                    return executeBody(elseIfBranch["then"], localVars)
                }
            }
        }

        return elseBody?.let { executeBody(it, localVars) }
    }
    private fun evaluateValue(node: JsonNode): Value {
        val value = node["value"]
        return when {
            value.isInt -> Value(Type.Integer, value.asInt())
            value.isDouble -> Value(Type.Float, value.asDouble())
            value.isTextual -> Value(Type.String, value.asText())
            value.isBoolean -> Value(Type.Boolean, value.asBoolean())
            else -> throw InterpreterException("Unsupported value type", Position.unknown(),"unknown_value",sourceMap)
        }
    }

    private fun evaluateVariable(node: JsonNode, localVars: MutableMap<String, Value>): Value? {
        val name = node["name"].asText()
        return localVars[name] ?: variables[name]
    }

    private fun evaluateOperation(node: JsonNode, localVars: MutableMap<String, Value>): Value {
        val left = evaluate(node["left"]) ?: throw InterpreterException(
            "Left operand is null",
            getNodePosition(node),
            "operation",
            sourceMap
        )
        val right = evaluate(node["right"]) ?: throw InterpreterException(
            "Right operand is null",
            getNodePosition(node),
            "operation",
            sourceMap
        )
        val operator = node["operator"].asText()
        return performOperation(operator, left, right,node)
    }



    private fun executeForLoop(node: JsonNode, localVars: MutableMap<String, Value>): Value? {
        val iteratorName = node["iterator"].asText()
        val rangeStart = evaluate(node["range_start"])
            ?: throw InterpreterException("Range start cannot be null",getNodePosition(node),"general_failure",sourceMap)
        val rangeEnd = evaluate(node["range_end"])
            ?:throw InterpreterException("Range end cannot be null",getNodePosition(node),"general_failure",sourceMap)

        if (rangeStart.type != Type.Integer || rangeEnd.type != Type.Integer) {
            throw InterpreterException("For loop ranges must be integers",getNodePosition(node),"general_failure",sourceMap)
        }

        var lastResult: Value? = null
        val start = rangeStart.value as Int
        val end = rangeEnd.value as Int

        for (i in start until end) {
            localVars[iteratorName] = Value(Type.Integer, i)
            try {
                lastResult = executeBody(node["body"], localVars)
            } catch (e: BreakException) {
                break
            } catch (e: ContinueException) {
                continue
            }
        }

        return lastResult
    }
    private fun performOperation(operator: String, left: Value, right: Value, node:JsonNode): Value {
        return when (operator) {
            "+" -> when {
                left.type == Type.Integer && right.type == Type.Integer ->
                    Value(Type.Integer, (left.value as Int) + (right.value as Int))
                left.type == Type.Float || right.type == Type.Float ->
                    Value(Type.Float, left.value.toString().toDouble() + right.value.toString().toDouble())
                left.type == Type.String || right.type == Type.String ->
                    Value(Type.String, "${left.value}${right.value}")
                else -> throw InterpreterException("Invalid types for addition", getNodePosition(node),"operation_exception",sourceMap)
            }
            "-" -> when {
                left.type == Type.Integer && right.type == Type.Integer ->
                    Value(Type.Integer, (left.value as Int) - (right.value as Int))
                left.type == Type.Float || right.type == Type.Float ->
                    Value(Type.Float, left.value.toString().toDouble() - right.value.toString().toDouble())
                else -> throw InterpreterException("Invalid types for subtraction",getNodePosition(node),"operation_exception",sourceMap)
            }
            "*" -> when {
                left.type == Type.Integer && right.type == Type.Integer ->
                    Value(Type.Integer, (left.value as Int) * (right.value as Int))
                left.type == Type.Float || right.type == Type.Float ->
                    Value(Type.Float, left.value.toString().toDouble() * right.value.toString().toDouble())
                else -> throw InterpreterException(
                    "Invalid Types for multiplication",
                    getNodePosition(node),
                    "arithmetic_error",
                    sourceMap
                )
            }
            "/" -> when {
                right.value.toString().toDouble() == 0.0 ->
                    throw InterpreterException(
                        "Division by zero",
                        getNodePosition(node),
                        "arithmetic_error",
                        sourceMap
                    )
                left.type == Type.Integer && right.type == Type.Integer ->
                    Value(Type.Integer, (left.value as Int) / (right.value as Int))
                left.type == Type.Float || right.type == Type.Float ->
                    Value(Type.Float, left.value.toString().toDouble() / right.value.toString().toDouble())
                else ->throw InterpreterException(
                    "Unknown operator: $operator",
                    getNodePosition(node),
                    "invalid_operator",
                    sourceMap
                )
            }
            "==" -> Value(Type.Boolean, left.value == right.value)
            "!=" -> Value(Type.Boolean, left.value != right.value)
            ">=" -> when {
                left.type == Type.Integer && right.type == Type.Integer ->
                    Value(Type.Boolean, (left.value as Int) >= (right.value as Int))
                left.type == Type.Float || right.type == Type.Float ->
                    Value(Type.Boolean, left.value.toString().toDouble() >= right.value.toString().toDouble())
                else -> throw InterpreterException(
                    "Invalid type for >= operation : $operator",
                    getNodePosition(node),
                    "invalid_operator",
                    sourceMap
                )
            }
            "<=" -> when {
                left.type == Type.Integer && right.type == Type.Integer ->
                    Value(Type.Boolean, (left.value as Int) <= (right.value as Int))
                left.type == Type.Float || right.type == Type.Float ->
                    Value(Type.Boolean, left.value.toString().toDouble() <= right.value.toString().toDouble())
                else -> throw InterpreterException(
                        "Invalid type for <= operation : $operator",
                    getNodePosition(node),
                "invalid_operator",
                sourceMap)
            }
            ">" -> when {
                left.type == Type.Integer && right.type == Type.Integer ->
                    Value(Type.Boolean, (left.value as Int) > (right.value as Int))
                left.type == Type.Float || right.type == Type.Float ->
                    Value(Type.Boolean, left.value.toString().toDouble() > right.value.toString().toDouble())
                else -> throw InterpreterException(
                    "Invalid type for > operation : $operator",
                    getNodePosition(node),
                    "invalid_operator",
                    sourceMap)
            }
            "<" -> when {
                left.type == Type.Integer && right.type == Type.Integer ->
                    Value(Type.Boolean, (left.value as Int) < (right.value as Int))
                left.type == Type.Float || right.type == Type.Float ->
                    Value(Type.Boolean, left.value.toString().toDouble() < right.value.toString().toDouble())
                else ->throw InterpreterException(
                    "Invalid type for < operation : $operator",
                    getNodePosition(node),
                    "invalid_operator",
                    sourceMap)
            }
            "||" -> when {
                left.type == Type.Boolean && right.type == Type.Boolean ->
                    Value(Type.Boolean, (left.value as Boolean) || (right.value as Boolean))
                else -> throw InterpreterException(
                    "Invalid types for logical OR operation: both operands must be boolean : $operator",
                    getNodePosition(node),
                    "invalid_operator",
                    sourceMap)
            }
            "&&" -> when {
                left.type == Type.Boolean && right.type == Type.Boolean ->
                    Value(Type.Boolean, (left.value as Boolean) && (right.value as Boolean))
                else -> throw InterpreterException(
                    "Invalid types for logical AND operation: both operands must be boolean : $operator",
                    getNodePosition(node),
                    "invalid_operator",
                    sourceMap)
            }
            "+=" -> performOperation("+", left, right,node)
            "-=" -> performOperation("-", left, right,node)
            "*=" -> performOperation("*", left, right,node)
            "/=" -> performOperation("/", left, right,node)
            else -> throw InterpreterException(
                "Unknown Error : $operator",
                getNodePosition(node),
                "invalid_operator",
                sourceMap)
        }
    }
    private fun callStdLibFunction(
        funcName: String,
        args: JsonNode,
        localVars: MutableMap<String, Value>
    ): Value {
        val evaluatedArgs = args.map { evaluate(it) ?: throw InterpreterException("Argument cannot be null",
            Position.unknown()
        ) }
        return stdLib.call(funcName, evaluatedArgs)
    }
    private fun callFunction(
        func: JsonNode,
        args: JsonNode,
        callerPosition: Position
    ): Value? {
        val functionName = func["name"].asText()
        val parameters = func["parameters"]

        if (args.size() != parameters.size()) {
            throw InterpreterException(
                "Function $functionName expects ${parameters.size()} arguments, got ${args.size()}",
                getNodePosition(func),
                "function_call",
                sourceMap
            )
        }

        val callVars = mutableMapOf<String, Value>()
        for (i in 0 until parameters.size()) {
            val paramName = parameters[i].asText()
            evaluate(args[i])?.let { value ->
                callVars[paramName] = value
            }
        }

        pushStackFrame(
            StackFrame(
            functionName = functionName,
            localVars = callVars,
            position = getNodePosition(func),
            caller = callerPosition
        )
        )

        try {
            return executeBody(func["body"],peekStackFrame()!!.localVars)
        } finally {
            popStackFrame()
        }
    }

    private fun executeBody(body: JsonNode, localVars: MutableMap<String, Value>): Value? {
        var result: Value? = null
        var shouldBreak = false

        body.forEach { statement ->
            if (!shouldBreak) {
                when (statement["type"]?.asText()) {
                    "return" -> {
                        result = evaluate(statement["value"])
                        shouldBreak = true
                    }
                    "break" -> {
                        shouldBreak = true
                        throw BreakException()
                    }
                    "continue" -> throw ContinueException()
                    "variable" -> {
                        if (statement.has("value")) {
                            val name = statement["name"].asText()
                            evaluate(statement["value"])?.let { value ->
                                localVars[name] = value
                            }
                        }
                    }
                    else -> result = evaluate(statement)
                }
            }
        }
        return result
    }
    private fun executeSwitchStatement(node: JsonNode, localVars: MutableMap<String, Value>): Value? {
        val expression = evaluate(node["expression"])
            ?:      throw InterpreterException(
                "Switch statement musn't be null",
                getNodePosition(node),
                "switch_statement",
                sourceMap
            )

        val cases = node["cases"]
        val defaultCase = node["default"]

        if (!cases.isArray) {
            throw InterpreterException(
                "Switch cases must be an array",
                getNodePosition(node),
                "switch_statement",
                sourceMap
            )
        }

        for (case in cases) {
            val caseValue = evaluate(case["value"])
                ?: throw InterpreterException("Case value cannot be null",getNodePosition(node))

            if (valuesAreEqual(expression, caseValue)) {
                val result = executeBody(case["body"], localVars)

                if (!case.has("fallthrough") || !case["fallthrough"].asBoolean()) {
                    return result
                }
            }
        }

        return defaultCase?.let { executeBody(it, localVars) }
    }

    private fun valuesAreEqual(val1: Value, val2: Value): Boolean {
        if (val1.type != val2.type) return false

        return when (val1.type) {
            Type.Integer -> (val1.value as Int) == (val2.value as Int)
            Type.Float -> (val1.value as Double) == (val2.value as Double)
            Type.String -> (val1.value as String) == (val2.value as String)
            Type.Boolean -> (val1.value as Boolean) == (val2.value as Boolean)
            else -> false
        }
    }
}
