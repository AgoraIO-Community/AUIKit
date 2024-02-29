package io.agora.uikit.utils;

import org.springframework.stereotype.Component;

import io.agora.media.RtcTokenBuilder;
import io.agora.media.RtcTokenBuilder2;
import io.agora.media.RtcTokenBuilder2.Role;
import io.agora.rtm.RtmTokenBuilder;
import io.agora.rtm.RtmTokenBuilder2;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TokenUtil {
    private RtcTokenBuilder2 rtcTokenBuilder;
    private RtcTokenBuilder rtcToken006Builder;
    private RtmTokenBuilder2 rtmTokenBuilder;
    private RtmTokenBuilder rtmToken006Builder;

    public TokenUtil() {
        rtcTokenBuilder = new RtcTokenBuilder2();
        rtcToken006Builder = new RtcTokenBuilder();
        rtmTokenBuilder = new RtmTokenBuilder2();
        rtmToken006Builder = new RtmTokenBuilder();
    }

    /**
     * Generate RTC token
     * 
     * @param appId
     * @param appCertificate
     * @param channelName
     * @param account
     * @param tokenExpirationInSeconds
     * @param privilegeExpirationInSeconds
     * @return
     */
    public String generateRtcToken(String appId, String appCertificate, String channelName, String account,
            int tokenExpirationInSeconds, int privilegeExpirationInSeconds) {
        log.info(
                "generateRtcToken, appId:{}, channelName:{}, account:{}, tokenExpirationInSeconds:{}, privilegeExpirationInSeconds:{}",
                appId, channelName, account, tokenExpirationInSeconds, privilegeExpirationInSeconds);

        String token = rtcTokenBuilder.buildTokenWithUserAccount(appId,
                appCertificate, channelName, account,
                Role.ROLE_PUBLISHER, tokenExpirationInSeconds, privilegeExpirationInSeconds);
        return token;
    }

    /**
     * Generate RTC token
     * 
     * @param appId
     * @param appCertificate
     * @param channelName
     * @param account
     * @param privilegeExpirationInSeconds
     * @return
     */
    public String generateRtcToken006(String appId, String appCertificate, String channelName, String account,
            int privilegeExpirationInSeconds) {
        log.info(
                "generateRtcToken006, appId:{}, channelName:{}, account:{}, privilegeExpirationInSeconds:{}",
                appId, channelName, account, privilegeExpirationInSeconds);

        String token = rtcToken006Builder.buildTokenWithUserAccount(appId, appCertificate, channelName, account,
                RtcTokenBuilder.Role.Role_Publisher,
                (int) (System.currentTimeMillis() / 1000 + privilegeExpirationInSeconds));
        return token;
    }

    /**
     * Generate RTM token
     * 
     * @param appId
     * @param appCertificate
     * @param userId
     * @param tokenExpirationInSeconds
     * @return
     */
    public String generateRtmToken(String appId, String appCertificate, String userId, int tokenExpirationInSeconds) {
        log.info("generateRtmToken, appId:{}, userId:{}, tokenExpirationInSeconds:{}", appId, userId,
                tokenExpirationInSeconds);

        String token = rtmTokenBuilder.buildToken(appId, appCertificate, userId,
                tokenExpirationInSeconds);
        return token;
    }

    /**
     * Generate RTM token
     * 
     * @param appId
     * @param appCertificate
     * @param userId
     * @param tokenExpirationInSeconds
     * @return
     */
    public String generateRtmToken006(String appId, String appCertificate, String userId, int tokenExpirationInSeconds)
            throws Exception {
        log.info("generateRtmToken006, appId:{}, userId:{}, tokenExpirationInSeconds:{}", appId, userId,
                tokenExpirationInSeconds);

        String token = rtmToken006Builder.buildToken(appId, appCertificate, userId,
                RtmTokenBuilder.Role.Rtm_User, (int) (System.currentTimeMillis() / 1000 + tokenExpirationInSeconds));
        return token;
    }
}
