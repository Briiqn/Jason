package dev.briiqn.jason

import com.fasterxml.jackson.databind.ObjectMapper
import java.util.*
import dev.briiqn.jason.interpreter.Interpreter

/*

fun createInterpreterConfig(): JsonNode {
    val mapper = ObjectMapper()
    val factory = JsonNodeFactory.instance

    return mapper.createObjectNode().apply {
        // Fibonacci calculator with recursion
        set<JsonNode>("assertEquals", mapper.createObjectNode().apply {
            put("type", "function")
            put("name", "assertEquals")
            put("return_type", "void")
            set<JsonNode>("parameters", mapper.createArrayNode().apply {
                add("expected")
                add("actual")
                add("message")
            })
            set<JsonNode>("parameter_types", mapper.createArrayNode().apply {
                add("any")
                add("any")
                add("string")
            })
            set<JsonNode>("body", mapper.createArrayNode().apply {
                add(mapper.createObjectNode().apply {
                    put("type", "if")
                    set<JsonNode>("condition", mapper.createObjectNode().apply {
                        put("type", "operation")
                        put("operator", "!=")
                        set<JsonNode>("left", mapper.createObjectNode().apply {
                            put("type", "variable")
                            put("name", "expected")
                        })
                        set<JsonNode>("right", mapper.createObjectNode().apply {
                            put("type", "variable")
                            put("name", "actual")
                        })
                    })
                    set<JsonNode>("then", mapper.createArrayNode().apply {
                        add(mapper.createObjectNode().apply {
                            put("type", "call")
                            put("name", "println")
                            set<JsonNode>("arguments", mapper.createArrayNode().apply {
                                add(mapper.createObjectNode().apply {
                                    put("type", "operation")
                                    put("operator", "+")
                                    set<JsonNode>("left", mapper.createObjectNode().apply {
                                        put("type", "value")
                                        put("value", "❌ Test failed: ")
                                    })
                                    set<JsonNode>("right", mapper.createObjectNode().apply {
                                        put("type", "variable")
                                        put("name", "message")
                                    })
                                })
                            })
                        })
                    })
                    set<JsonNode>("else", mapper.createArrayNode().apply {
                        add(mapper.createObjectNode().apply {
                            put("type", "call")
                            put("name", "println")
                            set<JsonNode>("arguments", mapper.createArrayNode().apply {
                                add(mapper.createObjectNode().apply {
                                    put("type", "operation")
                                    put("operator", "+")
                                    set<JsonNode>("left", mapper.createObjectNode().apply {
                                        put("type", "value")
                                        put("value", "✓ Test passed: ")
                                    })
                                    set<JsonNode>("right", mapper.createObjectNode().apply {
                                        put("type", "variable")
                                        put("name", "message")
                                    })
                                })
                            })
                        })
                    })
                })
            })
        })

        set<JsonNode>("fibonacci", mapper.createObjectNode().apply {
            put("type", "function")
            put("name", "fibonacci")
            put("return_type", "int")
            set<JsonNode>("parameters", mapper.createArrayNode().apply {
                add("n")
            })
            set<JsonNode>("parameter_types", mapper.createArrayNode().apply {
                add("int")
            })
            set<JsonNode>("body", mapper.createArrayNode().apply {
                add(mapper.createObjectNode().apply {
                    put("type", "if")
                    set<JsonNode>("condition", mapper.createObjectNode().apply {
                        put("type", "operation")
                        put("operator", "<=")
                        set<JsonNode>("left", mapper.createObjectNode().apply {
                            put("type", "variable")
                            put("name", "n")
                        })
                        set<JsonNode>("right", mapper.createObjectNode().apply {
                            put("type", "value")
                            put("value", 1)
                        })
                    })
                    set<JsonNode>("then", mapper.createArrayNode().apply {
                        add(mapper.createObjectNode().apply {
                            put("type", "return")
                            set<JsonNode>("value", mapper.createObjectNode().apply {
                                put("type", "variable")
                                put("name", "n")
                            })
                        })
                    })
                })
                add(mapper.createObjectNode().apply {
                    put("type", "return")
                    set<JsonNode>("value", mapper.createObjectNode().apply {
                        put("type", "operation")
                        put("operator", "+")
                        set<JsonNode>("left", mapper.createObjectNode().apply {
                            put("type", "call")
                            put("name", "fibonacci")
                            set<JsonNode>("arguments", mapper.createArrayNode().apply {
                                add(mapper.createObjectNode().apply {
                                    put("type", "operation")
                                    put("operator", "-")
                                    set<JsonNode>("left", mapper.createObjectNode().apply {
                                        put("type", "variable")
                                        put("name", "n")
                                    })
                                    set<JsonNode>("right", mapper.createObjectNode().apply {
                                        put("type", "value")
                                        put("value", 1)
                                    })
                                })
                            })
                        })
                        set<JsonNode>("right", mapper.createObjectNode().apply {
                            put("type", "call")
                            put("name", "fibonacci")
                            set<JsonNode>("arguments", mapper.createArrayNode().apply {
                                add(mapper.createObjectNode().apply {
                                    put("type", "operation")
                                    put("operator", "-")
                                    set<JsonNode>("left", mapper.createObjectNode().apply {
                                        put("type", "variable")
                                        put("name", "n")
                                    })
                                    set<JsonNode>("right", mapper.createObjectNode().apply {
                                        put("type", "value")
                                        put("value", 2)
                                    })
                                })
                            })
                        })
                    })
                })
            })
        })

        // Factorial with while loop
        set<JsonNode>("factorial", mapper.createObjectNode().apply {
            put("type", "function")
            put("name", "factorial")
            put("return_type", "int")
            set<JsonNode>("parameters", mapper.createArrayNode().apply {
                add("n")
            })
            set<JsonNode>("parameter_types", mapper.createArrayNode().apply {
                add("int")
            })
            set<JsonNode>("body", mapper.createArrayNode().apply {
                // Initialize result
                add(mapper.createObjectNode().apply {
                    put("type", "variable")
                    put("name", "result")
                    set<JsonNode>("value", mapper.createObjectNode().apply {
                        put("type", "value")
                        put("value", 1)
                    })
                })
                // Initialize counter
                add(mapper.createObjectNode().apply {
                    put("type", "variable")
                    put("name", "i")
                    set<JsonNode>("value", mapper.createObjectNode().apply {
                        put("type", "value")
                        put("value", 1)
                    })
                })
                // While loop
                add(mapper.createObjectNode().apply {
                    put("type", "while_loop")
                    set<JsonNode>("condition", mapper.createObjectNode().apply {
                        put("type", "operation")
                        put("operator", "<=")
                        set<JsonNode>("left", mapper.createObjectNode().apply {
                            put("type", "variable")
                            put("name", "i")
                        })
                        set<JsonNode>("right", mapper.createObjectNode().apply {
                            put("type", "variable")
                            put("name", "n")
                        })
                    })
                    set<JsonNode>("body", mapper.createArrayNode().apply {
                        // Multiply result by i
                        add(mapper.createObjectNode().apply {
                            put("type", "variable")
                            put("name", "result")
                            set<JsonNode>("value", mapper.createObjectNode().apply {
                                put("type", "operation")
                                put("operator", "*")
                                set<JsonNode>("left", mapper.createObjectNode().apply {
                                    put("type", "variable")
                                    put("name", "result")
                                })
                                set<JsonNode>("right", mapper.createObjectNode().apply {
                                    put("type", "variable")
                                    put("name", "i")
                                })
                            })
                        })
                        // Increment i
                        add(mapper.createObjectNode().apply {
                            put("type", "variable")
                            put("name", "i")
                            set<JsonNode>("value", mapper.createObjectNode().apply {
                                put("type", "operation")
                                put("operator", "+")
                                set<JsonNode>("left", mapper.createObjectNode().apply {
                                    put("type", "variable")
                                    put("name", "i")
                                })
                                set<JsonNode>("right", mapper.createObjectNode().apply {
                                    put("type", "value")
                                    put("value", 1)
                                })
                            })
                        })
                    })
                })
                // Return result
                add(mapper.createObjectNode().apply {
                    put("type", "return")
                    set<JsonNode>("value", mapper.createObjectNode().apply {
                        put("type", "variable")
                        put("name", "result")
                    })
                })
            })
        })

        // Test runner function
        set<JsonNode>("runAdvancedTests", mapper.createObjectNode().apply {
            put("type", "function")
            put("name", "runAdvancedTests")
            put("return_type", "void")
            set<JsonNode>("parameters", mapper.createArrayNode())
            set<JsonNode>("parameter_types", mapper.createArrayNode())
            set<JsonNode>("body", mapper.createArrayNode().apply {
                // Header
                add(mapper.createObjectNode().apply {
                    put("type", "call")
                    put("name", "println")
                    set<JsonNode>("arguments", mapper.createArrayNode().apply {
                        add(mapper.createObjectNode().apply {
                            put("type", "value")
                            put("value", "\n=== Running Advanced Test Suite ===\n")
                        })
                    })
                })

                // Test Fibonacci
                add(mapper.createObjectNode().apply {
                    put("type", "call")
                    put("name", "println")
                    set<JsonNode>("arguments", mapper.createArrayNode().apply {
                        add(mapper.createObjectNode().apply {
                            put("type", "value")
                            put("value", "Testing Fibonacci function:")
                        })
                    })
                })

                // Test cases for Fibonacci
              */
