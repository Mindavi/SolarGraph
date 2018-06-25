package eu.rickvanschijndel.solargraph.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Inverter_ {

    @SerializedName("dt_first_msg")
    @Expose
    var dtFirstMsg: String? = null
    @SerializedName("dt_latest_msg")
    @Expose
    var dtLatestMsg: String? = null

}
