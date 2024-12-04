package com.project01.myhome02

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.text.method.HideReturnsTransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.os.Handler

class SignUpActivity : AppCompatActivity() {

    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var mobileNumberEditText: EditText
    private lateinit var emailUsernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var loginLink: TextView
    private lateinit var termsAndConditionsLink: TextView
    private lateinit var appNameTextView: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    // List of custom fonts
    private val fontList = listOf(
        R.font.adventurerdemoregular, R.font.southcrew, R.font.drphibes, R.font.avaterrapersonal,
        R.font.anirm, R.font.standupsdemoregular, R.font.speedratedemoregular, R.font.modernwarfare,
        R.font.deadpoolmovie, R.font.jansina
    )
    private var currentFontIndex = 0
    private val handler = Handler()
    private val delayMillis: Long = 500 // Time between font changes in milliseconds

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        firstNameEditText = findViewById(R.id.firstNameEditText)
        lastNameEditText = findViewById(R.id.lastNameEditText)
        mobileNumberEditText = findViewById(R.id.mobileNumberEditText)
        emailUsernameEditText = findViewById(R.id.emailUsernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        registerButton = findViewById(R.id.registerButton)
        loginLink = findViewById(R.id.loginLink)
        termsAndConditionsLink = findViewById(R.id.termsAndConditionsLink)
        appNameTextView = findViewById(R.id.appNameTextView) // Initialize your TextView for font animation

        // Start font animation
        startFontAnimation()

        // Password visibility toggle setup
        setupPasswordVisibilityToggle(passwordEditText, findViewById(R.id.passwordToggle))
        setupPasswordVisibilityToggle(confirmPasswordEditText, findViewById(R.id.confirmPasswordToggle))

        // Register Button Click Listener
        registerButton.setOnClickListener {
            registerUser()
        }

        // Navigate to login when the login link is clicked
        loginLink.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }

        // Show Terms and Conditions popup when the link is clicked
        termsAndConditionsLink.setOnClickListener {
            showTermsAndConditionsDialog()
        }
    }

    private fun registerUser() {
        val firstName = firstNameEditText.text.toString().trim()
        val lastName = lastNameEditText.text.toString().trim()
        val mobileNumber = mobileNumberEditText.text.toString().trim()
        val email = emailUsernameEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()

        if (firstName.isEmpty() || lastName.isEmpty() || mobileNumber.isEmpty() ||
            email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        // Create user with email and password
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // User registration successful
                    sendEmailVerification(firstName, lastName, mobileNumber, email)
                } else {
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun sendEmailVerification(firstName: String, lastName: String, mobileNumber: String, email: String) {
        val user = auth.currentUser
        user?.sendEmailVerification()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Verification email sent to ${user.email}", Toast.LENGTH_SHORT).show()
                // Save user data to the database
                saveUserToDatabase(firstName, lastName, mobileNumber, email)
                // Optionally, you can navigate to another screen
                startActivity(Intent(this, SignInActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Failed to send verification email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupPasswordVisibilityToggle(editText: EditText, toggleIcon: ImageView) {
        toggleIcon.setOnClickListener {
            if (editText.transformationMethod is PasswordTransformationMethod) {
                editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
                toggleIcon.setImageResource(R.drawable.ic_visibility_on) // Replace with your visibility on icon
            } else {
                editText.transformationMethod = PasswordTransformationMethod.getInstance()
                toggleIcon.setImageResource(R.drawable.ic_visibility_off) // Replace with your visibility off icon
            }
            editText.setSelection(editText.text.length) // Move cursor to end of text
        }
    }

    private fun saveUserToDatabase(firstName: String, lastName: String, mobileNumber: String, email: String) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = database.child("users").child(userId)

        val userData = mapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "mobileNumber" to mobileNumber,
            "email" to email
        )

        userRef.setValue(userData).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "User data saved successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to save user data: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showTermsAndConditionsDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Terms and Conditions")
        builder.setMessage("Here are the Terms and Conditions of Aural Log...\n\n" +
                "1. Usage of the app is subject to acceptance of these terms.\n" +
                "2. Aural Log is intended for informational purposes only and should not be considered a substitute for hospital-generated reports or professional medical advice.\n" +
                "3. The accuracy of the test results may vary, and we do not guarantee complete accuracy.\n" +
                "4. We are not responsible for any actions taken based on the information provided by the app.\n" +
                "5. Users must not misuse the application in any manner.\n" +
                "6. Data collected through the app is stored securely in our database.\n" +
                "7. We reserve the right to modify these terms at any time.\n" +
                "\nPlease review and accept these terms before proceeding.")

        builder.setPositiveButton("Close") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun startFontAnimation() {
        handler.post(object : Runnable {
            override fun run() {
                val typeface = ResourcesCompat.getFont(this@SignUpActivity, fontList[currentFontIndex])
                appNameTextView.typeface = typeface

                // Update index for the next font
                currentFontIndex = (currentFontIndex + 1) % fontList.size

                // Repeat the animation
                handler.postDelayed(this, delayMillis)
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null) // Stop the animation when the activity is destroyed
    }
}
