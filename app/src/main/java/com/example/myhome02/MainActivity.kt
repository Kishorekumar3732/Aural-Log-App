package com.example.myhome02

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var toolbarTitle: TextView
    private val typewriterDelay: Long = 200 // Increased delay between each character

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set up the toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Remove the default title from the toolbar
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Initialize toolbar title TextView
        toolbarTitle = findViewById(R.id.toolbar_title)
        toolbarTitle.typeface = ResourcesCompat.getFont(this, R.font.adventurerdemoregular)

        // Start the typewriter effect for the title
        typeWriterAnimation("Aural Log", toolbarTitle)

        // User profile info
        val profileName = findViewById<TextView>(R.id.profile_name)
        val profileEmail = findViewById<TextView>(R.id.profile_email)

        val sharedPreferences = getSharedPreferences("ProfileData", MODE_PRIVATE)
        profileName.text = sharedPreferences.getString("name", "User name")
        profileEmail.text = sharedPreferences.getString("email", "user@example.com")

        // Click listener for the profile card to navigate to ProfileActivity
        findViewById<CardView>(R.id.profile_card).setOnClickListener {
            // Start ProfileActivity
            startActivity(Intent(this, ProfileActivity::class.java))

        }

        // Test button click listener
        findViewById<CardView>(R.id.test_button).setOnClickListener {
            startActivity(Intent(this, TestActivity::class.java))
        }
    }

    private fun typeWriterAnimation(text: String, textView: TextView) {
        textView.text = "" // Clear the text view
        val handler = Handler(Looper.getMainLooper())

        // Center the text in the TextView
        textView.textAlignment = View.TEXT_ALIGNMENT_CENTER

        val runnable = object : Runnable {
            var index = 0

            override fun run() {
                if (index < text.length) {
                    textView.text = text.take(index + 1) // Show characters from the start to the current index
                    index++
                    handler.postDelayed(this, typewriterDelay)
                }
            }
        }
        handler.post(runnable)
    }

    // Inflate the menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    // Handle menu item selections
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.history -> {
                startActivity(Intent(this, HistoryActivity::class.java))
                true
            }
            R.id.instructions -> {
                startActivity(Intent(this, InstructionsActivity::class.java))
                true
            }
            R.id.settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.feedback -> {
                // Create an email intent
                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:") // Only email apps should handle this
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("aurallog2@gmail.com")) // Set email address
                    putExtra(Intent.EXTRA_SUBJECT, "Feedback for Aural Log")
                    putExtra(Intent.EXTRA_TEXT, "Please provide your feedback here...")
                }

                // Verify that there's an email app to handle the intent
                if (emailIntent.resolveActivity(packageManager) != null) {
                    startActivity(emailIntent)
                } else {
                    // Optionally handle the case where no email app is available
                    Toast.makeText(this, "No email client found", Toast.LENGTH_SHORT).show()
                }
                true
            }
            R.id.about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                true
            }
            R.id.logout -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, SignInActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
