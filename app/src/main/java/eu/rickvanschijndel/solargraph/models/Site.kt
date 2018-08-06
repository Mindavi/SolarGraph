// generated using https://github.com/wuseal/JsonToKotlinClass

package eu.rickvanschijndel.solargraph.models

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
