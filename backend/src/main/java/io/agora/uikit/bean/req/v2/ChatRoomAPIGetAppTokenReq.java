package io.agora.uikit.bean.req.v2;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ChatRoomAPIGetAppTokenReq {
    @SerializedName("grant_type")
    private String grantType = "client_credentials";

    @SerializedName("client_id")
    private String clientId;

    @SerializedName("client_secret")
    private String clientSecret;

    @SerializedName("ttl")
    private Long ttl;
}

