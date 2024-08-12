package com.myapps.splitwiseclone.ui.screens.home.groups.messages

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.solver.widgets.Rectangle
import androidx.core.text.isDigitsOnly
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import com.myapps.splitwiseclone.DatabaseKeys
import com.myapps.splitwiseclone.R
import com.myapps.splitwiseclone.models.ExpenseSplit
import com.myapps.splitwiseclone.models.ScheduledSplit
import com.myapps.splitwiseclone.models.SplitDetail
import com.myapps.splitwiseclone.models.SplitGroup
import com.myapps.splitwiseclone.ui.Routes
import com.myapps.splitwiseclone.ui.components.CustomLoading
import com.myapps.splitwiseclone.ui.components.KeyboardAware
import kotlinx.coroutines.tasks.await


private const val TAG = "GroupMessagesScreen"

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupMessagesScreen(navController: NavHostController, groupId: String?) {

    var menuExpanded by remember { mutableStateOf(false) }


    var splitGroup by remember {
        mutableStateOf(SplitGroup())
    }
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        try {
            val snapshot =
                Firebase.database.reference.child("groups").child(groupId.toString()).get().await()
            splitGroup = snapshot.getValue(SplitGroup::class.java)!!

        } catch (e: Exception) {
            Toast.makeText(context, "Unable to fetch group details", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${splitGroup.groupName} Splits") },
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
                },
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
                            text = { Text(text = "Edit Group") },
                            onClick = {
                                menuExpanded = false
                                // Handle edit group action here
                                navController.navigate(Routes.groupEditScreen((groupId!!)))
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(text = "Exit Group") },
                            onClick = {
                                menuExpanded = false
                                splitGroup.groupMembers.remove(Firebase.auth.uid)
                                Firebase.database.reference.child(DatabaseKeys.splitGroups)
                                    .child(splitGroup.groupId).setValue(splitGroup)
                                    .addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            Toast.makeText(
                                                context,
                                                "Exited group",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            navController.navigate(Routes.homeScreen)
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Unable to exit group,try again",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                            }
                        )
                    }
                }
            )
        },
        content = {
            Box(modifier = Modifier.padding(it)) {
                GroupMessagesScreenContent(navController = navController, groupId = groupId!!)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupMessagesScreenContent(navController: NavHostController, groupId: String) {
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
            SchedulesNotificationArea(groupId, navController)
            MessagesArea(groupId = groupId, modifier = Modifier.weight(1f), navController)
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
                    placeholder = { Text(text = "Type amount") }
                )

                Button(
                    onClick = {
                        if (inputValue.isDigitsOnly() && inputValue.isNotBlank()) navController.navigate(
                            Routes.createSplitScreen(groupId, inputValue.toInt())
                        ) else Toast.makeText(
                            context,
                            "Invalid split amount",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Text(text = if (inputValue.isDigitsOnly() && inputValue.isNotBlank()) "Split" else "Split")
                }
            }
        }
    }
}

@Composable
fun SchedulesNotificationArea(groupId: String, navController: NavController) {
    var schedules by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        try {
            val snapshot =
                Firebase.database.reference.child(DatabaseKeys.schedules).child(groupId).get()
                    .await()
            val schedulesList =
                snapshot.children.mapNotNull { it.getValue(ScheduledSplit::class.java) }
            schedules = schedulesList.size
        } catch (e: Exception) {

        }
    }

    if (schedules > 0) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xffa8e4ff))
                .padding(8.dp)
                .border(3.dp, Color(0xffa8e4ff), RoundedCornerShape(8.dp)),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "You have $schedules active schedule",
                textAlign = TextAlign.Center
            )
            ClickableText(text = AnnotatedString("View"), onClick = {
                navController.navigate(Routes.groupSchedulesScreen(groupId))
                Log.d(TAG, "SchedulesNotificationArea: clicked view")
            }, style = TextStyle(color = Color.Blue), modifier = Modifier.padding(6.dp))
        }
    }
}


