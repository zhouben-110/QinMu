package com.qinmu.eyecare.util

import android.content.Context
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import com.qinmu.eyecare.data.model.RestSoundEffect
import java.io.File

object SoundManager {

    private var currentRingtone: Ringtone? = null
    private var mediaPlayer: MediaPlayer? = null

    /**
     * 播放所选提示音效
     */
    fun playSound(context: Context, soundEffect: RestSoundEffect) {
        stopSound()

        if (soundEffect == RestSoundEffect.CUSTOM_AUDIO) {
            val customFile = File(context.filesDir, "custom_eye_sound.aac")
            if (customFile.exists()) {
                try {
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(context, Uri.fromFile(customFile))
                        setOnCompletionListener { mp ->
                            mp.release()
                            if (mediaPlayer == mp) mediaPlayer = null
                        }
                        prepare()
                        start()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return
        }

        val type = soundEffect.ringtoneType ?: return

        try {
            val uri = RingtoneManager.getDefaultUri(type)
            val ringtone = RingtoneManager.getRingtone(context, uri)
            currentRingtone = ringtone
            ringtone?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 试听临时截取的音频或者 Uri
     */
    @Suppress("UNUSED_PARAMETER")
    fun previewAudioUri(context: Context, uri: Uri, startMs: Long, durationSeconds: Int) {
        stopSound()
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, uri)
                setOnCompletionListener { mp ->
                    mp.release()
                    if (mediaPlayer == mp) mediaPlayer = null
                }
                prepare()
                seekTo(startMs.toInt())
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 停止正在播放的音效
     */
    fun stopSound() {
        try {
            currentRingtone?.let {
                if (it.isPlaying) {
                    it.stop()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        currentRingtone = null

        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaPlayer = null
    }
}
