package com.myapps.splitwiseclone.ui.screens.home

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import com.myapps.splitwiseclone.R
import com.myapps.splitwiseclone.models.UserAccount
import com.myapps.splitwiseclone.ui.Routes


private const val TAG = "HomeScreen"

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Splitter") },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White // Optional: to set the text color
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Routes.createGroupScreen) },
                text = { Text(text = "New Group") },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_add_24),
                        contentDescription = null,
                        tint = Color.White
                    )
                },
                containerColor = MaterialTheme.colorScheme.primary
            )
        },
        content = {
            HomeScreenContent(navController = navController)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(navController: NavController) {
    val context = LocalContext.current
    val auth: FirebaseAuth = Firebase.auth
    var fullName by remember {
        mutableStateOf("")
    }
    var searchString by remember {
        mutableStateOf("")
    }
    val databaseRef =
        Firebase.auth.currentUser?.uid?.let { Firebase.database.reference.child("users").child(it) }
    databaseRef?.get()?.addOnSuccessListener { dataSnapShot ->
        val userAccount = dataSnapShot.getValue(UserAccount::class.java)
        if (userAccount != null) {
            fullName = userAccount.fullName
        } else {
            Toast.makeText(context, "User data not found.", Toast.LENGTH_SHORT).show()
        }
    }

    Column(modifier = Modifier.padding(top = 80.dp, start = 16.dp, end = 16.dp)) {
        Text(
            text = "Welcome $fullName",
            fontSize = 27.sp,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        OutlinedTextField(
            value = searchString,
            placeholder = { Text(text = "Search") },
            onValueChange = { searchString = it },
            modifier = Modifier.fillMaxWidth()
        )

        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().padding(20.dp)){
            Text(text = "Your groups will appear here")
        }
        Button(onClick = {
            auth.signOut()
            navController.navigate("login")
        }) {
            Text(text = "Sign Out")
        }
    }
}

@Composable
@Preview
fun HomeScreenPreview() {
    HomeScreen(navController = rememberNavController())
}

fun getUserName(uid: String): String {
    return ""
}