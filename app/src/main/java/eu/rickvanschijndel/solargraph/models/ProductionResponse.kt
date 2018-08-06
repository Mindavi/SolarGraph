// generated using https://github.com/wuseal/JsonToKotlinClass

package eu.rickvanschijndel.solargraph.models

data class ProductionResponse(
        val site: Site,
        val inverters: List<MonitorData>,
        val stats: Stats
)
