package com.myapps.splitwiseclone.ui.screens.home.groups.split

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import com.google.firebase.Firebase
import com.google.firebase.database.database
import com.myapps.splitwiseclone.R
import com.myapps.splitwiseclone.models.SplitGroup
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSplitScreen(navController: NavHostController, groupId: String?, amount: Any?) {

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
                Text(text = amount.toString())
                CreateSplitScreenContent(navController = navController, groupDetail = groupDetail)
            }
        }
    )
}

@Composable
fun CreateSplitScreenContent(navController: NavHostController, groupDetail: SplitGroup) {
    LazyColumn(content = {
        groupDetail.groupMembers.forEach{

        }
    })
}