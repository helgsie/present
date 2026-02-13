package `is`.hi.present.data.repository

import `is`.hi.present.data.supabase.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo

class AuthRepository {
    private val client = SupabaseClientProvider.client

    suspend fun signUp(
        email: String,
        password: String
    ) {
        client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }
    suspend fun signIn(
        email: String,
        password: String
    ){
        client.auth.signInWith(Email){
            this.email = email
            this.password = password
        }
    }
    suspend fun signOut() {
        client.auth.signOut()
    }
    fun getAccessToken(): String? {
        return client.auth.currentAccessTokenOrNull()
    }
    fun retrieveUser(): UserInfo? {
        return client.auth.currentUserOrNull()
    }
    suspend fun refreshCurrentSession() {
        client.auth.refreshCurrentSession()
    }
}