package com.myapps.splitwiseclone.ui.screens.home.creategroup

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.myapps.splitwiseclone.R
import com.myapps.splitwiseclone.ui.Routes

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Group") },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White // Optional: to set the text color
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        content = { innerPadding ->
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)) {
                CreateGroupScreenContent(navController = navController)
            }

        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreenContent(navController: NavController) {
    var groupName by remember {
        mutableStateOf("")
    }
    val context = LocalContext.current
    Column (modifier = Modifier.padding(16.dp)){
        Text(text = "Provide a group name", fontSize = 22.sp, modifier = Modifier.padding(bottom = 10.dp))
        OutlinedTextField(placeholder = { Text(text = "Group Name")},value = groupName, onValueChange = {groupName = it}, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Button(onClick = {
            if (groupName.isBlank()) {
                Toast.makeText(context,"Group name cannot be blank",Toast.LENGTH_SHORT).show()
                return@Button
            }
            navController.navigate(Routes.createGroupSelectMembersScreen(groupName))
        }) {
            Text(text = "Next")
        }
    }
}
