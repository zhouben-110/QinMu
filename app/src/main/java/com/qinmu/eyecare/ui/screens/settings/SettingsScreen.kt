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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.qinmu.eyecare.ui.theme.*
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
    var customDaQinRestText by remember(prefs.daQinRestSeconds) {
        mutableStateOf(prefs.daQinRestSeconds.toString())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Title Header
        Text(
            text = "⚙️ 偏好与系统权限设置",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp,
            color = TextPrimaryDarkNavy
        )
        Text(
            text = "定制专属您的极简护眼习惯、声音与保活规则",
            fontSize = 13.sp,
            color = TextSecondaryBlue
        )

        Spacer(modifier = Modifier.height(16.dp))

        // =========================================================================
        // 1. 提醒模式选择区
        // =========================================================================
        Text(
            text = "护眼提醒模式选择",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = AccentRoyalBlue,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        NeumorphicCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 20.dp
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
                                selectedColor = AccentRoyalBlue,
                                unselectedColor = TextMutedSky
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = mode.displayName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (isSelected) TextPrimaryDarkNavy else TextSecondaryBlue
                            )
                            Text(
                                text = mode.description,
                                fontSize = 12.sp,
                                color = TextMutedSky
                            )
                        }
                    }
                    if (mode != RemindMode.values().last()) {
                        HorizontalDivider(color = SpotifyBorder)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NeumorphicCardElevated, shape = RoundedCornerShape(500.dp))
                        .padding(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = AccentWarmOrange,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "提示：两种提醒模式均内嵌【跳过本次沁目】与【完成休息】按键",
                        fontSize = 11.sp,
                        color = TextSecondaryBlue
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // =========================================================================
        // 2. 智能会议与游戏免打扰配置
        // =========================================================================
        Text(
            text = "💼 会议与 🎮 游戏智能免打扰",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = AccentRoyalBlue,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        NeumorphicCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 20.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "手动模式锁定",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimaryDarkNavy
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
                                selectedContainerColor = AccentRoyalBlue,
                                selectedLabelColor = Color.White,
                                containerColor = NeumorphicCardElevated,
                                labelColor = TextSecondaryBlue
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = SpotifyBorder)
                Spacer(modifier = Modifier.height(12.dp))

                // Auto Meeting Switch
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
                            color = TextPrimaryDarkNavy
                        )
                        Text(
                            text = "自动检测腾讯会议、钉钉、Zoom、Teams等前台运行",
                            fontSize = 11.sp,
                            color = TextMutedSky
                        )
                    }
                    Switch(
                        checked = prefs.isAutoMeetingModeEnabled,
                        onCheckedChange = { viewModel.setAutoMeetingModeEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AccentRoyalBlue,
                            uncheckedThumbColor = TextMutedSky,
                            uncheckedTrackColor = NeumorphicCardElevated
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Auto Game Switch
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
                            color = TextPrimaryDarkNavy
                        )
                        Text(
                            text = "前台运行全屏游戏时，绝对挂起全屏遮罩与响铃",
                            fontSize = 11.sp,
                            color = TextMutedSky
                        )
                    }
                    Switch(
                        checked = prefs.isAutoGameModeEnabled,
                        onCheckedChange = { viewModel.setAutoGameModeEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AccentRoyalBlue,
                            uncheckedThumbColor = TextMutedSky,
                            uncheckedTrackColor = NeumorphicCardElevated
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // =========================================================================
        // 3. 提醒提示音效 (含音效试听与自选 MP3 截取)
        // =========================================================================
        Text(
            text = "提醒提示音效",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = AccentRoyalBlue,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        NeumorphicCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 20.dp
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
                                selectedColor = AccentRoyalBlue,
                                unselectedColor = TextMutedSky
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = sound.displayName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (isSelected) TextPrimaryDarkNavy else TextSecondaryBlue,
                            modifier = Modifier.weight(1f)
                        )
                        if (sound == RestSoundEffect.CUSTOM_AUDIO) {
                            NeumorphicPillButton(
                                onClick = { audioPickerLauncher.launch("audio/*") },
                                containerColor = NeumorphicCardElevated
                            ) {
                                Icon(imageVector = Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(14.dp), tint = AccentRoyalBlue)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("选择MP3截取", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentRoyalBlue)
                            }
                        } else if (sound != RestSoundEffect.MUTE) {
                            TextButton(
                                onClick = { SoundManager.playSound(context, sound) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = AccentRoyalBlue
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("试听", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentRoyalBlue)
                            }
                        }
                    }
                    if (sound != RestSoundEffect.values().last()) {
                        HorizontalDivider(color = SpotifyBorder)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // =========================================================================
        // 4. 时间周期与小沁大沁交替配置 (含自定义文本框与双循环参数)
        // =========================================================================
        Text(
            text = "小沁/大沁提醒周期与规则配置",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = AccentRoyalBlue,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        NeumorphicCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 20.dp
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
                            color = TextPrimaryDarkNavy
                        )
                        Text(
                            text = "连续完成数次【小沁】微休息后，下一次自动升级为【大沁】深度放松",
                            fontSize = 11.sp,
                            color = TextMutedSky
                        )
                    }
                    Switch(
                        checked = prefs.isDualCycleEnabled,
                        onCheckedChange = { viewModel.setDualCycleEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AccentRoyalBlue,
                            uncheckedThumbColor = TextMutedSky,
                            uncheckedTrackColor = NeumorphicCardElevated
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = SpotifyBorder)
                Spacer(modifier = Modifier.height(16.dp))

                // 1. 🌿 小沁（微休息）配置
                Text(
                    text = "1. 🌿 小沁（微休息）配置",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = AccentRoyalBlue
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "连屏提醒间隔", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextSecondaryBlue)
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
                                selectedContainerColor = AccentRoyalBlue,
                                selectedLabelColor = Color.White,
                                containerColor = NeumorphicCardElevated,
                                labelColor = TextSecondaryBlue
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
                    label = { Text("自定义小沁间隔 (分钟)", color = TextSecondaryBlue) },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = AccentRoyalBlue) },
                    trailingIcon = { Text("分钟", fontSize = 12.sp, color = TextMutedSky, modifier = Modifier.padding(end = 12.dp)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = NeumorphicCardElevated,
                        unfocusedContainerColor = NeumorphicCardElevated,
                        focusedBorderColor = AccentRoyalBlue,
                        unfocusedBorderColor = SpotifyBorder,
                        focusedTextColor = TextPrimaryDarkNavy,
                        unfocusedTextColor = TextPrimaryDarkNavy
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

                Text(text = "小沁休息时长 (远眺)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextSecondaryBlue)
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
                                selectedContainerColor = AccentRoyalBlue,
                                selectedLabelColor = Color.White,
                                containerColor = NeumorphicCardElevated,
                                labelColor = TextSecondaryBlue
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
                    label = { Text("自定义小沁时长 (秒)", color = TextSecondaryBlue) },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = AccentRoyalBlue) },
                    trailingIcon = { Text("秒", fontSize = 12.sp, color = TextMutedSky, modifier = Modifier.padding(end = 12.dp)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = NeumorphicCardElevated,
                        unfocusedContainerColor = NeumorphicCardElevated,
                        focusedBorderColor = AccentRoyalBlue,
                        unfocusedBorderColor = SpotifyBorder,
                        focusedTextColor = TextPrimaryDarkNavy,
                        unfocusedTextColor = TextPrimaryDarkNavy
                    ),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier = Modifier.fillMaxWidth()
                )

                // 2. 🧘 大沁（深度放松）规则配置
                if (prefs.isDualCycleEnabled) {
                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = SpotifyBorder)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "2. 🧘 大沁（深度放松）规则配置",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = AccentSoftSky
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "触发频次 (完成几项小沁后触发大沁)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextSecondaryBlue)
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
                                    selectedContainerColor = AccentSoftSky,
                                    selectedLabelColor = TextPrimaryDarkNavy,
                                    containerColor = NeumorphicCardElevated,
                                    labelColor = TextSecondaryBlue
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "大沁休息时长 (深度全身放松)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextSecondaryBlue)
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
                                    selectedContainerColor = AccentSoftSky,
                                    selectedLabelColor = TextPrimaryDarkNavy,
                                    containerColor = NeumorphicCardElevated,
                                    labelColor = TextSecondaryBlue
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
                        label = { Text("自定义大沁时长 (秒)", color = TextSecondaryBlue) },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = AccentSoftSky) },
                        trailingIcon = { Text("秒", fontSize = 12.sp, color = TextMutedSky, modifier = Modifier.padding(end = 12.dp)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = NeumorphicCardElevated,
                            unfocusedContainerColor = NeumorphicCardElevated,
                            focusedBorderColor = AccentSoftSky,
                            unfocusedBorderColor = SpotifyBorder,
                            focusedTextColor = TextPrimaryDarkNavy,
                            unfocusedTextColor = TextPrimaryDarkNavy
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

        // =========================================================================
        // 5. 电子设备正确视距与护眼常识指南卡片
        // =========================================================================
        Text(
            text = "📐 电子设备正确视距与护眼常识",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = AccentRoyalBlue,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        NeumorphicCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 20.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "📱 手机 / 平板视距：",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = AccentRoyalBlue
                    )
                    Text(
                        text = "33 ~ 40 cm (约一臂折半)",
                        fontSize = 13.sp,
                        color = TextSecondaryBlue
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "💻 电脑显示屏视距：",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = AccentRoyalBlue
                    )
                    Text(
                        text = "50 ~ 70 cm (约手臂伸直长度)",
                        fontSize = 13.sp,
                        color = TextSecondaryBlue
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "👀 视线倾角建议：",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = AccentRoyalBlue
                    )
                    Text(
                        text = "屏幕中心向下倾斜 10° - 15°",
                        fontSize = 13.sp,
                        color = TextSecondaryBlue
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // =========================================================================
        // 6. 系统高级权限配置
        // =========================================================================
        Text(
            text = "🛡️ 系统高级权限配置",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = AccentRoyalBlue,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        NeumorphicCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 20.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                PermissionItemRow(
                    title = "悬浮窗 (Overlay) 权限",
                    desc = "全屏强效护眼遮罩与滤镜必须依赖此权限",
                    isGranted = hasOverlayPermission,
                    onClick = {
                        PermissionUtils.requestOverlayPermission(context)
                        hasOverlayPermission = PermissionUtils.hasOverlayPermission(context)
                    }
                )

                HorizontalDivider(color = SpotifyBorder)

                PermissionItemRow(
                    title = "应用使用情况 (Usage Stats) 权限",
                    desc = "自动识别腾讯会议/钉钉/Zoom及全屏游戏应用依赖此权限",
                    isGranted = hasUsagePermission,
                    onClick = {
                        PermissionUtils.requestUsageStatsPermission(context)
                        hasUsagePermission = PermissionUtils.hasUsageStatsPermission(context)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // =========================================================================
        // 7. 后台防杀与心跳巡检 (WorkManager Keep-Alive Switch)
        // =========================================================================
        Text(
            text = "后台防杀与心跳巡检",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = AccentRoyalBlue,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        NeumorphicCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 20.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "15分钟后台周期性保活巡检 (WorkManager)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimaryDarkNavy
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "开启后每15分钟触发一次后台静默检查，若前台护眼服务被系统意外杀死将尝试重新唤醒拉起服务",
                            fontSize = 11.sp,
                            color = TextMutedSky,
                            lineHeight = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = prefs.isKeepAliveEnabled,
                        onCheckedChange = { viewModel.setKeepAliveEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AccentRoyalBlue,
                            uncheckedThumbColor = TextMutedSky,
                            uncheckedTrackColor = NeumorphicCardElevated
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // =========================================================================
        // 8. 检查应用新版本
        // =========================================================================
        NeumorphicPillButton(
            onClick = {
                isCheckingUpdate = true
                UpdateManager.checkUpdate(
                    context = context,
                    jsonUrl = "https://raw.githubusercontent.com/qinmu/version/main/version.json"
                ) { updateInfo ->
                    isCheckingUpdate = false
                    if (updateInfo != null) {
                        updateInfoState = updateInfo
                    } else {
                        Toast.makeText(context, "当前已是最新版本 (v1.0.0)", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            containerColor = AccentRoyalBlue
        ) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isCheckingUpdate) "正在检查更新..." else "检查版本更新 (v1.0.0)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        updateInfoState?.let { updateInfo ->
            UpdateDialog(
                updateInfo = updateInfo,
                onConfirm = {
                    UpdateManager.downloadAndInstallApk(context, updateInfo.downloadUrl)
                    updateInfoState = null
                },
                onDismiss = { updateInfoState = null }
            )
        }

        selectedAudioUri?.let { uri ->
            AudioTrimDialog(
                audioUri = uri,
                onDismiss = { selectedAudioUri = null },
                onConfirmTrim = { startMs, durationSec ->
                    coroutineScope.launch {
                        val outputFile = AudioTrimUtils.trimAudio(context, uri, startMs, durationSec)
                        if (outputFile != null && outputFile.exists()) {
                            viewModel.setSoundEffect(RestSoundEffect.CUSTOM_AUDIO)
                            SoundManager.playSound(context, RestSoundEffect.CUSTOM_AUDIO)
                            Toast.makeText(context, "成功保存 ${durationSec}秒 专属护眼提示音！", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "音效截取失败，请重试", Toast.LENGTH_SHORT).show()
                        }
                        selectedAudioUri = null
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun PermissionItemRow(
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
            tint = if (isGranted) AccentMintGreen else AccentWarmOrange
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimaryDarkNavy)
            Text(text = desc, fontSize = 11.sp, color = TextMutedSky)
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(500.dp))
                .background(if (isGranted) AccentMintGreen.copy(alpha = 0.15f) else AccentWarmOrange.copy(alpha = 0.15f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (isGranted) "已授权" else "去授权",
                fontSize = 11.sp,
                color = if (isGranted) AccentMintGreen else AccentWarmOrange,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextMutedSky
        )
    }
}
