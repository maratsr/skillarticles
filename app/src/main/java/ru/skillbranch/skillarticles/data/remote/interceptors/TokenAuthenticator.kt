package ru.skillbranch.skillarticles.data.remote.interceptors

import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import ru.skillbranch.skillarticles.data.local.PrefManager
import ru.skillbranch.skillarticles.data.remote.NetworkManager
import ru.skillbranch.skillarticles.data.remote.req.RefreshReq
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED

class TokenAuthenticator : Authenticator {
    private val network by lazy {NetworkManager.api}
    private val preferences = PrefManager

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.code == HTTP_UNAUTHORIZED) {
            val authResponse = network
                .refreshToken(RefreshReq(preferences.refreshToken))
                .execute()

            if (authResponse.isSuccessful && authResponse.body() != null) {
                preferences.accessToken = "Bearer {${authResponse.body()!!.accessToken}}"
                preferences.refreshToken = authResponse.body()!!.refreshToken
                return response
                    .request
                    .newBuilder()
                    .header("Authorization", preferences.accessToken)
                    .build()
            }
        }
        return null
    }

}