@Composable
fun MessagesArea(groupId: String, modifier: Modifier = Modifier, navController: NavController) {
    var isLoading by remember { mutableStateOf(false) }
    var splits by remember { mutableStateOf(listOf<ExpenseSplit>()) }
    val context = LocalContext.current
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val snapshot = Firebase.database.reference.child("splits").child(groupId).get().await()
            splits = snapshot.children.mapNotNull { it.getValue(ExpenseSplit::class.java) }
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to fetch group details", Toast.LENGTH_SHORT).show()
        }
        isLoading = false
    }

    // Scroll to the bottom whenever splits change
    LaunchedEffect(splits) {
        if (splits.isNotEmpty()) {
            listState.animateScrollToItem(splits.size - 1)
        }
    }

    if (isLoading) {
        CustomLoading()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        if (splits.isEmpty() && !isLoading) {
            Text(text = "You don't have any messages")
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                items(splits) { split ->
                    MessageItem(navController, split = split, groupId = groupId) { splitId ->
                        navController.navigate(Routes.viewSplitScreen(splitId, groupId))
                    }
                }
            }
        }
    }
}

@Composable
fun MessageItem(
    navController: NavController,
    split: ExpenseSplit,
    groupId: String,
    onItemClick: (String) -> Unit
) {
    val isCurrentUser = split.createdBy.uid == Firebase.auth.uid
    val unpaidMembers = remember(split.splitDetails) {
        split.splitDetails.filter { member -> !member.isPaid }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable {
                onItemClick(split.expenseSplitId)
            },
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start,

        ) {
        Column {
            Text(
                text = if (isCurrentUser) "You" else split.createdBy.fullName, modifier = Modifier
                    .padding(8.dp)
                    .align(if (isCurrentUser) Alignment.End else Alignment.Start)
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .background(
                        color = if (isCurrentUser) MaterialTheme.colorScheme.primary else Color(
                            0xff5e9fff
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(16.dp)
                    .wrapContentHeight()
            ) {
                Column {
                    SplitDetailMessage(navController, split, isCurrentUser, unpaidMembers, groupId)
                }

            }
        }
    }
}

@Composable
private fun SplitDetailMessage(
    navController: NavController,
    split: ExpenseSplit,
    isCurrentUser: Boolean,
    unpaidMembers: List<SplitDetail>,
    groupId: String
) {
    if (split.message.isNotBlank()) {
        Text(
            text = "For '${split.message}'",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isCurrentUser) MaterialTheme.colorScheme.onPrimary else Color.White
        )
        Spacer(modifier = Modifier.padding(4.dp))
    }
    Text(
        text = "$${split.totalAmount}", fontSize = 28.sp,
        color = if (isCurrentUser) MaterialTheme.colorScheme.onPrimary else Color.White
    )
    Spacer(modifier = Modifier.padding(4.dp))
    if (Firebase.auth.uid.toString() == split.createdBy.uid) {
        Text(
            text = if (unpaidMembers.isEmpty()) "All paid" else "${unpaidMembers.size} unpaid",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isCurrentUser) MaterialTheme.colorScheme.onPrimary else Color.White
        )
    }
    var isMemeberInSplit = false
    split.splitDetails.forEach {
        if (it.userAccount.uid == Firebase.auth.uid && it.isPaid) {
            Text(text = "You paid", color = Color.White)
            isMemeberInSplit = true
        }
        if (it.userAccount.uid == Firebase.auth.uid && !it.isPaid && split.createdBy.uid != Firebase.auth.uid) {
            isMemeberInSplit = true
            PayButton(navController, split, groupId, it.amount)
            Log.d(TAG, "SplitDetailMessage: Self user ${it.amount}")
        }
    }
    if (!isMemeberInSplit && Firebase.auth.uid != split.createdBy.uid) {
        Text(text = "No due", color = Color.White)
    }
}

@Composable
private fun PayButton(
    navController: NavController,
    split: ExpenseSplit,
    groupId: String,
    payAmount: Double
) {
    val context = LocalContext.current
    Button(onClick = {
        val updatedExpenseSplits = split.splitDetails.map { splitDetail ->
            if (splitDetail.userAccount.uid == Firebase.auth.uid) {
                splitDetail.copy(isPaid = true)
            } else {
                splitDetail
            }
        }
        Firebase.database.reference.child("splits").child(groupId)
            .child(split.expenseSplitId).child("splitDetails").setValue(updatedExpenseSplits)
            .addOnCompleteListener {
                Toast.makeText(context, "Paid", Toast.LENGTH_SHORT).show()
                navController.navigate(
                    Routes.paymentSuccessScreen(
                        payAmount.toInt(),
                        split.createdBy.fullName,
                        split.message
                    )
                )
            }.addOnFailureListener {
                Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
            }
    }) {
        Text(text = "Pay $$payAmount")
    }
}

@Composable
@Preview
fun GMSPreview() {
    GroupMessagesScreen(navController = rememberNavController(), groupId = "Test")
}