package com.myapps.splitwiseclone.ui.screens.home.groups.split.create

import android.app.DatePickerDialog
import android.content.Context
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
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
import com.myapps.splitwiseclone.constants.SplitModes
import com.myapps.splitwiseclone.constants.SplitTypes
import com.myapps.splitwiseclone.helpers.DateHelper.Companion.convertDateStringToMillis
import com.myapps.splitwiseclone.helpers.fetchObjectsByIds
import com.myapps.splitwiseclone.models.ExpenseSplit
import com.myapps.splitwiseclone.models.ScheduledSplit
import com.myapps.splitwiseclone.models.SplitGroup
import com.myapps.splitwiseclone.models.UserAccount
import com.myapps.splitwiseclone.ui.Routes
import com.myapps.splitwiseclone.ui.screens.home.groups.split.components.RecurringValuesInput
import com.myapps.splitwiseclone.ui.screens.home.groups.split.components.createRecurringSplit
import com.myapps.splitwiseclone.ui.screens.home.groups.split.helper.SplitsHelper
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
                if (it.isEmpty()) {
                    Toast.makeText(context, "No members", Toast.LENGTH_SHORT).show()
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

val MANUAL = "manual"
val PERCENTAGE = "percentage"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSplitScreenContent(
    navController: NavHostController,
    groupDetail: SplitGroup,
    amount: Int,
    groupMembers: List<UserAccount>
) {

    val splitValues = remember { mutableStateOf(mutableMapOf<String, Int>()) }

    val splitType = MANUAL

    var selectedDate by remember {
        mutableStateOf("")
    }

    //Split types are single, recurring, schedule
    var splitMode by remember {
        mutableStateOf(SplitModes.Single)
    }

    var recurringTimes by remember {
        mutableStateOf(1)
    }
    var recurringIntervalInDays by remember {
        mutableStateOf(1)
    }

    LaunchedEffect(groupMembers) {
        if (groupMembers.isNotEmpty()) {
            val initialEqualAmount = amount / groupMembers.size
            val remainder = amount % groupMembers.size
            val tempSplitValues = mutableMapOf<String, Int>()

            groupMembers.forEach { entry ->
                tempSplitValues[entry.uid] = initialEqualAmount
            }
            tempSplitValues[groupMembers.last().uid] = initialEqualAmount + remainder

            splitValues.value = tempSplitValues
        }
    }


    val context = LocalContext.current
    var message by remember {
        mutableStateOf("")
    }

    var isLoading by remember {
        mutableStateOf(false)
    }

    var splittingType by remember {
        mutableStateOf(SplitTypes.BY_ABSOLUTE)
    }

    Column {
        SplitModeDropdownMenuBox(selectedValue = splitMode) {
            splitMode = it
        }
        if (splitMode == SplitModes.Scheduled) {
            DateSelectionInput(selectedDate = selectedDate) {
                selectedDate = it
            }
        }
        if (splitMode == SplitModes.Recurring) {
            RecurringValuesInput(
                recurringTimes,
                recurringIntervalInDays,
                onRecurringTimesSelected = {
                    recurringTimes = it
                },
                onRecurringIntervalSelected = {
                    recurringIntervalInDays = it
                })
        }
        Spacer(modifier = Modifier.padding(20.dp))
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
        Text(text = "Split By", modifier = Modifier.padding(8.dp))
        Row {
            FilterChip(
                onClick = {
                    splittingType = SplitTypes.BY_ABSOLUTE
                },
                label = { Text(text = "Absolute Value") },
                modifier = Modifier
                    .padding(8.dp),
                selected = splittingType == SplitTypes.BY_ABSOLUTE
            )
            FilterChip(
                onClick = {
                    splittingType = SplitTypes.BY_PERCENTAGE
                },
                label = { Text(text = "Percentage") },
                modifier = Modifier
                    .padding(8.dp),
                selected = splittingType == SplitTypes.BY_PERCENTAGE

            )
        }

        Spacer(modifier = Modifier.padding(16.dp))
        Text(
            text = "Select members", modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )



        Box(modifier = Modifier.weight(1f)) {
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
                                Column(modifier = Modifier.fillMaxWidth(.7f)) {
                                    Spacer(modifier = Modifier.padding(4.dp))
                                    Text(
                                        text = it.fullName,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = it.email,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.padding(4.dp))
                                }
                                Text(text = if (splittingType == SplitTypes.BY_PERCENTAGE) "%" else "")
                                OutlinedTextField(
                                    value = splitValues.value[it.uid].toString(),
                                    onValueChange = { newValue ->
                                        if (newValue.isNotBlank()) {
                                            splitValues.value =
                                                splitValues.value.toMutableMap().apply {
                                                    this[it.uid] = newValue.toInt()
                                                }
                                        } else {
                                            splitValues.value =
                                                splitValues.value.toMutableMap().apply {
                                                    this[it.uid] = 0
                                                }
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier
                                        .wrapContentWidth()
                                        .align(Alignment.CenterVertically)
                                        .padding(6.dp),
                                    placeholder = { Text("0") },
                                    singleLine = true
                                )
                            }
                        }
                    }
                }
            })
        }
        if (isLoading) {
            LinearProgressIndicator()
        }
        ElevatedButton(
            onClick = {
                if (validateInputs(
                        splitValues = splitValues,
                        amount = amount,
                        context = context,
                        splitMode = splitMode,
                        selectedDate = selectedDate,
                        splitTypes = splittingType
                    )
                ) return@ElevatedButton

                if(splittingType == SplitTypes.BY_PERCENTAGE){
                    val percentageToAbsolute = HashMap<String,Int>()
                    splitValues.value.forEach{
                        percentageToAbsolute[it.key] = (amount/100) * it.value
                    }
                    splitValues.value = percentageToAbsolute
                }

                createNewSplitInGroup(
                    message = message,
                    amount = amount,
                    groupMembers = groupMembers,
                    splitValues = splitValues,
                    groupDetail = groupDetail,
                    navController = navController,
                    context = context,
                    splitMode = splitMode,
                    selectedDate = selectedDate,
                    setIsLoading = {
                        isLoading = it
                    },
                    recurringIntervalInDays = recurringIntervalInDays,
                    recurringTimes = recurringTimes,
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

private fun validateInputs(
    splitValues: MutableState<MutableMap<String, Int>>,
    amount: Int,
    splitMode: String,
    selectedDate: String,
    context: Context,
    splitTypes: String
): Boolean {
    var totalAmount = 0
    splitValues.value.forEach {
        totalAmount += it.value
    }

    if (splitMode == SplitModes.Scheduled && selectedDate.isEmpty()) {
        Toast.makeText(
            context,
            "Kindly select a date to schedule",
            Toast.LENGTH_SHORT
        ).show()
        return true
    }

    if(splitTypes == SplitTypes.BY_PERCENTAGE && totalAmount != 100){
        Toast.makeText(
            context,
            "Percentage ${if (totalAmount < 100) "${100 - totalAmount} is less" else "${totalAmount - 100} is more"}",
            Toast.LENGTH_SHORT
        ).show()
        return true
    }

    if (splitTypes == SplitTypes.BY_ABSOLUTE && totalAmount != amount) {
        Toast.makeText(
            context,
            "Amount ${if (totalAmount < amount) "$${amount - totalAmount} is less" else "$${totalAmount - amount} is more"}",
            Toast.LENGTH_SHORT
        ).show()
        return true
    }

    return false
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitModeDropdownMenuBox(selectedValue: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.padding(12.dp)
    ) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = { onSelected(it) },
            label = { Text("Split Mode") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            readOnly = true
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Single") },
                onClick = {
                    onSelected(SplitModes.Single)
                    expanded = false
                },
                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
            )
            DropdownMenuItem(
                text = { Text("Scheduled") },
                onClick = {
                    onSelected(SplitModes.Scheduled)
                    expanded = false
                },
                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
            )
            DropdownMenuItem(
                text = { Text("Recurring") },
                onClick = {
                    onSelected(SplitModes.Recurring)
                    expanded = false
                },
                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExposedDropdownMenuBoxPreview() {
    SplitModeDropdownMenuBox(selectedValue = SplitModes.Single) {
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSelectionInput(selectedDate: String, onDateSelected: (String) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val dateFormatter = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    }

    val dateSetListener =
        DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, month: Int, day: Int ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)
            onDateSelected(dateFormatter.format(calendar.time))
        }

    val datePickerDialog = DatePickerDialog(
        context,
        dateSetListener,
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = selectedDate,
            onValueChange = { onDateSelected(it) },
            readOnly = true, // Make the TextField read-only
            label = { Text("Select Trigger Date") },
            trailingIcon = {
                IconButton(onClick = { datePickerDialog.show() }) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_today),
                        contentDescription = "Select Date"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DateSelectionInputPreview() {
    DateSelectionInput("1999-08-20") {

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
    setIsLoading: (Boolean) -> Unit,
    splitMode: String,
    selectedDate: String,
    recurringTimes: Int,
    recurringIntervalInDays: Int
) {
    setIsLoading(true)
    when (splitMode) {
        SplitModes.Single -> {
            createSingleSplit(
                message,
                amount,
                groupMembers,
                splitValues,
                groupDetail,
                navController,
                setIsLoading,
                context
            )
        }

        SplitModes.Scheduled -> {
            createScheduledSplit(
                message,
                amount,
                groupMembers,
                splitValues,
                groupDetail,
                navController,
                setIsLoading,
                context,
                splitMode,
                selectedDate
            )
        }

        SplitModes.Recurring -> {
            createRecurringSplit(
                message,
                amount,
                groupMembers,
                splitValues,
                groupDetail,
                navController,
                setIsLoading,
                context,
                splitMode,
                selectedDate,
                recurringTimes,
                recurringIntervalInDays
            )
        }
    }
}

fun createScheduledSplit(
    message: String,
    amount: Int,
    groupMembers: List<UserAccount>,
    splitValues: MutableState<MutableMap<String, Int>>,
    groupDetail: SplitGroup,
    navController: NavHostController,
    setIsLoading: (Boolean) -> Unit,
    context: Context,
    splitMode: String,
    selectedDate: String
) {

    Firebase.database.reference.child(DatabaseKeys.userAccounts).child(Firebase.auth.uid.toString())
        .get()
        .addOnSuccessListener { it ->
            val expenseSplit = ExpenseSplit()
            expenseSplit.createdAt = System.currentTimeMillis()
            expenseSplit.message = message
            expenseSplit.createdBy = it.getValue(UserAccount::class.java)!!
            expenseSplit.totalAmount = amount.toDouble()

            expenseSplit.splitDetails =
                SplitsHelper.calculateSplitDetailsForMembers(groupMembers, splitValues)

            val scheduledSplit = ScheduledSplit()
            scheduledSplit.splitMode = splitMode
            scheduledSplit.expenseSplit = expenseSplit
            scheduledSplit.createdAt = System.currentTimeMillis()
            scheduledSplit.triggerTime = convertDateStringToMillis(selectedDate)
            val scheduleRef =
                Firebase.database.reference.child(DatabaseKeys.schedules).child(groupDetail.groupId)
                    .push()
            scheduledSplit.scheduledSplitId = scheduleRef.key.toString()

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

private fun createSingleSplit(
    message: String,
    amount: Int,
    groupMembers: List<UserAccount>,
    splitValues: MutableState<MutableMap<String, Int>>,
    groupDetail: SplitGroup,
    navController: NavHostController,
    setIsLoading: (Boolean) -> Unit,
    context: Context
) {
    Firebase.database.reference.child(DatabaseKeys.userAccounts).child(Firebase.auth.uid.toString())
        .get()
        .addOnSuccessListener { it ->
            val expenseSplit = ExpenseSplit()
            expenseSplit.createdAt = System.currentTimeMillis()
            expenseSplit.message = message
            expenseSplit.createdBy = it.getValue(UserAccount::class.java)!!
            expenseSplit.totalAmount = amount.toDouble()

            expenseSplit.splitDetails =
                SplitsHelper.calculateSplitDetailsForMembers(groupMembers, splitValues)

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