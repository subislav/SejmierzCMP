package pl.jsubczynski.sejmierz.domain.model

data class VotingDetail(
    val voting: Voting,
    val clubVotes: List<ClubVoteSummary>,
)

data class ClubVoteSummary(
    val club: String,
    val yes: Int,
    val no: Int,
    val abstain: Int,
    val absent: Int,
) {
    val total: Int get() = yes + no + abstain + absent
}
