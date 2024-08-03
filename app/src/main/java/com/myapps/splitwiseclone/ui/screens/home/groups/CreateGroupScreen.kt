package com.myapps.splitwiseclone.ui.screens.home.groups

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import com.myapps.splitwiseclone.R
import com.myapps.splitwiseclone.helpers.fetchObjectsByIds
import com.myapps.splitwiseclone.models.SplitGroup
import com.myapps.splitwiseclone.models.UserAccount
import com.myapps.splitwiseclone.ui.Routes
import com.myapps.splitwiseclone.ui.screens.home.common.states.SelectionState

private const val TAG = "CreateGroupScreen_"

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(navController: NavController) {

    val context = LocalContext.current

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    var groupName by rememberSaveable {
        mutableStateOf(
            savedStateHandle?.get<String>("groupName") ?: ""
        )
    }


    val selectedMembers by remember {
        mutableStateOf(SelectionState.selectedUsers)
    }

    Log.d(TAG, "CreateGroupScreen: ${selectedMembers.size}")


    var selectedMembersFullObjects by remember {
        mutableStateOf(listOf<UserAccount>())
    }

    if (selectedMembers.isNotEmpty()) {
        fetchObjectsByIds(selectedMembers) {
            selectedMembersFullObjects = it.values.toList()
        }
    }

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
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (groupName.isEmpty()) {
                        Toast.makeText(context, "Enter group name", Toast.LENGTH_SHORT).show()
                        return@ExtendedFloatingActionButton
                    }
                    if (selectedMembers.isEmpty()) {
                        navController.navigate(Routes.selectGroupSelectMembersScreen)
                        return@ExtendedFloatingActionButton
                    }

                    selectedMembers.add(Firebase.auth.uid.toString())
                    val splitGroup = SplitGroup(
                        createdBy = Firebase.auth.uid.toString(),
                        groupMembers = selectedMembers,
                        groupName = groupName
                    )
                    val newChildGroup = Firebase.database.reference.child("groups").push()
                    splitGroup.groupId = newChildGroup.key.toString()
                    newChildGroup.setValue(splitGroup).addOnSuccessListener {
                        Toast.makeText(
                            context,
                            "Group created",
                            Toast.LENGTH_SHORT
                        ).show()
                        navController.navigate(Routes.homeScreen)
                    }.addOnFailureListener {
                        Toast.makeText(
                            context,
                            "Unable to create, try again ${it.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                text = {
                    Text(text = if (SelectionState.selectedUsers.isEmpty()) "Select Members" else "Done")
                },
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
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Provide a group name",
                        fontSize = 20.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    OutlinedTextField(
                        placeholder = { Text(text = "Group Name") },
                        value = groupName,
                        onValueChange = {
                            groupName = it
                            savedStateHandle?.set("groupName", groupName)  // Save the state
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Text(
                        text = "Members",
                        fontSize = 20.sp,
                        modifier = Modifier.padding(bottom = 10.dp, top = 20.dp)
                    )
                    UsersList(users = selectedMembersFullObjects)
                }
            }

        }
    )
}


@Composable
fun UsersList(users: List<UserAccount>) {

    if (users.isEmpty()) {
        Text(
            text = "No members selected",
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            textAlign = TextAlign.Center,
            color = Color.LightGray
        )
    }

    users.forEach { user ->
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .wrapContentHeight()
                .height(55.dp),
            shape = RoundedCornerShape(2.dp),
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