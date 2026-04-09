package `is`.hi.present.domain

import kotlinx.serialization.Serializable

@Serializable
class Profile(
    val id: String,
    val display_name: String = "",
)