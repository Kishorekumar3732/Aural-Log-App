package com.example.myhome02

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class InstructionsActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instructions)

        // Find the TextView in your layout
        val instructionsTextView = findViewById<TextView>(R.id.instructions_text)

        // Set the instructions text
        instructionsTextView.text = """
            Select a Quiet Environment by choosing a room with minimal background noise.
            
            Use the Right Equipment such as wired or Bluetooth headphones (avoid built-in speakers or earbuds).
            
            If using Bluetooth, ensure proper Bluetooth Headphone Calibration with fully charged headphones to reduce latency.
            
            Adjust the Volume Levels to the recommended setting and avoid changing it during the test.
            
            Focus on the Test by sitting comfortably and avoiding distractions while listening carefully for tones or speech.
            
            The test will automatically Pause for Background Noise if detected, and will resume when the environment is quieter.
            
            For best results, ensure clean ears by Removing Earwax or obstructions (like earrings).
            
            Finally, note that the Medical Disclaimer reminds users that this test is not a substitute for professional audiological evaluations.
        """.trimIndent()
    }
}
