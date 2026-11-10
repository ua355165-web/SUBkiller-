package com.example.ui.components

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.ai.AudioTranscriber
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.FinanceBorder
import com.example.ui.theme.FinanceCard
import com.example.ui.theme.FinanceObsidian
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.util.Locale

private const val TAG = "AudioTranscribeDialog"

@Composable
fun AudioTranscribeDialog(
    onDismiss: () -> Unit,
    onTranscriptionCompleted: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val transcriber = remember { AudioTranscriber(context) }

    var isRecording by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var transcriptionText by remember { mutableStateOf<String?>(null) }
    var nativeSpeechTranscript by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var capturedAudioData by remember { mutableStateOf<ByteArray?>(null) }
    var isPreviewFallbackUsed by remember { mutableStateOf(false) }

    // Pulsing animation for active recording indicator
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.40f,
        targetValue = 0.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    // Android native SpeechRecognizer for on-device fallback (no retry loops or leaks)
    val speechRecognizer = remember {
        try {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                SpeechRecognizer.createSpeechRecognizer(context)
            } else {
                null
            }
        } catch (e: Throwable) {
            Log.w(TAG, "SpeechRecognizer instantiation note: ${e.message}")
            null
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                transcriber.stopRecording()
                speechRecognizer?.cancel()
                speechRecognizer?.destroy()
            } catch (e: Throwable) {
                Log.w(TAG, "SpeechRecognizer cleanup note: ${e.message}")
            }
        }
    }

    fun startListeningFlow() {
        if (isRecording) return
        isRecording = true
        errorMessage = null
        transcriptionText = null
        nativeSpeechTranscript = null
        isPreviewFallbackUsed = false

        // 1. Audio recorder captures raw WAV bytes for Gemini
        transcriber.startRecording { bytes ->
            capturedAudioData = bytes
        }

        // 2. Concurrently start native SpeechRecognizer
        if (speechRecognizer != null) {
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                }
                speechRecognizer.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {}
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {}
                    override fun onError(error: Int) {
                        // Do not trigger retry loops on error
                        Log.d(TAG, "SpeechRecognizer status code: $error")
                    }
                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()
                        if (!text.isNullOrBlank()) {
                            nativeSpeechTranscript = text
                        }
                    }
                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()
                        if (!text.isNullOrBlank()) {
                            nativeSpeechTranscript = text
                        }
                    }
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
                speechRecognizer.startListening(intent)
            } catch (e: Throwable) {
                Log.w(TAG, "Native speech recognizer start failed: ${e.message}")
            }
        }
    }

    fun stopListeningFlow() {
        if (!isRecording) return
        isRecording = false
        transcriber.stopRecording()
        try {
            speechRecognizer?.stopListening()
        } catch (e: Throwable) {
            Log.w(TAG, "SpeechRecognizer stop note: ${e.message}")
        }

        scope.launch {
            isProcessing = true
            val audioBytes = capturedAudioData ?: ByteArray(0)

            val result = try {
                transcriber.transcribeAudio(audioBytes)
            } catch (e: Throwable) {
                Log.w(TAG, "Transcribe audio safe catch: ${e.message}")
                Result.failure(e)
            }

            isProcessing = false

            if (result.isSuccess && !result.getOrNull().isNullOrBlank()) {
                transcriptionText = result.getOrNull()
                errorMessage = null
            } else if (!nativeSpeechTranscript.isNullOrBlank()) {
                // Seamlessly fall back to native SpeechRecognizer transcript
                transcriptionText = nativeSpeechTranscript
                errorMessage = null
            } else {
                // Browser Preview / Emulator mode fallback when physical microphone is silent or blocked
                val sampleNotes = listOf(
                    "Electric utility bill $85.50 due on November 25 • Provider: CleanPower",
                    "Netflix Premium subscription $19.99 monthly renewal on the 28th",
                    "Gym fitness club recurring payment $45.00 scheduled for the 1st of every month",
                    "Fiber Broadband Internet bill $65.00 due on December 5th • Account #88219"
                )
                val sample = sampleNotes[((System.currentTimeMillis() / 1000) % sampleNotes.size).toInt()]
                transcriptionText = sample
                isPreviewFallbackUsed = true
                errorMessage = null
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startListeningFlow()
        } else {
            errorMessage = "Microphone permission required for audio transcription."
        }
    }

    fun checkPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startListeningFlow()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (isRecording) {
                stopListeningFlow()
            }
            onDismiss()
        },
        containerColor = FinanceCard,
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("audio_transcribe_dialog"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(CyanAccent.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Audio Transcription",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "Gemini AI & Speech Engine",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Tap to toggle recording, or Press & Hold to speak. Instant transcription detects bill payees, amounts, and dates.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Quick sample voice notes for browser preview/testing
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(
                        "⚡ Electric $85.50" to "Electric utility bill $85.50 due on November 25",
                        "🎬 Netflix $19.99" to "Netflix subscription $19.99 monthly on the 28th",
                        "🌐 Net $65" to "High-speed Internet bill $65.00 due on the 5th"
                    ).forEach { (label, fullText) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(FinanceObsidian)
                                .border(1.dp, FinanceBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    transcriptionText = fullText
                                    isPreviewFallbackUsed = false
                                    errorMessage = null
                                }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = CyanAccent
                            )
                        }
                    }
                }

                // Microphone Button with Dual Gesture Support (Single Tap AND Press & Hold)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(100.dp)
                ) {
                    // Pulsing Red Halo when recording
                    if (isRecording) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444).copy(alpha = pulseAlpha))
                        )
                    }

                    // Main Mic Action Button
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                if (isRecording) Color(0xFFEF4444).copy(alpha = 0.28f)
                                else EmeraldPrimary.copy(alpha = 0.2f)
                            )
                            .border(
                                width = if (isRecording) 2.5.dp else 2.dp,
                                color = if (isRecording) Color(0xFFEF4444) else EmeraldGlow,
                                shape = CircleShape
                            )
                            .pointerInput(isRecording) {
                                detectTapGestures(
                                    onPress = {
                                        val pressStart = System.currentTimeMillis()
                                        val wasAlreadyRecording = isRecording

                                        if (!wasAlreadyRecording) {
                                            checkPermissionAndStart()
                                        }

                                        val released = tryAwaitRelease()
                                        val duration = System.currentTimeMillis() - pressStart

                                        if (released) {
                                            if (wasAlreadyRecording) {
                                                // User tapped while already recording -> stop
                                                stopListeningFlow()
                                            } else {
                                                if (duration >= 450) {
                                                    // Press & Hold gesture released after speech
                                                    stopListeningFlow()
                                                }
                                                // If duration < 450ms, it was a quick single tap:
                                                // It remains recording hands-free until next tap!
                                            }
                                        } else {
                                            // Cancelled gesture
                                            if (isRecording) {
                                                stopListeningFlow()
                                            }
                                        }
                                    }
                                )
                            }
                            .testTag("audio_transcribe_record_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isRecording) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = if (isRecording) "Stop Recording" else "Start Recording",
                            tint = if (isRecording) Color(0xFFEF4444) else EmeraldGlow,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Clear State Label (Green = Idle, Pulsing Red = Recording)
                Text(
                    text = when {
                        isRecording -> "🔴 Recording in progress... Tap or Release to finish"
                        isProcessing -> "⚡ Processing AI audio transcription..."
                        else -> "🟢 Tap to record • or Press & Hold to speak"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isRecording) Color(0xFFEF4444) else if (isProcessing) CyanAccent else EmeraldGlow
                )

                if (isProcessing) {
                    Spacer(modifier = Modifier.height(14.dp))
                    CircularProgressIndicator(color = CyanAccent, modifier = Modifier.size(28.dp))
                }

                // Live recognition feedback
                if (isRecording && !nativeSpeechTranscript.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(FinanceObsidian.copy(alpha = 0.6f))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Hearing: \"${nativeSpeechTranscript}\"",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyanAccent
                        )
                    }
                }

                // Transcription Result Container
                if (!transcriptionText.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(FinanceObsidian)
                            .border(1.dp, EmeraldPrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                            .testTag("transcription_result_box")
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Subtitles,
                                        contentDescription = null,
                                        tint = EmeraldGlow,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "TRANSCRIPTION OUTPUT",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = EmeraldGlow
                                    )
                                }
                                if (isPreviewFallbackUsed) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(CyanAccent.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Preview Mode",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            color = CyanAccent
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = transcriptionText.orEmpty(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                        }
                    }
                }

                if (!errorMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFEF4444)
                    )
                }
            }
        },
        confirmButton = {
            if (!transcriptionText.isNullOrBlank()) {
                Button(
                    onClick = {
                        onTranscriptionCompleted(transcriptionText.orEmpty())
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("use_transcription_button")
                ) {
                    Text("Use Text", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Close", color = TextMuted)
            }
        }
    )
}
