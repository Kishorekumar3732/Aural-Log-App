package com.example.myhome02


import com.example.myhome02.NoiseDetector
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import android.content.Intent
import android.widget.Button
import android.widget.TextView

class TestActivity : ComponentActivity() {

    private lateinit var testStatusTextView: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        testStatusTextView = findViewById(R.id.testStatusTextView)

        findViewById<Button>(R.id.pureToneTestButton).setOnClickListener {
            // Show the noise detector popup
            val noiseDetectorPopup = NoiseDetectorPopup(this)
            noiseDetectorPopup.setOnNoiseDetectedListener(object : NoiseDetectorPopup.OnNoiseDetectedListener {
                override fun onNoiseLevelAccepted() {
                    // Navigate to PureToneTestActivity if noise level is acceptable
                    startActivity(Intent(this@TestActivity, PureToneTestActivity::class.java))
                }

                override fun onNoiseLevelTooHigh(averageDecibel: Float) {
                    // Show message that noise level is too high
                    testStatusTextView.text = "Average noise too high ($averageDecibel dB). Please find a quieter place."
                }
            })
            noiseDetectorPopup.showPopup()
        }

        findViewById<Button>(R.id.speechTestButton).setOnClickListener {
            startActivity(Intent(this, SpeechTestActivity::class.java))
        }
    }
}
