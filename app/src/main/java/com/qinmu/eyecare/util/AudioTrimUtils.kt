package com.qinmu.eyecare.util

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.MediaMetadataRetriever
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer

object AudioTrimUtils {

    /**
     * 获取音频文件的总时长（秒）
     */
    fun getAudioDurationSeconds(context: Context, uri: Uri): Int {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            val timeMsStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val timeMs = timeMsStr?.toLongOrNull() ?: 0L
            (timeMs / 1000L).toInt()
        } catch (e: Exception) {
            e.printStackTrace()
            0
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 截取音频 1~3 秒片段并保存到内部私有目录
     */
    suspend fun trimAudio(
        context: Context,
        inputUri: Uri,
        startMs: Long,
        durationSeconds: Int
    ): File? = withContext(Dispatchers.IO) {
        val outputFile = File(context.filesDir, "custom_eye_sound.aac")
        if (outputFile.exists()) {
            outputFile.delete()
        }

        val endMs = startMs + (durationSeconds * 1000L)
        val startUs = startMs * 1000L
        val endUs = endMs * 1000L

        var extractor: MediaExtractor? = null
        var muxer: MediaMuxer? = null

        try {
            extractor = MediaExtractor().apply {
                setDataSource(context, inputUri, null)
            }

            var audioTrackIndex = -1
            var format: MediaFormat? = null

            for (i in 0 until extractor.trackCount) {
                val trackFormat = extractor.getTrackFormat(i)
                val mime = trackFormat.getString(MediaFormat.KEY_MIME)
                if (mime?.startsWith("audio/") == true) {
                    audioTrackIndex = i
                    format = trackFormat
                    break
                }
            }

            if (audioTrackIndex < 0 || format == null) {
                return@withContext saveRawAudioFallback(context, inputUri, outputFile)
            }

            extractor.selectTrack(audioTrackIndex)
            extractor.seekTo(startUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC)

            muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val muxerTrackIndex = muxer.addTrack(format)
            muxer.start()

            val maxBufferSize = if (format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
            } else {
                1024 * 1024
            }

            val buffer = ByteBuffer.allocate(maxBufferSize)
            val bufferInfo = MediaCodec.BufferInfo()

            while (true) {
                bufferInfo.offset = 0
                bufferInfo.size = extractor.readSampleData(buffer, 0)
                if (bufferInfo.size < 0) {
                    break
                }

                val sampleTimeUs = extractor.sampleTime
                if (sampleTimeUs > endUs) {
                    break
                }

                bufferInfo.presentationTimeUs = (sampleTimeUs - startUs).coerceAtLeast(0L)
                bufferInfo.flags = extractor.sampleFlags

                muxer.writeSampleData(muxerTrackIndex, buffer, bufferInfo)
                extractor.advance()
            }

            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            saveRawAudioFallback(context, inputUri, outputFile)
        } finally {
            try {
                muxer?.stop()
                muxer?.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                extractor?.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun saveRawAudioFallback(context: Context, inputUri: Uri, outputFile: File): File? {
        return try {
            context.contentResolver.openInputStream(inputUri)?.use { input ->
                outputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
