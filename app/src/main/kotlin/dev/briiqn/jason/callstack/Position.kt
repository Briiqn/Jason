package dev.briiqn.jason.callstack

import com.fasterxml.jackson.databind.JsonNode

data class Position(
    val line: Int,
    val column: Int,
    val sourcePath: String? = null,
    val lineContent: String? = null,
    val length: Int = 1,
    val endLine: Int = line,
    val endColumn: Int = column + length
) {
    companion object {
        fun unknown() = Position(-1, -1)
        
        fun fromNode(node: JsonNode, sourceMap: SourceMap? = null): Position {
            val line = node["_line"]?.asInt() ?: -1
            val column = node["_column"]?.asInt() ?: -1
            val length = node["_length"]?.asInt() ?: 1
            
            return if (line != -1 && column != -1) {
                Position(
                    line = line,
                    column = column,
                    sourcePath = sourceMap?.currentFile,
                    lineContent = sourceMap?.getLine(line),
                    length = length,
                    endLine = node["_endLine"]?.asInt() ?: line,
                    endColumn = node["_endColumn"]?.asInt() ?: (column + length)
                )
            } else {
                unknown()
            }
        }
    }

    fun formatError(message: String): String {
        val location = "${sourcePath ?: "unknown"}:$line:$column"
        val builder = StringBuilder()
        builder.appendLine("Error: $message")
        builder.appendLine("at $location")
        
        lineContent?.let { content ->
            builder.appendLine(content)
            builder.appendLine(" ".repeat(column - 1) + "^".repeat(minOf(length, content.length - column + 1)))
        }
        
        return builder.toString()
    }

    fun contains(other: Position): Boolean {
        if (line > other.line || endLine < other.line) return false
        if (line == other.line && column > other.column) return false
        if (endLine == other.line && endColumn < other.endColumn) return false
        return true
    }

    override fun toString(): String =
        buildString {
            append("${sourcePath ?: "unknown"}:$line:$column")
            if (endLine != line || endColumn != column + 1) {
                append("-$endLine:$endColumn")
            }
        }
}


