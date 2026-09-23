package pl.jsubczynski.sejmierz.domain.model

data class ClubDetail(
    val club: Club,
    val members: List<ClubMember>,
)

data class ClubMember(
    val id: Int,
    val firstName: String?,
    val lastName: String?,
    val function: String?,
) {
    val fullName: String get() = listOfNotNull(firstName, lastName).joinToString(" ").trim()
}
