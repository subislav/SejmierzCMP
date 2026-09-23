package pl.jsubczynski.sejmierz.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VotingDto(
    val sitting: Int = 0,
    val votingNumber: Int = 0,
    val sittingDay: Int? = null,
    val date: String? = null,
    val title: String? = null,
    val topic: String? = null,
    val description: String? = null,
    val kind: String? = null,
    val majorityType: String? = null,
    val yes: Int = 0,
    val no: Int = 0,
    val abstain: Int = 0,
    val notParticipating: Int = 0,
    val totalVoted: Int = 0,

    val votes: List<VotingVoteDto>? = null,
)

@Serializable
data class VotingVoteDto(
    @SerialName("MP") val mp: Int = 0,
    val club: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val secondName: String? = null,

    val vote: String? = null,
)
