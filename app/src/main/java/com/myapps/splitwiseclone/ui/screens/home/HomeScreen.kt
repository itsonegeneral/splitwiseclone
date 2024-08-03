package com.myapps.splitwiseclone.ui.screens.home

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import com.myapps.splitwiseclone.R
import com.myapps.splitwiseclone.models.SplitGroup
import com.myapps.splitwiseclone.models.UserAccount
import com.myapps.splitwiseclone.ui.Routes
import com.myapps.splitwiseclone.ui.screens.home.common.states.SelectionState
import kotlinx.coroutines.tasks.await


private const val TAG = "HomeScreen"

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {

    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Splitter") },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White // Optional: to set the text color
                ),
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_more_vert_24),
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(text = "Logout")},
                            onClick = {
                                menuExpanded = false
                                // Handle edit group action here
                                Firebase.auth.signOut()
                                navController.navigate(Routes.loginScreen)
                            }
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    SelectionState.selectedUsers.clear()
                    navController.navigate(Routes.createGroupScreen) },
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
    var isLoading by remember { mutableStateOf(false) }
    var groups by remember {
        mutableStateOf(listOf<SplitGroup>())
    }
    var filteredGroups by remember{
        mutableStateOf(listOf<SplitGroup>())
    }
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val snapshot = Firebase.database.reference.child("groups").get().await()
            val groupsList =
                snapshot.children.mapNotNull { it.getValue(SplitGroup::class.java) }
            groups = groupsList
            groups =
                groups.filter {
                    it.createdBy == Firebase.auth.uid.toString() || it.groupMembers.contains(
                        Firebase.auth.uid.toString()
                    )
                }
        } catch (e: Exception) {
            Log.d(TAG, "HomeScreenContent: ${e.message}")
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
        }
        isLoading = false
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
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                Column (horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Text(text = "Getting your groups...")
                }
            }
        }
        if (!isLoading && groups.isEmpty()) {
            Text(text = "You don't have any split groups, start creating one", modifier = Modifier.padding(20.dp))
        }
        if (!isLoading && groups.isNotEmpty()) {
            LazyColumn(
                verticalArrangement = Arrangement.Top,
                modifier = Modifier.padding(top = 10.dp, bottom = 10.dp)
            ) {
                item {
                    Text(text = "Your groups", color = Color.Gray, modifier= Modifier.padding(top = 15.dp, bottom = 8.dp))
                }

                groups.forEach { group ->
                   item {  ElevatedCard(
                       modifier = Modifier
                           .fillMaxWidth()
                           .padding(8.dp)
                           .wrapContentHeight(),
                       shape = RoundedCornerShape(2.dp),
                       onClick = {
                           if(group.groupId.isNotBlank()){
                               navController.navigate(Routes.groupMessagesScreen(group.groupId))
                           }else{
                               Toast.makeText(context, "This group was created with previous version, kindly create new group.", Toast.LENGTH_SHORT).show()
                           }

                       }
                   ) {
                       Text(
                           text = group.groupName,
                           fontWeight = FontWeight.SemiBold,
                           modifier = Modifier.padding(7.dp)
                       )
                       Text(
                           text = "${group.groupMembers.size} members",
                           fontWeight = FontWeight.Normal,
                           modifier = Modifier.padding(start = 7.dp, bottom = 10.dp)
                       )
                   } }
                }
            }
        }
    }

}

@Composable
fun HomeScreenPreview() {
    HomeScreen(navController = rememberNavController())
}

fun getUserName(uid: String): String {
    return ""
}