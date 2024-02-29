package io.agora.uikit.service;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import io.agora.uikit.bean.dto.v2.*;
import io.agora.uikit.bean.req.v2.ChatRoomAPICreateChatRoomReq;
import io.agora.uikit.bean.req.v2.ChatRoomAPICreateUserReq;
import io.agora.uikit.bean.req.v2.ChatRoomAPIGetAppTokenReq;
import io.agora.uikit.bean.req.v2.ChatRoomAPIGetUserTokenReq;

public interface IEMAPIService {
    @RequestLine("POST /{org_name}/{app_name}/token")
    @Headers("Content-Type:application/json")
    ChatRoomAPIGetAppTokenDto GetAppToken(ChatRoomAPIGetAppTokenReq req, @Param("org_name") String orgName, @Param("app_name") String appName);

    @RequestLine("POST /{org_name}/{app_name}/token")
    @Headers({"Authorization:Bearer {token}"})
    ChatRoomAPIGetUserTokenDto GetUserToken(ChatRoomAPIGetUserTokenReq req, @Param("org_name") String orgName, @Param("app_name") String appName, @Param("token") String token);


    @RequestLine("POST /{org_name}/{app_name}/users")
    @Headers({"Authorization:Bearer {token}"})
    ChatRoomAPICreateUserDto CreateUser(ChatRoomAPICreateUserReq req, @Param("org_name") String orgName, @Param("app_name") String appName, @Param("token") String token);


    @RequestLine("GET /{org_name}/{app_name}/users/{username}")
    @Headers({"Authorization:Bearer {token}"})
    ChatRoomAPIQueryUserDto QueryUser(@Param("org_name") String orgName, @Param("app_name") String appName, @Param("username") String username, @Param("token") String token);

    @RequestLine("POST /{org_name}/{app_name}/chatrooms")
    @Headers({"Authorization:Bearer {token}"})
    ChatRoomAPICreateChatRoomDto CreateChatRoom(ChatRoomAPICreateChatRoomReq req, @Param("org_name") String orgName, @Param("app_name") String appName, @Param("token") String token);
}
