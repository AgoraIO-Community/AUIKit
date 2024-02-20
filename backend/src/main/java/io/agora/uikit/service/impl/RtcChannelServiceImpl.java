package io.agora.uikit.service.impl;

import io.agora.uikit.bean.dto.CreateKickOutRuleDto;
import io.agora.uikit.bean.req.CreateKickOutRule;
import io.agora.uikit.service.IRtcChannelAPIService;
import io.agora.uikit.service.IRtcChannelService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class RtcChannelServiceImpl implements IRtcChannelService {
    @Resource
    private IRtcChannelAPIService rtcChannelAPIService;

    @Override
    public void kickOut(String basicAuth, String appId, String cname, Long uid, Integer time, List<String> privileges) throws Exception {
        CreateKickOutRule rule = new CreateKickOutRule()
                .setAppId(appId)
                .setCname(cname)
                .setIp("")
                .setUid(uid)
                .setTime(time)
                .setPrivileges(privileges);
        CreateKickOutRuleDto kickOutRuleDTO = rtcChannelAPIService.createKickOutRule(rule, basicAuth);
        if (kickOutRuleDTO == null) {
            throw new Exception("failed to kick out user");
        }
    }
}
