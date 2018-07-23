package eu.rickvanschijndel.solargraph.activities

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import eu.rickvanschijndel.solargraph.R
import eu.rickvanschijndel.solargraph.models.SiteResponse
import eu.rickvanschijndel.solargraph.rest.ApiImpl
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Response
import kotlinx.android.synthetic.main.activity_login.email_address
import kotlinx.android.synthetic.main.activity_login.password
import kotlinx.android.synthetic.main.activity_login.snack_layout
import kotlinx.android.synthetic.main.activity_login.login_button

class LoginActivity : AppCompatActivity() {
    companion object {
        const val USERNAME_PREFERENCE_NAME = "username"
        const val PASSWORD_PREFERENCE_NAME = "password"
    }

    private fun getActiveNetworkInfo(): NetworkInfo? {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo
    }

    private fun saveUsernameAndPassword() {
        val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
        editor.putString(USERNAME_PREFERENCE_NAME, email_address.editText?.text.toString())
        editor.putString(PASSWORD_PREFERENCE_NAME, password.editText?.text.toString())
        editor.apply()
    }

    private fun notifyLoginFailure(message: String) {
        runOnUiThread {
            Snackbar.make(snack_layout, getString(R.string.login_failure, message), Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        login_button.setOnClickListener { _ ->
            val networkInfo = getActiveNetworkInfo()
            if (networkInfo == null || !networkInfo.isConnected) {
                Snackbar.make(snack_layout, R.string.no_connection, Snackbar.LENGTH_INDEFINITE).show()
                return@setOnClickListener
            }

            val email = email_address.editText?.text.toString()
            val password = password.editText?.text.toString()
            if (email.isBlank() || password.isBlank()) {
                Snackbar.make(snack_layout, R.string.incomplete_credentials, Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val apiImpl = ApiImpl(email, password)
            apiImpl.client.getAvailableSites()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object: SingleObserver<Response<Collection<SiteResponse>>> {
                        override fun onSuccess(response: Response<Collection<SiteResponse>>) {
                            when(response.code()) {
                                200 -> {
                                    saveUsernameAndPassword()
                                    val graphActivity = Intent(applicationContext, GraphActivity::class.java)
                                    startActivity(graphActivity)
                                    finish()
                                }

                                401 -> {
                                    notifyLoginFailure(response.message())
                                    return
                                }

                                else -> {
                                    notifyLoginFailure(response.message())
                                    return
                                }
                            }
                        }

                        override fun onSubscribe(d: Disposable) {
                            //
                        }

                        override fun onError(e: Throwable) {
                            e.printStackTrace()
                            val message = e.message
                            if (message != null) {
                                notifyLoginFailure(message)
                            }
                        }
                    })
        }
    }
}
