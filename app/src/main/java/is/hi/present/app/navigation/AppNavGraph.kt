package `is`.hi.present.app.navigation

import androidx.compose.runtime.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.runtime.NavKey
import `is`.hi.present.ui.account.AccountSettingsScreen
import `is`.hi.present.ui.auth.AuthStatus
import `is`.hi.present.ui.auth.AuthViewModel
import `is`.hi.present.ui.auth.SignInScreen
import `is`.hi.present.ui.auth.SignUpScreen
import `is`.hi.present.ui.components.LoadingComponent
import `is`.hi.present.ui.ownedwishlist.create.CreateItemScreen
import `is`.hi.present.ui.ownedwishlist.detail.WishlistDetailScreen
import `is`.hi.present.ui.ownedwishlist.create.CreateWishlistScreen
import `is`.hi.present.ui.ownedwishlist.list.WishlistsScreen
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import `is`.hi.present.ui.sharedwishlist.join.AddSharedWishlistScreen
import `is`.hi.present.ui.sharedwishlist.detail.SharedWishlistDetailScreen
import `is`.hi.present.ui.sharedwishlist.list.SharedWishlistScreen
import `is`.hi.present.ui.sharedwishlist.item.SharedItemDetailScreen
import `is`.hi.present.ui.ownedwishlist.item.ItemDetailScreen
import `is`.hi.present.ui.ownedwishlist.invite.CreateTokenScreen
import `is`.hi.present.ui.auth.SetupProfileScreen
import `is`.hi.present.ui.ownedwishlist.detail.WishlistDetailViewModel
import `is`.hi.present.ui.ownedwishlist.list.WishlistsViewModel
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import `is`.hi.present.ui.components.AddButton
import `is`.hi.present.ui.components.WishlistsHeaderScreen

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
            if (status.isNewUser) {
                SetupProfileScreen(
                    viewModel = authViewModel,
                    onDone = { authViewModel.onProfileSetupComplete() }
                )
            } else {
                key(status.userId) {
                    AppNav(
                        authViewModel = authViewModel,
                        userId = status.userId,
                        pendingJoinToken = pendingJoinToken,
                        clearPendingJoinToken = { pendingJoinToken = null }
                    )
                }
            }
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
    val wishlistsViewModel: WishlistsViewModel = hiltViewModel()
    val wishlistDetailViewModel: WishlistDetailViewModel = hiltViewModel()

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
                WishlistsTabsRoute(
                    initialSelectedSegmentIndex = 0,
                    userId = userId,
                    authViewModel = authViewModel,
                    wishlistsViewModel = wishlistsViewModel,
                    wishlistDetailViewModel = wishlistDetailViewModel,
                    backStack = backStack
                )
            }

            entry<AppRoute.CreateWishlist> {
                CreateWishlistScreen(
                    ownerId = userId,
                    vm = wishlistsViewModel,
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
                    },
                    onOpenItem = {itemId ->
                        backStack.add(AppRoute.WishlistItemDetail(key.wishlistId, itemId))
                    }
                )
            }

            entry<AppRoute.SharedWishlistDetail> { key ->
                SharedWishlistDetailScreen(
                    wishlistId = key.wishlistId,
                    onBack = { backStack.removeLastOrNull() },
                    onOpenItem = { itemId ->
                        backStack.add(AppRoute.SharedItemDetail(itemId))
                    }
                )
            }

            entry<AppRoute.SharedItemDetail> { key ->
                SharedItemDetailScreen(
                    itemId = key.itemId,
                    onBack = { backStack.removeLastOrNull() }
                )
            }

            entry<AppRoute.CreateWishlistItem> { key ->
                CreateItemScreen(
                    wishlistId = key.wishlistId,
                    onBack = { backStack.removeLastOrNull() },
                    onDone = { backStack.removeLastOrNull() }
                )
            }

            entry<AppRoute.WishlistItemDetail> { key ->
                ItemDetailScreen(
                    wishlistId = key.wishlistId,
                    itemId = key.itemId,
                    onBack = { backStack.removeLastOrNull() }
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
                        backStack.add(AppRoute.SharedWishlistDetail(wishlistId))
                    }
                )
            }

            entry<AppRoute.SharedWishlists> {
                WishlistsTabsRoute(
                    initialSelectedSegmentIndex = 1,
                    userId = userId,
                    authViewModel = authViewModel,
                    wishlistsViewModel = wishlistsViewModel,
                    wishlistDetailViewModel = wishlistDetailViewModel,
                    backStack = backStack
                )
            }

            entry<AppRoute.AddSharedWishlist> {
                AddSharedWishlistScreen(
                    onBack = { backStack.removeLastOrNull() },
                    onJoined = { backStack.removeLastOrNull() }
                )
            }
        },
        transitionSpec = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(250)
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(250)
            )
        },
        popTransitionSpec = {
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(250)
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(250)
            )
        },
        predictivePopTransitionSpec = {
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(250)
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(250)
            )
        }
    )
}

@Composable
private fun WishlistsTabsRoute(
    initialSelectedSegmentIndex: Int,
    userId: String,
    authViewModel: AuthViewModel,
    wishlistsViewModel: WishlistsViewModel,
    wishlistDetailViewModel: WishlistDetailViewModel,
    backStack: androidx.navigation3.runtime.NavBackStack<NavKey>
) {
    var selectedSegmentIndex by rememberSaveable { mutableIntStateOf(initialSelectedSegmentIndex) }

    Scaffold(
        topBar = {
            WishlistsHeaderScreen(
                selectedSegmentIndex = selectedSegmentIndex,
                onSelectedChange = { selectedSegmentIndex = it },
                onAccountSettings = { backStack.add(AppRoute.AccountSettings) },
                onLogout = { authViewModel.signOut() },
                title = "Óskalistar"
            )
        },
        floatingActionButton = {
            AddButton(
                onClick = {
                    if (selectedSegmentIndex == 0) {
                        backStack.add(AppRoute.CreateWishlist)
                    } else {
                        backStack.add(AppRoute.AddSharedWishlist)
                    }
                },
                contentDescription = if (selectedSegmentIndex == 0) {
                    "Create wishlist"
                } else {
                    "Add shared wishlist"
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (selectedSegmentIndex == 0) {
                WishlistsScreen(
                    ownerId = userId,
                    wishlistVm = wishlistsViewModel,
                    detailVm = wishlistDetailViewModel,
                    onLogout = { authViewModel.signOut() },
                    onAccountSettings = { backStack.add(AppRoute.AccountSettings) },
                    onCreateWishlist = { backStack.add(AppRoute.CreateWishlist) },
                    onOpenSharedWishlists = { },
                    onOpenWishlist = { id -> backStack.add(AppRoute.WishlistDetail(id)) },
                    onSelectWishlists = { },
                    selectedSegmentIndex = selectedSegmentIndex,
                    embeddedInHeaderScreen = true
                )
            } else {
                SharedWishlistScreen(
                    onAddSharedWishlist = { backStack.add(AppRoute.AddSharedWishlist) },
                    onOpenWishlist = { id -> backStack.add(AppRoute.SharedWishlistDetail(id)) },
                    onSelectWishlists = { },
                    onLogout = { authViewModel.signOut() },
                    onAccountSettings = { backStack.add(AppRoute.AccountSettings) },
                    selectedSegmentIndex = selectedSegmentIndex,
                    onOpenSharedWishlists = { },
                    embeddedInHeaderScreen = true
                )
            }
        }
    }
}