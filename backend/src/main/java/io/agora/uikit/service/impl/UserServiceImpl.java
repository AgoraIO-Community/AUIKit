package io.agora.uikit.service.impl;

import io.agora.uikit.bean.dto.KickOutRuleDto;
import io.agora.uikit.bean.enums.ReturnCodeEnum;
import io.agora.uikit.bean.enums.RtcChannelRulesEnum;
import io.agora.uikit.bean.exception.BusinessException;
import io.agora.uikit.bean.req.UserKickOutReq;
import io.agora.uikit.config.WhitelistConfig;
import io.agora.uikit.service.IRtcChannelService;
import io.agora.uikit.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;

@Slf4j
@Service
public class UserServiceImpl implements IUserService {
    @Resource
    private IRtcChannelService rtcChannelService;

    @Resource
    private WhitelistConfig whitelistConfig;

    @Override
    public KickOutRuleDto kickOut(UserKickOutReq req) throws Exception {
        var config = whitelistConfig.getRtcKickOutAuthFromWhitelist(req.getAppId());
        if (config == null && req.getBasicAuth() == null) {
            log.info("kick out user:{},cname:{} failed,rtc kick out auth from whitelist", req.getUid(), req.getRoomId());
            throw new BusinessException(HttpStatus.OK.value(), ReturnCodeEnum.USER_KICK_OUT_AUTH_NOT_FOUND_ERROR);
        } else {
            if (config != null) {
                req.setBasicAuth(config.getBasicAuth());
            }
        }

        try {
            rtcChannelService.kickOut(req.getBasicAuth(), req.getAppId(), req.getRoomId(), req.getUid(), 60, new ArrayList<>() {
                {
                    add(RtcChannelRulesEnum.JOIN_CHANNEL.getRule());
                }
            });

            log.info("kick out user:{},cname:{} successfully", req.getUid(), req.getRoomId());
            return new KickOutRuleDto().setUid(req.getUid());
        } catch (Exception ex) {
            log.info("failed to kick out user:{},cname:{},err:{}", req.getUid(), req.getRoomId(), ex.toString());
            throw new BusinessException(HttpStatus.OK.value(), ReturnCodeEnum.USER_KICK_OUT_ERROR);
        }
    }
}
