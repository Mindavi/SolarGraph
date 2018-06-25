package eu.rickvanschijndel.solargraph.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Toast
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import eu.rickvanschijndel.solargraph.Login
import eu.rickvanschijndel.solargraph.LoginCallback
import eu.rickvanschijndel.solargraph.R
import kotlinx.android.synthetic.main.activity_login.*
import okhttp3.OkHttpClient

class LoginActivity : AppCompatActivity(), LoginCallback {
    private lateinit var cookieJar: PersistentCookieJar
    private lateinit var client: OkHttpClient
    private lateinit var login: Login

    companion object {
        const val USERNAME_PREFERENCE_NAME = "username"
        const val PASSWORD_PREFERENCE_NAME = "password"
    }

    private fun saveUsernameAndPassword() {
        val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
        editor.putString(USERNAME_PREFERENCE_NAME, email_address.text.toString())
        editor.putString(PASSWORD_PREFERENCE_NAME, password.text.toString())
        editor.apply()
    }

    override fun onUpdate(event: LoginCallback.LoginEvent, updateMessage: String?) {
        when(event) {
            LoginCallback.LoginEvent.STATUS_CHANGED -> {

            }
            LoginCallback.LoginEvent.LOGGED_IN -> {
                val graphActivity = Intent(this, GraphActivity::class.java)
                startActivity(graphActivity)
                finish()
            }
            LoginCallback.LoginEvent.LOGIN_FAILURE -> {
                Toast.makeText(this, "Could not log in", Toast.LENGTH_SHORT).show()
            }
            LoginCallback.LoginEvent.NO_CREDENTIALS -> {

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        cookieJar = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(this))
        client = OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .build()
        login = Login(this, client)

        login_button.setOnClickListener { _ ->
            if (email_address.text.isNullOrBlank() || password.text.isNullOrBlank()) {
                Toast.makeText(this, "Fill in a username or password", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            login.setUsername(email_address.text.toString())
            login.setPassword(password.text.toString())
            saveUsernameAndPassword()
            login.login()
        }
    }
}
