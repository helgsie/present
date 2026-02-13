package `is`.hi.present.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import `is`.hi.present.ui.wishlistdetail.WishlistDetailScreen
import `is`.hi.present.ui.wishlists.WishlistsScreen
import `is`.hi.present.ui.wishlists.CreateWishlistScreen

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.WISHLISTS
    ) {
        composable(Routes.WISHLISTS) {
            WishlistsScreen(navController = navController)
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
    }
}
