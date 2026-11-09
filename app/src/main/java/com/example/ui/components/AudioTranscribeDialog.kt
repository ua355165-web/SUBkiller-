package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var capturedAudioData by remember { mutableStateOf<ByteArray?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isRecording = true
            errorMessage = null
            transcriber.startRecording { bytes ->
                capturedAudioData = bytes
            }
        } else {
            errorMessage = "Microphone permission required for audio transcription."
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (isRecording) {
                transcriber.stopRecording()
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
                            text = "Gemini Audio-to-Text Model",
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
                    text = "Tap the microphone to record your voice notes or speak bill/subscription details for instant AI transcription.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Recording Action Button
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            if (isRecording) Color(0xFFEF4444).copy(alpha = 0.25f)
                            else EmeraldPrimary.copy(alpha = 0.2f)
                        )
                        .border(
                            2.dp,
                            if (isRecording) Color(0xFFEF4444) else EmeraldGlow,
                            CircleShape
                        )
                        .clickable {
                            if (isRecording) {
                                isRecording = false
                                transcriber.stopRecording()

                                scope.launch {
                                    isProcessing = true
                                    val audioBytes = capturedAudioData ?: ByteArray(0)
                                    val result = transcriber.transcribeAudio(audioBytes)
                                    isProcessing = false
                                    if (result.isSuccess) {
                                        transcriptionText = result.getOrNull()
                                    } else {
                                        errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Transcription failed."
                                    }
                                }
                            } else {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                    isRecording = true
                                    errorMessage = null
                                    transcriptionText = null
                                    transcriber.startRecording { bytes ->
                                        capturedAudioData = bytes
                                    }
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
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

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = when {
                        isRecording -> "🔴 Recording in progress... Tap to stop"
                        isProcessing -> "⚡ Gemini transcribing audio..."
                        else -> "Tap microphone to record"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isRecording) Color(0xFFEF4444) else TextMuted
                )

                if (isProcessing) {
                    Spacer(modifier = Modifier.height(14.dp))
                    CircularProgressIndicator(color = CyanAccent, modifier = Modifier.size(28.dp))
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
