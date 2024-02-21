package io.agora.uikit.bean.req.v2;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class RoomDestroyReq {
    @NotBlank(message = "appId cannot be empty")
    private String appId;

    @NotBlank(message = "sceneId cannot be empty")
    private String sceneId;

    // Room id
    @NotBlank(message = "roomId cannot be empty")
    private String roomId;
}
