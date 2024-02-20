package io.agora.uikit.bean.dto.v2;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ChatRoomAPICreateChatRoomDto {
    @SerializedName("data")
    private Wrapper wrapper;

    public ChatRoomAPICreateChatRoomDto(Wrapper wrapper) {
        this.wrapper = wrapper;
    }

    public String getRoomId() {
        return this.wrapper != null ? this.wrapper.roomId : null;
    }

    @Data
    @Accessors(chain = true)
    public static class Wrapper {
        @SerializedName("id")
        private String roomId;

        public Wrapper(String roomId) {
            this.roomId = roomId;
        }
    }
}
