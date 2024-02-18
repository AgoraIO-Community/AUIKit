package io.agora.uikit.bean.req.v2;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
public class AddRoomReq {
    private String id;
    private String appId;

    private String sceneId;

    private String roomId;

    // payload
    private Map<String, Object> payload;

    // update time
    private long updateTime;
    // Create time
    private Long createTime;
}
