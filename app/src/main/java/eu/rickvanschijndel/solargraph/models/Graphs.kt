// generated using https://github.com/wuseal/JsonToKotlinClass

package eu.rickvanschijndel.solargraph.models

data class Graphs(
        val realtime_power: Map<String, Double>,
        val daily_output: Map<String, Double>,
        val monthly_output: Map<String, Double>
)
