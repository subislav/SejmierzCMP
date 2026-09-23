package pl.jsubczynski.sejmierz.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ClubDto(
    val id: String = "",
    val name: String = "",
    val membersCount: Int = 0,
    val email: String? = null,
    val phone: String? = null,
    val fax: String? = null,
    val members: List<ClubMemberDto>? = null,
)

@Serializable
data class ClubMemberDto(
    val id: Int = 0,
    val firstName: String? = null,
    val lastName: String? = null,
    val function: String? = null,
)
