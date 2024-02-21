package io.agora.uikit.service;

import io.agora.uikit.bean.dto.v2.RoomCreateDto;
import io.agora.uikit.bean.dto.v2.RoomListDto;
import io.agora.uikit.bean.dto.v2.RoomListEntity;
import io.agora.uikit.bean.dto.v2.RoomQueryDto;
import io.agora.uikit.bean.req.v2.*;

public interface IRoomV2Service {
    RoomCreateDto create(RoomCreateReq roomCreateReq) throws Exception;

    void addRoomList(AddRoomReq addRoomReq) throws Exception;

    void update(RoomUpdateReq roomUpdateReq) throws Exception;

    void destroy(RoomDestroyReq roomDestroyReq) throws Exception;

    RoomListDto<RoomListEntity> getRoomList(RoomListReq roomListReq);

    RoomQueryDto query(RoomQueryReq roomQueryReq) throws Exception;

    void removeRoomList(RoomDestroyReq roomDestroyReq) throws Exception;
}
