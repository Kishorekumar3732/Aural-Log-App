package com.example.myhome02

import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.Window
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.example.myhome02.NoiseDetector
import com.example.myhome02.R

class NoiseDetectorPopup(private val context: Context) {

    private val noiseDetector = NoiseDetector(context)
    private lateinit var dialog: Dialog
    private lateinit var noiseMessage: TextView
    private lateinit var currentDbTextView: TextView
    private lateinit var averageDbTextView: TextView
    private lateinit var noiseProgressBar: ProgressBar
    private lateinit var timerProgressBar: ProgressBar
    private lateinit var closeButton: Button
    private val handler = Handler(Looper.getMainLooper())
    private val noiseReadings = mutableListOf<Float>()

    private var timerProgress = 0
    private val noiseCheckDuration = 8000L // 8 seconds
    private val timerInterval = 80L // Interval for the circular progress update
    private var isRecording = false // Flag to control recording

    // Listener for notifying the results back to the activity
    private var listener: OnNoiseDetectedListener? = null

    interface OnNoiseDetectedListener {
        fun onNoiseLevelAccepted()
        fun onNoiseLevelTooHigh(averageDecibel: Float)
    }

    fun setOnNoiseDetectedListener(listener: OnNoiseDetectedListener) {
        this.listener = listener
    }

    fun showPopup() {
        dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = LayoutInflater.from(context).inflate(R.layout.activity_popup_noise_detector, null)
        dialog.setContentView(view)

        noiseMessage = view.findViewById(R.id.noise_message)
        currentDbTextView = view.findViewById(R.id.current_db_text_view)
        averageDbTextView = view.findViewById(R.id.average_db_text_view)
        noiseProgressBar = view.findViewById(R.id.noise_progress_bar)
        timerProgressBar = view.findViewById(R.id.timer_progress_bar)
        closeButton = view.findViewById(R.id.close_button)

        dialog.setCancelable(false)
        dialog.show()

        closeButton.setOnClickListener {
            dialog.dismiss() // Close the dialog without evaluating noise level
        }

        // Start the noise detection loop and timer
        startNoiseCheck()
        startTimerProgress()
    }

    private fun startNoiseCheck() {
        noiseReadings.clear()
        noiseMessage.text = "Checking noise levels..."
        noiseProgressBar.progress = 0 // Reset segmented progress bar
        currentDbTextView.text = "Current dB: 0"
        averageDbTextView.text = "Average dB: 0"
        isRecording = true // Start recording

        // Start recording for 8 seconds
        noiseDetector.startRecording()

        handler.post(checkNoiseRunnable)

        // Stop recording after 8 seconds
        handler.postDelayed({
            isRecording = false
            noiseDetector.stopRecording() // Stop recording after 8 seconds
            evaluateNoiseLevel() // Evaluate noise level after recording
        }, noiseCheckDuration)
    }

    private fun startTimerProgress() {
        timerProgress = 0
        timerProgressBar.max = (noiseCheckDuration / timerInterval).toInt() // Total ticks for 8 seconds

        handler.post(object : Runnable {
            override fun run() {
                if (timerProgress <= timerProgressBar.max) {
                    timerProgressBar.progress = timerProgress
                    timerProgress++
                    handler.postDelayed(this, timerInterval)
                }
            }
        })
    }

    private val checkNoiseRunnable = object : Runnable {
        override fun run() {
            if (isRecording) {
                val decibel = noiseDetector.startRecording() // Get the current decibel level

                // Update the noise level segmented progress bar in real-time
                noiseProgressBar.progress = (decibel.coerceAtMost(100f)).toInt() // Max noise level scaled to 100
                currentDbTextView.text = "Current dB: ${decibel.toInt()}"

                // Add valid readings to the list
                if (!decibel.isNaN()) {
                    noiseReadings.add(decibel)
                }

                // Schedule the next noise check in real-time
                handler.postDelayed(this, 500L) // Update every 0.5 seconds for smoother progress updates
            }
        }
    }

    private fun evaluateNoiseLevel() {
        // Calculate the average decibel level
        val averageDecibel = if (noiseReadings.isNotEmpty()) {
            noiseReadings.sum() / noiseReadings.size
        } else {
            0f
        }

        // Update the average dB TextView
        averageDbTextView.text = "Average dB: ${averageDecibel.toInt()}"

        // Check if the average noise level is above the threshold
        if (averageDecibel > 100f) { // Threshold set to 100 dB
            noiseMessage.text = "Average noise too high (${averageDecibel}dB). Please move to a quieter place."
            listener?.onNoiseLevelTooHigh(averageDecibel)

            // Restart the noise check until noise level is acceptable
            restartNoiseCheck()
        } else {
            noiseMessage.text = "Noise level acceptable (${averageDecibel}dB). Proceeding to Pure Tone Test..."
            listener?.onNoiseLevelAccepted()
            dialog.dismiss() // Close the dialog when noise level is accepted
        }

        // Enable the close button after evaluation
        closeButton.isEnabled = true
    }

    private fun restartNoiseCheck() {
        // Restart the noise check after a brief delay
        handler.postDelayed({
            noiseReadings.clear() // Clear previous readings
            startNoiseCheck() // Start a new noise check
        }, 2000) // Wait for 2 seconds before restarting
    }

    fun release() {
        noiseDetector.release()
        handler.removeCallbacksAndMessages(null) // Remove all pending callbacks
    }
}
