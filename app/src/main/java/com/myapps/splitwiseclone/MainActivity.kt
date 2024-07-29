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
import com.myapps.splitwiseclone.ui.Routes
import com.myapps.splitwiseclone.ui.components.KeyboardAware
import com.myapps.splitwiseclone.ui.screens.home.HomeScreen
import com.myapps.splitwiseclone.ui.screens.auth.LoginScreen
import com.myapps.splitwiseclone.ui.screens.auth.RegisterScreen
import com.myapps.splitwiseclone.ui.screens.home.creategroup.CreateGroupScreen
import com.myapps.splitwiseclone.ui.screens.home.creategroup.SelectGroupMembersScreen
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


@Composable
fun NavigationComponent(navController: NavHostController) {
    val auth = Firebase.auth
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
        composable(Routes.createGroupScreen) { CreateGroupScreen(navController = navController) }
        composable(
            route = Routes.createGroupSelectMembersScreen,
            arguments = listOf(navArgument("groupName") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupName")
            SelectGroupMembersScreen(navController, groupId)
        }
    }
}