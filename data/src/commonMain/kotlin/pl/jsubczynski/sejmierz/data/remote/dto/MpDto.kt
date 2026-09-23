package pl.jsubczynski.sejmierz.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MpDto(
    val id: Int,
    val firstLastName: String = "",
    val firstName: String? = null,
    val lastName: String? = null,
    val club: String? = null,
    val active: Boolean = false,
    val districtName: String? = null,
    val districtNum: Int? = null,
    val voivodeship: String? = null,
    val profession: String? = null,
    val educationLevel: String? = null,
    val email: String? = null,
    val numberOfVotes: Int? = null,
    val birthDate: String? = null,
    val birthLocation: String? = null,
)
