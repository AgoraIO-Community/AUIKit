package io.agora.uikit.service;

import io.agora.uikit.bean.dto.KickOutRuleDto;
import io.agora.uikit.bean.req.UserKickOutReq;

public interface IUserService {
    KickOutRuleDto kickOut(UserKickOutReq req) throws Exception;
}
