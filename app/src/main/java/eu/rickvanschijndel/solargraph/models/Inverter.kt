package eu.rickvanschijndel.solargraph.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Inverter {

    @SerializedName("inverternr")
    @Expose
    var inverternr: Int? = null
    @SerializedName("product_code")
    @Expose
    var productCode: String? = null
    @SerializedName("serial_number")
    @Expose
    var serialNumber: String? = null
    @SerializedName("comm_type")
    @Expose
    var commType: String? = null
    @SerializedName("comm_identifier")
    @Expose
    var commIdentifier: String? = null
    @SerializedName("dt_installed")
    @Expose
    var dtInstalled: String? = null
    @SerializedName("mppts")
    @Expose
    var mppts: Map<Int, Mppt>? = null
    @SerializedName("dt_replaced")
    @Expose
    var dtReplaced: Any? = null
    @SerializedName("dma_device_type")
    @Expose
    var dmaDeviceType: String? = null

}
