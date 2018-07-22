package eu.rickvanschijndel.solargraph.models


import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Mppt {

    @SerializedName("mpptnr")
    @Expose
    var mpptnr: String? = null
    @SerializedName("module_group")
    @Expose
    var moduleGroup: String? = null
    @SerializedName("strings")
    @Expose
    var strings: List<Any>? = null

}
