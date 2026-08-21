package com.jarvis.assistant

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object GeminiHelper {

     private const val API_KEY = "AQ.Ab8RN6LxcmYjNquJ3iXDNuGiKsTDV3AzjZD2Pqkgj2zFU3cZsA"

    private const val API_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$API_KEY"

    fun ask(question: String, memoryContext: String = ""): String {
        return try {
            val url = URL(API_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 15000
            connection.readTimeout = 20000

            val prompt = buildString {
                if (memoryContext.isNotEmpty()) {
                    append(memoryContext)
                    append("\n")
                }
                append("Answer this briefly and naturally in Hinglish (Hindi+English mix), since it will be spoken aloud — keep it short, 2-3 sentences max:\n")
                append(question)
            }

            val body = JSONObject().apply {
                put(
                    "contents", JSONArray().put(
                        JSONObject().put(
                            "parts", JSONArray().put(
                                JSONObject().put("text", prompt)
                            )
                        )
                    )
                )
            }

            connection.outputStream.use { it.write(body.toString().toByteArray()) }

            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().readText()
                val json = JSONObject(response)
                json.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
            } else {
                "Boss, abhi jawab nahi mil paya."
            }
        } catch (e: Exception) {
            "Boss, jawab dhoondte waqt kuch gadbad ho gayi."
        }
    }
}
