package pl.jsubczynski.sejmierz.domain.model

data class Mp(
    val id: Int,
    val firstLastName: String,
    val club: String?,
    val active: Boolean,
    val districtName: String?,
    val districtNum: Int?,
    val voivodeship: String?,
    val profession: String?,
    val educationLevel: String?,
    val email: String?,
    val numberOfVotes: Int?,
    val birthDate: String?,
    val birthLocation: String?,
) {

    fun photoUrl(term: Term): String =
        "https://api.sejm.gov.pl/sejm/term${term.number}/MP/$id/photo"
}
