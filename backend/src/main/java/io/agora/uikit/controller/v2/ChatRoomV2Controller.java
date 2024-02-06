package io.agora.uikit.controller.v2;

import io.agora.uikit.bean.dto.R;
import io.agora.uikit.bean.dto.v2.ChatRoomCreateDto;
import io.agora.uikit.bean.req.v2.ChatRoomCreateReq;
import io.agora.uikit.service.IChatRoomV2Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@Validated
@RestController
@RequestMapping(value = "/v2/chatRoom", produces = MediaType.APPLICATION_JSON_VALUE)
public class ChatRoomV2Controller {

    @Resource
    private IChatRoomV2Service chatRoomV2Service;


    @PostMapping("/create")
    @ResponseBody
    public R<ChatRoomCreateDto> create(@Validated @RequestBody ChatRoomCreateReq req) throws Exception {
        ChatRoomCreateDto createDto = chatRoomV2Service.Create(req);
        return R.success(createDto);
    }
}
