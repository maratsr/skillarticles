package ru.skillbranch.skillarticles.data.remote.interceptors

import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import ru.skillbranch.skillarticles.data.local.PrefManager
import ru.skillbranch.skillarticles.data.remote.RestService
import ru.skillbranch.skillarticles.data.remote.req.RefreshReq
import dagger.Lazy

class TokenAuthenticator(
    val prefs: PrefManager,
    val lazyApi: Lazy<RestService> // Будет заинжекчен в момент первого обращения, Lazy - именно Dagger компонент, а не котлина!
    ) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        return if (response.code != 401) null
        else{
            val res = lazyApi.get().refreshAccessToken(RefreshReq( prefs.refreshToken)).execute()

            return if(!res.isSuccessful) null
            else {
                // save new pair (access+refresh tokens)
                val newAccessToken = res.body()!!.accessToken
                prefs.accessToken = "Bearer ${newAccessToken}"
                prefs.refreshToken = res.body()!!.refreshToken

                // retry request with bew access token
                response.request.newBuilder()
                    .header("Authorization", "Bearer ${newAccessToken}")
                    .build()
            }
        }
    }
}