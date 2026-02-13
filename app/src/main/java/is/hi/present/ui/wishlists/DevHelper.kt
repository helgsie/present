import `is`.hi.present.BuildConfig
import `is`.hi.present.data.supabase.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email

 suspend fun devSignInAsWishlistUser() {
    if (!BuildConfig.DEBUG) return

    val client = SupabaseClientProvider.client

     // bara notað fyrir að testa wishlist.. eyða þegar auth er komið
    runCatching { client.auth.signOut() }

    client.auth.signInWith(Email) {
        email = "wishlistuser@test.is"
        password = "test"
    }
}
