package com.myapps.splitwiseclone.ui.screens.home.groups.split.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import com.myapps.splitwiseclone.DatabaseKeys
import com.myapps.splitwiseclone.constants.SplitModes
import com.myapps.splitwiseclone.models.ExpenseSplit
import com.myapps.splitwiseclone.models.ScheduledSplit
import com.myapps.splitwiseclone.models.SplitGroup
import com.myapps.splitwiseclone.models.UserAccount
import com.myapps.splitwiseclone.server.ServerMocker.Companion.addDaysToCurrentTimeInMillis
import com.myapps.splitwiseclone.ui.Routes
import com.myapps.splitwiseclone.ui.screens.home.groups.split.helper.SplitsHelper.Companion.calculateSplitDetailsForMembers

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringValuesInput(
    recurringTimes: Int,
    recurringIntervalInDays: Int,
    onRecurringTimesSelected: (Int) -> Unit,
    onRecurringIntervalSelected: (Int) -> Unit
) {

    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(.5f),
            value = recurringTimes.toString(),
            onValueChange = {
                if (it.isNotBlank()) {
                    onRecurringTimesSelected(it.toInt())
                } else {
                    onRecurringTimesSelected(1)
                }
            },
            label = { Text(text = "Times to repeat") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        Spacer(modifier = Modifier.padding(10.dp))
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(.8f),
            value = recurringIntervalInDays.toString(),
            onValueChange = {
                if (it.isNotBlank()) {
                    onRecurringIntervalSelected(it.toInt())
                } else {
                    onRecurringIntervalSelected(1)
                }
            },
            label = { Text(text = "Frequency (days)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
    }
}

fun createRecurringSplit(
    message: String,
    amount: Int,
    groupMembers: List<UserAccount>,
    splitValues: MutableState<MutableMap<String, Int>>,
    groupDetail: SplitGroup,
    navController: NavHostController,
    setIsLoading: (Boolean) -> Unit,
    context: Context,
    splitMode: String,
    selectedDate: String,
    recurringTimes: Int,
    recurringInterval: Int
) {
    Firebase.database.reference.child(DatabaseKeys.userAccounts).child(Firebase.auth.uid.toString())
        .get()
        .addOnSuccessListener { it ->
            val expenseSplit = ExpenseSplit()
            expenseSplit.createdAt = System.currentTimeMillis()
            expenseSplit.message = message
            expenseSplit.createdBy = it.getValue(UserAccount::class.java)!!
            expenseSplit.totalAmount = amount.toDouble()

            expenseSplit.splitDetails = calculateSplitDetailsForMembers(groupMembers,splitValues)

            val scheduledSplit = ScheduledSplit()
            scheduledSplit.splitMode = splitMode
            scheduledSplit.expenseSplit = expenseSplit
            scheduledSplit.createdAt = System.currentTimeMillis()

            scheduledSplit.triggerTime = addDaysToCurrentTimeInMillis(recurringInterval)

            val scheduleRef =
                Firebase.database.reference.child(DatabaseKeys.schedules).child(groupDetail.groupId)
                    .push()
            scheduledSplit.scheduledSplitId = scheduleRef.key.toString()
            scheduledSplit.timesToExecute = recurringTimes
            scheduledSplit.triggerInterval = recurringInterval
            scheduledSplit.splitMode = SplitModes.Recurring

            scheduleRef.setValue(scheduledSplit).addOnSuccessListener {
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