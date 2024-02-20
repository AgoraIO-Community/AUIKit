package io.agora.uikit.bean.enums;

import lombok.Getter;

@Getter
public enum RtcChannelRulesEnum {
    JOIN_CHANNEL("join_channel"),
    PUBLISH_AUDIO("publish_audio"),
    PUBLISH_VIDEO("publish_video");
    private final String rule;

    RtcChannelRulesEnum(String rule) {
        this.rule = rule;
    }
}
