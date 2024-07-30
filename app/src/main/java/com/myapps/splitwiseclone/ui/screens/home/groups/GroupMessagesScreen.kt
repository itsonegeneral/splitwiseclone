package com.myapps.splitwiseclone.ui.screens.home.groups

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.isDigitsOnly
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import com.myapps.splitwiseclone.R
import com.myapps.splitwiseclone.models.ExpenseSplit
import com.myapps.splitwiseclone.models.SplitGroup
import com.myapps.splitwiseclone.models.UserAccount
import com.myapps.splitwiseclone.ui.Routes
import com.myapps.splitwiseclone.ui.components.KeyboardAware
import kotlinx.coroutines.tasks.await


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupMessagesScreen(navController: NavHostController, groupId: String?) {

    var groupDetail by remember {
        mutableStateOf(SplitGroup())
    }
    var context = LocalContext.current
    LaunchedEffect(Unit) {
        try {
            val snapshot =
                Firebase.database.reference.child("groups").child(groupId.toString()).get().await()
            groupDetail = snapshot.getValue(SplitGroup::class.java)!!

        } catch (e: Exception) {
            Toast.makeText(context, "Unable to fetch group details", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${groupDetail.groupName} Splits") },
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
        content = {
            Box(modifier = Modifier.padding(it)) {
                GroupMessagesScreenContent(navController = navController, groupDetail = groupDetail)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupMessagesScreenContent(navController: NavHostController, groupDetail: SplitGroup) {
    var inputValue by remember {
        mutableStateOf("")
    }

    val context = LocalContext.current

    KeyboardAware {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .imePadding() // Adjust padding based on the keyboard's presence
        ) {
            MessagesArea(group = groupDetail, modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .navigationBarsPadding() // Add padding for navigation bars if needed
            ) {
                OutlinedTextField(
                    value = inputValue,
                    onValueChange = { inputValue = it },
                    modifier = Modifier
                        .weight(1f) // Use weight to fill available space
                        .padding(end = 8.dp),
                    placeholder = { Text(text = "Type a message or amount") }
                )

                Button(
                    onClick = {
                        if (inputValue.isDigitsOnly() && inputValue.isNotBlank()) navController.navigate(
                            Routes.createSplitScreen(groupDetail.groupId, inputValue.toInt())
                        ) else Toast.makeText(
                            context,
                            "Sending messages will be available soon",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Text(text = if (inputValue.isDigitsOnly() && inputValue.isNotBlank()) "Split" else "Send")
                }
            }
        }
    }
}


@Composable
fun MessagesArea(group: SplitGroup, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        if (group.expenseSplits.isEmpty()) {
            Text(text = "You don't have any messages")
        } else {
            LazyColumn {
                items(group.expenseSplits) { expense ->
                    MessageItem(expenseSplit = expense)
                }
            }
        }
    }
}

@Composable
fun MessageItem(expenseSplit: ExpenseSplit) {
    val isCurrentUser = expenseSplit.createdBy.uid == Firebase.auth.uid
    val unpaidMembers = expenseSplit.splitDetails.filter { member -> member.isPaid }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .background(
                    color = if (isCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(16.dp)
//                .shadow(4.dp)
                .wrapContentHeight()
        ) {
            Column {
                Text(
                    text = "$${expenseSplit.totalAmount}", fontSize = 28.sp,
                    color = if (isCurrentUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.inversePrimary
                )
                Spacer(modifier = Modifier.padding(4.dp))
                Text(
                    text = expenseSplit.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isCurrentUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.inversePrimary
                )
                Text(
                    text = if (unpaidMembers.isEmpty()) "All paid" else "${unpaidMembers.size} remaining",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isCurrentUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.inversePrimary
                )
            }

        }
    }
}

@Composable
@Preview
fun GMSPreview() {
    GroupMessagesScreen(navController = rememberNavController(), groupId = "Test")
}