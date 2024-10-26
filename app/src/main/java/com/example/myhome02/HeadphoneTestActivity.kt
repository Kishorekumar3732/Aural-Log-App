package com.example.myhome02

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class HeadphoneTestActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var statusTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_headphone_test)

        statusTextView = findViewById(R.id.status_text)

        if (isHeadphonesConnected()) {
            // Play the sounds
            playSoundInEar("left")
        } else {
            showHeadphoneNotConnectedPopup()
        }
    }

    private fun playSoundInEar(ear: String) {
        statusTextView.text = "Playing sound in $ear ear"

        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        when (ear) {
            "left" -> {
                mediaPlayer = MediaPlayer.create(this, R.raw.test_sound) // Use any sound file
                audioManager.setSpeakerphoneOn(false)
                audioManager.setStreamSolo(AudioManager.STREAM_MUSIC, true)
                mediaPlayer.setVolume(1.0f, 0.0f) // Left only
                mediaPlayer.start()
            }
            "right" -> {
                mediaPlayer = MediaPlayer.create(this, R.raw.test_sound)
                audioManager.setStreamSolo(AudioManager.STREAM_MUSIC, true)
                mediaPlayer.setVolume(0.0f, 1.0f) // Right only
                mediaPlayer.start()
            }
            "center" -> {
                mediaPlayer = MediaPlayer.create(this, R.raw.test_sound)
                mediaPlayer.setVolume(1.0f, 1.0f) // Both ears
                mediaPlayer.start()
            }
        }

        mediaPlayer.setOnCompletionListener {
            // Play the next sound after completion, e.g., switch from left to right to center
            if (ear == "left") {
                playSoundInEar("right")
            } else if (ear == "right") {
                playSoundInEar("center")
            } else {
                statusTextView.text = "Test complete!"
            }
        }
    }

    private fun isHeadphonesConnected(): Boolean {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return audioManager.isWiredHeadsetOn || audioManager.isBluetoothA2dpOn
    }

    private fun showHeadphoneNotConnectedPopup() {
        AlertDialog.Builder(this)
            .setTitle("No Headphones Connected")
            .setMessage("Please connect Bluetooth or wired headphones to start the test.")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }
}
