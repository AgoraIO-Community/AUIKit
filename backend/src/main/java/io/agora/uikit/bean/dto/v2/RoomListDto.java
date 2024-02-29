package io.agora.uikit.bean.dto.v2;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Accessors(chain = true)
public class RoomListDto<T> {
    private String appId;

    private String sceneId;
    // Page size
    private int pageSize;
    // Count
    private int count;
    // List
    private List<T> list;
}
