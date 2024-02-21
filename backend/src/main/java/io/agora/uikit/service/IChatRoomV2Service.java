package io.agora.uikit.service;

import io.agora.uikit.bean.dto.v2.ChatRoomCreateDto;
import io.agora.uikit.bean.req.v2.ChatRoomCreateReq;

public interface IChatRoomV2Service {
    ChatRoomCreateDto Create(ChatRoomCreateReq req) throws Exception;
}
