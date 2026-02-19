package `is`.hi.present.ui.auth

fun AuthErrorMessage(raw: String): String {
    val s = raw.lowercase()

    return when {
        "unable to resolve host" in s || "no address associated with hostname" in s ->
            "Can’t reach the server. Check your internet and try again."
        "failed to connect" in s || "timeout" in s ->
            "Connection problem. Please try again."
        "invalid login credentials" in s ->
            "Email or password is incorrect."
        "user already registered" in s || "already registered" in s ->
            "An account with this email already exists."
        "email not confirmed" in s || ("confirm" in s && "email" in s) ->
            "Please confirm your email before signing in."
        "password" in s && ("short" in s || "at least" in s || "characters" in s) ->
            "Password is too short."
        "email" in s && ("invalid" in s || "malformed" in s) ->
            "Please enter a valid email address."
        else ->
            "Something went wrong. Please try again."
    }
}
