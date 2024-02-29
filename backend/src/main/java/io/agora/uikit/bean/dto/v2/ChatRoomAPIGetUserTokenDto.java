package io.agora.uikit.bean.dto.v2;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ChatRoomAPIGetUserTokenDto {
    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("expires_in")
    private Long expiresIn;

    @SerializedName("user")
    private Wrapper user;

    @Data
    @Accessors(chain = true)
    public static class Wrapper {
        @SerializedName("uuid")
        private String uuid;

        @SerializedName("type")
        private String type;


        @SerializedName("created")
        private long created;

        @SerializedName("modified")
        private long modified;

        @SerializedName("username")
        private String username;

        @SerializedName("activated")
        private boolean activated;
    }
}
