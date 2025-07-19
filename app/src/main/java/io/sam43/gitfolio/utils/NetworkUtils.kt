
package io.sam43.gitfolio.utils

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object NetworkUtils {

    private val _networkState = MutableSharedFlow<Boolean>(replay = 1) // using replay cache of last emitted value
    val networkState = _networkState.asSharedFlow()

    fun isNetworkAvailable(context: Context): Boolean {
        return try {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                    ?: return false.also { _networkState.tryEmit(false) }
            val network =
                connectivityManager.activeNetwork
                    ?: return false.also { _networkState.tryEmit(false) }
            val activeNetwork =
                connectivityManager.getNetworkCapabilities(network)
                    ?: return false.also { _networkState.tryEmit(false) }

            val isAvailable = when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
            _networkState.tryEmit(isAvailable)
            isAvailable
        } catch (e: Exception) {
            _networkState.tryEmit(false)
            false
        }
    }
}

fun Activity.showSnackbar(message: String) {
    Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show()
}
