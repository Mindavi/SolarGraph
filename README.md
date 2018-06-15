# SolarGraph

View real-time power production of your Autarco PV system.

<img src="assets/solargraph.jpeg" alt="Real-time production" height="50%" width="50%">

## Usage

In the GraphActivity.kt, fill in your user name and password.
Compile the application to an APK and run it.
It should show a graph with your PV production data.

## To do

* Add a screen to login and and store the user data somewhere (perhaps in SharedPreferences)
    - There's no support for OAuth on the login endpoint, so the username and password must be stored
