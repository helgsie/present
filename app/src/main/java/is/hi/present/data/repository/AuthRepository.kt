package `is`.hi.present.data.repository

import `is`.hi.present.data.supabase.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.postgrest
import `is`.hi.present.domain.model.UserProfile
import io.ktor.client.*
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class AuthRepository {
    private val client = SupabaseClientProvider.client
    private val httpClient = HttpClient(OkHttp)
    private val deleteUserUrl = "https://basxwliixpeofbiezgab.supabase.co/functions/v1/delete-user"
    suspend fun signUp(
        email: String,
        password: String
    ): UserInfo {
        val result = client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
        val user = client.auth.currentUserOrNull()
            ?: throw Exception("Failed to get signed-up user")

        client.postgrest["profiles"].insert(
            mapOf(
                "id" to user.id,
                "display_name" to email
            )
        )
        return user
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
    suspend fun getProfile(userId: String): UserProfile? {
        val result = client.postgrest["profiles"]
            .select {
                filter { eq("id", userId) }
            }
            .decodeList<UserProfile>()

        return result.firstOrNull()
    }
    suspend fun deleteAuthUser(userId: String) {
        val response: HttpResponse = httpClient.post(deleteUserUrl) {
            contentType(ContentType.Application.Json)
            setBody("""{"userId":"$userId"}""")
        }

        if (response.status != HttpStatusCode.OK) {
            val text = response.bodyAsText()
            throw Exception("Failed to delete auth user: $text")
        }
    }
    suspend fun deleteAccount() {
        val user = client.auth.currentUserOrNull() ?: throw Exception("No user logged in")
        val userId = user.id

        deleteAuthUser(userId)

        client.postgrest["wishlists"].delete { filter { eq("owner_id", userId) } }
        client.postgrest["profiles"].delete { filter { eq("id", userId) } }

        client.auth.signOut()
    }

}