package com.example.myhome02

import androidx.lifecycle.ViewModel
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class GoogleSignInViewModel : ViewModel() {
    val user = MutableLiveData<User?>(null)
    private lateinit var sessionManager: SessionManager
    private val firestore = FirebaseFirestore.getInstance()

    fun initSessionManager(context: Context) {
        sessionManager = SessionManager(context)
        // Check if user is already logged in
        if (sessionManager.isLoggedIn()) {
            user.value = sessionManager.getUser()
        }
    }

    // Function to handle Google Sign-In
    fun handleGoogleSignIn(context: Context, navController: NavController) {
        viewModelScope.launch {
            // Collect the result of the Google Sign-In process
            googleSignIn(context).collect { result ->
                result.fold(
                    onSuccess = { authResult ->
                        // Handle successful sign-in
                        val currentUser = authResult.user
                        if (currentUser != null) {
                            val newUser = User(
                                id = currentUser.uid,
                                name = currentUser.displayName ?: "No Name",
                                photoUrl = currentUser.photoUrl?.toString() ?: "",
                                email = currentUser.email ?: "No Email"
                            )
                            user.value = newUser

                            // Save user session
                            sessionManager.saveUserSession(newUser)

                            // Save user data to Firestore
                            saveUserToFirestore(newUser)

                            // Fetch user data from Firestore immediately after sign-in
                            fetchUserData(newUser.id, context)

                            // Show success message
                            Toast.makeText(
                                context,
                                "Account created successfully!",
                                Toast.LENGTH_LONG
                            ).show()

                            // Navigate to the profile screen
                            navController.navigate("profile")
                        }
                    },
                    onFailure = { e ->
                        // Handle sign-in error
                        Toast.makeText(
                            context,
                            "Something went wrong: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.d("Issue", "handleGoogleSignIn: ${e.message}")
                    }
                )
            }
        }
    }


    // Function to save user data to Firestore
    private fun saveUserToFirestore(user: User) {
        val userDocument = firestore.collection("users").document(user.id)
        userDocument.set(user)
            .addOnSuccessListener {
                Log.d("Firestore", "User profile added/updated successfully")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error adding user profile", e)
            }
    }

    // Function to fetch user data from Firestore
    fun fetchUserData(userId: String, context: Context) {
        val userDocument = firestore.collection("users").document(userId)
        userDocument.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Convert document to User object
                    val fetchedUser = document.toObject(User::class.java)
                    if (fetchedUser != null) {
                        user.value = fetchedUser // Update the LiveData
                        sessionManager.saveUserSession(fetchedUser) // Update session
                        Log.d("Firestore", "User data fetched successfully: $fetchedUser")
                    } else {
                        Log.e("Firestore", "Failed to convert document to User")
                    }
                } else {
                    Log.e("Firestore", "No such document or document does not exist")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching user data: ${e.message}")
                Toast.makeText(context, "Error fetching user data: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }



    // Function to update user profile data in Firestore
    fun updateUserProfile(updatedUser: User) {
        val userDocument = firestore.collection("users").document(updatedUser.id)
        userDocument.update("name", updatedUser.name, "photoUrl", updatedUser.photoUrl)
            .addOnSuccessListener {
                Log.d("Firestore", "User profile updated successfully")
                // Update LiveData to reflect changes
                user.value = updatedUser
                // Optionally, show a toast or notification to inform the user
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error updating user profile", e)
                // Optionally, show an error message
            }
    }

    // Function to perform Google Sign-In and return a Flow of AuthResult
    private suspend fun googleSignIn(context: Context): Flow<Result<AuthResult>> {
        // Initialize Firebase Auth instance
        val firebaseAuth = FirebaseAuth.getInstance()

        // Return a Flow that emits the result of the Google Sign-In process
        return callbackFlow {
            try {
                // Initialize Credential Manager
                val credentialManager: CredentialManager = CredentialManager.create(context)

                // Generate a nonce (a random number used once) for security
                val ranNonce: String = UUID.randomUUID().toString()
                val bytes: ByteArray = ranNonce.toByteArray()
                val md: MessageDigest = MessageDigest.getInstance("SHA-256")
                val digest: ByteArray = md.digest(bytes)
                val hashedNonce: String = digest.fold("") { str, it -> str + "%02x".format(it) }

                // Set up Google ID option with necessary parameters
                val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(R.string.web_client_id))
                    .setNonce(hashedNonce)
                    .setAutoSelectEnabled(true)
                    .build()

                // Create a credential request with the Google ID option
                val request: GetCredentialRequest = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                // Get the credential result from the Credential Manager
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential

                // Check if the received credential is a valid Google ID Token
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)

                    // Sign in with Firebase using the auth credential
                    val authResult = firebaseAuth.signInWithCredential(authCredential).await()

                    // Send the successful result
                    trySend(Result.success(authResult))
                } else {
                    // Throw an exception if the credential type is invalid
                    throw RuntimeException("Received an invalid credential type.")
                }
            } catch (e: GetCredentialCancellationException) {
                // Handle sign-in cancellation
                trySend(Result.failure(Exception("Sign-in was canceled.")))
            } catch (e: Exception) {
                // Handle other exceptions
                trySend(Result.failure(e))
            }

            awaitClose { }
        }
    }
}
