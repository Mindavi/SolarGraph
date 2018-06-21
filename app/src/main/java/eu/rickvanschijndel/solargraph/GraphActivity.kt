package eu.rickvanschijndel.solargraph

import android.content.Context
import android.graphics.Color
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
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
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
                .url("https://my.autarco.com/api/site/or0q8h99")
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
        val statsObject = JSONObject(responseData).getJSONObject("stats")
        val kpiObject = statsObject.getJSONObject("kpis")
//        val currentProduction = kpiObject.getLong("current_production")
        val outputToday = kpiObject.getLong("output_today")
        val outputMonth = kpiObject.getInt("output_month")
        val outputTotal = kpiObject.getInt("output_to_date")
        runOnUiThread {
            today_power.text = getString(R.string.today_power, outputToday)
            monthly_power.text = getString(R.string.month_power, outputMonth)
            total_power.text = getString(R.string.total_power, outputTotal)
        }

        val realTimePowerObject = statsObject.getJSONObject("graphs").getJSONObject("realtime_power")
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        var dataPoints = arrayOf<Entry>()
        for (key in realTimePowerObject.keys()) {
            val power = realTimePowerObject.getDouble(key)
            val date = dateFormatter.parse(key)
            // we lose some precision by converting to float unfortunately
            dataPoints += Entry(date.time.toFloat(), power.toFloat())
        }

        val sortedDataPoints = dataPoints.sortedWith(compareBy({it.x}))


        val series = LineDataSet(sortedDataPoints, "Power")
        series.color = Color.GREEN
        series.lineWidth = 5.0f
        val data = LineData(series)
        data.setDrawValues(false)
        graph.data = data
        // would be cool if this formatter could round to 15 mins, as the data is like that
        // already and we lose some precision by using floats as data type (MPAndroidChart requires this)
        val timeFormatter = SimpleDateFormat("HH:mm", Locale.US)
        graph.xAxis.setValueFormatter { value, _ ->
            timeFormatter.format(value)
        }
        val firstPower = sortedDataPoints.first { it.y > 0}
        val lastPower = sortedDataPoints.last{ it.y > 0}
        graph.xAxis.axisMinimum = firstPower.x
        graph.xAxis.axisMaximum = lastPower.x
        graph.legend.isEnabled = false
        graph.setOnChartValueSelectedListener(object: OnChartValueSelectedListener{
            override fun onNothingSelected() {
                // pass
            }

            override fun onValueSelected(e: Entry?, h: Highlight?) {
                Toast.makeText(this@GraphActivity, "Power: ${e?.y}W at ${timeFormatter.format(e?.x)}", Toast.LENGTH_SHORT).show()
            }
        })
//        graph.isScaleXEnabled = true
        graph.isScaleYEnabled = true
        graph.description.isEnabled = false

        runOnUiThread {
            network_info.setText(R.string.got_data)

            // redraw
            graph.invalidate()
        }
    }

    private fun getActiveNetworkInfo(): NetworkInfo? {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)

        graph.setNoDataText("Waiting for data...")

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
