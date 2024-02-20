package io.agora.uikit.bean.exception;

import java.util.Map;

import io.agora.uikit.bean.enums.ReturnCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BusinessException extends RuntimeException {
    private Integer code;
    private String message;
    private Integer status = 500;
    private Map<Object, Object> ext;

    public BusinessException(String message) {
        super(message);
        this.message = message;
    }

    public BusinessException(Integer code) {
        this.code = code;
    }

    public BusinessException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public BusinessException(Integer status, Integer code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public BusinessException(Integer status, ReturnCodeEnum returnCodeEnum) {
        this.status = status;
        this.code = returnCodeEnum.getCode();
        this.message = returnCodeEnum.getMessage();
    }

    public BusinessException(ReturnCodeEnum returnCodeEnum) {
        this.code = returnCodeEnum.getCode();
        this.message = returnCodeEnum.getMessage();
    }
}
