package `is`.hi.present.navigation

import androidx.compose.runtime.*
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import `is`.hi.present.ui.account.AccountSettingsScreen
import `is`.hi.present.ui.auth.AuthStatus
import `is`.hi.present.ui.auth.AuthViewModel
import `is`.hi.present.ui.auth.SignInScreen
import `is`.hi.present.ui.auth.SignUpScreen
import `is`.hi.present.ui.components.LoadingComponent
import `is`.hi.present.ui.wishlistdetail.CreateItemScreen
import `is`.hi.present.ui.wishlistdetail.WishlistDetailScreen
import `is`.hi.present.ui.wishlists.CreateWishlistScreen
import `is`.hi.present.ui.wishlists.WishlistsScreen
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import `is`.hi.present.ui.sharedWishlist.AddSharedWishlistScreen
import `is`.hi.present.ui.sharedWishlist.SharedWishlistScreen
import `is`.hi.present.ui.wishlists.CreateTokenScreen
import `is`.hi.present.ui.wishlists.WishlistsViewModel

@Composable
fun AppNavGraphNav3(
    startJoinToken: String? = null,
) {
    val authViewModel: AuthViewModel = hiltViewModel()

    var pendingJoinToken by remember { mutableStateOf(startJoinToken) }
    val authStatus by authViewModel.authStatus.collectAsStateWithLifecycle()

    when (val status = authStatus) {
        AuthStatus.Loading -> {
            LoadingComponent()
        }

        AuthStatus.LoggedOut -> {
            AuthNav(
                authViewModel = authViewModel,
            )
        }

        is AuthStatus.LoggedIn -> {
            AppNav(
                authViewModel = authViewModel,
                userId = status.userId,
                pendingJoinToken = pendingJoinToken,
                clearPendingJoinToken = { pendingJoinToken = null }
            )
        }
    }
}

@Composable
private fun AuthNav(
    authViewModel: AuthViewModel,
) {
    val backStack = rememberNavBackStack(AppRoute.SignIn)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {

            entry<AppRoute.SignIn> {
                SignInScreen(
                    viewModel = authViewModel,
                    onGoToSignUp = { backStack.add(AppRoute.SignUp) },
                    onSuccess = {}
                )
            }

            entry<AppRoute.SignUp> {
                SignUpScreen(
                    viewModel = authViewModel,
                    onGoToSignIn = { backStack.removeLastOrNull() },
                    onSuccess = {}
                )
            }
        }
    )
}

@Composable
private fun AppNav(
    authViewModel: AuthViewModel,
    userId: String,
    pendingJoinToken: String?,
    clearPendingJoinToken: () -> Unit,
) {
    val startDestination: AppRoute = remember(userId, pendingJoinToken) {
        when {
            !pendingJoinToken.isNullOrBlank() -> AppRoute.JoinWishlist(pendingJoinToken)
            else -> AppRoute.Wishlists
        }
    }

    val backStack = rememberNavBackStack(startDestination)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {

            entry<AppRoute.Wishlists> {
                val vm: WishlistsViewModel = hiltViewModel()
                WishlistsScreen(
                    ownerId = userId,
                    vm = vm,
                    onLogout = {
                        authViewModel.signOut()
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
                    ownerId = userId,
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
                    onSignedOut = { authViewModel.signOut() },
                    onAccountDeleted = { authViewModel.deleteAccount {} }
                )
            }

            entry<AppRoute.JoinWishlist> { key ->
                CreateTokenScreen(
                    token = key.token,
                    onJoined = { wishlistId ->
                        clearPendingJoinToken()
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
                    onLogout = { authViewModel.signOut() },
                    onAccountSettings = { backStack.add(AppRoute.AccountSettings) },
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