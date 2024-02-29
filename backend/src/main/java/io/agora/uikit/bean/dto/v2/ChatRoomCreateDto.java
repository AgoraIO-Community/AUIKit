package io.agora.uikit.bean.dto.v2;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ChatRoomCreateDto {
    @SerializedName("chatId")
    private String chatId;

    @SerializedName("userToken")
    private String userToken;

    @SerializedName("userUuid")
    private String userUuid;

    @SerializedName("appKey")
    private String appKey;

}
