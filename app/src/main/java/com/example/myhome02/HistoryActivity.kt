package com.example.myhome02

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat

class HistoryActivity : AppCompatActivity() {
    private lateinit var toolbarTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        // Set up the toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Remove the default title from the toolbar
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Initialize toolbar title TextView
        toolbarTitle = findViewById(R.id.toolbar_title)
        toolbarTitle.typeface = ResourcesCompat.getFont(this, R.font.adventurerdemoregular)

        // Optional: Reuse the typewriter effect from MainActivity if needed
    }
}
