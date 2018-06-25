package eu.rickvanschijndel.solargraph.models


import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Site {

    @SerializedName("public_key")
    @Expose
    var publicKey: String? = null
    @SerializedName("name")
    @Expose
    var name: String? = null
    @SerializedName("address")
    @Expose
    var address: Address? = null
    @SerializedName("timezone")
    @Expose
    var timezone: String? = null
    @SerializedName("nominal_pv_power")
    @Expose
    var nominalPvPower: String? = null
    @SerializedName("systems")
    @Expose
    var systems: List<System>? = null
    @SerializedName("dt_created")
    @Expose
    var dtCreated: String? = null
    @SerializedName("dt_updated")
    @Expose
    var dtUpdated: String? = null

}
