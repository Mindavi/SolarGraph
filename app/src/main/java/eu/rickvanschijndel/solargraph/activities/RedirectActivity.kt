package eu.rickvanschijndel.solargraph.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import eu.rickvanschijndel.solargraph.R

class RedirectActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_redirect)

        val username = PreferenceManager.getDefaultSharedPreferences(applicationContext).getString("username", "")
        val password = PreferenceManager.getDefaultSharedPreferences(applicationContext).getString("password", "")
        if (username.isEmpty() || password.isEmpty()) {
            val loginActivity = Intent(this, LoginActivity::class.java)
            startActivity(loginActivity)
        }
        else {
            val graphActivity = Intent(this, GraphActivity::class.java)
            startActivity(graphActivity)
        }
        finish()
    }
}
