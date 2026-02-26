package `is`.hi.present.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import `is`.hi.present.ui.account.AccountSettingsScreen
import `is`.hi.present.ui.auth.AuthViewModel
import `is`.hi.present.ui.auth.SignInScreen
import `is`.hi.present.ui.auth.SignUpScreen
import `is`.hi.present.ui.components.LoadingComponent
import `is`.hi.present.ui.wishlistdetail.CreateItemScreen
import `is`.hi.present.ui.wishlistdetail.WishlistDetailScreen
import `is`.hi.present.ui.wishlists.CreateWishlistScreen
import `is`.hi.present.ui.wishlists.WishlistsScreen
import `is`.hi.present.ui.wishlists.WishlistsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AppNavGraphNav3(
    authViewModel: AuthViewModel = AuthViewModel(),
) {
    val context = LocalContext.current

    var isCheckingAuth by remember { mutableStateOf(true) }
    var startDestination by remember { mutableStateOf<AppRoute>(AppRoute.SignIn) }

    LaunchedEffect(Unit) {
        val token = authViewModel.getToken(context)
        startDestination = if (!token.isNullOrEmpty()) AppRoute.Wishlists else AppRoute.SignIn
        isCheckingAuth = false
    }

    if (isCheckingAuth) {
        LoadingComponent()
        return
    }

    val backStack = rememberNavBackStack(startDestination)
    val wishlistsVm: WishlistsViewModel = viewModel()

    fun resetTo(route: AppRoute) {
        backStack.clear()
        backStack.add(route)
    }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {

            entry<AppRoute.SignIn> {
                SignInScreen(
                    viewModel = authViewModel,
                    onGoToSignUp = { backStack.add(AppRoute.SignUp) },
                    onSuccess = { resetTo(AppRoute.Wishlists) }
                )
            }

            entry<AppRoute.SignUp> {
                SignUpScreen(
                    viewModel = authViewModel,
                    onGoToSignIn = { backStack.removeLastOrNull() },
                    onSuccess = { resetTo(AppRoute.Wishlists) }
                )
            }

            entry<AppRoute.Wishlists> {
                WishlistsScreen(
                    vm = wishlistsVm,
                    onLogout = {
                        authViewModel.signOut(context) {
                            resetTo(AppRoute.SignIn)
                        }
                    },
                    onCreateWishlist = { backStack.add(AppRoute.CreateWishlist) },
                    onOpenWishlist = { id -> backStack.add(AppRoute.WishlistDetail(id)) },
                    onAccountSettings = { backStack.add(AppRoute.AccountSettings) }
                )
            }

            entry<AppRoute.CreateWishlist> {
                CreateWishlistScreen(
                    vm = wishlistsVm,
                    onBack = { backStack.removeLastOrNull() },
                    onDone = { backStack.removeLastOrNull() }
                )
            }

            entry<AppRoute.WishlistDetail> { key ->
                WishlistDetailScreen(
                    wishlistId = key.wishlistId,
                    onBack = { backStack.removeLastOrNull() },
                    onCreateItem = { id -> backStack.add(AppRoute.CreateWishlistItem(id)) },
                    wishlistsVm = wishlistsVm
                )
            }

            entry<AppRoute.CreateWishlistItem> { key ->
                CreateItemScreen(
                    wishlistId = key.wishlistId,
                    onBack = { backStack.removeLastOrNull() },
                    onDone = { backStack.removeLastOrNull() }
                )
            }

            entry<AppRoute.AccountSettings> {
                AccountSettingsScreen(
                    viewModel = authViewModel,
                    onBack = { backStack.removeLastOrNull() },
                    onSignedOut = { resetTo(AppRoute.SignIn) },
                    onAccountDeleted = { resetTo(AppRoute.SignIn) }
                )
            }

        }
    )
}