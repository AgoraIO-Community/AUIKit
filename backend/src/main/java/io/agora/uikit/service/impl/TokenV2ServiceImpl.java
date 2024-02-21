package io.agora.uikit.service.impl;

import io.agora.uikit.bean.config.TokenConfig;
import io.agora.uikit.bean.dto.TokenDto;
import io.agora.uikit.config.WhitelistConfig;
import io.agora.uikit.service.ITokenV2Service;
import io.agora.uikit.utils.TokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class TokenV2ServiceImpl implements ITokenV2Service {
    @Resource
    private TokenUtil tokenUtil;

    @Value("${token.expirationInSeconds}")
    private int tokenExpirationInSeconds = 3600 * 24;
    @Value("${token.privilegeExpirationInSeconds}")
    private int privilegeExpirationInSeconds = 3600 * 24;

    @Resource
    private WhitelistConfig whiteListConfig;


    @Override
    public String generateRtcToken(String appId, String appCert, String channelName, String account) {
        log.info("generateRtcToken, appId:{}, channelName:{}, account:{}", appId, channelName, account);
        return tokenUtil.generateRtcToken(appId, appCert, channelName, account,
                tokenExpirationInSeconds, privilegeExpirationInSeconds);
    }

    /**
     * Generate RTC token
     *
     * @param channelName
     * @param account
     * @return
     */
    @Override
    public String generateRtcToken006(String appId, String appCert, String channelName, String account) {
        log.info("generateRtcToken006, appId:{}, channelName:{}, account:{}", appId, channelName, account);
        return tokenUtil.generateRtcToken006(appId, appCert, channelName, account,
                privilegeExpirationInSeconds);
    }

    /**
     * Generate RTM token
     *
     * @param userId
     * @return
     */
    @Override
    public String generateRtmToken(String appId, String appCert, String userId) {
        log.info("generateRtmToken, appId:{}, userId:{}", appId, userId);
        return tokenUtil.generateRtmToken(appId, appCert, userId, tokenExpirationInSeconds);
    }

    /**
     * Generate RTM token
     *
     * @param userId
     * @return
     */
    @Override
    public String generateRtmToken006(String appId, String appCert, String userId) throws Exception {
        log.info("generateRtmToken006, appId:{}, userId:{}", appId, userId);
        return tokenUtil.generateRtmToken006(appId, appCert, userId, tokenExpirationInSeconds);
    }

    /**
     * Get token
     *
     * @param appId
     * @param appCert
     * @param channelName
     * @param account
     * @return
     */
    @Override
    public TokenDto generateToken(String appId, String appCert, String channelName, String account) {
        log.info("getToken, appId:{}, channelName:{}, account:{}", appId, channelName, account);
        TokenConfig token = whiteListConfig.getTokenFromWhitelist(appId, appCert);
        appId = token.getAppId();
        appCert = token.getAppCert();
        TokenDto tokenDto = new TokenDto();
        String rtcToken = generateRtcToken(appId, appCert, channelName, account);
        String rtmToken = generateRtmToken(appId, appCert, account);
        tokenDto.setAppId(appId);
        tokenDto.setRtcToken(rtcToken);
        tokenDto.setRtmToken(rtmToken);

        return tokenDto;
    }

    /**
     * Get token
     *
     * @param appId
     * @param appCert
     * @param channelName
     * @param account
     * @return
     */
    @Override
    public TokenDto generateToken006(String appId, String appCert, String channelName, String account) throws Exception {
        log.info("generateToken006, appId:{}, channelName:{}, account:{}", appId, channelName, account);
        TokenConfig token = whiteListConfig.getTokenFromWhitelist(appId, appCert);
        appId = token.getAppId();
        appCert = token.getAppCert();
        TokenDto tokenDto = new TokenDto();
        String rtcToken = generateRtcToken006(appId, appCert, channelName, account);
        String rtmToken = generateRtmToken006(appId, appCert, account);
        tokenDto.setAppId(appId);
        tokenDto.setRtcToken(rtcToken);
        tokenDto.setRtmToken(rtmToken);

        return tokenDto;
    }
}
