package com.example.data.ai

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

private const val TAG = "AudioTranscriber"

class AudioTranscriber(private val context: Context) {
    private var isRecording = false
    private var audioRecord: AudioRecord? = null

    private val model by lazy {
        try {
            Firebase.ai.generativeModel("gemini-3.8-flash")
        } catch (e: Exception) {
            Firebase.ai.generativeModel("gemini-3.5-flash-lite")
        }
    }

    @SuppressLint("MissingPermission")
    fun startRecording(onDataCaptured: (ByteArray) -> Unit) {
        val sampleRate = 16000
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                minBufferSize * 2
            )

            audioRecord?.startRecording()
            isRecording = true

            Thread {
                val outputStream = ByteArrayOutputStream()
                val buffer = ByteArray(minBufferSize)
                while (isRecording) {
                    val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (read > 0) {
                        outputStream.write(buffer, 0, read)
                    }
                }
                val rawPcm = outputStream.toByteArray()
                val wavHeaderAndData = pcmToWav(rawPcm, sampleRate, 1, 16)
                onDataCaptured(wavHeaderAndData)
            }.start()
        } catch (e: Exception) {
            Log.e(TAG, "Error starting microphone recording", e)
        }
    }

    fun stopRecording() {
        isRecording = false
        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping audio recorder", e)
        }
    }

    suspend fun transcribeAudio(audioBytes: ByteArray, userPrompt: String = "Transcribe this audio recording accurately into clear text. Extract any subscription, utility bill amount, due date, or payee details mentioned."): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (audioBytes.isEmpty()) {
                return@withContext Result.failure(IllegalArgumentException("Audio recording is empty."))
            }

            val contentPayload = com.google.firebase.ai.type.Content.Builder().apply {
                inlineData(audioBytes, "audio/wav")
                text(userPrompt)
            }.build()

            val response = model.generateContent(contentPayload)

            val text = response.text
            if (!text.isNullOrBlank()) {
                Result.success(text)
            } else {
                Result.failure(Exception("Transcription returned empty output."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Audio transcription failed", e)
            Result.failure(e)
        }
    }

    private fun pcmToWav(pcmData: ByteArray, sampleRate: Int, channels: Int, bitDepth: Int): ByteArray {
        val totalDataLen = pcmData.size + 36
        val byteRate = sampleRate * channels * (bitDepth / 8)
        val header = ByteArray(44)

        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()

        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = (totalDataLen shr 8 and 0xff).toByte()
        header[6] = (totalDataLen shr 16 and 0xff).toByte()
        header[7] = (totalDataLen shr 24 and 0xff).toByte()

        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()

        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()

        header[16] = 16 // 16 for PCM
        header[17] = 0
        header[18] = 0
        header[19] = 0

        header[20] = 1 // Audio format 1 = PCM
        header[21] = 0

        header[22] = channels.toByte()
        header[23] = 0

        header[24] = (sampleRate and 0xff).toByte()
        header[25] = (sampleRate shr 8 and 0xff).toByte()
        header[26] = (sampleRate shr 16 and 0xff).toByte()
        header[27] = (sampleRate shr 24 and 0xff).toByte()

        header[28] = (byteRate and 0xff).toByte()
        header[29] = (byteRate shr 8 and 0xff).toByte()
        header[30] = (byteRate shr 16 and 0xff).toByte()
        header[31] = (byteRate shr 24 and 0xff).toByte()

        header[32] = (channels * (bitDepth / 8)).toByte() // block align
        header[33] = 0

        header[34] = bitDepth.toByte() // bits per sample
        header[35] = 0

        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()

        header[40] = (pcmData.size and 0xff).toByte()
        header[41] = (pcmData.size shr 8 and 0xff).toByte()
        header[42] = (pcmData.size shr 16 and 0xff).toByte()
        header[43] = (pcmData.size shr 24 and 0xff).toByte()

        return header + pcmData
    }
}
