package com.project01.myhome02

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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

class GoogleSignInViewModel : ViewModel() {
    val user = MutableLiveData<User?>(null)
    private lateinit var sessionManager: SessionManager
    private val firestore = FirebaseFirestore.getInstance()

    // Initialize Session Manager and check for saved user session
    fun initSessionManager(context: Context) {
        sessionManager = SessionManager(context)
        // Check if the user is logged in from session data
        if (sessionManager.isLoggedIn()) {
            val savedUser = sessionManager.getUser()
            user.value = savedUser ?: run {
                sessionManager.clearSession() // Clear session if corrupted
                null
            }
        }
    }

    // Revalidate the session to check if the Firebase user is consistent with the session
    fun revalidateSession() {
        val firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null && sessionManager.isLoggedIn()) {
            val savedUser = sessionManager.getUser()
            if (savedUser?.id == currentUser.uid) {
                user.value = savedUser
            } else {
                val newUser = User(
                    id = currentUser.uid,
                    name = currentUser.displayName ?: "No Name",
                    photoUrl = currentUser.photoUrl?.toString() ?: "",
                    email = currentUser.email ?: "No Email"
                )
                sessionManager.saveUserSession(newUser)
                user.value = newUser
            }
        } else {
            sessionManager.clearSession()
            user.value = null
        }
    }

    // Handle Google Sign-In
    fun handleGoogleSignIn(context: Context, navController: NavController) {
        viewModelScope.launch {
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

                            // Fetch user data immediately after sign-in
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

    // Save user data to Firestore
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

    // Logout functionality
    fun logout() {
        FirebaseAuth.getInstance().signOut()
        sessionManager.clearSession()
        user.value = null
    }

    // Fetch user data from Firestore
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

    fun updateUserProfile(updatedUser: User) {
        val userDocument = firestore.collection("users").document(updatedUser.id)
        userDocument.set(updatedUser)
            .addOnSuccessListener {
                // Update LiveData with the new user data
                user.postValue(updatedUser)

                // Update session data
                sessionManager.saveUserSession(updatedUser)

                Log.d("Firestore", "User profile updated successfully")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error updating user profile: ${e.message}")
            }
    }


    // Function to perform Google Sign-In and return a Flow of AuthResult
    private suspend fun googleSignIn(context: Context): Flow<Result<AuthResult>> {
        val firebaseAuth = FirebaseAuth.getInstance()

        return callbackFlow {
            try {
                val credentialManager: CredentialManager = CredentialManager.create(context)
                val ranNonce: String = UUID.randomUUID().toString()
                val bytes: ByteArray = ranNonce.toByteArray()
                val md: MessageDigest = MessageDigest.getInstance("SHA-256")
                val digest: ByteArray = md.digest(bytes)
                val hashedNonce: String = digest.fold("") { str, it -> str + "%02x".format(it) }

                val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(R.string.web_client_id))
                    .setNonce(hashedNonce)
                    .setAutoSelectEnabled(true)
                    .build()

                val request: GetCredentialRequest = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(context, request)
                val credential = result.credential

                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                    val authResult = firebaseAuth.signInWithCredential(authCredential).await()

                    trySend(Result.success(authResult))
                } else {
                    throw RuntimeException("Received an invalid credential type.")
                }
            } catch (e: GetCredentialCancellationException) {
                trySend(Result.failure(Exception("Sign-in was canceled.")))
            } catch (e: Exception) {
                trySend(Result.failure(e))
            }

            awaitClose { }
        }
    }
}
