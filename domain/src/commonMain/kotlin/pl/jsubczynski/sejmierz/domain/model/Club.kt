package pl.jsubczynski.sejmierz.domain.model

data class Club(
    val id: String,
    val name: String,
    val membersCount: Int,
    val email: String?,
    val phone: String?,
    val fax: String?,
) {

    fun logoUrl(term: Term): String =
        "https://api.sejm.gov.pl/sejm/term${term.number}/clubs/$id/logo"
}
