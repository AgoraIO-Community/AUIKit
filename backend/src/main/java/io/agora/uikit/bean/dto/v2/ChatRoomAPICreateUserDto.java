package io.agora.uikit.bean.dto.v2;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class ChatRoomAPICreateUserDto {

    @SerializedName("entities")
    private List<entity> entities;

    public entity getUser() {
        return entities != null && !entities.isEmpty() ? entities.get(0) : null;
    }

    @Data
    @Accessors(chain = true)
    public static class entity {
        @SerializedName("uuid")
        private String uuid;

        @SerializedName("type")
        private String type;

        @SerializedName("username")
        private String username;

        @SerializedName("created")
        private long created;

        @SerializedName("modified")
        private long modified;
    }
}
