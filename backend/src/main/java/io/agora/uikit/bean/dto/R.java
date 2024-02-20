package io.agora.uikit.bean.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.agora.uikit.bean.enums.ReturnCodeEnum;
import lombok.Data;

/**
 * Api response
 */
@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class R<T> implements java.io.Serializable {
    private String message = ReturnCodeEnum.SUCCESS.getMessage();
    private Integer code = ReturnCodeEnum.SUCCESS.getCode();
    private T data;

    public R() {
        super();
    }

    public R(T data) {
        super();
        this.code = ReturnCodeEnum.SUCCESS.getCode();
        this.data = data;
    }

    public R(int code, String message) {
        super();
        this.message = message;
        this.code = code;
    }

    public R(T data, int code, String message) {
        super();
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public R(Throwable e) {
        super();
        this.code = ReturnCodeEnum.SERVER_ERROR.getCode();
        this.message = e.getMessage();
    }

    public static <T> R<T> build(ReturnCodeEnum retCodeEnum) {
        return new R<>(null, retCodeEnum.getCode(), retCodeEnum.getMessage());
    }

    public static <T> R<T> build(ReturnCodeEnum retCodeEnum, T data) {
        return new R<>(data, retCodeEnum.getCode(), retCodeEnum.getMessage());
    }

    /**
     * Error
     * 
     * @param <T>
     * @return
     */
    public static <T> R<T> error() {
        return new R<>(null, ReturnCodeEnum.SERVER_ERROR.getCode(), ReturnCodeEnum.SERVER_ERROR.getMessage());
    }

    /**
     * Error
     *
     * @param message
     * @param <T>
     * @return
     */
    public static <T> R<T> error(String message) {
        return new R<>(null, ReturnCodeEnum.SERVER_ERROR.getCode(), message);
    }

    /**
     * Error
     *
     * @param message
     * @param <T>
     * @return
     */
    public static <T> R<T> error(Integer code, String message) {
        return new R<>(null, code, message);
    }

    /**
     * Error
     *
     * @param returnCodeEnum
     * @param <T>
     * @return
     */
    public static <T> R<T> error(ReturnCodeEnum returnCodeEnum) {
        return new R<>(null, returnCodeEnum.getCode(), returnCodeEnum.getMessage());
    }

    /**
     * Success
     *
     * @param data
     * @param <T>
     * @return
     */
    public static <T> R<T> success(T data) {
        return new R<>(data, ReturnCodeEnum.SUCCESS.getCode(), ReturnCodeEnum.SUCCESS.getMessage());
    }

    /**
     * Success
     *
     * @param message
     * @param data
     * @param <T>
     * @return
     */
    public static <T> R<T> success(String message, T data) {
        return new R<>(data, ReturnCodeEnum.SUCCESS.getCode(), message);
    }

    /**
     * Check success
     * 
     * @return
     */
    @JsonIgnore
    public Boolean isSuccess() {
        return this.code.equals(ReturnCodeEnum.SUCCESS.getCode());
    }
}
