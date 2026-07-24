package com.qinmu.eyecare.ui.screens.settings

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.qinmu.eyecare.ui.theme.GreenPrimary
import com.qinmu.eyecare.util.AudioTrimUtils
import com.qinmu.eyecare.util.SoundManager
import kotlinx.coroutines.launch

@Composable
fun AudioTrimDialog(
    audioUri: Uri,
    onConfirmTrim: (startMs: Long, durationSeconds: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val totalDurationSec = remember(audioUri) {
        AudioTrimUtils.getAudioDurationSeconds(context, audioUri).coerceAtLeast(3)
    }

    var startSec by remember { mutableFloatStateOf(0f) }
    var trimDurationSec by remember { mutableIntStateOf(2) } // 默认2秒

    DisposableEffect(Unit) {
        onDispose {
            SoundManager.stopSound()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = GreenPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "截取 MP3 护眼提示音",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "截取片段时长选择:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1, 2, 3).forEach { sec ->
                        FilterChip(
                            selected = trimDurationSec == sec,
                            onClick = { trimDurationSec = sec },
                            label = { Text("${sec}秒") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val maxStartSec = (totalDurationSec - trimDurationSec).coerceAtLeast(0).toFloat()
                val currentStartInt = startSec.toInt()

                Text(
                    text = "起点秒数: $currentStartInt 秒 (截至: ${currentStartInt + trimDurationSec} 秒 / 共 $totalDurationSec 秒)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                Slider(
                    value = startSec,
                    onValueChange = { startSec = it.coerceIn(0f, maxStartSec) },
                    valueRange = 0f..maxStartSec.coerceAtLeast(1f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 试听 1~3 秒片段按钮
                OutlinedButton(
                    onClick = {
                        val startMs = (startSec * 1000L).toLong()
                        SoundManager.previewAudioUri(context, audioUri, startMs, trimDurationSec)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("试听选定 ${trimDurationSec}秒 片段")
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val startMs = (startSec * 1000L).toLong()
                            onConfirmTrim(startMs, trimDurationSec)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("保存音效", color = Color.White)
                    }
                }
            }
        }
    }
}
