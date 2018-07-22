package eu.rickvanschijndel.solargraph.activities

import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.util.Log
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import eu.rickvanschijndel.solargraph.Login
import eu.rickvanschijndel.solargraph.LoginCallback
import eu.rickvanschijndel.solargraph.R
import eu.rickvanschijndel.solargraph.models.ProductionResponse
import eu.rickvanschijndel.solargraph.rest.ApiClient
import eu.rickvanschijndel.solargraph.rest.ApiInterface
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_graph.*
import okhttp3.OkHttpClient
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import java.util.concurrent.TimeUnit

class GraphActivity : AppCompatActivity(), LoginCallback {
    private lateinit var client: OkHttpClient
    private lateinit var apiClient: ApiInterface
    private lateinit var cookieJar: PersistentCookieJar
    private lateinit var login: Login
    private var retries = 0

    companion object {
        private const val TAG = "GraphActivity"
        private const val MAX_RETRIES = 3
        private const val MAX_TIMEOUT_SECONDS = 20L
        private const val RESPONSE_OK = 200
        private const val RESPONSE_UNAUTHORIZED = 401
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)

        setupGraph()

        cookieJar = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(this))
        client = OkHttpClient.Builder()
                .readTimeout(MAX_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .cookieJar(cookieJar)
                .build()
        login = Login(this, client)
        apiClient = ApiClient(client).client.create(ApiInterface::class.java)

        val networkInfo = getActiveNetworkInfo()
        if (networkInfo?.isConnected == true) {
            loadUsernameAndPassword()
            login.login()
        }
        else {
            network_info.setText(R.string.no_connection)
        }
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
                    if (retries <= MAX_RETRIES) {
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

    private fun getProductionData(): Single<Response<ProductionResponse>> {
        return apiClient.getAvailableSites().flatMap({
            return@flatMap apiClient.getProductionData(it.body()!!.toTypedArray()[0].publicKey!!)
        })
    }

    private fun retrieveData() {
        runOnUiThread {
            network_info.setText(R.string.retrieving_data)
        }
        getProductionData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object: SingleObserver<Response<ProductionResponse>> {
                    override fun onSuccess(response: Response<ProductionResponse>) {
                        when(response.code()) {
                            RESPONSE_OK -> {
                                val body = response.body()!!
                                onDataRetrieved(body)
                            }
                            RESPONSE_UNAUTHORIZED -> {
                                cookieJar.clear()
                                login.login()
                            }
                            else -> {
                                network_info.text = response.message()
                            }
                        }
                    }

                    override fun onSubscribe(d: Disposable) {
                        //
                    }

                    override fun onError(e: Throwable) {
                        Log.d(TAG, e.toString())
                        network_info.text = e.toString()
                    }
                })
    }

    private fun onDataRetrieved(responseData: ProductionResponse) {
        val kpis = responseData.stats?.kpis
        val outputToday = kpis?.outputToday
        val outputMonth = kpis?.outputMonth
        val outputTotal = kpis?.outputToDate
        today_power.text = getString(R.string.today_power, outputToday)
        monthly_power.text = getString(R.string.month_power, outputMonth)
        total_power.text = getString(R.string.total_power, outputTotal)


        val realTimePowerMap = responseData.stats?.graphs?.realTimePower
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        var dataPoints = arrayOf<Entry>()
        if (realTimePowerMap != null) {
            for (key in realTimePowerMap.keys) {
                val power = realTimePowerMap[key]!!
                val date = dateFormatter.parse(key)
                // we lose some precision by converting to float unfortunately
                // this is visible as rounding errors in the 15 minutes between data points (e.g. 14:14 instead of 14:15)
                dataPoints += Entry(date.time.toFloat(), power.toFloat())
            }
        }

        val sortedDataPoints = dataPoints.sortedWith(compareBy({it.x}))

        val series = LineDataSet(sortedDataPoints, "Power")
        series.color = Color.GREEN
        series.lineWidth = 5.0f
        val data = LineData(series)
        data.setDrawValues(false)
        graph.data = data

        val firstPower = sortedDataPoints.firstOrNull{ it.y > 0}
        val lastPower = sortedDataPoints.lastOrNull{ it.y > 0}
        if (firstPower != null) {
            graph.xAxis.axisMinimum = firstPower.x
        }
        if (lastPower != null) {
            graph.xAxis.axisMaximum = lastPower.x
        }

        network_info.setText(R.string.got_data)

        // redraw
        graph.invalidate()

    }

    private fun getActiveNetworkInfo(): NetworkInfo? {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo
    }

    private fun setupGraph() {
        graph.setNoDataText("Waiting for data...")
        graph.legend.isEnabled = false
        // would be cool if this formatter could round to 15 mins, as the data is like that
        // already and we lose some precision by using floats as data type (MPAndroidChart requires this)
        val timeFormatter = SimpleDateFormat("HH:mm", Locale.US)
        graph.setOnChartValueSelectedListener(object: OnChartValueSelectedListener{
            override fun onNothingSelected() {
                // pass
            }

            override fun onValueSelected(e: Entry?, h: Highlight?) {
                Snackbar.make(graph_snack_layout, "Power: ${e?.y}W at ${timeFormatter.format(e?.x)}", Snackbar.LENGTH_SHORT).show()
            }
        })
        graph.isScaleXEnabled = true
        graph.isScaleYEnabled = true
        graph.description.isEnabled = false

        graph.xAxis.setValueFormatter { value, _ ->
            // round to nearest 15 minutes
            // this might not work correctly when the values
            // get more precision (more often than every 15 minutes)
            // but it does work for now
            // it'll probably fail in the higher values when that day comes
            val roundedDate = Date(value.toLong())
            roundedDate.minutes = (roundedDate.minutes + 8) / 15 * 15
            roundedDate.seconds = 0
            Log.d(TAG, roundedDate.minutes.toString() + " " + roundedDate)
            timeFormatter.format(value)
        }
    }
}
