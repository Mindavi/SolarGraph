package eu.rickvanschijndel.solargraph.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class SiteResponse {

    @SerializedName("public_key")
    @Expose
    var publicKey: String? = null
    @SerializedName("name")
    @Expose
    var name: String? = null
    @SerializedName("address")
    @Expose
    var address: String? = null
    @SerializedName("date")
    @Expose
    var date: String? = null

}
