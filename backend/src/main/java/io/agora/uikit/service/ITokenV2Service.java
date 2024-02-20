package io.agora.uikit.service;

import io.agora.uikit.bean.dto.TokenDto;

public interface ITokenV2Service {
    /**
     * Generate RTC token
     *
     * @param appId
     * @param appCert
     * @param channelName
     * @param account
     * @return
     */
    String generateRtcToken(String appId, String appCert, String channelName, String account);

    /**
     * Generate RTC token
     *
     * @param channelName
     * @param account
     * @return
     */
    String generateRtcToken006(String appId, String appCert, String channelName, String account);

    /**
     * Generate RTM token
     *
     * @param userId
     * @return
     */
    String generateRtmToken(String appId, String appCert, String userId);

    /**
     * Generate RTM token
     *
     * @param userId
     * @return
     */
    String generateRtmToken006(String appId, String appCert, String userId) throws Exception;

    /**
     * Generate token
     *
     * @param appId
     * @param appCert
     * @param channelName
     * @param account
     * @return
     */
    TokenDto generateToken(String appId, String appCert, String channelName, String account);

    /**
     * Generate token
     *
     * @param appId
     * @param appCert
     * @param channelName
     * @param account
     * @return
     */
    TokenDto generateToken006(String appId, String appCert, String channelName, String account) throws Exception;
}
