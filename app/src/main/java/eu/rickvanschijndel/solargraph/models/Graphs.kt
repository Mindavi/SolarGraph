package eu.rickvanschijndel.solargraph.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Graphs {

    @SerializedName("realtime_power")
    @Expose
    var realTimePower: Map<String, Double>? = null

    @SerializedName("daily_output")
    @Expose
    var dailyOutput: Map<String, Double>? = null

    @SerializedName("monthly_output")
    @Expose
    var monthlyOutput: Map<String, Double>? = null

}
