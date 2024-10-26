package com.example.myhome02

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class SignInActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var appNameTextView: TextView

    // List of custom fonts
    private val fontList = listOf(
        R.font.adventurerdemoregular, R.font.southcrew, R.font.drphibes, R.font.avaterrapersonal,
        R.font.anirm, R.font.standupsdemoregular, R.font.speedratedemoregular, R.font.modernwarfare,
        R.font.deadpoolmovie, R.font.jansina
    )
    private var currentFontIndex = 0
    private val handler = Handler()
    private val delayMillis: Long = 500 // Time between font changes in milliseconds



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Initialize app name TextView and start the font animation
        appNameTextView = findViewById(R.id.appNameTextView)
        startFontAnimation()

        // Initialize Google Sign-In options
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val googleSignInButton: SignInButton = findViewById(R.id.googleSignInButton)
        googleSignInButton.setOnClickListener { signInWithGoogle() }

        val logInButton: Button = findViewById(R.id.logInButton)
        val emailEditText: EditText = findViewById(R.id.emailEditText)
        val passwordEditText: EditText = findViewById(R.id.passwordEditText)
        val forgotPasswordText: TextView = findViewById(R.id.forgotPasswordButton)
        val passwordToggle: ImageView = findViewById(R.id.passwordToggle)

        val signUpTextView: TextView = findViewById(R.id.signUpLink)
        signUpTextView.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        // Toggle password visibility
        passwordToggle.setOnClickListener {
            if (passwordEditText.transformationMethod is PasswordTransformationMethod) {
                passwordEditText.transformationMethod = HideReturnsTransformationMethod.getInstance()
                passwordToggle.setImageResource(R.drawable.ic_visibility_on)
            } else {
                passwordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
                passwordToggle.setImageResource(R.drawable.ic_visibility_off)
            }
            passwordEditText.setSelection(passwordEditText.text.length) // Move cursor to end
        }

        // Handle password reset
        forgotPasswordText.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (email.isNotEmpty()) {
                sendPasswordResetEmail(email)
            } else {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            }
        }

        logInButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginWithEmailPassword(email, password)
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Font animation
    private fun startFontAnimation() {
        handler.post(object : Runnable {
            override fun run() {
                val typeface = ResourcesCompat.getFont(this@SignInActivity, fontList[currentFontIndex])
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

    // Sign in with Google
    private fun signInWithGoogle() {
        googleSignInClient.signOut().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleGoogleSignInResult(task)
        }
    }

    private fun handleGoogleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            if (account != null) {
                // Sign in the user with Firebase
                auth.signInWithCredential(GoogleAuthProvider.getCredential(account.idToken, null))
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // User is signed in, now store data
                            storeGoogleUserDataInFirestore(account)
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Firebase sign-in failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        } catch (e: ApiException) {
            Toast.makeText(this, "Sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    // Store Google user data in Firestore
    // Store Google user data in Firestore
    private fun storeGoogleUserDataInFirestore(account: GoogleSignInAccount) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = firestore.collection("users").document(userId)

        val userData = mapOf(
            "name" to account.displayName,
            "email" to account.email,
            "photoUrl" to account.photoUrl.toString(),
            "provider" to "Google"
        )

        userRef.set(userData).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Google user data saved successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to save Google user data: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // Login with email and password
    private fun loginWithEmailPassword(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                if (user != null && user.isEmailVerified) {
                    storeManualUserDataInFirestore(email)
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Please verify your email before logging in.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Store manually entered email in Firestore (no password)
    private fun storeManualUserDataInFirestore(email: String) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = firestore.collection("users").document(userId)

        val userData = mapOf(
            "email" to email,
            "provider" to "Manual"
        )

        userRef.set(userData).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "User data saved successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to save user data: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Send password reset email
    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Reset password email sent", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to send reset email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val RC_SIGN_IN = 1001
    }
}
