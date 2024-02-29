package io.agora.uikit.config;

import io.agora.uikit.bean.config.ChatRoomConfig;
import io.agora.uikit.bean.config.RTCKickOutAuthConfig;
import io.agora.uikit.bean.config.TokenConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "whitelist")
public class WhitelistConfig {
    private TokenConfig token;

    private ChatRoomConfig chatRoom;

    private RTCKickOutAuthConfig rtcKickOutAuth;

    public TokenConfig getTokenFromWhitelist(String appId, String appCert) {
        if (token.getAppId().equals(appId)) {
            return token;
        }
        return new TokenConfig()
                .setAppId(appId)
                .setAppCert(appCert);
    }


    public ChatRoomConfig getChatRoomFromWhitelist(String appId) {
        if (chatRoom.getAppId().equals(appId)) {
            return chatRoom;
        }
        return null;
    }

    public RTCKickOutAuthConfig getRtcKickOutAuthFromWhitelist(String appId) {
        if (rtcKickOutAuth.getAppId().equals(appId)) {
            return rtcKickOutAuth;
        }
        return null;
    }
}
