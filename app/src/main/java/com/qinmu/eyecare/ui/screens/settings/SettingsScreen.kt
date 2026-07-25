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
import com.qinmu.eyecare.data.model.SpecialMode
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "⚙️ 偏好与系统权限设置",
                            fontWeight = FontWeight.Bold,
                            color = com.qinmu.eyecare.ui.theme.SpotifyTextPrimary,
                            fontSize = 18.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = com.qinmu.eyecare.ui.theme.SpotifyDarkBase
                )
            )
        },
        containerColor = com.qinmu.eyecare.ui.theme.SpotifyDarkBase
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 1. 提醒模式选择区
            Text(
                text = "护眼提醒模式选择",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = com.qinmu.eyecare.ui.theme.SpotifyGreen,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = com.qinmu.eyecare.ui.theme.SpotifyDarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    RemindMode.values().forEach { mode ->
                        val isSelected = prefs.remindMode == mode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setRemindMode(mode) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.setRemindMode(mode) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = com.qinmu.eyecare.ui.theme.SpotifyGreen,
                                    unselectedColor = com.qinmu.eyecare.ui.theme.SpotifyTextSecondary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = mode.displayName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (isSelected) com.qinmu.eyecare.ui.theme.SpotifyTextPrimary else com.qinmu.eyecare.ui.theme.SpotifyTextSecondary
                                )
                                Text(
                                    text = mode.description,
                                    fontSize = 12.sp,
                                    color = com.qinmu.eyecare.ui.theme.SpotifyTextMuted
                                )
                            }
                        }
                        if (mode != RemindMode.values().last()) {
                            Divider(color = com.qinmu.eyecare.ui.theme.SpotifyBorder)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(com.qinmu.eyecare.ui.theme.SpotifyDarkControl, shape = RoundedCornerShape(500.dp))
                            .padding(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = com.qinmu.eyecare.ui.theme.SpotifyOrange,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "提示：两种提醒模式均内嵌【跳过本次沁目】与【完成休息】按键",
                            fontSize = 11.sp,
                            color = com.qinmu.eyecare.ui.theme.SpotifyTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 1.5 智能会议与游戏免打扰配置
            Text(
                text = "💼 会议与 🎮 游戏智能免打扰",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = com.qinmu.eyecare.ui.theme.SpotifyGreen,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = com.qinmu.eyecare.ui.theme.SpotifyDarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "手动模式锁定",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = com.qinmu.eyecare.ui.theme.SpotifyTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SpecialMode.values().forEach { mode ->
                            val isSelected = prefs.manualSpecialMode == mode
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setManualSpecialMode(mode) },
                                shape = RoundedCornerShape(500.dp),
                                label = { Text("${mode.iconRes} ${mode.displayName}") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = com.qinmu.eyecare.ui.theme.SpotifyGreen,
                                    selectedLabelColor = Color.Black,
                                    containerColor = com.qinmu.eyecare.ui.theme.SpotifyDarkControl,
                                    labelColor = com.qinmu.eyecare.ui.theme.SpotifyTextSecondary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = com.qinmu.eyecare.ui.theme.SpotifyBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "💼 自动识别会议应用 (免打扰)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = com.qinmu.eyecare.ui.theme.SpotifyTextPrimary
                            )
                            Text(
                                text = "自动检测腾讯会议、钉钉、Zoom、Teams等前台运行",
                                fontSize = 11.sp,
                                color = com.qinmu.eyecare.ui.theme.SpotifyTextMuted
                            )
                        }
                        Switch(
                            checked = prefs.isAutoMeetingModeEnabled,
                            onCheckedChange = { viewModel.setAutoMeetingModeEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = com.qinmu.eyecare.ui.theme.SpotifyGreen,
                                uncheckedThumbColor = com.qinmu.eyecare.ui.theme.SpotifyTextSecondary,
                                uncheckedTrackColor = com.qinmu.eyecare.ui.theme.SpotifyDarkControl
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🎮 自动识别游戏应用 (免打扰)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = com.qinmu.eyecare.ui.theme.SpotifyTextPrimary
                            )
                            Text(
                                text = "前台运行全屏游戏时，绝对挂起全屏遮罩与响铃",
                                fontSize = 11.sp,
                                color = com.qinmu.eyecare.ui.theme.SpotifyTextMuted
                            )
                        }
                        Switch(
                            checked = prefs.isAutoGameModeEnabled,
                            onCheckedChange = { viewModel.setAutoGameModeEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = com.qinmu.eyecare.ui.theme.SpotifyGreen,
                                uncheckedThumbColor = com.qinmu.eyecare.ui.theme.SpotifyTextSecondary,
                                uncheckedTrackColor = com.qinmu.eyecare.ui.theme.SpotifyDarkControl
                            )
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
                color = com.qinmu.eyecare.ui.theme.SpotifyGreen,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = com.qinmu.eyecare.ui.theme.SpotifyDarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    RestSoundEffect.values().forEach { sound ->
                        val isSelected = prefs.soundEffect == sound
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
                                selected = isSelected,
                                onClick = {
                                    if (sound == RestSoundEffect.CUSTOM_AUDIO) {
                                        audioPickerLauncher.launch("audio/*")
                                    } else {
                                        viewModel.setSoundEffect(sound)
                                        SoundManager.playSound(context, sound)
                                    }
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = com.qinmu.eyecare.ui.theme.SpotifyGreen,
                                    unselectedColor = com.qinmu.eyecare.ui.theme.SpotifyTextSecondary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = sound.displayName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (isSelected) com.qinmu.eyecare.ui.theme.SpotifyTextPrimary else com.qinmu.eyecare.ui.theme.SpotifyTextSecondary,
                                modifier = Modifier.weight(1f)
                            )
                            if (sound == RestSoundEffect.CUSTOM_AUDIO) {
                                OutlinedButton(
                                    onClick = { audioPickerLauncher.launch("audio/*") },
                                    shape = RoundedCornerShape(500.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = com.qinmu.eyecare.ui.theme.SpotifyGreen
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, com.qinmu.eyecare.ui.theme.SpotifyGreen),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("选择MP3截取", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else if (sound != RestSoundEffect.MUTE) {
                                TextButton(
                                    onClick = { SoundManager.playSound(context, sound) },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = com.qinmu.eyecare.ui.theme.SpotifyGreen
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("试听", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        if (sound != RestSoundEffect.values().last()) {
                            Divider(color = com.qinmu.eyecare.ui.theme.SpotifyBorder)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            var customDaQinRestText by remember(prefs.daQinRestSeconds) {
                mutableStateOf(prefs.daQinRestSeconds.toString())
            }

            // 3. 时间周期与小沁大沁交替配置
            Text(
                text = "小沁/大沁提醒周期与规则配置",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = com.qinmu.eyecare.ui.theme.SpotifyGreen,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = com.qinmu.eyecare.ui.theme.SpotifyDarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // 小大沁交替模式总开关
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "开启【小沁 + 大沁】智能交替守护",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = com.qinmu.eyecare.ui.theme.SpotifyTextPrimary
                            )
                            Text(
                                text = "连续完成数次【小沁】微休息后，下一次自动升级为【大沁】深度放松",
                                fontSize = 11.sp,
                                color = com.qinmu.eyecare.ui.theme.SpotifyTextMuted
                            )
                        }
                        Switch(
                            checked = prefs.isDualCycleEnabled,
                            onCheckedChange = { viewModel.setDualCycleEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = com.qinmu.eyecare.ui.theme.SpotifyGreen,
                                uncheckedThumbColor = com.qinmu.eyecare.ui.theme.SpotifyTextSecondary,
                                uncheckedTrackColor = com.qinmu.eyecare.ui.theme.SpotifyDarkControl
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = com.qinmu.eyecare.ui.theme.SpotifyBorder)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "1. 🌿 小沁（微休息）配置",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = com.qinmu.eyecare.ui.theme.SpotifyGreen
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "连屏提醒间隔", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = com.qinmu.eyecare.ui.theme.SpotifyTextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(15, 20, 30, 45).forEach { minutes ->
                            val isSelected = prefs.remindIntervalMinutes == minutes
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    viewModel.setRemindInterval(minutes)
                                    customIntervalText = minutes.toString()
                                },
                                shape = RoundedCornerShape(500.dp),
                                label = { Text("${minutes}分钟") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = com.qinmu.eyecare.ui.theme.SpotifyGreen,
                                    selectedLabelColor = Color.Black,
                                    containerColor = com.qinmu.eyecare.ui.theme.SpotifyDarkControl,
                                    labelColor = com.qinmu.eyecare.ui.theme.SpotifyTextSecondary
                                )
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
                        label = { Text("自定义小沁间隔 (分钟)", color = com.qinmu.eyecare.ui.theme.SpotifyTextSecondary) },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = com.qinmu.eyecare.ui.theme.SpotifyGreen) },
                        trailingIcon = { Text("分钟", fontSize = 12.sp, color = com.qinmu.eyecare.ui.theme.SpotifyTextMuted, modifier = Modifier.padding(end = 12.dp)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = com.qinmu.eyecare.ui.theme.SpotifyDarkControl,
                            unfocusedContainerColor = com.qinmu.eyecare.ui.theme.SpotifyDarkControl,
                            focusedBorderColor = com.qinmu.eyecare.ui.theme.SpotifyGreen,
                            unfocusedBorderColor = com.qinmu.eyecare.ui.theme.SpotifyBorder,
                            focusedTextColor = com.qinmu.eyecare.ui.theme.SpotifyTextPrimary,
                            unfocusedTextColor = com.qinmu.eyecare.ui.theme.SpotifyTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(text = "小沁休息时长 (远眺)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = com.qinmu.eyecare.ui.theme.SpotifyTextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(15, 20, 30, 45).forEach { seconds ->
                            val isSelected = prefs.restDurationSeconds == seconds
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    viewModel.setRestDuration(seconds)
                                    customRestText = seconds.toString()
                                },
                                shape = RoundedCornerShape(500.dp),
                                label = { Text("${seconds}秒") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = com.qinmu.eyecare.ui.theme.SpotifyGreen,
                                    selectedLabelColor = Color.Black,
                                    containerColor = com.qinmu.eyecare.ui.theme.SpotifyDarkControl,
                                    labelColor = com.qinmu.eyecare.ui.theme.SpotifyTextSecondary
                                )
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
                        label = { Text("自定义小沁时长 (秒)", color = com.qinmu.eyecare.ui.theme.SpotifyTextSecondary) },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = com.qinmu.eyecare.ui.theme.SpotifyGreen) },
                        trailingIcon = { Text("秒", fontSize = 12.sp, color = com.qinmu.eyecare.ui.theme.SpotifyTextMuted, modifier = Modifier.padding(end = 12.dp)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = com.qinmu.eyecare.ui.theme.SpotifyDarkControl,
                            unfocusedContainerColor = com.qinmu.eyecare.ui.theme.SpotifyDarkControl,
                            focusedBorderColor = com.qinmu.eyecare.ui.theme.SpotifyGreen,
                            unfocusedBorderColor = com.qinmu.eyecare.ui.theme.SpotifyBorder,
                            focusedTextColor = com.qinmu.eyecare.ui.theme.SpotifyTextPrimary,
                            unfocusedTextColor = com.qinmu.eyecare.ui.theme.SpotifyTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (prefs.isDualCycleEnabled) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Divider(color = com.qinmu.eyecare.ui.theme.SpotifyBorder)
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "2. 🧘 大沁（深度放松）规则配置",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = com.qinmu.eyecare.ui.theme.SpotifyBlue
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(text = "触发频次 (完成几项小沁后触发大沁)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = com.qinmu.eyecare.ui.theme.SpotifyTextSecondary)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf(2, 3, 4, 5).forEach { count ->
                                val isSelected = prefs.daQinCycleCount == count
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.setDaQinCycleCount(count) },
                                    shape = RoundedCornerShape(500.dp),
                                    label = { Text("每 ${count} 次小沁") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = com.qinmu.eyecare.ui.theme.SpotifyBlue,
                                        selectedLabelColor = Color.Black,
                                        containerColor = com.qinmu.eyecare.ui.theme.SpotifyDarkControl,
                                        labelColor = com.qinmu.eyecare.ui.theme.SpotifyTextSecondary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(text = "大沁休息时长 (深度全身放松)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = com.qinmu.eyecare.ui.theme.SpotifyTextSecondary)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf(120, 180, 300, 600).forEach { seconds ->
                                val min = seconds / 60
                                val isSelected = prefs.daQinRestSeconds == seconds
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        viewModel.setDaQinRestSeconds(seconds)
                                        customDaQinRestText = seconds.toString()
                                    },
                                    shape = RoundedCornerShape(500.dp),
                                    label = { Text("${min}分钟") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = com.qinmu.eyecare.ui.theme.SpotifyBlue,
                                        selectedLabelColor = Color.Black,
                                        containerColor = com.qinmu.eyecare.ui.theme.SpotifyDarkControl,
                                        labelColor = com.qinmu.eyecare.ui.theme.SpotifyTextSecondary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = customDaQinRestText,
                            onValueChange = { newValue ->
                                val filtered = newValue.filter { it.isDigit() }
                                customDaQinRestText = filtered
                                filtered.toIntOrNull()?.let { sec ->
                                    viewModel.setDaQinRestSeconds(sec)
                                }
                            },
                            label = { Text("自定义大沁时长 (秒)", color = com.qinmu.eyecare.ui.theme.SpotifyTextSecondary) },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = com.qinmu.eyecare.ui.theme.SpotifyBlue) },
                            trailingIcon = { Text("秒", fontSize = 12.sp, color = com.qinmu.eyecare.ui.theme.SpotifyTextMuted, modifier = Modifier.padding(end = 12.dp)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = com.qinmu.eyecare.ui.theme.SpotifyDarkControl,
                                unfocusedContainerColor = com.qinmu.eyecare.ui.theme.SpotifyDarkControl,
                                focusedBorderColor = com.qinmu.eyecare.ui.theme.SpotifyBlue,
                                unfocusedBorderColor = com.qinmu.eyecare.ui.theme.SpotifyBorder,
                                focusedTextColor = com.qinmu.eyecare.ui.theme.SpotifyTextPrimary,
                                unfocusedTextColor = com.qinmu.eyecare.ui.theme.SpotifyTextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 正确视距与用眼指南卡片
            Text(
                text = "📐 电子设备正确视距与护眼常识",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = com.qinmu.eyecare.ui.theme.SpotifyGreen,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = com.qinmu.eyecare.ui.theme.SpotifyDarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "📱 手机 / 平板视距：",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = com.qinmu.eyecare.ui.theme.SpotifyGreen
                        )
                        Text(
                            text = "33 ~ 40 cm (约一臂折半)",
                            fontSize = 13.sp,
                            color = com.qinmu.eyecare.ui.theme.SpotifyTextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "💻 电脑显示屏视距：",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = com.qinmu.eyecare.ui.theme.SpotifyGreen
                        )
                        Text(
                            text = "50 ~ 70 cm (约手臂伸直长度)",
                            fontSize = 13.sp,
                            color = com.qinmu.eyecare.ui.theme.SpotifyTextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "👀 视线倾角建议：",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = com.qinmu.eyecare.ui.theme.SpotifyGreen
                        )
                        Text(
                            text = "屏幕中心向下倾斜 10° - 15°",
                            fontSize = 13.sp,
                            color = com.qinmu.eyecare.ui.theme.SpotifyTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. 版本与更新
            Text(
                text = "关于与应用更新",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = com.qinmu.eyecare.ui.theme.SpotifyGreen,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = com.qinmu.eyecare.ui.theme.SpotifyDarkSurface)
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
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = com.qinmu.eyecare.ui.theme.SpotifyGreen
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "检查新版本", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = com.qinmu.eyecare.ui.theme.SpotifyTextPrimary)
                            Text(text = "当前版本 v1.0.0", fontSize = 11.sp, color = com.qinmu.eyecare.ui.theme.SpotifyTextMuted)
                        }
                        if (isCheckingUpdate) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = com.qinmu.eyecare.ui.theme.SpotifyGreen)
                        } else {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = com.qinmu.eyecare.ui.theme.SpotifyTextMuted
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
                color = com.qinmu.eyecare.ui.theme.SpotifyGreen,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = com.qinmu.eyecare.ui.theme.SpotifyDarkSurface)
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

                    Divider(color = com.qinmu.eyecare.ui.theme.SpotifyBorder)

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
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            tint = if (isGranted) com.qinmu.eyecare.ui.theme.SpotifyGreen else com.qinmu.eyecare.ui.theme.SpotifyOrange
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = com.qinmu.eyecare.ui.theme.SpotifyTextPrimary)
            Text(text = desc, fontSize = 11.sp, color = com.qinmu.eyecare.ui.theme.SpotifyTextMuted)
        }
        Surface(
            color = if (isGranted) com.qinmu.eyecare.ui.theme.SpotifyGreen.copy(alpha = 0.15f) else com.qinmu.eyecare.ui.theme.SpotifyOrange.copy(alpha = 0.15f),
            shape = RoundedCornerShape(500.dp)
        ) {
            Text(
                text = if (isGranted) "已授权" else "去授权",
                fontSize = 11.sp,
                color = if (isGranted) com.qinmu.eyecare.ui.theme.SpotifyGreen else com.qinmu.eyecare.ui.theme.SpotifyOrange,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = com.qinmu.eyecare.ui.theme.SpotifyTextMuted
        )
    }
}
