package dev.briiqn.jason.exception

import dev.briiqn.jason.callstack.Position
import dev.briiqn.jason.callstack.SourceMap

class InterpreterException(
    message: String,
    val position: Position,
    val nodeType: String? = null,
    val sourceMap: SourceMap? = null,
    cause: Throwable? = null
) : Exception(buildErrorMessage(message, position, nodeType, sourceMap), cause) {
    companion object {
        private fun buildErrorMessage(
            message: String,
            position: Position,
            nodeType: String?,
            sourceMap: SourceMap?
        ): String {
            return buildString {
                appendLine(position.formatError(message))
                nodeType?.let { appendLine("Node type: $it") }

                // Add additional context for multi-line errors
                if (position.endLine > position.line && sourceMap != null) {
                    appendLine("Additional context:")
                    sourceMap.getLines(position.line + 1, position.endLine).forEach { line ->
                        appendLine(line)
                    }
                }
            }
        }
    }

    fun getDetailedMessage(): String = buildErrorMessage(message ?: "", position, nodeType, sourceMap)
}