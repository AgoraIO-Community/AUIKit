package io.agora.uikit.service.impl;

import io.agora.uikit.bean.config.ChatRoomConfig;
import io.agora.uikit.bean.dto.v2.ChatRoomCreateDto;
import io.agora.uikit.bean.req.v2.ChatRoomAPICreateChatRoomReq;
import io.agora.uikit.bean.req.v2.ChatRoomAPICreateUserReq;
import io.agora.uikit.bean.req.v2.ChatRoomCreateReq;
import io.agora.uikit.config.WhitelistConfig;
import io.agora.uikit.service.IChatRoomAPIService;
import io.agora.uikit.service.IChatRoomV2Service;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.UUID;

@Slf4j
@Service
public class ChatRoomV2ServiceImpl implements IChatRoomV2Service {
    @Resource
    private IChatRoomAPIService chatRoomAPIService;

    @Resource
    private WhitelistConfig whiteListConfig;

    @Override
    public ChatRoomCreateDto Create(ChatRoomCreateReq req) throws Exception {
        log.info("create,req:{}", req);
        var imConfig = req.getImConfig();
        ChatRoomConfig chatRoomFromWhitelist = whiteListConfig.getChatRoomFromWhitelist(req.getAppId());
        if (chatRoomFromWhitelist != null) {
            if (imConfig == null) {
                imConfig = new ChatRoomCreateReq.ImConfig();
            }

            imConfig.setOrgName(chatRoomFromWhitelist.getOrgName());
            imConfig.setAppName(chatRoomFromWhitelist.getAppName());
            imConfig.setClientId(chatRoomFromWhitelist.getClientId());
            imConfig.setClientSecret(chatRoomFromWhitelist.getClientSecret());
        }
        if (imConfig == null) {
            throw new Exception("im config is null");
        }
        var appToken = getAppToken(imConfig.getOrgName(), imConfig.getAppName(), imConfig.getClientId(), imConfig.getClientSecret());
        log.info("acquire token:{}", appToken);
        var user = req.getUser();
        String userUuid = "";
        String userToken = "";
        String chatRoomId = "";
        switch (req.getType()) {
            // create user and chat room
            case 0:
                if (Strings.isBlank(user.getPassword())) {
                    user.setPassword(UUID.randomUUID().toString());
                }
                userUuid = createUser(imConfig.getOrgName(), imConfig.getAppName(), appToken, user.getUsername(), user.getPassword());
                userToken = getUserToken(imConfig.getOrgName(), imConfig.getAppName(), appToken, user.getUsername());
                chatRoomId = createRoom(imConfig.getOrgName(), imConfig.getAppName(), appToken, user.getUsername(), req.getChatRoomConfig());
                break;
            // create user
            case 1:
                if (Strings.isBlank(user.getPassword())) {
                    user.setPassword(UUID.randomUUID().toString());
                }
                userUuid = createUser(imConfig.getOrgName(), imConfig.getAppName(), appToken, user.getUsername(), user.getPassword());
                userToken = getUserToken(imConfig.getOrgName(), imConfig.getAppName(), appToken, user.getUsername());
                break;
            // create chatroom
            case 2:
                chatRoomId = createRoom(imConfig.getOrgName(), imConfig.getAppName(), appToken, user.getUsername(), req.getChatRoomConfig());
                break;
            default:
                throw new Exception("invalid type");
        }

        return new ChatRoomCreateDto()
                .setUserToken(userToken)
                .setUserUuid(userUuid)
                .setChatId(chatRoomId)
                .setAppKey(imConfig.getOrgName() + "#" + imConfig.getAppName());
    }

    private String createRoom(String orgName, String appName, String appToken, String ownerName, ChatRoomCreateReq.ChatRoomConfig chatRoomConfig) throws Exception {
        var createRoomAPIDto = chatRoomAPIService.createChatRoom(
                new ChatRoomAPICreateChatRoomReq()
                        .setName(chatRoomConfig.getName())
                        .setDescription(chatRoomConfig.getDescription())
                        .setMaxUsers(chatRoomConfig.getMaxUsers())
                        .setOwner(ownerName)
                        .setMembers(new ArrayList<>() {
                            {
                                add(ownerName);
                            }
                        })
                        .setCustom(chatRoomConfig.getCustom()),
                orgName, appName, appToken
        );

        return createRoomAPIDto.getRoomId();
    }

    public String getAppToken(String orgName, String appName, String clientID, String clientSecret) throws Exception {
        return chatRoomAPIService.getAppToken(orgName, appName, clientID, clientSecret);
    }

    private String createUser(String orgName, String appName, String appToken, String username, String password) throws Exception {
        try {
            var chatRoomAPIQueryUserDto = chatRoomAPIService.queryUser(orgName, appName, username, appToken);
            if (chatRoomAPIQueryUserDto != null) {
                return chatRoomAPIQueryUserDto.getUser().getUuid();
            }
        } catch (Exception ex) {
            log.info("query user err:{}", ex.getMessage());
        }
        return chatRoomAPIService.createUser(
                new ChatRoomAPICreateUserReq()
                        .setUsername(username)
                        .setPassword(password),
                orgName, appName, appToken
        ).getUser().getUuid();
    }

    public String getUserToken(String orgName, String appName, String appToken, String username) throws Exception {
        return chatRoomAPIService.getUserToken(username, orgName, appName, appToken);
    }
}
