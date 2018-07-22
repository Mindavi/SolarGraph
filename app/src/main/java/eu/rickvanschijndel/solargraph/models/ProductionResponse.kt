package eu.rickvanschijndel.solargraph.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ProductionResponse {

    @SerializedName("site")
    @Expose
    var site: Site? = null
    @SerializedName("inverters")
    @Expose
    var inverters: List<Inverter_>? = null
    @SerializedName("stats")
    @Expose
    var stats: Stats? = null

}
