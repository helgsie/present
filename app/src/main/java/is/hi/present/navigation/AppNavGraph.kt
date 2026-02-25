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
import androidx.lifecycle.viewmodel.compose.viewModel
import `is`.hi.present.ui.sharedWishlist.AddSharedWishlistScreen
import `is`.hi.present.ui.sharedWishlist.SharedWishlistScreen
import `is`.hi.present.ui.wishlists.CreateTokenScreen
import `is`.hi.present.ui.wishlists.WishlistsViewModel

@Composable
fun AppNavGraphNav3(
    authViewModel: AuthViewModel = AuthViewModel(),
    startJoinToken: String? = null,
) {
    val context = LocalContext.current

    var isCheckingAuth by remember { mutableStateOf(true) }
    var pendingJoinToken by remember { mutableStateOf(startJoinToken) }
    var startDestination by remember { mutableStateOf<AppRoute>(AppRoute.SignIn) }

    LaunchedEffect(Unit) {
        val token = authViewModel.getToken(context)
        val loggedIn = !token.isNullOrEmpty()

        startDestination = when {
            loggedIn && !pendingJoinToken.isNullOrBlank() ->
                AppRoute.JoinWishlist(pendingJoinToken!!)

            loggedIn -> AppRoute.Wishlists
            else -> AppRoute.SignIn
        }
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
                    onSuccess = {
                        val next = pendingJoinToken?.let { AppRoute.JoinWishlist(it) }
                            ?: AppRoute.Wishlists
                        pendingJoinToken = null
                        resetTo(next)
                    }
                )
            }

            entry<AppRoute.SignUp> {
                SignUpScreen(
                    viewModel = authViewModel,
                    onGoToSignIn = { backStack.removeLastOrNull() },
                    onSuccess = {
                        val next = pendingJoinToken?.let { AppRoute.JoinWishlist(it) }
                            ?: AppRoute.Wishlists
                        pendingJoinToken = null
                        resetTo(next)
                    }
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
                    onAccountSettings = { backStack.add(AppRoute.AccountSettings) },
                    onOpenSharedWishlists = { backStack.add(AppRoute.SharedWishlists) }
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
                    onCreateItem = { wishlistId ->
                        backStack.add(AppRoute.CreateWishlistItem(wishlistId))
                    }
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

            entry<AppRoute.JoinWishlist> { key ->
                CreateTokenScreen(
                    token = key.token,
                    onJoined = { wishlistId ->
                        backStack.removeLastOrNull()
                        backStack.add(AppRoute.WishlistDetail(wishlistId))
                    }
                )
            }

            entry<AppRoute.SharedWishlists> {
                SharedWishlistScreen(
                    onBack = { backStack.removeLastOrNull() },
                    onAddSharedWishlist = { backStack.add(AppRoute.AddSharedWishlist) },
                    onOpenWishlist = { id -> backStack.add(AppRoute.WishlistDetail(id)) },
                    onOpenWishlists = { backStack.add(AppRoute.Wishlists)},
                    onLogout = {
                        authViewModel.signOut(context) {
                            resetTo(AppRoute.SignIn)
                        }
                    },
                    onAccountSettings = {backStack.add(AppRoute.AccountSettings)}
                )
            }

            entry<AppRoute.AddSharedWishlist> {
                AddSharedWishlistScreen(
                    onBack = { backStack.removeLastOrNull() },
                    onJoined = { wishlistId ->
                        backStack.removeLastOrNull()
                        backStack.add(AppRoute.WishlistDetail(wishlistId))
                    }
                )
            }
        }
    )
}