package io.agora.uikit.bean.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class TokenConfig {
    private String appId;
    private String appCert;
}
