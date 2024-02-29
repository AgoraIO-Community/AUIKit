package io.agora.uikit.bean.req;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class TokenV2Req {
    // Channel name
    @NotBlank(message = "channelName cannot be empty")
    private String channelName;

    // User id
    @NotBlank(message = "userId cannot be empty")
    private String userId;

    @NotBlank(message = "appId cannot be empty")
    private String appId;

    private String appCert;
}
