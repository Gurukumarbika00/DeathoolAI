package com.deathpool.ai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

data class ChatMessage(val sender: String, val text: String, val isUser: Boolean)

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {
    private lateinit var tts: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)

        setContent {
            val messages = remember { mutableStateListOf<ChatMessage>() }
            var isProcessing by remember { mutableStateOf(false) }

            fun submitQuery(userText: String) {
                messages.add(ChatMessage("User", userText, true))
                isProcessing = true

                lifecycleScope.launch {
                    val lower = userText.lowercase()
                    isProcessing = false

                    when {
                        lower.contains("youtube") -> {
                            val intent = packageManager.getLaunchIntentForPackage("com.google.android.youtube")
                            if (intent != null) startActivity(intent)
                            messages.add(ChatMessage("Deathpool", "YouTube open kar raha hu.", false))
                            speak("YouTube open kar raha hu")
                        }
                        lower.contains("short") || lower.contains("video") -> {
                            messages.add(ChatMessage("Deathpool", "9:16 Short process shuru ho gaya...", false))
                            speak("Video processing shuru kar di hai.")
                            val dir = getExternalFilesDir(Environment.DIRECTORY_MOVIES)
                            val input = File(dir, "input.mp4").absolutePath
                            val output = File(dir, "short_${System.currentTimeMillis()}.mp4").absolutePath
                            val cmd = "-y -ss 0 -t 30 -i \"$input\" -vf \"crop=ih*(9/16):ih\" -c:v libx264 -preset ultrafast -c:a copy \"$output\""
                            FFmpegKit.executeAsync(cmd) { session ->
                                val success = ReturnCode.isSuccess(session.returnCode)
                                lifecycleScope.launch {
                                    val reply = if (success) "Short ready ho gaya hai!" else "Video process me error aayi."
                                    messages.add(ChatMessage("Deathpool", reply, false))
                                    speak(reply)
                                }
                            }
                        }
                        else -> {
                            val reply = "Main Deathpool AI hu. Offline mode me ready!"
                            messages.add(ChatMessage("Deathpool", reply, false))
                            speak(reply)
                        }
                    }
                }
            }

            GeminiUI(messages, isProcessing, onSend = { submitQuery(it) })
        }
    }

    private fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "AI_VOICE")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) tts.language = Locale("hi", "IN")
    }

    override fun onDestroy() {
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }
}

@Composable
fun GeminiUI(messages: List<ChatMessage>, isProcessing: Boolean, onSend: (String) -> Unit) {
    var textInput by remember { mutableStateOf("") }
    val darkBg = Color(0xFF131314)
    val cardBg = Color(0xFF1E1F20)
    val gradient = Brush.horizontalGradient(listOf(Color(0xFF4E85DE), Color(0xFF9B72CB), Color(0xFFD96570)))

    Scaffold(
        containerColor = darkBg,
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("DEATHPOOL AI", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, brush = gradient))
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF4E85DE))
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp).clip(RoundedCornerShape(32.dp)).background(cardBg).padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("Command boliye ya likhiye...", color = Color.Gray, fontSize = 14.sp) },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            onSend(textInput)
                            textInput = ""
                        }
                    },
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(gradient)
                ) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = "Send", tint = Color.White)
                }
            }
        }
    ) { p ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(p).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(messages) { msg ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start) {
                    Box(modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(if (msg.isUser) Color(0xFF2B2C2F) else cardBg).padding(12.dp)) {
                        Text(msg.text, color = Color.White, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
