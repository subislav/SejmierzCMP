package pl.jsubczynski.sejmierz.ui.core

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import pl.jsubczynski.sejmierz.ui.resources.Res
import pl.jsubczynski.sejmierz.ui.resources.*
import pl.jsubczynski.sejmierz.domain.error.DomainError

@Composable
fun DomainError.toMessage(): String = when (this) {
    is DomainError.Network -> stringResource(Res.string.error_network)
    is DomainError.Http -> stringResource(Res.string.error_http, code)
    is DomainError.Serialization -> stringResource(Res.string.error_serialization)
    DomainError.NotCached -> stringResource(Res.string.error_not_cached)
    DomainError.Empty -> stringResource(Res.string.error_empty)
    is DomainError.Unknown -> stringResource(Res.string.error_unknown)
}
