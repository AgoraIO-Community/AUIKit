package io.agora.uikit.bean.enums;

import lombok.Getter;

import java.util.Objects;

@Getter
public enum ReturnCodeEnum {
    SUCCESS(0, "Success"),
    ERROR(-1, "error"),
    PARAMS_ERROR(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    SERVER_ERROR(500, "Internal Server Error"),
    METHOD_ERROR(501, "Not Implemented"),

    ROOM_ACQUIRE_LOCK_ERROR(10101, "Acquire lock failed"),
    ROOM_RELEASE_LOCK_ERROR(10102, "Release lock failed"),
    ROOM_NOT_EXISTS_ERROR(10104, "Room not exists"),

    App_Cert_NOT_EXISTS_ERROR(10601, "App Cert not exists"),

    USER_KICK_OUT_ERROR(10701, "kick out user err"),

    USER_KICK_OUT_AUTH_NOT_FOUND_ERROR(107012, "kick out user auth not found"),
    ;

    private final Integer code;
    private final String message;

    ReturnCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static ReturnCodeEnum getEnumByCode(Integer code) {
        for (ReturnCodeEnum retCodeEnum : ReturnCodeEnum.values()) {
            if (Objects.equals(retCodeEnum.code, code)) {
                return retCodeEnum;
            }
        }
        return null;
    }
}
