package dev.briiqn.jason.callstack

import java.io.File

class SourceMap {
    private val sourceLines = mutableMapOf<String, List<String>>()
    var currentFile: String? = null
        private set

    fun loadFile(filePath: String) {
        currentFile = filePath
        if (!sourceLines.containsKey(filePath)) {
            sourceLines[filePath] = File(filePath).readLines()
        }
    }

    fun getLine(lineNumber: Int): String? {
        return currentFile?.let { file ->
            sourceLines[file]?.getOrNull(lineNumber - 1)
        }
    }

    fun getLines(startLine: Int, endLine: Int): List<String> {
        return currentFile?.let { file ->
            sourceLines[file]?.subList(
                maxOf(0, startLine - 1),
                minOf(sourceLines[file]?.size ?: 0, endLine)
            )
        } ?: emptyList()
    }
}