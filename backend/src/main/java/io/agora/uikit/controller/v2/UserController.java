package io.agora.uikit.controller.v2;

import io.agora.uikit.bean.dto.KickOutRuleDto;
import io.agora.uikit.bean.dto.R;
import io.agora.uikit.bean.req.UserKickOutReq;
import io.agora.uikit.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@Validated
@RestController
@RequestMapping(value = "/v2/users", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {
    @Resource
    private IUserService userService;

    @PostMapping("/kickOut")
    @ResponseBody
    public R<KickOutRuleDto> kickOut(@Validated @RequestBody UserKickOutReq req) throws Exception {
        log.info("kick out req:{}", req);
        KickOutRuleDto kickOutRuleDto = userService.kickOut(req);
        return R.success(kickOutRuleDto);
    }
}
