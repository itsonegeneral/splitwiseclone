package com.myapps.splitwiseclone.ui.screens.home.groups

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import com.myapps.splitwiseclone.R
import com.myapps.splitwiseclone.helpers.fetchObjectsByIds
import com.myapps.splitwiseclone.models.SplitGroup
import com.myapps.splitwiseclone.models.UserAccount
import com.myapps.splitwiseclone.ui.Routes
import com.myapps.splitwiseclone.ui.components.CustomLoading
import com.myapps.splitwiseclone.ui.components.KeyboardAware
import com.myapps.splitwiseclone.ui.screens.home.common.states.SelectionState
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGroupScreen(navController: NavController, groupId: String?) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Group") },
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                EditGroupScreenContent(navController = navController, groupId = groupId!!)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGroupScreenContent(navController: NavController, groupId: String) {

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    var updatedGroupName by rememberSaveable {
        mutableStateOf(
            savedStateHandle?.get<String>("groupName") ?: ""
        )
    }

    var splitGroup by remember {
        mutableStateOf(SplitGroup())
    }
    var splitGroupMembers by remember {
        mutableStateOf(listOf<UserAccount>())
    }
    var isLoading = false
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        isLoading = false
        try {
            val snapshot = Firebase.database.reference.child("groups").child(groupId).get().await()
            splitGroup = snapshot.getValue(SplitGroup::class.java)!!
            updatedGroupName = splitGroup.groupName
            if (SelectionState.selectedUsers.isNotEmpty()) {
                //Came back from selection users screen
                splitGroup.groupMembers = SelectionState.selectedUsers
            }
            fetchObjectsByIds(splitGroup.groupMembers) {
                splitGroupMembers = ArrayList()
                splitGroupMembers = ArrayList(it.values.toList())
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to fetch group details", Toast.LENGTH_SHORT).show()
        }
        isLoading = true
    }

    if (isLoading) {
        CustomLoading()
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Spacer(modifier = Modifier.padding(8.dp))
        Text(text = "Group Name", fontSize = 20.sp)
        Spacer(modifier = Modifier.padding(8.dp))
        OutlinedTextField(modifier = Modifier.fillMaxWidth(),
            value = updatedGroupName,
            onValueChange =
            {
                updatedGroupName = it
                savedStateHandle?.set("groupName", updatedGroupName)
            })
        Spacer(modifier = Modifier.padding(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Members", fontSize = 20.sp, modifier = Modifier.fillMaxWidth(.8f))
            IconButton(onClick = {
                SelectionState.selectedUsers.clear()
                navController.navigate(Routes.selectGroupSelectMembersScreen)
            }, modifier = Modifier.fillMaxWidth(1f)) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_add_24),
                    contentDescription = "Add",
                )
            }
        }

        if (splitGroupMembers.isEmpty() && !isLoading) {
            Text(text = "No members in this group", textAlign = TextAlign.Center)
        }

        Box(modifier = Modifier.weight(1f)){
            LazyColumn(content = {
                splitGroupMembers.forEach {
                    item {
                        ElevatedCard(
                            modifier = Modifier
                                .padding(10.dp)
                                .fillMaxWidth()
                                .border(
                                    width = 0.dp,
                                    color = MaterialTheme.colorScheme.background, // Adjust border color as needed
                                    shape = RoundedCornerShape(0.dp) // Adjust corner radius here
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.background,
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 8.dp
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .wrapContentHeight()
                                    .height(68.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_account_circle_24),
                                    contentDescription = "Account",
                                    modifier = Modifier
                                        .width(38.dp)
                                        .height(38.dp)
                                        .padding(6.dp)
                                )
                                Column(modifier = Modifier.fillMaxWidth(.8f)) {
                                    Spacer(modifier = Modifier.padding(4.dp))
                                    Text(
                                        text = it.fullName,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(text = it.email, style = MaterialTheme.typography.bodyMedium)
                                    Spacer(modifier = Modifier.padding(4.dp))

                                }
                                IconButton(onClick = {
                                    if (it.uid == Firebase.auth.uid) {
                                        //Cannot remove self, only can exit
                                        Toast.makeText(
                                            context,
                                            "You cannot remove yourself, you can exit either",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        splitGroupMembers =
                                            splitGroupMembers.filter { member -> it.uid != member.uid }
                                    }
                                }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.baseline_remove_24),
                                        contentDescription = "Remove",
                                    )
                                }
                            }
                        }
                    }
                }
            })
        }
        Button(onClick = {
            splitGroup.groupName = updatedGroupName
            val updatedGroupMembers = ArrayList<String>()
            splitGroupMembers.forEach { updatedGroupMembers.add(it.uid) }
            if(!updatedGroupMembers.contains(Firebase.auth.uid)){
                updatedGroupMembers.add(Firebase.auth.uid.toString())
            }
            splitGroup.groupMembers = updatedGroupMembers

            isLoading = true
            updateSplitGroup(splitGroup = splitGroup) { isSuccess ->
                isLoading = false
                if (isSuccess) {
                    Toast.makeText(context, "Group updated", Toast.LENGTH_SHORT).show()
                    navController.navigate(Routes.groupMessagesScreen(groupId))
                } else {
                    Toast.makeText(context, "Failed to update, try again", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }) {
            Text(modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, text = "Update")
        }
    }
}

fun updateSplitGroup(splitGroup: SplitGroup, onComplete: (Boolean) -> Unit) {
    Firebase.database.reference.child("groups").child(splitGroup.groupId).setValue(splitGroup)
        .addOnCompleteListener {
            onComplete(it.isSuccessful)
        }
}


@Composable
@Preview
private fun EGSPreview() {
    EditGroupScreen(navController = rememberNavController(), groupId = "groupId")
}