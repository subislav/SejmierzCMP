package pl.jsubczynski.sejmierz.data.mapper

import pl.jsubczynski.sejmierz.data.db.ClubEntity
import pl.jsubczynski.sejmierz.data.remote.dto.ClubDto
import pl.jsubczynski.sejmierz.data.remote.dto.ClubMemberDto
import pl.jsubczynski.sejmierz.domain.model.Club
import pl.jsubczynski.sejmierz.domain.model.ClubDetail
import pl.jsubczynski.sejmierz.domain.model.ClubMember

fun ClubDto.toDomain(): Club = Club(
    id = id,
    name = name,
    membersCount = membersCount,
    email = email?.takeIf { it.isNotBlank() },
    phone = phone?.takeIf { it.isNotBlank() },
    fax = fax?.takeIf { it.isNotBlank() },
)

fun ClubDto.toDetailDomain(): ClubDetail = ClubDetail(
    club = toDomain(),
    members = members.orEmpty().map { it.toDomain() },
)

fun ClubMemberDto.toDomain(): ClubMember = ClubMember(
    id = id,
    firstName = firstName,
    lastName = lastName,
    function = function,
)

fun ClubEntity.toDomain(): Club = Club(
    id = id,
    name = name,
    membersCount = membersCount.toInt(),
    email = email,
    phone = phone,
    fax = fax,
)
