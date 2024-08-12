package com.myapps.splitwiseclone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.myapps.splitwiseclone.server.ServerMocker
import com.myapps.splitwiseclone.ui.Routes
import com.myapps.splitwiseclone.ui.components.KeyboardAware
import com.myapps.splitwiseclone.ui.screens.home.HomeScreen
import com.myapps.splitwiseclone.ui.screens.auth.LoginScreen
import com.myapps.splitwiseclone.ui.screens.auth.RegisterScreen
import com.myapps.splitwiseclone.ui.screens.home.groups.CreateGroupScreen
import com.myapps.splitwiseclone.ui.screens.home.groups.EditGroupScreen
import com.myapps.splitwiseclone.ui.screens.home.common.SelectGroupMembersScreen
import com.myapps.splitwiseclone.ui.screens.home.groups.messages.GroupMessagesScreen
import com.myapps.splitwiseclone.ui.screens.home.groups.split.CreateSplitScreen
import com.myapps.splitwiseclone.ui.screens.home.groups.split.ViewSplitScreen
import com.myapps.splitwiseclone.ui.screens.home.groups.split.payments.PaymentSuccessScreen
import com.myapps.splitwiseclone.ui.screens.home.schedules.ViewSchedulesScreen
import com.myapps.splitwiseclone.ui.theme.SplitwisecloneTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            SplitwisecloneTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavigationComponent(navController = navController)
                }
            }
        }
    }
}


private const val TAG = "MainActivity"

@Composable
fun NavigationComponent(navController: NavHostController) {
    val auth = Firebase.auth
    ServerMocker.refreshSchedules()
    NavHost(
        navController = navController,
        startDestination = if (auth.currentUser == null) "login" else "home"
    ) {
        //Authentication Pages
        composable(Routes.loginScreen) {
            KeyboardAware {
                LoginScreen(navController = navController)
            }
        }
        composable(Routes.registerScreen) { RegisterScreen(navController = navController) }


        composable(Routes.homeScreen) { HomeScreen(navController = navController) }
        composable(Routes.createGroupScreen) {
            CreateGroupScreen(navController = navController)
        }
        composable(Routes.selectGroupSelectMembersScreen) {
            SelectGroupMembersScreen(navController)
        }
        composable(
            route = Routes.groupMessagesScreen,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")
            GroupMessagesScreen(navController, groupId)
        }
        composable(
            route = Routes.groupEditScreen,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")
            EditGroupScreen(navController, groupId)
        }

        composable(
            route = Routes.groupSchedulesScreen,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")
            ViewSchedulesScreen(navController, groupId)
        }


        composable(
            route = Routes.createSplitScreen,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType },
                navArgument("amount") { type = NavType.IntType })
        ) { backStackEntry ->
            val groupId: String? = backStackEntry.arguments?.getString("groupId")
            val amount: Int? = backStackEntry.arguments?.getInt("amount")
            CreateSplitScreen(navController, groupId, amount)
        }

        composable(
            route = Routes.viewSplitScreen,
            arguments = listOf(navArgument("splitId") { type = NavType.StringType },
                navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId: String? = backStackEntry.arguments?.getString("splitId")
            val amount: String? = backStackEntry.arguments?.getString("groupId")
            ViewSplitScreen(navController, groupId, amount)
        }

        composable(
            route = Routes.paymentSuccessScreen,
            arguments = listOf(navArgument("amount") { type = NavType.IntType },
                navArgument("paidTo") { type = NavType.StringType },
                navArgument("message") { type = NavType.StringType })
        ) { backStackEntry ->
            val amount: Int? = backStackEntry.arguments?.getInt("amount")
            val paidTo: String? = backStackEntry.arguments?.getString("paidTo")
            val message: String? = backStackEntry.arguments?.getString("message")
            PaymentSuccessScreen(navController, amount = amount, paidTo = paidTo, message = message)
        }
    }
}