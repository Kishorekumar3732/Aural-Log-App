package com.example.myhome02

import android.os.Bundle
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.TextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.example.myhome02.ui.theme.AppColor

class ProfileActivity : ComponentActivity() {
    private lateinit var googleSignInViewModel: GoogleSignInViewModel
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the session manager
        sessionManager = SessionManager(this)

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
        }
    }
}


@Composable
fun ProfileScreen(
    googleSignInViewModel: GoogleSignInViewModel,
    sessionManager: SessionManager,
    navController: NavController
) {
    val user by googleSignInViewModel.user.observeAsState()
    var isEditingName by remember { mutableStateOf(false) } // Track if the user is in editing mode for name
    var isEditingImage by remember { mutableStateOf(false) } // Track if the user is in editing mode for image
    var editableName by remember { mutableStateOf(user?.name ?: "") } // Editable name
    var editablePhotoUrl by remember { mutableStateOf(user?.photoUrl ?: "") } // Editable photo URL

    // Get context and lifecycle owner
    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            editablePhotoUrl = it.toString() // Update the photo URL
            isEditingImage = false // Exit image editing mode after selecting image
        }
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
                            .clickable { isEditingImage = true } // Set image editing mode
                    ) {
                        AsyncImage(
                            model = editablePhotoUrl,  // Use editablePhotoUrl
                            contentDescription = "Avatar",
                            placeholder = painterResource(R.drawable.avatar),
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
                            .clickable {
                                // Launch file picker for new image only
                                isEditingImage = true
                                imagePickerLauncher.launch("image/*")
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
                        sessionManager.clearSession() // Clear session on logout
                        navController.navigate("login") { // Navigate to login screen
                            popUpTo(0) // Remove all previous destinations from the backstack
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

