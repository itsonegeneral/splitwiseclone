package com.myapps.splitwiseclone.ui.screens.home.creategroup

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableStateListOf
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
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import com.myapps.splitwiseclone.R
import com.myapps.splitwiseclone.models.SplitGroup
import com.myapps.splitwiseclone.models.UserAccount
import com.myapps.splitwiseclone.ui.Routes
import kotlinx.coroutines.tasks.await
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectGroupMembersScreen(navController: NavController, groupName: String?) {

    var selectedUsers by remember {
        mutableStateOf(listOf<String>())
    }

    var users by remember {
        mutableStateOf(listOf<UserAccount>())
    }
    val context = LocalContext.current

    var isLoading by remember {
        mutableStateOf(false)
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
                    if(selectedUsers.isEmpty()){
                        Toast.makeText(context,"No members selected",Toast.LENGTH_SHORT).show()
                        return@ExtendedFloatingActionButton
                    }

                    isLoading = true
                    val splitGroup = SplitGroup(createdBy = Firebase.auth.uid.toString(), groupMembers = selectedUsers as ArrayList<String>, groupName = groupName.toString())
                    val newChildGroup =  Firebase.database.reference.child("groups").push()
                    splitGroup.groupId = newChildGroup.key.toString()
                    newChildGroup.setValue(splitGroup).addOnSuccessListener {
                        isLoading = false
                        navController.navigate(Routes.homeScreen)
                    }.addOnFailureListener{
                        isLoading = false
                        Toast.makeText(context,"Unable to create, try again ${it.message}",Toast.LENGTH_SHORT).show()

                    }
                },
                text = { Text(text = "Create") },
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
                LaunchedEffect(Unit) {
                    isLoading = true
                    try {
                        val snapshot = Firebase.database.reference.child("users").get().await()
                        val userList =
                            snapshot.children.mapNotNull { it.getValue(UserAccount::class.java) }
                        users = userList
                    } catch (e: Exception) {
                        Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                    }
                    isLoading = false
                }

                if (isLoading) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Column {
                            CircularProgressIndicator()
                            Text(text = if (isLoading && selectedUsers.isEmpty()) "Getting your friends list..." else "Creating your group, hang on.")
                        }
                    }
                    return@Scaffold
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Add your friends to $groupName group",
                        fontSize = 22.sp,
                        modifier = Modifier.padding(top = 20.dp, bottom = 10.dp)
                    )
                    users.forEach { user ->
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .wrapContentHeight()
                                .border(
                                    width = 2.dp,
                                    color = if (selectedUsers.contains(user.uid)) Color.Blue else Color.Transparent // Change border color based on selection
                                )
                                .height(55.dp),
                            shape = RoundedCornerShape(2.dp),
                            onClick = {
                                selectedUsers = if (selectedUsers.contains(user.uid)) {
                                    selectedUsers - user.uid
                                } else {
                                    selectedUsers + user.uid
                                }
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
    )
}
