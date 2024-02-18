package io.agora.uikit.bean.req.v2;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ChatRoomAPICreateUserReq {
    @SerializedName("username")
    private String username;

    @SerializedName("password")
    private String password;
}
