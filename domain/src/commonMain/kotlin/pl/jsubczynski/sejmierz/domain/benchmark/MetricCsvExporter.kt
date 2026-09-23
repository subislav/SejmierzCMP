package pl.jsubczynski.sejmierz.domain.benchmark

object MetricCsvExporter {

    private const val SEPARATOR = ","

    fun toCsv(metrics: List<Metric>): String {
        if (metrics.isEmpty()) return "name,category,value,unit,timestampMs\n"

        val tagKeys = metrics.flatMap { it.tags.keys }.toSortedSet()
        val header = buildList {
            add("name")
            add("category")
            add("value")
            add("unit")
            add("timestampMs")
            addAll(tagKeys)
        }

        return buildString {
            appendLine(header.joinToString(SEPARATOR))
            metrics.forEach { metric ->
                val row = buildList {
                    add(metric.name.escape())
                    add(metric.category.name)
                    add(metric.value.toString())
                    add(metric.unit)
                    add(metric.timestampMs.toString())
                    tagKeys.forEach { key -> add((metric.tags[key] ?: "").escape()) }
                }
                appendLine(row.joinToString(SEPARATOR))
            }
        }
    }

    private fun String.escape(): String =
        if (any { it == ',' || it == '"' || it == '\n' }) {
            "\"" + replace("\"", "\"\"") + "\""
        } else {
            this
        }
}
