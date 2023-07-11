package io.agora.auikit.service.http

import io.agora.auikit.service.callback.AUIException
import org.json.JSONObject
import retrofit2.Response

class Utils {
    companion object {

        @JvmStatic
        fun <T>errorFromResponse(response: Response<T>): AUIException {
            val errorMsg = response.errorBody()?.string()
            var code = -1
            var msg = "error"
            if (errorMsg != null) {
                val obj = JSONObject(errorMsg)
                code = if (obj.has("code")) obj.getInt("code") else -1
                msg = if (obj.has("message")) obj.getString("message") else "error"
            }
            return AUIException(code, msg)
        }

    }
}