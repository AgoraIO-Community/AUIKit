package io.agora.uikit.bean.dto.v2;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Accessors(chain = true)
public class RoomListEntity {
    private String appId;
    private String sceneId;
    private String roomId;
    private Map<String, Object> payload;
    private Long updateTime;
    private Long createTime;
}
