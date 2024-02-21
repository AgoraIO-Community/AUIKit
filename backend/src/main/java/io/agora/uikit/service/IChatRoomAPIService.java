package io.agora.uikit.service;

import io.agora.uikit.bean.dto.v2.ChatRoomAPICreateChatRoomDto;
import io.agora.uikit.bean.dto.v2.ChatRoomAPICreateUserDto;
import io.agora.uikit.bean.dto.v2.ChatRoomAPIQueryUserDto;
import io.agora.uikit.bean.req.v2.ChatRoomAPICreateChatRoomReq;
import io.agora.uikit.bean.req.v2.ChatRoomAPICreateUserReq;

public interface IChatRoomAPIService {
    String getAppToken(String orgName, String appName, String clientID, String clientSecret);

    String getUserToken(String username, String orgName, String appName, String token);


    ChatRoomAPICreateUserDto createUser(ChatRoomAPICreateUserReq req, String orgName, String appName, String token);


    ChatRoomAPIQueryUserDto queryUser(String orgName, String appName, String username, String token);

    ChatRoomAPICreateChatRoomDto createChatRoom(ChatRoomAPICreateChatRoomReq req, String orgName, String appName, String token);
}
