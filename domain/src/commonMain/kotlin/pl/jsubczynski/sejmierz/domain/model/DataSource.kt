package pl.jsubczynski.sejmierz.domain.model

enum class DataSource {

    NETWORK,


    LOCAL,
}

enum class DataSourceMode {
    ONLINE,
    OFFLINE;

    fun toDataSource(): DataSource = when (this) {
        ONLINE -> DataSource.NETWORK
        OFFLINE -> DataSource.LOCAL
    }
}
