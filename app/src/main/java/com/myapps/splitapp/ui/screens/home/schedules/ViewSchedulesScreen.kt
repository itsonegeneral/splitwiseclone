package com.myapps.splitapp.ui.screens.home.schedules

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.database.database
import com.myapps.splitapp.DatabaseKeys
import com.myapps.splitapp.R
import com.myapps.splitapp.models.ScheduledSplit
import com.myapps.splitapp.models.SplitGroup
import com.myapps.splitapp.ui.components.CustomLoading
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewSchedulesScreen(navController: NavController, groupId: String?) {

    var splitGroup by remember {
        mutableStateOf(SplitGroup())
    }
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        try {
            isLoading = true
            val snapshot =
                Firebase.database.reference.child("groups").child(groupId.toString()).get().await()
            splitGroup = snapshot.getValue(SplitGroup::class.java)!!
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to fetch group details", Toast.LENGTH_SHORT).show()
        }
        isLoading = false
    }



    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${splitGroup.groupName} Schedules") },
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
            )
        },
        content = {
            Box(modifier = Modifier.padding(it)) {
                ViewSchedulesScreenContent(groupId = groupId!!)
            }
        }
    )
}


@Composable
fun ViewSchedulesScreenContent(groupId: String) {

    var schedules by remember {
        mutableStateOf(listOf<ScheduledSplit>())
    }

    var isLoading by remember {
        mutableStateOf(false)
    }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        refreshSchedules(
            setIsLoading = { isLoading = it },
            setSchedules = { schedules = it },
            groupId,
            context
        )
    }


    if (isLoading) {
        CustomLoading("Loading schedules")
        return
    }

    LazyColumn(content = {
        items(schedules) { schedule ->
            ScheduleItem(schedule = schedule, groupId) { deletedId ->
                schedules =  schedules.filter { it.scheduledSplitId != deletedId}
            }
        }
    })
}

private suspend fun refreshSchedules(
    setIsLoading: (Boolean) -> Unit,
    setSchedules: (List<ScheduledSplit>) -> Unit,
    groupId: String,
    context: Context
) {
    setIsLoading(true)
    try {
        val snapshot =
            Firebase.database.reference.child(DatabaseKeys.schedules).child(groupId).get()
                .await()
        setSchedules(snapshot.children.mapNotNull { it.getValue(ScheduledSplit::class.java) })
    } catch (e: Exception) {
        Toast.makeText(context, "Unable to fetch schedules ${e.message}", Toast.LENGTH_SHORT)
            .show()
    }
    setIsLoading(false)
}

@Composable
fun ScheduleItem(schedule: ScheduledSplit, groupId: String, onScheduleDeleted: (String) -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .padding(8.dp)
            .shadow(2.dp, shape = RoundedCornerShape(4.dp))
            .fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp, start = 12.dp,end = 8.dp)) {
            Text(text = "Type : ${schedule.splitMode}", modifier = Modifier.weight(1f))
            IconButton(onClick = {
                Firebase.database.reference.child(DatabaseKeys.schedules).child(groupId).child(schedule.scheduledSplitId).removeValue().addOnCompleteListener {
                    if(it.isSuccessful){
                        onScheduleDeleted(schedule.scheduledSplitId)
                    }else{
                        Toast.makeText(context,"Unable to delete, try again",Toast.LENGTH_SHORT).show()
                    }
                }
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_cancel_24),
                    contentDescription = "Cancel",
                    tint = Color(0xffFF8080)
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp, start = 12.dp,end = 8.dp)
        ) {
            Text(
                text = "Message : ${schedule.expenseSplit.message}",
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Amount : $${schedule.expenseSplit.totalAmount}",
                modifier = Modifier.weight(1f)
            )
        }
       Spacer(modifier = Modifier.padding(4.dp))
    }

}
