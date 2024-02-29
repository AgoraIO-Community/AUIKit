package io.agora.uikit.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import io.agora.uikit.bean.dto.R;
import io.agora.uikit.bean.enums.ReturnCodeEnum;
import io.agora.uikit.bean.exception.BusinessException;
import io.agora.uikit.metric.PrometheusMetric;
import lombok.extern.slf4j.Slf4j;

/**
 * Global exception handler
 */
@Slf4j
@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {
    @Autowired
    private PrometheusMetric prometheusMetric;

    /**
     * Request method not supported exception
     * 
     * @param e
     * @param httpResponse
     * @param servletRequest
     * @return
     */
    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    public R<Object> HttpRequestMethodNotSupportedExceptionHandle(Exception e, HttpServletResponse httpResponse,
            HttpServletRequest servletRequest) {
        log.error("HttpRequestMethodNotSupportedExceptionHandle, unsupported method exception, {}",
                e.getClass().getName(), e);
        httpResponse.setStatus(HttpStatus.NOT_IMPLEMENTED.value());
        return R.error(ReturnCodeEnum.METHOD_ERROR);
    }

    /**
     * Parameter error
     * 
     * @param e
     * @param httpResponse
     * @param servletRequest
     * @return
     */
    @ExceptionHandler(value = MethodArgumentTypeMismatchException.class)
    public R<Object> MethodArgumentTypeMismatchExceptionHandle(Exception e, HttpServletResponse httpResponse,
            HttpServletRequest servletRequest) {
        log.error("MethodArgumentTypeMismatchExceptionHandle, param exception, {}", e.getClass().getName(), e);
        httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        return R.error(ReturnCodeEnum.PARAMS_ERROR);
    }

    /**
     * Parameter error
     * 
     * @param e
     * @param httpResponse
     * @param servletRequest
     * @return
     */
    @ExceptionHandler(value = { MethodArgumentNotValidException.class })
    public R<Object> MethodArgumentNotValidExceptionHandle(MethodArgumentNotValidException e,
            HttpServletResponse httpResponse, HttpServletRequest servletRequest) {
        String msg = "";
        for (ObjectError objectError : e.getBindingResult().getAllErrors()) {
            msg += objectError.getDefaultMessage() + "; ";
        }

        log.error("MethodArgumentNotValidExceptionHandle, param error, error:{}", msg);
        httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        return R.error(ReturnCodeEnum.PARAMS_ERROR.getCode(), msg);
    }

    /**
     * Parameter error
     * 
     * @param e
     * @param httpResponse
     * @param servletRequest
     * @return
     */
    @ExceptionHandler(value = { ConstraintViolationException.class })
    public R<Object> ConstraintViolationExceptionHandle(ConstraintViolationException e,
            HttpServletResponse httpResponse, HttpServletRequest servletRequest) {
        String msg = e.getMessage();
        log.error("ConstraintViolationExceptionHandle, param error, error:{}", msg);
        httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        return R.error(ReturnCodeEnum.PARAMS_ERROR.getCode(), msg);
    }

    /**
     * Parameter error
     * 
     * @param e
     * @param httpResponse
     * @param servletRequest
     * @return
     */
    @ExceptionHandler(value = BindException.class)
    public R<Object> BindExceptionHandle(BindException e, HttpServletResponse httpResponse,
            HttpServletRequest servletRequest) {
        String msg = e.getAllErrors().get(0).getDefaultMessage();
        log.error("BindExceptionHandle, param error, error:{}", msg);
        httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        return R.error(ReturnCodeEnum.PARAMS_ERROR.getCode(), msg);
    }

    /**
     * Parameter error
     * 
     * @param e
     * @param httpResponse
     * @param servletRequest
     * @return
     */
    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public R<Object> HttpMessageNotReadableExceptionHandle(HttpMessageNotReadableException e,
            HttpServletResponse httpResponse, HttpServletRequest servletRequest) {
        String msg = e.getMessage();
        log.error("HttpMessageNotReadableExceptionHandle, param error, error:{}", msg);
        httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        return R.error(ReturnCodeEnum.PARAMS_ERROR.getCode(), msg);
    }

    /**
     * Exception Handle
     * 
     * @param e
     * @param httpResponse
     * @param servletRequest
     * @return
     */
    @ExceptionHandler(value = Exception.class)
    public R<Object> ExceptionHandle(Exception e, HttpServletResponse httpResponse, HttpServletRequest servletRequest) {
        String msg = e.getMessage();
        log.error("ExceptionHandle, exception, {}, {}", e.getClass().getName(), msg, e);
        httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return R.error(ReturnCodeEnum.SERVER_ERROR.getCode(), msg);
    }

    /**
     * Business exception handle
     * 
     * @param e
     * @param httpResponse
     * @param servletRequest
     * @return
     */
    @ExceptionHandler(value = BusinessException.class)
    public R<Object> BusinessExceptionHandle(BusinessException e, HttpServletResponse httpResponse,
            HttpServletRequest servletRequest) {
        log.error("BusinessExceptionHandle, business exception, code:{}, error:{}", e.getCode(), e.getMessage());
        if (e.getStatus() != null) {
            httpResponse.setStatus(e.getStatus());
        } else {
            httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        // Report metric
        prometheusMetric.getHttpRequestsCounter()
                .labels(servletRequest.getRequestURI(), e.getCode().toString(), e.getMessage())
                .inc();
        return R.error(e.getCode(), e.getMessage());
    }
}