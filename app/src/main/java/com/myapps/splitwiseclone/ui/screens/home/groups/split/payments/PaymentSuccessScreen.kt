package com.myapps.splitwiseclone.ui.screens.home.groups.split.payments

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.rememberNavController
import com.myapps.splitwiseclone.R
import com.myapps.splitwiseclone.ui.Routes
import java.text.SimpleDateFormat
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentSuccessScreen(
    navController: NavController,
    amount: Int?,
    paidTo: String?,
    message: String?
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment Success") },
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
                PaymentSuccessScreenContent(
                    navController = navController,
                    amount = amount!!,
                    paidTo = paidTo.toString(),
                    message = message.toString()
                )
            }
        }
    )
}

@SuppressLint("SimpleDateFormat")
@Composable
fun PaymentSuccessScreenContent(
    navController: NavController,
    amount: Int,
    paidTo: String,
    message: String
) {
    Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
        Column(
            Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_check_24),
                contentDescription = "Done",
                tint = Color(0xff009688),
                modifier = Modifier
                    .width(80.dp)
                    .height(80.dp)
            )
            Text(text = "Payment Success", fontSize = 24.sp)
            Spacer(modifier = Modifier.padding(8.dp))
            Text(
                text = if (message.isNotBlank()) {
                    "Paid to $paidTo for $message"
                } else {
                    "Paid to $paidTo"
                }
            )
            Spacer(modifier = Modifier.padding(4.dp))
            Text(text = "Date : ${SimpleDateFormat("dd-MMM-yyyy HH:mm").format(Calendar.getInstance().time)}")
            Spacer(modifier = Modifier.padding(16.dp))
            Button(onClick = {
                navController.navigate(Routes.homeScreen) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        inclusive = true
                    }
                }
            }) {
                Text(text = "Go Home")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PSSPreview() {
    PaymentSuccessScreenContent(
        navController = rememberNavController(),
        amount = 1022,
        paidTo = "Rithik",
        message = "something"
    )
}