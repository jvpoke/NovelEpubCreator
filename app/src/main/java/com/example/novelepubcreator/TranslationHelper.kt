package com.example.novelepubcreator

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await

object TranslationHelper {

    suspend fun translateOffline(text: String, sourceLang: String = TranslateLanguage.ENGLISH): String {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLang)
            .setTargetLanguage(TranslateLanguage.PORTUGUESE)
            .build()
        val translator = Translation.getClient(options)

        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()

        return try {
            translator.downloadModelIfNeeded(conditions).await()
            translator.translate(text).await()
        } catch (e: Exception) {
            text // Retorna original em caso de erro
        } finally {
            translator.close()
        }
    }

    // Para tradução online, poderíamos usar uma API como Google Cloud ou LibreTranslate
    // Aqui deixaremos um placeholder que simula a chamada
    suspend fun translateOnline(text: String): String {
        // Simulação de chamada de API
        return " [Traduzido Online] $text"
    }
}
