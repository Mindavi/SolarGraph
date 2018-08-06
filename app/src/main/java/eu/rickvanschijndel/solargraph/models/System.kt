// generated using https://github.com/wuseal/JsonToKotlinClass

package eu.rickvanschijndel.solargraph.models

data class System(
        val name: String,
        val retailer: String,
        val acpog_number: String,
        val nominal_power: Int,
        val inverters: List<Inverter>,
        val dt_created: String,
        val dt_updated: String
)
