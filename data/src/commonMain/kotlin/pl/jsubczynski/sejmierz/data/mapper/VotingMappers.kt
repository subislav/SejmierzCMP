package pl.jsubczynski.sejmierz.data.mapper

import pl.jsubczynski.sejmierz.data.db.VotingEntity
import pl.jsubczynski.sejmierz.data.remote.dto.VotingDto
import pl.jsubczynski.sejmierz.domain.model.ClubVoteSummary
import pl.jsubczynski.sejmierz.domain.model.Voting
import pl.jsubczynski.sejmierz.domain.model.VotingDetail

fun VotingDto.toDomain(): Voting = Voting(
    sitting = sitting,
    votingNumber = votingNumber,
    sittingDay = sittingDay,
    date = date,
    title = title,
    topic = topic,
    description = description,
    kind = kind,
    majorityType = majorityType,
    yes = yes,
    no = no,
    abstain = abstain,
    notParticipating = notParticipating,
    totalVoted = totalVoted,
)

fun VotingDto.toDetailDomain(): VotingDetail = VotingDetail(
    voting = toDomain(),
    clubVotes = aggregateClubVotes(),
)

fun VotingDto.aggregateClubVotes(): List<ClubVoteSummary> {
    val votes = votes ?: return emptyList()
    return votes
        .groupBy { it.club ?: "—" }
        .map { (club, clubVotes) ->
            ClubVoteSummary(
                club = club,
                yes = clubVotes.count { it.vote == "YES" },
                no = clubVotes.count { it.vote == "NO" },
                abstain = clubVotes.count { it.vote == "ABSTAIN" },
                absent = clubVotes.count { it.vote != "YES" && it.vote != "NO" && it.vote != "ABSTAIN" },
            )
        }
        .sortedByDescending { it.total }
}

fun VotingEntity.toDomain(): Voting = Voting(
    sitting = sitting.toInt(),
    votingNumber = votingNumber.toInt(),
    sittingDay = sittingDay?.toInt(),
    date = date,
    title = title,
    topic = topic,
    description = description,
    kind = kind,
    majorityType = majorityType,
    yes = yes.toInt(),
    no = no.toInt(),
    abstain = abstain.toInt(),
    notParticipating = notParticipating.toInt(),
    totalVoted = totalVoted.toInt(),
)
