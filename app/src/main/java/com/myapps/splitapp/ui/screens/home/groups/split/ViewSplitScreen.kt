package com.myapps.splitapp.ui.screens.home.groups.split

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import com.myapps.splitapp.DatabaseKeys
import com.myapps.splitapp.R
import com.myapps.splitapp.models.ExpenseSplit
import kotlinx.coroutines.tasks.await


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewSplitScreen(navController: NavController, splitId: String?, groupId: String?) {

    var expenseSplit by remember {
        mutableStateOf(ExpenseSplit())
    }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        try {
            val snapshot =
                Firebase.database.reference.child(DatabaseKeys.splits).child(groupId.toString())
                    .child(splitId.toString()).get().await()
            expenseSplit = snapshot.getValue(ExpenseSplit::class.java)!!
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to fetch split details", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Split Details") },
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
                if (expenseSplit.totalAmount.toInt() != 0)
                    ViewSplitScreenContent(navController, expenseSplit, groupId.toString())
            }
        }
    )
}

@Composable
fun ViewSplitScreenContent(
    navController: NavController,
    expenseSplit: ExpenseSplit,
    groupId: String
) {

    val context = LocalContext.current

    Column {
        Spacer(modifier = Modifier.padding(20.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "Split amount")
            Spacer(modifier = Modifier.padding(6.dp))
            Text(text = "${expenseSplit.totalAmount}", fontSize = 34.sp)
            Spacer(modifier = Modifier.padding(6.dp))
            LazyColumn(modifier = Modifier.fillMaxSize(), content = {
                items(expenseSplit.splitDetails) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, start = 12.dp, end = 12.dp)
                        ) {
                            Text(
                                text = "${it.userAccount.fullName} ${if (expenseSplit.createdBy.uid == it.userAccount.uid) "(Owner)" else ""}",
                                fontWeight = FontWeight.SemiBold
                            )
                            if (expenseSplit.createdBy.uid == it.userAccount.uid)
                                Text(text = if (it.isPaid) "Paid" else "Unpaid")
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp, start = 12.dp, end = 12.dp)
                        ) {
                            Text(text = "Amount : ${it.amount}")
                            if (!it.isPaid && expenseSplit.createdBy.uid == Firebase.auth.currentUser?.uid) {
                                TextButton(onClick = {

                                    it.isPaid = true

                                    Firebase.database.reference.child(DatabaseKeys.splits)
                                        .child(groupId).child(expenseSplit.expenseSplitId)
                                        .setValue(expenseSplit).addOnCompleteListener {
                                            if (it.isSuccessful) {
                                                Toast.makeText(
                                                    context,
                                                    "Marked as paid",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "Failed",
                                                    Toast.LENGTH_SHORT
                                                ).show()

                                            }
                                        }

                                }) {
                                    Text(text = "Mark as paid")
                                }
                            }
                        }
                    }
                }
            })

        }
    }
}