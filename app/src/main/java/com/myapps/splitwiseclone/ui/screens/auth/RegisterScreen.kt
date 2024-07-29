package com.myapps.splitwiseclone.ui.screens.auth

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import com.myapps.splitwiseclone.models.UserAccount
import com.myapps.splitwiseclone.ui.Routes

private const val TAG = "RegisterScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    val auth: FirebaseAuth = Firebase.auth
    val database: DatabaseReference = Firebase.database.reference


    //Form Data
    var contactNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }


    Surface {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(20.dp)
                .fillMaxSize()
        ) {
            Text(text = "Let's get started", fontSize = 22.sp)
            OutlinedTextField(
                value = fullName, onValueChange = {
                    fullName = it
                },
                label = { Text("Name") },
                singleLine = true
            )
            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            OutlinedTextField(
                value = contactNumber,
                onValueChange = { contactNumber = it },
                label = { Text("Contact Number") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.padding(10.dp))
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(onClick = {
                    isLoading = true
                    auth.createUserWithEmailAndPassword(
                        email,
                        password
                    )
                        .addOnCompleteListener { task ->
                            isLoading = false
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success")
                                val user = auth.currentUser
                                if (user != null) {
                                    val userAccount = UserAccount(user.uid,fullName,email,contactNumber.toDouble())
                                    database.child("users").child(user.uid).setValue(userAccount)
                                    Toast.makeText(
                                        context,
                                        "Account created",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navController.navigate(Routes.loginScreen)
                                }
                            } else {
                                isLoading = false
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.exception)
                                Toast.makeText(
                                    context,
                                    "Authentication failed.",
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        }
                }) {
                    Text(text = "Register")
                }
            }

        }
    }
}


@Composable
@Preview(showBackground = true)
fun RegisterScreenPreview() {
    RegisterScreen(navController = rememberNavController())
}