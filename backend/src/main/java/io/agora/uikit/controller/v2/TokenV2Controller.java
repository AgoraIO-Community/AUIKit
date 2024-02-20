package io.agora.uikit.controller.v2;

import io.agora.uikit.bean.dto.R;
import io.agora.uikit.bean.dto.TokenDto;
import io.agora.uikit.bean.req.TokenV2Req;
import io.agora.uikit.service.ITokenV2Service;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Validated
@RestController
@RequestMapping(value = "/v2", produces = MediaType.APPLICATION_JSON_VALUE)
public class TokenV2Controller {
    @Resource
    private ITokenV2Service tokenService;

    /**
     * Generate token
     *
     * @param tokenReq
     * @return
     */
    @PostMapping("/token/generate")
    @ResponseBody
    public R<TokenDto> generate(@Validated @RequestBody TokenV2Req tokenReq) {
        return R.success(tokenService.generateToken(tokenReq.getAppId(), tokenReq.getAppCert(), tokenReq.getChannelName(), tokenReq.getUserId()));
    }

    /**
     * Generate token
     *
     * @param tokenReq
     * @return
     */
    @PostMapping("/token006/generate")
    @ResponseBody
    public R<TokenDto> generateToken006(@Validated @RequestBody TokenV2Req tokenReq) throws Exception {
        return R.success(tokenService.generateToken006(tokenReq.getAppId(), tokenReq.getAppCert(), tokenReq.getChannelName(), tokenReq.getUserId()));
    }

    /**
     * Generate IM token
     *
     * @param tokenReq
     * @return
     */
    @PostMapping("/im/token")
    @ResponseBody
    public R<TokenDto> generateIMToken(@Validated @RequestBody TokenV2Req tokenReq) throws Exception {
        return R.success(tokenService.generateToken006(tokenReq.getAppId(), tokenReq.getAppCert(), tokenReq.getChannelName(), tokenReq.getUserId()));
    }
}