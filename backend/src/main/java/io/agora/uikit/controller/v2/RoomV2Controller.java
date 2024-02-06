package io.agora.uikit.controller.v2;

import io.agora.uikit.bean.dto.R;
import io.agora.uikit.bean.dto.v2.*;
import io.agora.uikit.bean.req.v2.*;
import io.agora.uikit.service.IRoomV2Service;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Validated
@RestController
@RequestMapping(value = "/v2/room", produces = MediaType.APPLICATION_JSON_VALUE)
public class RoomV2Controller {
    @Resource
    private IRoomV2Service roomService;

    @PostMapping("/create")
    @ResponseBody
    public R<RoomDto> create(@Validated @RequestBody RoomCreateReq roomCreateReq) throws Exception {
        RoomCreateDto roomCreateDto = roomService.create(roomCreateReq);
        return R.success(new RoomDto()
                .setAppId(roomCreateDto.getAppId())
                .setSceneId(roomCreateDto.getSceneId())
                .setRoomId(roomCreateDto.getRoomId())
                .setCreateTime(roomCreateDto.getCreateTime())
                .setUpdateTime(roomCreateDto.getUpdateTime())
                .setPayload(roomCreateDto.getPayload())
        );
    }

    @PostMapping("/destroy")
    @ResponseBody
    public R<RoomDto> destroy(@Validated @RequestBody RoomDestroyReq roomDestroyReq) throws Exception {
        roomService.destroy(roomDestroyReq);
        return R.success(new RoomDto().setRoomId(roomDestroyReq.getRoomId()));
    }

    @PostMapping("/update")
    @ResponseBody
    public R<Void> update(@Validated @RequestBody RoomUpdateReq updateReq) throws Exception {
        roomService.update(updateReq);
        return R.success(null);
    }

    @PostMapping("/list")
    @ResponseBody
    public R<RoomListDto<RoomListEntity>> list(@Validated @RequestBody RoomListReq roomListReq) throws Exception {
        if (roomListReq.getLastCreateTime() == null || roomListReq.getLastCreateTime() == 0) {
            roomListReq.setLastCreateTime(System.currentTimeMillis());
        }
        var roomList = roomService.getRoomList(roomListReq);
        return R.success(roomList);
    }

    @PostMapping("/query")
    @ResponseBody
    public R<RoomQueryDto> query(@Validated @RequestBody RoomQueryReq roomQueryReq) throws Exception {
        RoomQueryDto roomQueryDto = roomService.query(roomQueryReq);
        return R.success(roomQueryDto);
    }
}