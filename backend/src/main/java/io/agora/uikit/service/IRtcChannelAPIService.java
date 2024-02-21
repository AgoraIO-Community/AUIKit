package io.agora.uikit.service;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import io.agora.uikit.bean.dto.CreateKickOutRuleDto;
import io.agora.uikit.bean.req.CreateKickOutRule;

@Headers("Content-Type:application/json;charset=UTF-8")
public interface IRtcChannelAPIService {
    @RequestLine("POST /dev/v1/kicking-rule")
    @Headers({"Authorization:basic {basicAuth}"})
    CreateKickOutRuleDto createKickOutRule(CreateKickOutRule kickOutRule, @Param("basicAuth") String basicAuth);
}
