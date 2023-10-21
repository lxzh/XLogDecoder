package ledou.client

import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.internal.platform.Platform
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.time.Duration
import java.util.*
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * @author Virogu
 * @since 2022-12-29 15:28
 **/

val jsonDefault = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

val client by lazy {
    OkHttpClient.Builder().apply {
        callTimeout(Duration.ofSeconds(30))
        readTimeout(Duration.ofSeconds(30))
        addInterceptor(basicHeaderInterceptor)
        addInterceptor(basicParamInterceptor)
        sslSocketFactory(
            sSLSocketFactory,
            Platform.get().platformTrustManager()
        )
    }.build()
}

private val sSLSocketFactory: SSLSocketFactory
    get() = try {
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustManager, SecureRandom())
        sslContext.socketFactory
    } catch (e: java.lang.Exception) {
        throw RuntimeException(e)
    }

private val trustManager: Array<TrustManager> = arrayOf(
    object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return arrayOf()
        }
    }
)


private val basicHeaderInterceptor = Interceptor { chain ->
    val original = chain.request()
    val request = original.newBuilder()
        .header("Content-Type", "application/json")
        .header("User-Agent", WXUserAgent)
        .header("xweb_xhr", "1")
        .method(original.method, original.body)
        .build()
    chain.proceed(request)
}

private val basicParamInterceptor = Interceptor { chain ->
    val original = chain.request()
    val request = original.newBuilder()
        .apply {
            //println("original: $original")
            when (original.method.lowercase(Locale.getDefault())) {
                "get" -> {
                    val httpUrl = original.url
                        .newBuilder()
                        .addQueryParameter("h5openid", h5openid)
                        .addQueryParameter("h5token", h5token)
                        .addQueryParameter("timestamp", "${System.currentTimeMillis()}")
                        .addQueryParameter("uin", "null")
                        .addQueryParameter("skey", "null")
                        .addQueryParameter("pf", pf)
                        .addQueryParameter("from", "0")
                        .build()
                    url(httpUrl)
                }

                "post" -> {
                    val formBody = original.body
                    if (formBody !is FormBody) {
                        return@apply
                    }
                    val bodyBuilder: FormBody.Builder = FormBody.Builder()
                    for (i in 0 until formBody.size) {
                        bodyBuilder.addEncoded(formBody.encodedName(i), formBody.encodedValue(i))
                    }
                    val newBody = bodyBuilder
                        .addEncoded("h5openid", h5openid)
                        .addEncoded("h5token", h5token)
                        .addEncoded("timestamp", "${System.currentTimeMillis()}")
                        .addEncoded("uin", "null")
                        .addEncoded("skey", "null")
                        .addEncoded("pf", pf)
                        .addEncoded("from", "0")
                        .build()
                    post(newBody)
                }

                else -> {
                    method(original.method, original.body)
                }
            }
        }
        .build()
    chain.proceed(request)
}