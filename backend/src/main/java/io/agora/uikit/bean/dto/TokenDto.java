package io.agora.uikit.bean.dto;

import lombok.Data;

@Data
public class TokenDto {
    // AppId
    private String appId;
    // RTC token
    private String rtcToken;
    // RTM token
    private String rtmToken;
}
