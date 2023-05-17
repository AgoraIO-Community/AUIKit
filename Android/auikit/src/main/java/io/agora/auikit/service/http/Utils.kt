package io.agora.auikit.service.http

import io.agora.auikit.service.callback.AUiException
import org.json.JSONObject
import retrofit2.Response

class Utils {
    companion object {

        @JvmStatic
        fun <T>errorFromResponse(response: Response<T>): AUiException {
            val errorMsg = response.errorBody()?.string()
            var code = -1
            var msg = "error"
            if (errorMsg != null) {
                val obj = JSONObject(errorMsg)
                code = obj.getInt("code")
                msg = obj.getString("message")
            }
            return AUiException(code, msg)
        }

    }
}