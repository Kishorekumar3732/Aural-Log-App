package com.project01.myhome02

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Snackbar
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.project01.myhome02.ui.theme.AppColor
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

class ProfileActivity : ComponentActivity() {
    private lateinit var googleSignInViewModel: GoogleSignInViewModel
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the session manager
        sessionManager = SessionManager(this)

        if (!sessionManager.isLoggedIn()) {
            // Redirect to LoginActivity if the session is not valid
            startActivity(Intent(this, SignInActivity::class.java))
            finish() // Prevent returning to this activity
        }

        // Initialize the ViewModel
        googleSignInViewModel = ViewModelProvider(this).get(GoogleSignInViewModel::class.java)

        // Set content to your Composable function
        setContent {
            // Provide the view model and session manager
            ProfileScreen(
                googleSignInViewModel = googleSignInViewModel,
                navController = rememberNavController(), // Or pass the navController from somewhere else if needed
                sessionManager = sessionManager
            )

            val navController = rememberNavController() // Create NavController here
            AppNavGraph(navController, googleSignInViewModel, sessionManager)
        }
    }
}

private const val LOGIN_ROUTE = "login"

@Composable
fun ProfileScreen(
    googleSignInViewModel: GoogleSignInViewModel,
    sessionManager: SessionManager,
    navController: NavController
) {
    val user by googleSignInViewModel.user.observeAsState()
    var isEditingName by remember { mutableStateOf(false) }
    var editableName by remember { mutableStateOf(user?.name ?: "") }
    var editablePhotoUrl by remember { mutableStateOf(user?.photoUrl ?: "") }
    val context = LocalContext.current
    var showSaveSnackbar by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            editablePhotoUrl = it.toString()
            showSaveSnackbar = true // Show Snackbar after image update
        }
    }

    if (showSaveSnackbar) {
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                TextButton(onClick = { showSaveSnackbar = false }) {
                    Text("Dismiss", color = Color.White)
                }
            }
        ) { Text("Profile updated successfully!") }
    }

    Box {
        Image(
            modifier = Modifier.fillMaxWidth(),
            painter = painterResource(id = R.drawable.profile_screen_app_bar_bg),
            contentDescription = "App bar background",
            contentScale = ContentScale.FillWidth
        )
        IconButton(
            onClick = { navController.popBackStack() }, // Navigate back
            modifier = Modifier.align(Alignment.TopStart).padding(top = 32.dp, start = 8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                tint = Color.White,
                contentDescription = "Arrow Back",
                modifier = Modifier.size(35.dp)
            )
        }
        Text(
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 43.dp),
            text = "Profile",
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight(600),
                color = Color.White
            )
        )
        Card(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 106.dp, bottom = 16.dp)
                .shadow(elevation = 4.dp, shape = RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = AppColor.white)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.padding(vertical = 24.dp)
            ) {
                Box {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .clickable { imagePickerLauncher.launch("image/*") }
                    ) {
                        AsyncImage(
                            model = editablePhotoUrl,  // Use editablePhotoUrl
                            contentDescription = "Avatar",
                            placeholder = painterResource(R.drawable.avatar),
                            error = painterResource(R.drawable.avatar),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Box(
                        modifier = Modifier
                            .wrapContentSize()
                            .size(34.dp)
                            .offset(y = (-4).dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFF953C6).copy(alpha = 0.8f),
                                        Color(0xFFB91D73).copy(alpha = 0.8f)
                                    )
                                )
                            )
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .clickable { imagePickerLauncher.launch("image/*")
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Image",
                            tint = AppColor.white
                        )
                    }
                }

                user?.let {
                    // Show editable name field if editing name
                    if (isEditingName) {
                        TextField(
                            value = editableName,
                            onValueChange = { editableName = it },
                            label = { Text("Name") },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        )
                    } else {
                        ProfileItem(
                            title = "Name",
                            subtitle = it.name,
                            onEditClick = { isEditingName = true } // Set name editing mode
                        )
                    }
                }

                user?.let {
                    ProfileItem(
                        title = "Email",
                        subtitle = it.email
                    )
                }

                if (isEditingName) {
                    Button(
                        onClick = {
                            val updatedUser = user?.copy(
                                name = editableName,
                                photoUrl = editablePhotoUrl
                            )
                            if (updatedUser != null) {
                                googleSignInViewModel.updateUserProfile(updatedUser)
                                isEditingName = false // Exit name editing mode after saving
                                showSaveSnackbar = true // Show Snackbar after save
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(45.dp)
                            .padding(horizontal = 16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            width = 1.5.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFF953C6),
                                    Color(0xFFB91D73)
                                ),
                                start = Offset.Zero,
                                end = Offset.Infinite
                            )
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Save",
                            textAlign = TextAlign.Center,
                            style = TextStyle(
                                fontSize = 17.sp,
                                fontWeight = FontWeight(600),
                                color = Color(0xFFB91D73)
                            )
                        )
                    }
                }

                Button(
                    onClick = {
                        Log.d("Logout", "User logged out. Clearing session.")
                        sessionManager.clearSession()
                        navController.navigate("login") {
                            Log.d("Logout", "Navigating to Login Screen")
                            popUpTo(0) // Clear backstack
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(45.dp)
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFF953C6),
                                Color(0xFFB91D73)
                            ),
                            start = Offset.Zero,
                            end = Offset.Infinite
                        )
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Log out",
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            fontSize = 17.sp,
                            fontWeight = FontWeight(600),
                            color = Color(0xFFB91D73)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    googleSignInViewModel: GoogleSignInViewModel,
    sessionManager: SessionManager
) {
    val startDestination = if (sessionManager.isLoggedIn()) "profile" else "login"
    Log.d("AppNavGraph", "Start destination: $startDestination")

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            Log.d("Navigation", "Navigated to Login Screen")
            LoginScreen(
                onLoginSuccess = {
                    Log.d("Navigation", "Login successful. Navigating to Profile Screen.")
                    navController.navigate("profile") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("profile") {
            Log.d("Navigation", "Navigated to Profile Screen")
            ProfileScreen(
                googleSignInViewModel = googleSignInViewModel,
                sessionManager = sessionManager,
                navController = navController
            )
        }
    }
}


@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        Log.d("LoginScreen", "Launching SignInActivity")
        context.startActivity(Intent(context, SignInActivity::class.java))
        // Simulate successful login for testing
        onLoginSuccess()
    }
}



@Composable
fun ProfileItem(
    title: String,
    subtitle: String,
    onEditClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontSize = 18.sp,
                color = AppColor.darkBlue
            )
        )
        Row {
            Text(
                text = subtitle,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .align(Alignment.CenterVertically),
                style = TextStyle(
                    fontSize = 14.sp,
                    color = Color(0xFFA5A5A5)
                )
            )
            Spacer(modifier = Modifier.weight(1f))
            onEditClick?.let {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onEditClick() },
                    tint = AppColor.darkBlue
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            thickness = 1.dp,
            color = Color.Gray
        )
    }
}

