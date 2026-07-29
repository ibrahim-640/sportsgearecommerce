package com.example.sportsgear.ai

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig

object GeminiClient {
    val chatModel by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(
                modelName = "gemini-flash-latest",
                systemInstruction = content {
                    text("""
                        You are a friendly shopping assistant for a sports-gear store.
                        You will be given the current product catalog before each user
                        message. Only state prices and stock using that catalog data —
                        never invent numbers. If something isn't in the catalog, say so
                        honestly. Keep answers short and conversational.
                    """.trimIndent())
                }
            )
    }

    val searchModel by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(
                modelName = "gemini-flash-lite-latest",
                generationConfig = generationConfig {
                    responseMimeType = "application/json"
                }
            )
    }
    val productAnalysisModel by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(
                modelName = "gemini-flash-latest",
                generationConfig = generationConfig {
                    responseMimeType = "application/json"
                }
            )
    }
}