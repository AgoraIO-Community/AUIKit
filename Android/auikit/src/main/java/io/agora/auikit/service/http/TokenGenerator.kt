package io.agora.auikit.service.http

import android.util.SparseArray
import io.agora.auikit.BuildConfig
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject


object TokenGenerator {
    private val scope = CoroutineScope(Job() + Dispatchers.Main)
    private val okHttpClient by lazy {
        val builder = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        }
        builder.build()
    }

    enum class TokenGeneratorType {
        token006, token007;
    }

    enum class AgoraTokenType(val value: Int) {
        rtc(1), rtm(2), chat(3);
    }

    data class ChannelToken(
        val id: Int,
        val channelName: String,
        val uid: String,
        val genType: TokenGeneratorType,
        val tokenType: AgoraTokenType
    )

    fun generateMultiChannelTokens(
        appId: String,
        appCert: String,
        tokens: List<ChannelToken>,
        success: (SparseArray<String>) -> Unit,
        failure: ((Exception?) -> Unit)? = null
    ){
        scope.launch(Dispatchers.Main) {
            try {
                val out = SparseArray<String>()
                tokens.forEach {
                    out[it.id] = fetchToken(appId, appCert, it.channelName, it.uid, it.genType, it.tokenType)
                }
                success.invoke(out)
            } catch (e: Exception) {
                failure?.invoke(e)
            }
        }
    }

    fun generateTokens(
        appId: String,
        appCert: String,
        channelName: String,
        uid: String,
        genType: TokenGeneratorType,
        tokenTypes: Array<AgoraTokenType>,
        success: (Map<AgoraTokenType, String>) -> Unit,
        failure: ((Exception?) -> Unit)? = null
    ) {
        scope.launch(Dispatchers.Main) {
            try {
                val out = mutableMapOf<AgoraTokenType, String>()
                tokenTypes.forEach {
                    out[it] = fetchToken(appId, appCert, channelName, uid, genType, it)
                }
                success.invoke(out)
            } catch (e: Exception) {
                failure?.invoke(e)
            }
        }
    }

    fun generateToken(
        appId: String,
        appCert: String,
        channelName: String,
        uid: String,
        genType: TokenGeneratorType,
        tokenType: AgoraTokenType,
        success: (String) -> Unit,
        failure: ((Exception?) -> Unit)? = null
    ) {
        scope.launch(Dispatchers.Main) {
            try {
                success.invoke(fetchToken(appId, appCert, channelName, uid, genType, tokenType))
            } catch (e: Exception) {
                failure?.invoke(e)
            }
        }
    }

    private suspend fun fetchToken(
        appId: String, appCert: String, channelName: String, uid: String, genType: TokenGeneratorType, tokenType: AgoraTokenType
    ) = withContext(Dispatchers.IO) {

        val postBody = JSONObject()
        postBody.put("appId", appId)
        postBody.put("appCertificate", appCert)
        postBody.put("channelName", channelName)
        postBody.put("expire", 1500) // s
        postBody.put("src", "Android")
        postBody.put("ts", System.currentTimeMillis().toString() + "")
        postBody.put("type", tokenType.value)
        postBody.put("uid", uid + "")

        val request = Request.Builder().url(
            if (genType == TokenGeneratorType.token006) "https://toolbox.bj2.agoralab.co/v1/token006/generate"
            else "https://toolbox.bj2.agoralab.co/v1/token/generate"
        ).addHeader("Content-Type", "application/json").post(postBody.toString().toRequestBody()).build()
        val execute = okHttpClient.newCall(request).execute()
        if (execute.isSuccessful) {
            val body = execute.body
                ?: throw RuntimeException("Fetch token error: httpCode=${execute.code}, httpMsg=${execute.message}, body is null")
            val bodyJobj = JSONObject(body.string())
            if (bodyJobj["code"] != 0) {
                throw RuntimeException("Fetch token error: httpCode=${execute.code}, httpMsg=${execute.message}, reqCode=${bodyJobj["code"]}, reqMsg=${bodyJobj["message"]},")
            } else {
                (bodyJobj["data"] as JSONObject)["token"] as String
            }
        } else {
            throw RuntimeException("Fetch token error: httpCode=${execute.code}, httpMsg=${execute.message}")
        }
    }
}