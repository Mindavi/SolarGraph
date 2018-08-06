// generated using https://github.com/wuseal/JsonToKotlinClass

package eu.rickvanschijndel.solargraph.models

data class SiteResponse(
        val public_key: String,
        val name: String,
        val address: String,
        val date: String
)

data class ProductionResponse(
        val site: Site,
        val inverters: List<MonitorData>,
        val stats: Stats
)

data class Site(
        val public_key: String,
        val name: String,
        val address: Address,
        val timezone: String,
        val nominal_pv_power: String,
        val systems: List<System>,
        val dt_created: String,
        val dt_updated: String
)

data class System(
        val name: String,
        val retailer: String,
        val acpog_number: String,
        val nominal_power: Int,
        val inverters: List<Inverter>,
        val dt_created: String,
        val dt_updated: String
)

data class Inverter(
        val inverternr: Int,
        val product_code: String,
        val serial_number: String,
        val comm_type: String,
        val comm_identifier: String,
        val dt_installed: String,
        val dt_replaced: Any,
        val mppts: Map<Int, Mppt>,
        val dma_device_type: String
)

data class Mppt(
        val mpptnr: String,
        val module_group: String,
        val strings: List<Any>
)

data class Address(
        val address_line_1: String,
        val city: String,
        val state: String,
        val country: String
)

data class Stats(
        val graphs: Graphs,
        val kpis: Kpis
)

data class Kpis(
        val current_production: Int,
        val output_today: Int,
        val output_month: Int,
        val output_to_date: Int
)

data class Graphs(
        val realtime_power: Map<String, Double>,
        val daily_output: Map<String, Double>,
        val monthly_output: Map<String, Double>
)

data class MonitorData(
        val dt_first_msg: String,
        val dt_latest_msg: String
)
