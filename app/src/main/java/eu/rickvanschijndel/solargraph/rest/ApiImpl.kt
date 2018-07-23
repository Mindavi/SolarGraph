package eu.rickvanschijndel.solargraph.rest

import eu.rickvanschijndel.solargraph.BasicAuthInterceptor
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class ApiImpl(username: String, password: String) {
    val client: ApiInterface
    companion object {
        private const val MAX_TIMEOUT_SECONDS = 20L
    }

    init {
        val httpClient = OkHttpClient.Builder()
                .readTimeout(MAX_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .addInterceptor(BasicAuthInterceptor(username, password))
                .build()
        client = ApiClient(httpClient).client.create(ApiInterface::class.java)
    }
}
