# JASON (Just Another JVM Scripting Language)

JASON is a JSON-based scripting language that combines the readability of JSON with the power of programming constructs. It's designed to be simple to understand yet capable of handling complex programming tasks.

## Table of Contents
- [Features](#features)
- [Basic Syntax](#basic-syntax)
- [Standard Library](#standard-library)
- [Control Structures](#control-structures)
- [Java Interoperability](#java-interoperability)
- [Examples](#examples)
- [Error Handling](#error-handling)
- [Best Practices](#best-practices)
- [Limitations & Future Improvements](#limitations--future-improvements)

## Features

- JSON-based syntax for easy parsing and writing
- First-class function support
- Built-in standard library
- Type system with support for integers, floats, strings
- Control flow structures (if-else, while loops)
- Java interoperability through reflection and annotations
- Mathematical functions and operations
- Unit testing framework

## Basic Syntax

### Function Declaration

```json
{
  "myFunction": {
    "type": "function",
    "name": "myFunction",
    "return_type": "int",
    "parameters": ["x", "y"],
    "parameter_types": ["int", "int"],
    "body": [
      // Function statements
    ]
  }
}
```

### Variables and Operations

```json
{
  "type": "variable",
  "name": "result",
  "value": {
    "type": "operation",
    "operator": "+",
    "left": {
      "type": "variable",
      "name": "x"
    },
    "right": {
      "type": "value",
      "value": 1
    }
  }
}
```

## Standard Library

### Built-in Functions

- `print(value)`: Prints a value
- `println(value)`: Prints a value with a newline
- `download(url)`: Downloads content from a URL
- `invoke(className, methodName, ...args)`: Invokes Java methods

### Mathematical Functions

- `abs(number)`: Absolute value
- `sqrt(number)`: Square root
- `sin(number)`: Sine
- `cos(number)`: Cosine
- `tan(number)`: Tangent

## Control Structures

### If-Else Statement

```json
{
  "type": "if",
  "condition": {
    "type": "operation",
    "operator": "==",
    "left": {
      "type": "variable",
      "name": "x"
    },
    "right": {
      "type": "value",
      "value": 0
    }
  },
  "then": [
    // Then statements
  ],
  "else": [
    // Else statements
  ]
}
```

### While Loop

```json
{
  "type": "while_loop",
  "condition": {
    "type": "operation",
    "operator": "<",
    "left": {
      "type": "variable",
      "name": "i"
    },
    "right": {
      "type": "value",
      "value": 10
    }
  },
  "body": [
    // Loop body
  ]
}
```

## Java Interoperability

### Annotations

JASON provides annotations for Java integration:
- `@Interpreter.ScriptType`: Marks a class as available to JASON
- `@Interpreter.ScriptConstructor`: Exposes constructors
- `@Interpreter.ScriptFunction`: Exposes methods

### Example Java Class

```java
@Interpreter.ScriptType(name = "Person")
public class Person {
    private String name;
    private int age;

    @Interpreter.ScriptConstructor(name="withParams")
    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    @Interpreter.ScriptConstructor(name="default")
    public Person() {
        this.name="joe";
        this.age=80;
    }

    @Interpreter.ScriptFunction(name = "getName")
    public String getName() {
        return name;
    }

    @Interpreter.ScriptFunction(name = "greet")
    public void greet(){
        System.out.println("Greetings, im " + name + " and i am " + age);
    }
}
```

### Using Java Classes in JASON

```json
{
  "createPerson": {
    "type": "function",
    "name": "createPerson",
    "return_type": "void",
    "parameters": [],
    "parameter_types": [],
    "body": [
      {
        "type": "variable",
        "name": "person",
        "value": {
          "type": "call",
          "name": "Person.withParams",
          "arguments": [
            {
              "type": "value",
              "value": "Alice"
            },
            {
              "type": "value",
              "value": 25
            }
          ]
        }
      },
      {
        "type": "call",
        "name": "person.greet",
        "arguments": []
      }
    ]
  }
}
```

## Examples

### 1. Fibonacci Sequence

```json
{
  "fibonacci": {
    "type": "function",
    "name": "fibonacci",
    "return_type": "int",
    "parameters": ["n"],
    "parameter_types": ["int"],
    "body": [
      {
        "type": "if",
        "condition": {
          "type": "operation",
          "operator": "<=",
          "left": {
            "type": "variable",
            "name": "n"
          },
          "right": {
            "type": "value",
            "value": 1
          }
        },
        "then": [
          {
            "type": "return",
            "value": {
              "type": "variable",
              "name": "n"
            }
          }
        ]
      },
      {
        "type": "return",
        "value": {
          "type": "operation",
          "operator": "+",
          "left": {
            "type": "call",
            "name": "fibonacci",
            "arguments": [
              {
                "type": "operation",
                "operator": "-",
                "left": {
                  "type": "variable",
                  "name": "n"
                },
                "right": {
                  "type": "value",
                  "value": 1
                }
              }
            ]
          },
          "right": {
            "type": "call",
            "name": "fibonacci",
            "arguments": [
              {
                "type": "operation",
                "operator": "-",
                "left": {
                  "type": "variable",
                  "name": "n"
                },
                "right": {
                  "type": "value",
                  "value": 2
                }
              }
            ]
          }
        }
      }
    ]
  }
}
```

### 2. Unit Testing

```json
{
  "assertEquals": {
    "type": "function",
    "name": "assertEquals",
    "return_type": "void",
    "parameters": ["expected", "actual", "message"],
    "parameter_types": ["any", "any", "string"],
    "body": [
      {
        "type": "if",
        "condition": {
          "type": "operation",
          "operator": "!=",
          "left": {
            "type": "variable",
            "name": "expected"
          },
          "right": {
            "type": "variable",
            "name": "actual"
          }
        },
        "then": [
          {
            "type": "call",
            "name": "println",
            "arguments": [
              {
                "type": "operation",
                "operator": "+",
                "left": {
                  "type": "value",
                  "value": "❌ Test failed: "
                },
                "right": {
                  "type": "variable",
                  "name": "message"
                }
              }
            ]
          }
        ],
        "else": [
          {
            "type": "call",
            "name": "println",
            "arguments": [
              {
                "type": "operation",
                "operator": "+",
                "left": {
                  "type": "value",
                  "value": "✓ Test passed: "
                },
                "right": {
                  "type": "variable",
                  "name": "message"
                }
              }
            ]
          }
        ]
      }
    ]
  }
}
```

## Error Handling

The interpreter throws `InterpreterException` with detailed error messages and position information for:
- Type mismatches
- Unknown functions
- Invalid operations
- Runtime errors

## Best Practices

1. **Code Organization**
    - Use descriptive function and variable names
    - Structure code using functions for reusability
    - Include proper type annotations

2. **Java Integration**
    - Use meaningful names for exposed constructors and methods
    - Keep method signatures simple
    - Provide proper error handling
    - Document parameter types and return values

3. **Testing**
    - Use the built-in testing framework
    - Write comprehensive test cases
    - Test edge cases and error conditions

4. **Performance**
    - Avoid deep recursion when possible
    - Use appropriate data types
    - Consider caching results for expensive operations

## Limitations & Future Improvements

### Current Limitations
- Basic data types only (int, float, string)
- No direct array support
- Limited to synchronous operations
- Standard library functions implemented in Kotlin/Java

### Planned Improvements
- Array and collection support
- More data structures
- Enhanced standard library
- Asynchronous operations
- Native implementation of standard library functions
- Additional mathematical and utility functions

## Contributing

We welcome contributions! Please feel free to submit pull requests or open issues for:
- Bug fixes
- New features
- Documentation improvements
- Test cases
- Examples