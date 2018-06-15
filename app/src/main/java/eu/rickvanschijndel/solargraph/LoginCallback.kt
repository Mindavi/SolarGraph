package eu.rickvanschijndel.solargraph

interface LoginCallback {
    fun onUpdate(event: LoginEvent, updateMessage: String?)

    enum class LoginEvent {
        STATUS_CHANGED,
        LOGGED_IN,
        LOGIN_FAILURE,
        NO_CREDENTIALS,
    }
}
