package pl.jsubczynski.sejmierz.data.mapper

import pl.jsubczynski.sejmierz.data.db.MpEntity
import pl.jsubczynski.sejmierz.data.remote.dto.MpDto
import pl.jsubczynski.sejmierz.domain.model.Mp

fun MpDto.toDomain(): Mp = Mp(
    id = id,
    firstLastName = firstLastName.ifBlank {
        listOfNotNull(firstName, lastName).joinToString(" ").trim()
    },
    club = club,
    active = active,
    districtName = districtName,
    districtNum = districtNum,
    voivodeship = voivodeship,
    profession = profession,
    educationLevel = educationLevel,
    email = email,
    numberOfVotes = numberOfVotes,
    birthDate = birthDate,
    birthLocation = birthLocation,
)

fun MpEntity.toDomain(): Mp = Mp(
    id = id.toInt(),
    firstLastName = firstLastName,
    club = club,
    active = active == 1L,
    districtName = districtName,
    districtNum = districtNum?.toInt(),
    voivodeship = voivodeship,
    profession = profession,
    educationLevel = educationLevel,
    email = email,
    numberOfVotes = numberOfVotes?.toInt(),
    birthDate = birthDate,
    birthLocation = birthLocation,
)
