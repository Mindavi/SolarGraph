package eu.rickvanschijndel.solargraph

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.LineGraphSeries
import com.jjoe64.graphview.series.DataPoint
import kotlinx.android.synthetic.main.activity_graph.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class GraphActivity : AppCompatActivity(), LoginCallback {
    private lateinit var client: OkHttpClient
    private lateinit var cookieJar: PersistentCookieJar
    private lateinit var login: Login
    private var retries = 0

    companion object {
        private const val TAG = "GraphActivity"
        private const val maxRetries = 3
    }

    private fun loadUsernameAndPassword() {
        val username = PreferenceManager.getDefaultSharedPreferences(this).getString(LoginActivity.USERNAME_PREFERENCE_NAME, "")
        val password = PreferenceManager.getDefaultSharedPreferences(this).getString(LoginActivity.PASSWORD_PREFERENCE_NAME, "")
        login.setUsername(username)
        login.setPassword(password)
    }

    override fun onUpdate(event: LoginCallback.LoginEvent, updateMessage: String?) {
        when(event) {
            LoginCallback.LoginEvent.STATUS_CHANGED -> {
                runOnUiThread {
                    if (updateMessage != null) {
                        network_info.text = updateMessage
                    }
                }
            }
            LoginCallback.LoginEvent.LOGGED_IN -> {
                retrieveData()
                retries = 0
            }
            LoginCallback.LoginEvent.NO_CREDENTIALS -> {
                runOnUiThread {
                    network_info.setText(R.string.no_credentials)
                }
            }
            LoginCallback.LoginEvent.LOGIN_FAILURE -> {
                runOnUiThread {
                    network_info.setText(R.string.retry_logging_in)
                    retries++
                    if (retries <= maxRetries) {
                        login.login()
                        return@runOnUiThread
                    }
                    else {
                        network_info.setText(R.string.max_retries_exceeded)
                    }
                }
            }
        }
    }

    private fun retrieveData() {
        runOnUiThread {
            network_info.setText(R.string.retrieving_data)
        }
        val request = Request.Builder()
                .url("https://my.autarco.com/api/site/or0q8h99/inverter/current?i=410017621936")
                .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                e?.printStackTrace()
            }

            override fun onResponse(call: Call?, response: Response?) {
                if (response == null || !response.isSuccessful) {
                    if (response != null) {
                        Log.d(TAG, response.message())
                        runOnUiThread {
                            network_info.text = response.message()
                        }
                        if (response.code() == 401) {
                            cookieJar.clear()
                        }
                    }
                    Log.d(TAG, "Invalid response")
                    return
                }
                val responseData = response.body()?.string() ?: return
                onDataRetrieved(responseData)
            }
        })
    }


    private fun onDataRetrieved(responseData: String) {
        val jsonObject = JSONObject(responseData).getJSONObject("stats").getJSONObject("graphs").getJSONObject("realtime_power")
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        var dataPoints = arrayOf<DataPoint>()
        for (key in jsonObject.keys()) {
            val power = jsonObject.getDouble(key)
            val date = formatter.parse(key)
            dataPoints += DataPoint(date, power)
        }

        val sortedDataPoints = dataPoints.sortedWith(compareBy({it.x}))

        val series = LineGraphSeries(sortedDataPoints.toTypedArray())
        runOnUiThread {
            network_info.setText(R.string.got_data)
            graph.addSeries(series)
            val timeFormatter = SimpleDateFormat("HH:00", Locale.US)
            graph.gridLabelRenderer.labelFormatter = DateAsXAxisLabelFormatter(this, timeFormatter)
            val firstPower = sortedDataPoints.first { it.y > 0}
            val lastPower = sortedDataPoints.last{ it.y > 0}
            graph.viewport.setMinX(firstPower.x)
            graph.viewport.setMaxX(lastPower.x)
            graph.viewport.isXAxisBoundsManual = true
            graph.viewport.setMaxY(series.highestValueY)
            graph.viewport.isYAxisBoundsManual = true
            series.setOnDataPointTapListener { _, dataPoint ->
                Toast.makeText(this, "Power: ${dataPoint.y}W", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getActiveNetworkInfo(): NetworkInfo? {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)

        cookieJar = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(this))
        client = OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .build()
        login = Login(this, client)

        val networkInfo = getActiveNetworkInfo()
        if (networkInfo?.isConnected == true) {
            loadUsernameAndPassword()
            login.login()
        }
        else {
            network_info.setText(R.string.no_connection)
        }
    }
}
