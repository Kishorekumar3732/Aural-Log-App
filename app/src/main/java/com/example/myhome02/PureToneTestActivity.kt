package com.example.myhome02

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.pow

class PureToneTestActivity : AppCompatActivity() {

    private lateinit var playTone: Button
    private lateinit var startExam: Button
    private lateinit var canHear: Button
    private lateinit var cannotHear: Button
    private lateinit var resultsTextView: TextView
    private lateinit var soundLevelTextView: TextView

    private val duration = 2 // seconds
    private val sampleRate = 44100 // Hz
    private val sampleNum = duration * sampleRate
    private val sample = DoubleArray(sampleNum)
    private val freqArray = intArrayOf(125, 250, 500, 1000, 2000, 4000, 8000) // Hz
    private var soundLevel: Int = 30 // dB
    private var idx = 0
    private var actualFreq = freqArray[idx]
    private var isRightEar = true
    private val rightEarList = IntArray(freqArray.size)
    private val leftEarList = IntArray(freqArray.size)
    private val generatedSnd = ByteArray(2 * sampleNum)
    private lateinit var am: AudioManager
    private var audioTrack: AudioTrack? = null
    private val handler = Handler(Looper.getMainLooper())
    private var fineTuneCounter = 0
    private var lastAction: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pure_tone_test)

        // Initialize UI elements
        playTone = findViewById(R.id.playButton)
        startExam = findViewById(R.id.startExamButton)
        canHear = findViewById(R.id.canHearButton)
        cannotHear = findViewById(R.id.cannotHearButton)
        resultsTextView = findViewById(R.id.resultsTextView)
        soundLevelTextView = findViewById(R.id.soundLevelTextView)

        am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0)

        // Set initial button states
        playTone.isEnabled = false
        canHear.isEnabled = false
        cannotHear.isEnabled = false

        // Set button click listeners
        startExam.setOnClickListener {
            handler.post {
                try {
                    runExam()
                } catch (e: Exception) {
                    showToast("Error")
                }
            }
        }

        canHear.setOnClickListener {
            handler.post {
                try {
                    handleCanHear()
                } catch (e: Exception) {
                    showToast("Error")
                }
            }
        }

        cannotHear.setOnClickListener {
            handler.post {
                try {
                    handleCannotHear()
                } catch (e: Exception) {
                    showToast("Error")
                }
            }
        }

        playTone.setOnClickListener {
            handler.post {
                try {
                    genTone()
                    playSound()
                } catch (e: Exception) {
                    showToast("Error")
                }
            }
        }
    }

    private fun runExam() {
        resetTest()
        startTestingSequence()
    }

    private fun resetTest() {
        soundLevel = 30
        idx = 0
        isRightEar = true
        actualFreq = freqArray[idx]
        rightEarList.fill(0)
        leftEarList.fill(0)
        fineTuneCounter = 0
        lastAction = null
        setButtonStates(start = true, canHear = false, cannotHear = false, play = false)
        updateSoundLevelTextView()
    }

    private fun startTestingSequence() {
        genTone()
        playSound()
        setButtonStates(start = false, canHear = true, cannotHear = true, play = true)
    }

    private fun handleCanHear() {
        fineTuneCounter = 0
        if (lastAction == "cannotHear") {
            startAdjustmentPhase(isInitialCanHear = true)
        } else {
            if (soundLevel > 10) {
                soundLevel -= 10
                updateSoundLevelTextView()
                genTone()
                playSound()
            } else {
                startAdjustmentPhase(isInitialCanHear = true)
            }
        }
        lastAction = "canHear"
    }

    private fun handleCannotHear() {
        fineTuneCounter = 0
        if (lastAction == "canHear") {
            startAdjustmentPhase(isInitialCanHear = false)
        } else {
            if (soundLevel < 100) {
                soundLevel += 10
                updateSoundLevelTextView()
                genTone()
                playSound()
            } else {
                startAdjustmentPhase(isInitialCanHear = false)
            }
        }
        lastAction = "cannotHear"
    }

    private fun startAdjustmentPhase(isInitialCanHear: Boolean) {
        soundLevel += if (isInitialCanHear) -5 else 5
        updateSoundLevelTextView()
        fineTuneHearing(isMarkedLevelHeard = isInitialCanHear)
    }

    private fun fineTuneHearing(isMarkedLevelHeard: Boolean) {
        fineTuneCounter = 0
        var consistentClicks = 0
        val maxConsistentClicks = 3 // Require 3 consistent responses to finalize

        // Enable buttons for fine-tuning phase
        setButtonStates(start = false, canHear = true, cannotHear = true, play = false)

        if (isMarkedLevelHeard) {
            canHear.setOnClickListener {
                if (consistentClicks < maxConsistentClicks) {
                    soundLevel = (soundLevel - 1).coerceAtLeast(0) // Decrease by 1 dB
                    updateSoundLevelTextView()
                    genTone()
                    playSound()
                    consistentClicks++
                } else {
                    finalizeThreshold()
                }
            }

            cannotHear.setOnClickListener {
                // If user switches from hearing to not hearing, reset the consistent count
                consistentClicks = 0
                soundLevel = (soundLevel + 1).coerceAtMost(100) // Increase by 1 dB
                updateSoundLevelTextView()
                genTone()
                playSound()
            }
        } else {
            cannotHear.setOnClickListener {
                if (consistentClicks < maxConsistentClicks) {
                    soundLevel = (soundLevel + 1).coerceAtMost(100) // Increase by 1 dB
                    updateSoundLevelTextView()
                    genTone()
                    playSound()
                    consistentClicks++
                } else {
                    finalizeThreshold()
                }
            }

            canHear.setOnClickListener {
                // If user switches from not hearing to hearing, reset the consistent count
                consistentClicks = 0
                soundLevel = (soundLevel - 1).coerceAtLeast(0) // Decrease by 1 dB
                updateSoundLevelTextView()
                genTone()
                playSound()
            }
        }
    }

    private fun startFineTune(userCanHear: Boolean) {
        if (userCanHear) {
            // Logic when the user initially hears the marked level
            canHear.setOnClickListener {
                if (fineTuneCounter < 4 && soundLevel > 0) {
                    soundLevel = (soundLevel - 1).coerceAtLeast(0) // Decrease by 1 dB
                    fineTuneCounter++
                    updateSoundLevelTextView()
                    genTone()
                    playSound()
                } else {
                    finalizeThreshold()
                }
            }

            cannotHear.setOnClickListener {
                // User switches to 'cannot hear', finalize the last heard sound level as the threshold
                finalizeThreshold()
            }

        } else {
            // Logic when the user initially cannot hear the marked level
            cannotHear.setOnClickListener {
                if (fineTuneCounter < 4 && soundLevel < 100) {
                    soundLevel = (soundLevel + 1).coerceAtMost(100) // Increase by 1 dB
                    fineTuneCounter++
                    updateSoundLevelTextView()
                    genTone()
                    playSound()
                } else {
                    finalizeThreshold()
                }
            }

            canHear.setOnClickListener {
                // User switches to 'can hear', finalize the first heard sound level as the threshold
                finalizeThreshold()
            }
        }
    }

    private fun finalizeThreshold() {
        updateResults() // Update the results with the determined threshold
        moveToNextTest() // Move to the next test or frequency
    }


    private fun updateResults() {
        if (isRightEar) {
            rightEarList[idx] = soundLevel
        } else {
            leftEarList[idx] = soundLevel
        }
        displayCurrentResults()
    }

    private fun displayCurrentResults() {
        val results = StringBuilder().apply {
            append("Right Ear Results:\n")
            freqArray.indices.forEach { i ->
                append("${freqArray[i]} Hz: ${rightEarList[i]} dB\n")
            }
            append("\nLeft Ear Results:\n")
            freqArray.indices.forEach { i ->
                append("${freqArray[i]} Hz: ${leftEarList[i]} dB\n")
            }
        }
        resultsTextView.text = results.toString()
    }

    private fun moveToNextTest() {
        if (isRightEar) {
            if (idx < freqArray.size - 1) {
                idx++
            } else {
                idx = 0
                isRightEar = false
            }
        } else {
            if (idx < freqArray.size - 1) {
                idx++
            } else {
                completeTest()
                return
            }
        }
        // Reset sound level and prepare for the next frequency/ear
        soundLevel = 30
        actualFreq = freqArray[idx]
        fineTuneCounter = 0
        lastAction = null

        updateSoundLevelTextView()
        setButtonStates(start = false, canHear = false, cannotHear = false, play = true)
        startTestingSequence()
    }

    private fun completeTest() {
        displayCurrentResults()
        setButtonStates(start = false, canHear = false, cannotHear = false, play = false)
        showToast("Test Completed")
    }

    // Update setButtonStates function to reset button listeners as well
    private fun setButtonStates(start: Boolean, canHear: Boolean, cannotHear: Boolean, play: Boolean) {
        startExam.isEnabled = start
        this.canHear.isEnabled = canHear
        this.cannotHear.isEnabled = cannotHear
        playTone.isEnabled = play

        // Reassign listeners if necessary based on current state
        if (canHear) {
            this.canHear.setOnClickListener {
                handleCanHear()
            }
        } else {
            this.canHear.setOnClickListener(null)
        }

        if (cannotHear) {
            this.cannotHear.setOnClickListener {
                handleCannotHear()
            }
        } else {
            this.cannotHear.setOnClickListener(null)
        }

        if (play) {
            playTone.setOnClickListener {
                genTone()
                playSound()
            }
        } else {
            playTone.setOnClickListener(null)
        }
    }

    private fun genTone() {
        for (i in sample.indices) {
            sample[i] = kotlin.math.sin(2 * Math.PI * i / (sampleRate / actualFreq))
        }
        var idx = 0
        val ramp = sampleNum / 20

        for (i in 0 until ramp) {
            val dVal = sample[i]
            val valInt = (dVal * (10.0.pow(soundLevel / 20.0) * i / ramp)).toInt()
            generatedSnd[idx++] = (valInt and 0x00ff).toByte()
            generatedSnd[idx++] = ((valInt and 0xff00) ushr 8).toByte()
        }

        for (i in ramp until sampleNum - ramp) {
            val dVal = sample[i]
            val valInt = (dVal * (10.0.pow(soundLevel / 20.0))).toInt()
            generatedSnd[idx++] = (valInt and 0x00ff).toByte()
            generatedSnd[idx++] = ((valInt and 0xff00) ushr 8).toByte()
        }

        for (i in sampleNum - ramp until sampleNum) {
            val dVal = sample[i]
            val valInt = (dVal * (10.0.pow(soundLevel / 20.0) * (sampleNum - i) / ramp)).toInt()
            generatedSnd[idx++] = (valInt and 0x00ff).toByte()
            generatedSnd[idx++] = ((valInt and 0xff00) ushr 8).toByte()
        }
    }

    private fun playSound() {
        audioTrack?.stop()
        audioTrack?.release()

        audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            generatedSnd.size,
            AudioTrack.MODE_STATIC
        )

        audioTrack?.write(generatedSnd, 0, generatedSnd.size)
        audioTrack?.play()
    }

    private fun updateSoundLevelTextView() {
        soundLevelTextView.text = "Current Sound Level: $soundLevel dB"
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}