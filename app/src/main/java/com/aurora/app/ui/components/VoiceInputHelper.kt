package com.aurora.app.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * State holder for voice input powered by Android's [SpeechRecognizer].
 *
 * Exposes the current recognition status, the latest result text, any error
 * message, and functions to start / stop listening.
 */
class VoiceInputState internal constructor(private val context: Context) {

    /** Whether the recognizer is actively listening for speech. */
    var isListening: Boolean by mutableStateOf(false)
        internal set

    /** The most recent recognition result, or `null` if none is available. */
    var result: String? by mutableStateOf(null)
        internal set

    /** A human-readable error message when recognition fails, or `null`. */
    var error: String? by mutableStateOf(null)
        internal set

    internal var speechRecognizer: SpeechRecognizer? = null

    /**
     * Begin listening for speech input.
     *
     * Before starting, the caller must ensure the [Manifest.permission.RECORD_AUDIO]
     * permission has been granted. If the permission is missing the call is a no-op
     * and [error] is set to an explanatory message.
     */
    fun startListening() {
        // Check for RECORD_AUDIO permission.
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            error = "RECORD_AUDIO permission is not granted"
            return
        }

        // Reset previous state.
        error = null
        result = null

        val recognizer = speechRecognizer ?: SpeechRecognizer.createSpeechRecognizer(context).also {
            speechRecognizer = it
        }

        recognizer.setRecognitionListener(object : RecognitionListener {

            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
            }

            override fun onBeginningOfSpeech() { /* no-op */ }

            override fun onRmsChanged(rmsdB: Float) { /* no-op */ }

            override fun onBufferReceived(buffer: ByteArray?) { /* no-op */ }

            override fun onEndOfSpeech() {
                isListening = false
            }

            override fun onError(err: Int) {
                isListening = false
                error = mapErrorCode(err)
            }

            override fun onResults(results: Bundle?) {
                isListening = false
                val matches = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                result = matches?.firstOrNull()
            }

            override fun onPartialResults(partialResults: Bundle?) { /* no-op */ }

            override fun onEvent(eventType: Int, params: Bundle?) { /* no-op */ }
        })

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }

        recognizer.startListening(intent)
        isListening = true
    }

    /** Stop listening and cancel any in-flight recognition. */
    fun stopListening() {
        speechRecognizer?.stopListening()
        isListening = false
    }

    internal fun destroy() {
        speechRecognizer?.apply {
            cancel()
            destroy()
        }
        speechRecognizer = null
        isListening = false
    }
}

/**
 * Create and remember a [VoiceInputState] that is automatically tied to the
 * composable lifecycle. The underlying [SpeechRecognizer] is released when
 * the calling composable leaves the composition.
 */
@Composable
fun rememberVoiceInputState(): VoiceInputState {
    val context = LocalContext.current
    val state = remember { VoiceInputState(context) }

    DisposableEffect(Unit) {
        onDispose {
            state.destroy()
        }
    }

    return state
}

// ---------------------------------------------------------------------------
// Internal helpers
// ---------------------------------------------------------------------------

/** Map a [SpeechRecognizer] error code to a readable message. */
private fun mapErrorCode(errorCode: Int): String = when (errorCode) {
    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
    SpeechRecognizer.ERROR_CLIENT -> "Client side error"
    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
    SpeechRecognizer.ERROR_NETWORK -> "Network error"
    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
    SpeechRecognizer.ERROR_NO_MATCH -> "No speech match found"
    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service is busy"
    SpeechRecognizer.ERROR_SERVER -> "Server error"
    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input detected"
    else -> "Unknown recognition error ($errorCode)"
}
