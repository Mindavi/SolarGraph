package eu.rickvanschijndel.solargraph

import android.content.Context
import android.util.Log
import com.franmontiel.persistentcookiejar.ClearableCookieJar
import okhttp3.*
import org.jsoup.Jsoup
import java.io.IOException
import java.lang.ref.WeakReference

class Login(context: Context, client: OkHttpClient) {
    private val mContext: WeakReference<Context> = WeakReference(context)
    private val mHttpClient: WeakReference<OkHttpClient> = WeakReference(client)
    private var mUsername: String? = null
    private var mPassword: String? = null

    private companion object {
        const val TAG = "Login"
        const val BASE_URL = "https://my.autarco.com"
        const val LOGIN_ROUTE = "/auth/login"
        const val SESSION_COOKIE_NAME = "autarco_session"

        const val USERNAME_FIELD = "username"
        const val PASSWORD_FIELD = "password"
        const val TOKEN_FIELD = "_token"
    }

    private fun statusChanged(updateMessage: String?) {
        update(LoginCallback.LoginEvent.STATUS_CHANGED, updateMessage)
    }

    private fun loginSuccess() {
        update(LoginCallback.LoginEvent.LOGGED_IN, "Logged in")
    }

    private fun update(event: LoginCallback.LoginEvent, updateMessage: String?) {
        val context = mContext.get()
        if (context is LoginCallback) {
            context.onUpdate(event, updateMessage)
        }
    }

    private fun isAlreadyLoggedIn(): Boolean {
        val url = HttpUrl.parse(BASE_URL + LOGIN_ROUTE) ?: throw IllegalArgumentException("LOGIN_URL")
        val cookies = mHttpClient.get()?.cookieJar()?.loadForRequest(url)
        if (cookies?.find { it.name() == SESSION_COOKIE_NAME } != null) {
            return true
        }
        return false
    }

    private fun runLoginSequence() {
        if (isAlreadyLoggedIn()) {
            loginSuccess()
            return
        }
        statusChanged(mContext.get()?.getString(R.string.requesting_login_token))

        val request = Request.Builder()
                .url(BASE_URL + LOGIN_ROUTE)
                .build()
        mHttpClient.get()?.newCall(request)?.enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                e?.printStackTrace()
            }

            override fun onResponse(call: Call?, response: Response?) {
                if (response == null || !response.isSuccessful) {
                    if (response != null) {
                        Log.w(TAG, response.message())
                        statusChanged(response.message())
                    } else {
                        statusChanged("Could not get a response")
                    }
                    Log.w(TAG, "Unsuccessful request")
                    return
                }
                Log.d(TAG, "Got a response")
                val loginPage = response.body()?.string()
                val document = Jsoup.parse(loginPage)
                val inputElements = document.select("input")
                for (inputElement in inputElements) {
                    if (inputElement.attr("name") == TOKEN_FIELD) {
                        val token = inputElement.attr("value")
                        if (token.isNullOrBlank()) {
                            statusChanged("Couldn't get a login token")
                            return
                        }
                        doLogin(token)
                        return
                    }
                }
            }
        })
    }

    private fun doLogin(token: String) {
        if (mUsername.isNullOrBlank() || mPassword.isNullOrBlank()) {
            statusChanged("No username or password set")
            return
        }

        statusChanged("Logging in")
        val loginRequestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(USERNAME_FIELD, mUsername!!)
                .addFormDataPart(PASSWORD_FIELD, mPassword!!)
                .addFormDataPart(TOKEN_FIELD, token)
                .build()

        val request = Request.Builder()
                .url(BASE_URL + LOGIN_ROUTE)
                .post(loginRequestBody)
                .build()
        mHttpClient.get()?.newCall(request)?.enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                e?.printStackTrace()
            }

            override fun onResponse(call: Call?, response: Response?) {
                if (response == null || !response.isSuccessful) {
                    Log.d(TAG, "Invalid response")
                    val cookieJar = mHttpClient.get()?.cookieJar() as ClearableCookieJar
                    cookieJar.clear()
                    update(LoginCallback.LoginEvent.LOGIN_FAILURE, response?.message())
                    return
                }
                loginSuccess()
            }
        })
    }

    fun setUsername(username: String) {
        if (username.isBlank()) return
        val cookieJar: ClearableCookieJar = mHttpClient.get()?.cookieJar() as ClearableCookieJar
        cookieJar.clear()
        mUsername = username
    }
    fun setPassword(password: String) {
        if (password.isBlank()) return
        mPassword = password
    }

    fun login() {
        if (mUsername.isNullOrBlank()) {
            update(LoginCallback.LoginEvent.NO_CREDENTIALS, "username")
            return
        }
        if (mPassword.isNullOrBlank()) {
            update(LoginCallback.LoginEvent.NO_CREDENTIALS, "password")
            return
        }
        runLoginSequence()
    }
}
