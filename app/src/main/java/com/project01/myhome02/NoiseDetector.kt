package com.project01.myhome02

import android.content.Context
import android.media.MediaRecorder
import android.util.Log

class NoiseDetector(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null

    // Starts recording audio and returns the noise level in decibels
    fun startRecording(): Float {
        return try {
            if (mediaRecorder == null) {
                mediaRecorder = MediaRecorder().apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                    setOutputFile("/dev/null") // You can change this to a temporary file if needed
                    prepare()
                    start()
                }
            }

            // Get the maximum amplitude and convert it to decibels
            val amplitude = mediaRecorder?.maxAmplitude?.toFloat() ?: 0f
            calculateDecibel(amplitude)
        } catch (e: Exception) {
            Log.e("NoiseDetector", "Error starting recording: ${e.message}")
            Float.NaN // Return NaN in case of an error
        }
    }

    // Stops the recording safely
    fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                reset() // Resets the MediaRecorder to its uninitialized state
                release() // Releases the resources associated with MediaRecorder
            }
            mediaRecorder = null
        } catch (e: Exception) {
            Log.e("NoiseDetector", "Error stopping recording: ${e.message}")
        }
    }

    // Releases the MediaRecorder resources when the object is no longer needed
    fun release() {
        mediaRecorder?.release()
        mediaRecorder = null
    }

    // Converts amplitude to decibels (dB)
    private fun calculateDecibel(amplitude: Float): Float {
        return if (amplitude > 0) {
            (20 * Math.log10(amplitude.toDouble())).toFloat() // Convert amplitude to decibels
        } else {
            0f // Return 0 dB if the amplitude is zero
        }
    }
}
