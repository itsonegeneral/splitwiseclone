package com.myapps.splitapp.ui.screens.home.common

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.database.database
import com.myapps.splitapp.DatabaseKeys
import com.myapps.splitapp.helpers.getContactNumbers
import com.myapps.splitapp.models.UserAccount
import com.myapps.splitapp.ui.components.CustomLoading
import com.myapps.splitapp.ui.screens.home.common.states.SelectionState
import com.myapps.splitapp.R
import kotlinx.coroutines.tasks.await

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectGroupMembersScreen(
    navController: NavController
) {

    var isLoading by remember {
        mutableStateOf(false)
    }

    var users by remember {
        mutableStateOf(listOf<UserAccount>())
    }

    val context = LocalContext.current
    var selectedUsers by remember {
        mutableStateOf(listOf<String>())
    }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val snapshot =
                Firebase.database.reference.child(DatabaseKeys.userAccounts).get().await()
            users = snapshot.children.mapNotNull { it.getValue(UserAccount::class.java) }
        } catch (e: Exception) {
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
        }
        isLoading = false
    }



    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Users") },
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
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (selectedUsers.isEmpty()) {
                        Toast.makeText(context, "No members selected", Toast.LENGTH_SHORT).show()
                        return@ExtendedFloatingActionButton
                    } else {
                        SelectionState.selectedUsers = ArrayList(selectedUsers.toList())
                        navController.popBackStack()
                    }
                },
                text = { Text(text = "Confirm") },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_check_24),
                        contentDescription = null,
                        tint = Color.White
                    )
                },
                containerColor = MaterialTheme.colorScheme.primary
            )
        },
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {

                if (isLoading) {
                    CustomLoading()
                }
                SelectGroupMembersContent(users, selectedUsers) {
                    selectedUsers = it
                }
            }
        }
    )
}

private const val TAG = "SelectGroupMembersScree"

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SelectGroupMembersContent(
    users: List<UserAccount>,
    selectedUsers: List<String>,
    setSelectedUsers: (List<String>) -> Unit
) {
    val context = LocalContext.current
    val contacts = getContactNumbers(context)
    var selectedUsers1 = selectedUsers
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Add your friends to group",
            fontSize = 22.sp,
            modifier = Modifier.padding(top = 20.dp, bottom = 10.dp)
        )
        users.forEach { user ->
            if (!contacts.contains(String.format("%.0f", user.contactNumber))) {
                Log.d(
                    TAG,
                    "SelectGroupMembersContent: ${
                        String.format(
                            "%.0f",
                            user.contactNumber
                        )
                    } is not "
                )
            } else {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .wrapContentHeight()
                        .border(
                            width = 2.dp,
                            color = if (selectedUsers1.contains(user.uid)) Color.Blue else Color.Transparent // Change border color based on selection
                        )
                        .height(55.dp),
                    shape = RoundedCornerShape(2.dp),
                    onClick = {
                        selectedUsers1 = if (selectedUsers1.contains(user.uid)) {
                            selectedUsers1 - user.uid
                        } else {
                            selectedUsers1 + user.uid
                        }
                        setSelectedUsers(selectedUsers1)
                    },
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(text = user.fullName, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = String.format("%.0f", user.contactNumber),
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SelectGroupMembersContent(setSelectedUsers: (List<String>) -> Unit) {


}
