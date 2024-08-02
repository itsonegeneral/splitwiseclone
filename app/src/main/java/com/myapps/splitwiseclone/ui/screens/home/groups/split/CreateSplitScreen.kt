package com.myapps.splitwiseclone.ui.screens.home.groups.split

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import com.myapps.splitwiseclone.DatabaseKeys
import com.myapps.splitwiseclone.R
import com.myapps.splitwiseclone.helpers.fetchObjectsByIds
import com.myapps.splitwiseclone.models.ExpenseSplit
import com.myapps.splitwiseclone.models.SplitDetail
import com.myapps.splitwiseclone.models.SplitGroup
import com.myapps.splitwiseclone.models.UserAccount
import com.myapps.splitwiseclone.ui.Routes
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSplitScreen(navController: NavHostController, groupId: String?, amount: Int?) {

    var groupDetail by remember {
        mutableStateOf(SplitGroup())
    }

    var groupMembers by remember {
        mutableStateOf(listOf<UserAccount>())
    }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        try {
            val snapshot =
                Firebase.database.reference.child("groups").child(groupId!!).get().await()
            groupDetail = snapshot.getValue(SplitGroup::class.java)!!
            fetchObjectsByIds(groupDetail.groupMembers) {
                if(it.isEmpty()){
                    Toast.makeText(context,"No members",Toast.LENGTH_SHORT).show()
                    return@fetchObjectsByIds
                }
                groupMembers = it.values.toList()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to fetch group details", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Split") },
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
                CreateSplitScreenContent(
                    navController = navController,
                    groupDetail = groupDetail,
                    amount = amount!!,
                    groupMembers = groupMembers
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSplitScreenContent(
    navController: NavHostController,
    groupDetail: SplitGroup,
    amount: Int,
    groupMembers: List<UserAccount>
) {

    val splitValues = remember { mutableStateOf(mutableMapOf<String, Int>()) }


    if(groupMembers.isNotEmpty()){
        val initialEqualAmount = amount.div((groupMembers.size))
        groupMembers.forEach { entry ->
            splitValues.value[entry.uid] = initialEqualAmount
        }
    }


    val context = LocalContext.current
    var message by remember {
        mutableStateOf("")
    }

    var isLoading by remember {
        mutableStateOf(false)
    }

    Column {
        Spacer(modifier = Modifier.padding(30.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "Split amount")
            Spacer(modifier = Modifier.padding(6.dp))
            Text(text = "$$amount", fontSize = 34.sp)
            Spacer(modifier = Modifier.padding(6.dp))
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier.fillMaxWidth(.5f),
                placeholder = {
                    Text(
                        text = "Message"
                    )
                }, singleLine = true
            )
        }
        Spacer(modifier = Modifier.padding(16.dp))
        Text(
            text = "Select members", modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        LazyColumn(content = {
            groupMembers.forEach {
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
                            OutlinedTextField(
                                value = splitValues.value[it.uid].toString(),
                                onValueChange = { newValue ->
                                    if (newValue.isNotBlank()) {
                                        splitValues.value = splitValues.value.toMutableMap().apply {
                                            this[it.uid] = newValue.toInt()
                                        }
                                    } else {
                                        splitValues.value = splitValues.value.toMutableMap().apply {
                                            this[it.uid] = 0
                                        }
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .align(Alignment.CenterVertically),
                                placeholder = { Text("0") },
                                singleLine = true
                            )
                        }
                    }
                }
            }
        })
        if (isLoading) {
            LinearProgressIndicator()
        }
        ElevatedButton(
            onClick = {
                createNewSplitInGroup(
                    message,
                    amount,
                    groupMembers,
                    splitValues,
                    groupDetail,
                    navController,
                    context,
                    setIsLoading = {
                        isLoading = it
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
        ) {
            Text(text = "Send Splits")
        }
    }
}


private fun createNewSplitInGroup(
    message: String,
    amount: Int,
    groupMembers: List<UserAccount>,
    splitValues: MutableState<MutableMap<String, Int>>,
    groupDetail: SplitGroup,
    navController: NavHostController,
    context: Context,
    setIsLoading: (Boolean) -> Unit
) {
    setIsLoading(true)
    Firebase.database.reference.child(DatabaseKeys.userAccounts).child(Firebase.auth.uid.toString()).get()
        .addOnSuccessListener { it ->
            val expenseSplit = ExpenseSplit()
            expenseSplit.createdAt = System.currentTimeMillis()
            expenseSplit.message = message
            expenseSplit.createdBy = it.getValue(UserAccount::class.java)!!
            expenseSplit.totalAmount = amount.toDouble()
            val splitDetails = ArrayList<SplitDetail>()

            groupMembers.forEach { user ->
                if (splitValues.value.containsKey(user.uid)) {
                    val splitDetail = SplitDetail()
                    splitDetail.amount = splitValues.value[user.uid]!!.toDouble()
                    splitDetail.isPaid = false
                    splitDetail.userAccount = user
                    splitDetails.add(splitDetail)
                }
            }

            expenseSplit.splitDetails = splitDetails

            val splitRef =
                Firebase.database.reference.child("splits").child(groupDetail.groupId)
                    .push()
            expenseSplit.expenseSplitId = splitRef.key.toString()

            splitRef.setValue(expenseSplit).addOnSuccessListener {
                navController.navigate(Routes.groupMessagesScreen(groupId = groupDetail.groupId))
                setIsLoading(false)
            }.addOnFailureListener {
                Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                setIsLoading(false)
            }

        }.addOnFailureListener {
            setIsLoading(false)
            Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
        }
}



@Composable
@Preview
fun CSSPreview() {
    CreateSplitScreen(navController = rememberNavController(), groupId = "sad", amount = 11)
}