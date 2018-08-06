// generated using https://github.com/wuseal/JsonToKotlinClass

package eu.rickvanschijndel.solargraph.models

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
