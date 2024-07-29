package com.myapps.splitwiseclone.ui.screens.home

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import com.myapps.splitwiseclone.models.UserAccount


private const val TAG = "HomeScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController){
    val context = LocalContext.current
    val auth : FirebaseAuth = Firebase.auth
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
    Column {
        Text(text = "Welcome $fullName")
        OutlinedTextField(value = searchString, onValueChange = {searchString = it})
        Button(onClick = {
            auth.signOut()
            navController.navigate("login")
        }) {
            Text(text = "Sign Out")
        }
    }
}

fun getUserName(uid: String): String {
    return ""
}