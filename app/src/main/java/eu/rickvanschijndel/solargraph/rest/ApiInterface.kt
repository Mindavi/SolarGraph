package eu.rickvanschijndel.solargraph.rest

import eu.rickvanschijndel.solargraph.models.ProductionResponse
import eu.rickvanschijndel.solargraph.models.SiteResponse
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiInterface {
    @GET("site")
    fun getAvailableSites(): Single<Response<Collection<SiteResponse>>>

    @GET("site/{site_id}")
    fun getProductionData(@Path("site_id") site_id: String): Single<Response<ProductionResponse>>
}
