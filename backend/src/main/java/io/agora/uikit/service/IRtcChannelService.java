package io.agora.uikit.service;

import java.util.List;

public interface IRtcChannelService {
    void kickOut(String basicAuth, String appId, String cname, Long uid, Integer time, List<String> privileges) throws Exception;
}
