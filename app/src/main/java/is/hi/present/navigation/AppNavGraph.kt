package `is`.hi.present.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import `is`.hi.present.ui.account.AccountSettingsScreen
import `is`.hi.present.ui.auth.AuthScreen
import `is`.hi.present.ui.auth.AuthViewModel
import `is`.hi.present.ui.components.LoadingComponent
import `is`.hi.present.ui.wishlistdetail.CreateItemScreen
import `is`.hi.present.ui.wishlistdetail.WishlistDetailScreen
import `is`.hi.present.ui.wishlists.WishlistsScreen
import `is`.hi.present.ui.wishlists.CreateWishlistScreen

@Composable
fun AppNavGraph(
    authViewModel: AuthViewModel = AuthViewModel(),
    navController: NavHostController = rememberNavController()
)
{
    val context = LocalContext.current

    var isCheckingAuth by remember { mutableStateOf(true) }
    var startDestination by remember { mutableStateOf(Routes.AUTH) }

    LaunchedEffect(Unit) {
        val token = authViewModel.getToken(context)
        startDestination = if (!token.isNullOrEmpty()) Routes.WISHLISTS else Routes.AUTH
        isCheckingAuth = false
    }

    if (isCheckingAuth) {
        LoadingComponent()
    } else {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {

            // AUTH SCREEN
            composable(Routes.AUTH) {
                AuthScreen(
                    viewModel = authViewModel,
                    onSuccess = {
                        navController.navigate(Routes.WISHLISTS) {
                            popUpTo(Routes.AUTH) { inclusive = true }
                        }
                    }
                )
            }
            // WISHLISTS SCREEN
            composable(Routes.WISHLISTS) {
                WishlistsScreen(
                    navController = navController,
                    onLogout = {
                        authViewModel.signOut(context) {
                            navController.navigate(Routes.AUTH) {
                                popUpTo(Routes.WISHLISTS) { inclusive = true }
                            }
                        }
                    }
                )
            }

            composable(Routes.CREATE_WISHLIST) {
                CreateWishlistScreen(navController = navController)
            }

            composable(Routes.WISHLIST_DETAIL,
                arguments = listOf(navArgument("wishlistId") { type = NavType.StringType })
            ) { backStackEntry ->
                val wishlistId = backStackEntry.arguments?.getString("wishlistId") ?: return@composable
                WishlistDetailScreen(
                    navController = navController,
                    wishlistId = wishlistId
                )
            }

            composable(Routes.CREATE_WISHLIST_ITEM) {
                CreateItemScreen(navController = navController)
            }
            composable(Routes.ACCOUNT_SETTINGS) {
                AccountSettingsScreen(
                    viewModel = authViewModel,
                    navController = navController
                )
            }
        }
    }
}
