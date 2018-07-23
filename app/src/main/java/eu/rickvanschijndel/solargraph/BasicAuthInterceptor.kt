package eu.rickvanschijndel.solargraph

/**
 * taken from: Alphaaa, https://stackoverflow.com/a/36056355
 */

import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response

class BasicAuthInterceptor(user: String, password: String): Interceptor {
    private val credentials = Credentials.basic(user, password)

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val authenticatedRequest = request.newBuilder()
                .header("Authorization", credentials).build()
        return chain.proceed(authenticatedRequest)
    }
}
