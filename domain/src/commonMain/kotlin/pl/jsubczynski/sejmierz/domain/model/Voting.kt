package pl.jsubczynski.sejmierz.domain.model

data class Voting(
    val sitting: Int,
    val votingNumber: Int,
    val sittingDay: Int?,
    val date: String?,
    val title: String?,
    val topic: String?,
    val description: String?,
    val kind: String?,
    val majorityType: String?,
    val yes: Int,
    val no: Int,
    val abstain: Int,
    val notParticipating: Int,
    val totalVoted: Int,
)
