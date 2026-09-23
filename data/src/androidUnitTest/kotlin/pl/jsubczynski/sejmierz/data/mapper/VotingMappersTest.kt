package pl.jsubczynski.sejmierz.data.mapper

import org.junit.Assert.assertEquals
import org.junit.Test
import pl.jsubczynski.sejmierz.data.remote.dto.VotingDto
import pl.jsubczynski.sejmierz.data.remote.dto.VotingVoteDto

class VotingMappersTest {

    @Test
    fun `aggregates per-MP votes into per-club tallies`() {
        val dto = VotingDto(
            sitting = 2,
            votingNumber = 5,
            votes = listOf(
                vote("KO", "YES"),
                vote("KO", "YES"),
                vote("KO", "NO"),
                vote("PiS", "ABSTAIN"),
                vote("PiS", "ABSENT"),
                vote("PiS", null),
            ),
        )

        val byClub = dto.aggregateClubVotes().associateBy { it.club }

        val ko = byClub.getValue("KO")
        assertEquals(2, ko.yes)
        assertEquals(1, ko.no)
        assertEquals(0, ko.abstain)
        assertEquals(0, ko.absent)
        assertEquals(3, ko.total)

        val pis = byClub.getValue("PiS")
        assertEquals(0, pis.yes)
        assertEquals(0, pis.no)
        assertEquals(1, pis.abstain)
        assertEquals(2, pis.absent)
        assertEquals(3, pis.total)
    }

    @Test
    fun `detail with no votes yields empty club breakdown`() {
        val detail = VotingDto(sitting = 1, votingNumber = 1, votes = null).toDetailDomain()
        assertEquals(1, detail.voting.sitting)
        assertEquals(0, detail.clubVotes.size)
    }

    @Test
    fun `club tallies are sorted by total descending`() {
        val dto = VotingDto(
            votes = listOf(
                vote("Small", "YES"),
                vote("Big", "YES"),
                vote("Big", "YES"),
                vote("Big", "NO"),
            ),
        )

        val order = dto.aggregateClubVotes().map { it.club }
        assertEquals(listOf("Big", "Small"), order)
    }

    private fun vote(club: String, vote: String?) = VotingVoteDto(club = club, vote = vote)
}
