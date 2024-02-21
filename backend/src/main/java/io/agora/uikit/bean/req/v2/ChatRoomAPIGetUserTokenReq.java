package io.agora.uikit.bean.req.v2;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ChatRoomAPIGetUserTokenReq {
    @SerializedName("grant_type")
    private String grantType = "inherit";

    @SerializedName("username")
    private String username;

    @SerializedName("autoCreateUser")
    private Boolean autoCreateUser = true;

    @SerializedName("ttl")
    private Long ttl;
}
