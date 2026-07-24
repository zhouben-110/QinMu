package com.qinmu.eyecare.ui.screens.settings

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qinmu.eyecare.data.model.RemindMode
import com.qinmu.eyecare.data.model.RestSoundEffect
import com.qinmu.eyecare.ui.screens.dashboard.UpdateDialog
import com.qinmu.eyecare.ui.theme.GreenPrimary
import com.qinmu.eyecare.ui.theme.WarmOrange
import com.qinmu.eyecare.util.AudioTrimUtils
import com.qinmu.eyecare.util.PermissionUtils
import com.qinmu.eyecare.util.SoundManager
import com.qinmu.eyecare.util.UpdateInfo
import com.qinmu.eyecare.util.UpdateManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val prefs by viewModel.userPreferences.collectAsState()
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    var hasOverlayPermission by remember { mutableStateOf(PermissionUtils.hasOverlayPermission(context)) }
    var hasUsagePermission by remember { mutableStateOf(PermissionUtils.hasUsageStatsPermission(context)) }

    var updateInfoState by remember { mutableStateOf<UpdateInfo?>(null) }
    var isCheckingUpdate by remember { mutableStateOf(false) }

    var selectedAudioUri by remember { mutableStateOf<Uri?>(null) }
    var isTrimmingAudio by remember { mutableStateOf(false) }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedAudioUri = uri
        }
    }

    var customIntervalText by remember(prefs.remindIntervalMinutes) {
        mutableStateOf(prefs.remindIntervalMinutes.toString())
    }
    var customRestText by remember(prefs.restDurationSeconds) {
        mutableStateOf(prefs.restDurationSeconds.toString())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "⚙️ 偏好与系统权限设置",
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // 1. 提醒模式选择区
            Text(
                text = "护眼提醒模式选择",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = GreenPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    RemindMode.values().forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setRemindMode(mode) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = prefs.remindMode == mode,
                                onClick = { viewModel.setRemindMode(mode) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = mode.displayName,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = mode.description,
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                        if (mode != RemindMode.values().last()) {
                            Divider(color = Color(0xFFF0F0F0))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF3E0), shape = RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = WarmOrange,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "提示：两种提醒模式均内嵌【跳过本次沁目】与【完成休息】按键",
                            fontSize = 11.sp,
                            color = Color(0xFFE65100)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. 提醒音效设置区 (含音效试听与自选 MP3 截取)
            Text(
                text = "提醒提示音效",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = GreenPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    RestSoundEffect.values().forEach { sound ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (sound == RestSoundEffect.CUSTOM_AUDIO) {
                                        audioPickerLauncher.launch("audio/*")
                                    } else {
                                        viewModel.setSoundEffect(sound)
                                        SoundManager.playSound(context, sound)
                                    }
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = prefs.soundEffect == sound,
                                onClick = {
                                    if (sound == RestSoundEffect.CUSTOM_AUDIO) {
                                        audioPickerLauncher.launch("audio/*")
                                    } else {
                                        viewModel.setSoundEffect(sound)
                                        SoundManager.playSound(context, sound)
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = sound.displayName,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                modifier = Modifier.weight(1f)
                            )
                            if (sound == RestSoundEffect.CUSTOM_AUDIO) {
                                OutlinedButton(
                                    onClick = { audioPickerLauncher.launch("audio/*") },
                                    shape = RoundedCornerShape(16.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("选择MP3截取", fontSize = 11.sp)
                                }
                            } else if (sound != RestSoundEffect.MUTE) {
                                TextButton(
                                    onClick = { SoundManager.playSound(context, sound) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("试听", fontSize = 12.sp)
                                }
                            }
                        }
                        if (sound != RestSoundEffect.values().last()) {
                            Divider(color = Color(0xFFF0F0F0))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. 自定义时间周期配置区 (支持点选与键盘手动输入)
            Text(
                text = "时间周期配置 (支持自由输入)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = GreenPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. 连屏使用提醒间隔",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(15, 20, 30, 45).forEach { minutes ->
                            FilterChip(
                                selected = prefs.remindIntervalMinutes == minutes,
                                onClick = {
                                    viewModel.setRemindInterval(minutes)
                                    customIntervalText = minutes.toString()
                                },
                                label = { Text("${minutes}分钟") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = customIntervalText,
                        onValueChange = { newValue ->
                            val filtered = newValue.filter { it.isDigit() }
                            customIntervalText = filtered
                            filtered.toIntOrNull()?.let { min ->
                                viewModel.setRemindInterval(min)
                            }
                        },
                        label = { Text("自定义提醒间隔 (分钟)") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        trailingIcon = { Text("分钟", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(end = 12.dp)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Divider(color = Color(0xFFF0F0F0))
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "2. 单次护眼休息时长",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(20, 60, 180, 300).forEach { seconds ->
                            val labelText = if (seconds < 60) "${seconds}秒" else "${seconds / 60}分钟"
                            FilterChip(
                                selected = prefs.restDurationSeconds == seconds,
                                onClick = {
                                    viewModel.setRestDuration(seconds)
                                    customRestText = seconds.toString()
                                },
                                label = { Text(labelText) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = customRestText,
                        onValueChange = { newValue ->
                            val filtered = newValue.filter { it.isDigit() }
                            customRestText = filtered
                            filtered.toIntOrNull()?.let { sec ->
                                viewModel.setRestDuration(sec)
                            }
                        },
                        label = { Text("自定义休息时长 (秒)") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        trailingIcon = { Text("秒", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(end = 12.dp)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. 版本与更新
            Text(
                text = "关于与应用更新",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = GreenPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                isCheckingUpdate = true
                                val updateJsonUrl = "https://raw.githubusercontent.com/qinmu/version/main/version.json"
                                UpdateManager.checkUpdate(context, updateJsonUrl) { info ->
                                    isCheckingUpdate = false
                                    if (info != null) {
                                        updateInfoState = info
                                    } else {
                                        Toast.makeText(context, "当前已是最新版本 v1.0.0", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = GreenPrimary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "检查新版本", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(text = "当前版本 v1.0.0", fontSize = 11.sp, color = Color.Gray)
                        }
                        if (isCheckingUpdate) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. 权限管理
            Text(
                text = "系统权限状态",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = GreenPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    PermissionItem(
                        title = "悬浮窗 (Overlay) 权限",
                        desc = "全屏强效护眼遮罩与滤镜必须",
                        isGranted = hasOverlayPermission,
                        onClick = {
                            PermissionUtils.requestOverlayPermission(context)
                            hasOverlayPermission = PermissionUtils.hasOverlayPermission(context)
                        }
                    )

                    Divider(color = Color(0xFFF0F0F0))

                    PermissionItem(
                        title = "应用使用情况 (Usage Stats) 权限",
                        desc = "精准统计各应用屏幕使用时长",
                        isGranted = hasUsagePermission,
                        onClick = {
                            PermissionUtils.requestUsageStatsPermission(context)
                            hasUsagePermission = PermissionUtils.hasUsageStatsPermission(context)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // MP3 音频截取弹窗
        selectedAudioUri?.let { uri ->
            AudioTrimDialog(
                audioUri = uri,
                onConfirmTrim = { startMs, durationSec ->
                    coroutineScope.launch {
                        isTrimmingAudio = true
                        val trimmedFile = AudioTrimUtils.trimAudio(context, uri, startMs, durationSec)
                        isTrimmingAudio = false
                        selectedAudioUri = null
                        if (trimmedFile != null && trimmedFile.exists()) {
                            viewModel.setSoundEffect(RestSoundEffect.CUSTOM_AUDIO)
                            SoundManager.playSound(context, RestSoundEffect.CUSTOM_AUDIO)
                            Toast.makeText(context, "成功保存 ${durationSec}秒 专属护眼提示音！", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "音频处理失败，请重试", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onDismiss = {
                    selectedAudioUri = null
                }
            )
        }

        // 版本更新对话框
        updateInfoState?.let { info ->
            UpdateDialog(
                updateInfo = info,
                onConfirm = {
                    UpdateManager.downloadAndInstallApk(context, info.downloadUrl)
                    updateInfoState = null
                },
                onDismiss = {
                    updateInfoState = null
                }
            )
        }
    }
}

@Composable
private fun PermissionItem(
    title: String,
    desc: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            tint = if (isGranted) GreenPrimary else WarmOrange
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(text = desc, fontSize = 11.sp, color = Color.Gray)
        }
        Text(
            text = if (isGranted) "已授权" else "去授权",
            fontSize = 12.sp,
            color = if (isGranted) GreenPrimary else WarmOrange,
            fontWeight = FontWeight.Bold
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.Gray
        )
    }
}
