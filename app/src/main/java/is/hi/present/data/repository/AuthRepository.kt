package `is`.hi.present.data.repository

import `is`.hi.present.BuildConfig
import `is`.hi.present.domain.Profile
import dagger.hilt.android.scopes.ViewModelScoped
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.headers
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val supabase: SupabaseClient,
    private val httpClient: HttpClient,
) {
    private val deleteUserUrl: String = "${BuildConfig.SUPABASE_URL}/functions/v1/delete-user"
    suspend fun signUp(
        email: String,
        password: String
    ): UserInfo {
        runCatching {
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
        }

        if (supabase.auth.currentUserOrNull() == null) {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
        }

        val user = supabase.auth.currentUserOrNull()
            ?: throw Exception("Failed to get signed-up user")
        supabase.postgrest["profiles"].upsert(
            mapOf(
                "id" to user.id,
                "display_name" to email,
            )
        )
        return user
    }

    suspend fun signIn(
        email: String,
        password: String
    ){
        supabase.auth.signInWith(Email){
            this.email = email
            this.password = password
        }
    }

    suspend fun signOut() {
        runCatching { supabase.auth.signOut() }
    }

    fun getAccessToken(): String? {
        return supabase.auth.currentAccessTokenOrNull()
    }

    fun retrieveUser(): UserInfo? {
        return supabase.auth.currentUserOrNull()
    }

    suspend fun refreshCurrentSession() {
        try {
            supabase.auth.refreshCurrentSession()
        } catch (e: Exception) {
        }
    }

    fun getCurrentUserId(): String? {
        android.util.Log.d("AuthRepository", "getCurrentUserId=${supabase.auth.currentUserOrNull()?.id}")
        return supabase.auth.currentUserOrNull()?.id
    }

    fun getCurrentUserEmail(): String? {
        return supabase.auth.currentUserOrNull()?.email
    }

    fun hasCachedSession(): Boolean {
        return supabase.auth.currentSessionOrNull() != null
    }

    suspend fun getProfile(userId: String): Profile? {
        val result = supabase.postgrest["profiles"]
            .select {
                filter { eq("id", userId) }
            }
            .decodeList<Profile>()

        return result.firstOrNull()
    }
    suspend fun getProfiles(userIds: List<String>): List<Profile> {
        if (userIds.isEmpty()) return emptyList()
        return supabase.postgrest["profiles"]
            .select {
                filter { isIn("id", userIds) }
            }
            .decodeList<Profile>()
    }
    private suspend fun deleteAuthUser(userId: String) {
        val accessToken = getAccessToken()

        val response = httpClient.post(deleteUserUrl) {
            headers {
                append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                if (!accessToken.isNullOrBlank()) {
                    append("Authorization", "Bearer $accessToken")
                }
            }
            setBody("""{"userId":"$userId"}""")
        }

        if (response.status != HttpStatusCode.OK) {
            val text = response.bodyAsText()
            throw IllegalStateException("Failed to delete auth user: $text")
        }
    }
    suspend fun deleteAccount() {
        val user = supabase.auth.currentUserOrNull() ?: throw IllegalStateException("No user logged in")
        val userId = user.id

        // þurfum að bæta hér við item_claims og wishlist_shares þegar það er komið
        supabase.postgrest["wishlists"].delete { filter { eq("owner_id", userId) } }
        supabase.postgrest["profiles"].delete { filter { eq("id", userId) } }
        deleteAuthUser(userId)
        supabase.auth.signOut()

    }
    suspend fun updateDisplayName(name: String): Result<Unit> = runCatching {
        val userId = getCurrentUserId()
            ?: throw IllegalStateException("User not logged in")
        supabase
            .from("profiles")
            .update({
                set("display_name", name)
            }) {
                filter { eq("id", userId) }
            }
    }

}