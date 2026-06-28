package com.example.data.repository

import com.example.BuildConfig
import com.example.data.remote.gemini.Content
import com.example.data.remote.gemini.GenerateContentRequest
import com.example.data.remote.gemini.Part
import com.example.data.remote.gemini.RetrofitClient
import com.example.domain.repository.AIAssistantRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AIAssistantRepositoryImpl : AIAssistantRepository {
    private val apiKey = BuildConfig.GEMINI_API_KEY
    private val service = RetrofitClient.service

    private suspend fun generateContent(prompt: String, systemInstruction: String? = null): String = withContext(Dispatchers.IO) {
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Please configure your Gemini API Key in the AI Studio Secrets panel."
        }
        
        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            systemInstruction = systemInstruction?.let { Content(parts = listOf(Part(text = it))) }
        )
        
        try {
            val response = service.generateContent(apiKey, request)
            response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "No response from AI."
        } catch (e: Exception) {
            "Error connecting to AI: ${e.message}"
        }
    }

    override suspend fun getTopicSummary(topic: String): String {
        val prompt = "Provide a brief, engaging summary of the topic: $topic. Keep it under 3 sentences."
        return generateContent(prompt)
    }

    override suspend fun getExplanation(question: String, correctAnswer: String): String {
        val prompt = "Explain why '$correctAnswer' is the correct answer to the question: '$question'. Keep it concise and educational."
        val systemInstruction = "You are a helpful quiz tutor. Provide short, clear explanations."
        return generateContent(prompt, systemInstruction)
    }

    override suspend fun getTutorHint(question: String, options: List<String>): String {
        val prompt = "Give a subtle hint for the question: '$question' with options: ${options.joinToString()}. Do not reveal the exact answer. Make it fun."
        val systemInstruction = "You are a witty quiz tutor providing hints."
        return generateContent(prompt, systemInstruction)
    }

    override suspend fun generateQuizQuestions(topic: String, count: Int, difficulty: String): String {
        val prompt = "Generate $count trivia questions about '$topic' at a '$difficulty' difficulty level. Format each question on a new line with 4 options separated by pipes (|), and the correct option index (0-3) at the end. Example: What is the capital of France?|London|Berlin|Paris|Rome|2"
        val systemInstruction = "You are a quiz question generator. Output ONLY the requested format without any extra text."
        return generateContent(prompt, systemInstruction)
    }
    
    override suspend fun getPerformanceAnalysis(correct: Int, wrong: Int, category: String): String {
        val prompt = "I just finished a quiz in the '$category' category. I got $correct correct and $wrong wrong. Give me a brief performance analysis, identify my weakness if any, and suggest a topic for my next practice."
        val systemInstruction = "You are an encouraging AI learning coach."
        return generateContent(prompt, systemInstruction)
    }
}
