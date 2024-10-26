package com.example.myhome02

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AboutActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val aboutText = findViewById<TextView>(R.id.about_text)
        aboutText.text = """
            Aural Log is an Android app designed to revolutionize hearing testing by providing accurate pure tone and speech tests via smartphones using Bluetooth or wired headphones. 
            The app leverages to analyze test results in real-time, categorizing hearing loss levels (normal, mild, severe) and generating detailed reports instantly. 
            With a user-friendly interface, Aural Log ensures a seamless experience, enabling users to manage and track their test history over time. 
            By offering a portable, reliable, and cost-effective solution for hearing screening, the app promotes early detection and proactive management of hearing health, making it accessible to a wider audience. 
            It also features background noise detection to ensure accurate testing in various environments. 
            The app aims to democratize hearing healthcare, making regular screenings simple, accessible, and efficient for everyone.
        """.trimIndent()
    }
}
