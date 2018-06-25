package eu.rickvanschijndel.solargraph.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class System {

    @SerializedName("name")
    @Expose
    var name: String? = null
    @SerializedName("retailer")
    @Expose
    var retailer: String? = null
    @SerializedName("acpog_number")
    @Expose
    var acpogNumber: String? = null
    @SerializedName("nominal_power")
    @Expose
    var nominalPower: Int? = null
    @SerializedName("inverters")
    @Expose
    var inverters: List<Inverter>? = null
    @SerializedName("dt_created")
    @Expose
    var dtCreated: String? = null
    @SerializedName("dt_updated")
    @Expose
    var dtUpdated: String? = null

}
