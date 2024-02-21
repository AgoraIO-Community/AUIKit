package io.agora.uikit.bean.req.v2;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

@Data
@Accessors(chain = true)
public class RoomLeaveReq {
    // Room id
    @NotBlank(message = "roomId cannot be empty")
    private String roomId;

    // User id
    @NotBlank(message = "userId cannot be empty")
    private String userId;
}
