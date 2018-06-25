package eu.rickvanschijndel.solargraph.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Stats {

    @SerializedName("graphs")
    @Expose
    var graphs: Graphs? = null
    @SerializedName("kpis")
    @Expose
    var kpis: Kpis? = null

}
