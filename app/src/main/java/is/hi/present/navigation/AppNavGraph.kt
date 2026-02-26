package `is`.hi.present.navigation

import androidx.compose.runtime.*
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `is`.hi.present.ui.sharedWishlist.AddSharedWishlistScreen
import `is`.hi.present.ui.sharedWishlist.SharedWishlistScreen
import `is`.hi.present.ui.wishlists.CreateTokenScreen

@Composable
fun AppNavGraphNav3(
    startJoinToken: String? = null,
) {
    val authViewModel: AuthViewModel = hiltViewModel()

    var isCheckingAuth by remember { mutableStateOf(true) }
    var pendingJoinToken by remember { mutableStateOf(startJoinToken) }
    var startDestination by remember { mutableStateOf<AppRoute>(AppRoute.SignIn) }

    LaunchedEffect(Unit) {
        val token = authViewModel.getToken()
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
                    onLogout = {
                        authViewModel.signOut {
                            resetTo(AppRoute.SignIn)
                        }
                    },
                    onCreateWishlist = { backStack.add(AppRoute.CreateWishlist) },
                    onOpenWishlist = { id -> backStack.add(AppRoute.WishlistDetail(id)) },
                    onAccountSettings = { backStack.add(AppRoute.AccountSettings) },
                    onOpenSharedWishlists = { backStack.add(AppRoute.SharedWishlists) },
                    onSelectWishlists = { },
                    selectedSegmentIndex = 0,
                    )
            }

            entry<AppRoute.CreateWishlist> {
                CreateWishlistScreen(
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
                    onAddSharedWishlist = { backStack.add(AppRoute.AddSharedWishlist) },
                    onOpenWishlist = { id -> backStack.add(AppRoute.WishlistDetail(id)) },
                    onSelectWishlists = { backStack.add(AppRoute.Wishlists) },
                    onLogout = {
                        authViewModel.signOut {
                            resetTo(AppRoute.SignIn)
                        }
                    },
                    onAccountSettings = {backStack.add(AppRoute.AccountSettings)},
                    selectedSegmentIndex = 1,
                    onOpenSharedWishlists = { }
                )
            }

            entry<AppRoute.AddSharedWishlist> {
                AddSharedWishlistScreen(
                    onBack = { backStack.removeLastOrNull() },
                    onJoined = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}