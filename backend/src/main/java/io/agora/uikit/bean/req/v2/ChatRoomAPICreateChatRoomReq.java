package io.agora.uikit.bean.req.v2;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class ChatRoomAPICreateChatRoomReq {
    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("maxusers")
    private int maxUsers;

    @SerializedName("owner")
    private String owner;

    @SerializedName("members")
    private List<String> members;

    @SerializedName("custom")
    private String custom;
}
