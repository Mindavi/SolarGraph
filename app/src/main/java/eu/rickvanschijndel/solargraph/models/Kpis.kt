package eu.rickvanschijndel.solargraph.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Kpis {

    @SerializedName("current_production")
    @Expose
    var currentProduction: Int? = null
    @SerializedName("output_today")
    @Expose
    var outputToday: Int? = null
    @SerializedName("output_month")
    @Expose
    var outputMonth: Int? = null
    @SerializedName("output_to_date")
    @Expose
    var outputToDate: Int? = null

}
