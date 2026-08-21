package com.jarvis.assistant

import android.content.Context
import java.io.File

object MemoryHelper {
    private const val FILE_NAME = "raksha_memory.txt"

    fun remember(context: Context, fact: String) {
        val file = File(context.filesDir, FILE_NAME)
        file.appendText(fact.trim() + "\n")
    }

    fun getAllFacts(context: Context): List<String> {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) return emptyList()
        return file.readLines().filter { it.isNotBlank() }
    }

    fun getContextString(context: Context): String {
        val facts = getAllFacts(context)
        if (facts.isEmpty()) return ""
        return "Known facts about the user (use only if relevant to the question):\n" +
            facts.joinToString("\n") { "- $it" }
    }
}