/*  listOf(
                    Triple(0, 0, "fibonacci(0) should be 0"),
                    Triple(1, 1, "fibonacci(1) should be 1"),
                    Triple(5, 5, "fibonacci(5) should be 5"),
                    Triple(7, 13, "fibonacci(7) should be 13")
                ).forEach { (input, expected, message) ->
                    add(mapper.createObjectNode().apply {
                        put("type", "call")
                        put("name", "assertEquals")
                        set<JsonNode>("arguments", mapper.createArrayNode().apply {
                            add(mapper.createObjectNode().apply {
                                put("type", "value")
                                put("value", expected)
                            })
                            add(mapper.createObjectNode().apply {
                                put("type", "call")
                                put("name", "fibonacci")
                                set<JsonNode>("arguments", mapper.createArrayNode().apply {
                                    add(mapper.createObjectNode().apply {
                                        put("type", "value")
                                        put("value", input)
                                    })
                                })
                            })
                            add(mapper.createObjectNode().apply {
                                put("type", "value")
                                put("value", message)
                            })
                        })
                    })
                }*//*


                // Test Factorial
                add(mapper.createObjectNode().apply {
                    put("type", "call")
                    put("name", "println")
                    set<JsonNode>("arguments", mapper.createArrayNode().apply {
                        add(mapper.createObjectNode().apply {
                            put("type", "value")
                            put("value", "\nTesting Factorial function:")
                        })
                    })
                })

                // Test cases for Factorial
                listOf(
                    Triple(0, 1, "factorial(0) should be 1"),
                    Triple(1, 1, "factorial(1) should be 1"),
                    Triple(5, 120, "factorial(5) should be 120"),
                    Triple(7, 5040, "factorial(7) should be 5040")
                ).forEach { (input, expected, message) ->
                    add(mapper.createObjectNode().apply {
                        put("type", "call")
                        put("name", "assertEquals")
                        set<JsonNode>("arguments", mapper.createArrayNode().apply {
                            add(mapper.createObjectNode().apply {
                                put("type", "value")
                                put("value", expected)
                            })
                            add(mapper.createObjectNode().apply {
                                put("type", "call")
                                put("name", "factorial")
                                set<JsonNode>("arguments", mapper.createArrayNode().apply {
                                    add(mapper.createObjectNode().apply {
                                        put("type", "value")
                                        put("value", input)
                                    })
                                })
                            })
                            add(mapper.createObjectNode().apply {
                                put("type", "value")
                                put("value", message)
                            })
                        })
                    })
                }

                // Footer
                add(mapper.createObjectNode().apply {
                    put("type", "call")
                    put("name", "println")
                    set<JsonNode>("arguments", mapper.createArrayNode().apply {
                        add(mapper.createObjectNode().apply {
                            put("type", "value")
                            put("value", "\n=== Advanced Test Suite Completed ===\n")
                        })
                    })
                })
            })
        })

        // Main functiorn that runs the tests
        set<JsonNode>("main", mapper.createArrayNode().apply {
            add(mapper.createObjectNode().apply {
                put("type", "call")
                put("name", "runAdvancedTests")
                set<JsonNode>("arguments", mapper.createArrayNode())
            })
        })
    }
}
*/


fun main() {
    val mapper = ObjectMapper()
    val scanner = Scanner(System.`in`)
    val interpreter = Interpreter()
    //test
    interpreter.registerJavaClass(Person::class.java)

    println("Interpreter ready. \nCommands:\n")
    println("1. Type 'run <filename>' to execute a JSON file")
    println("2. Type JSON to evaluate in real-time")
    println("3. Type 'exit' to quit")

    while (true) {
        print(">> ")
        val input = scanner.nextLine().trim()

        when {
            input.lowercase() == "exit" -> break

            input.lowercase().startsWith("run ") -> {
                val filename = input.substring(4).trim()
                try {
                    interpreter.executeFile(filename)
                } catch (e: Exception) {
                    println("Error running file: ${e.message}")
                }
            }

            else -> try {
                val jsonInput = mapper.readTree(input)
                val result = interpreter.evaluate(jsonInput)
                result?.let { println("Result: $it") }
            } catch (e: Exception) {
                println("Error interpreting input: ${e.message}")
            }
        }
    }

    println("Exiting interpreter.")
}