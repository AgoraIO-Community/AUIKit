package io.agora.uikit.bean.dto.v2;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Accessors(chain = true)
public class RoomCreateDto {
    private String appId;
    private String sceneId;
    // Room id
    private String roomId;
    private Map<String, Object> payload;
    private long updateTime;
    private Long createTime;
}
