package io.agora.uikit.service.impl;

import io.agora.uikit.bean.dto.v2.*;
import io.agora.uikit.bean.req.v2.ChatRoomAPICreateChatRoomReq;
import io.agora.uikit.bean.req.v2.ChatRoomAPICreateUserReq;
import io.agora.uikit.bean.req.v2.ChatRoomAPIGetAppTokenReq;
import io.agora.uikit.bean.req.v2.ChatRoomAPIGetUserTokenReq;
import io.agora.uikit.service.IChatRoomAPIService;
import io.agora.uikit.service.IEMAPIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class ChatRoomAPIServiceImpl implements IChatRoomAPIService {
    @Resource
    private IEMAPIService emAPIService;

    @Override
    @Cacheable(cacheNames = "chatRoomAPIAppToken", key = "#clientID")
    public String getAppToken(String orgName, String appName, String clientID, String clientSecret) {
        log.info("getAppToken,orgName:{},appName:{},clientID:{},clientSecret:{}", orgName, appName, clientID, clientSecret);
        return emAPIService.GetAppToken(new ChatRoomAPIGetAppTokenReq()
                        .setClientId(clientID)
                        .setClientSecret(clientSecret),
                orgName, appName
        ).getAccessToken();
    }

    @Override
    @Cacheable(cacheNames = "chatRoomAPIUserToken", key = "#orgName+'_'+#appName+'_'+#username")
    public String getUserToken(String username, String orgName, String appName, String token) {
        log.info("getUserToken,username:{},orgName:{},appName:{}", username, orgName, appName);
        return emAPIService.GetUserToken(
                new ChatRoomAPIGetUserTokenReq().setUsername(username),
                orgName, appName, token
        ).getAccessToken();
    }

    @Override
    public ChatRoomAPICreateUserDto createUser(ChatRoomAPICreateUserReq req, String orgName, String appName, String token) {
        return emAPIService.CreateUser(req, orgName, appName, token);
    }

    @Override
    public ChatRoomAPIQueryUserDto queryUser(String orgName, String appName, String username, String token) {
        return emAPIService.QueryUser(orgName, appName, username, token);
    }

    @Override
    public ChatRoomAPICreateChatRoomDto createChatRoom(ChatRoomAPICreateChatRoomReq req, String orgName, String appName, String token) {
        return emAPIService.CreateChatRoom(req, orgName, appName, token);
    }
}
