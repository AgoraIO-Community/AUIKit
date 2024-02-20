package io.agora.uikit.bean.req.v2;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
@Accessors(chain = true)
public class RoomListReq {
    @NotBlank(message = "appId cannot be empty")
    private String appId;

    @NotBlank(message = "sceneId cannot be empty")
    private String sceneId;

    private Long lastCreateTime;

    // Page size
    @Max(value = 50, message = "pageSize must be less than or equal to 50")
    @Min(value = 1, message = "pageSize must be greater than 0")
    private int pageSize;
}
