package com.flow.base.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应包装器 R<T>
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class R<T> implements Serializable {

    private Integer code;
    private String message;
    private T data;
    private Long timestamp;

    private R() {
        this.timestamp = System.currentTimeMillis();
    }

    // ---- 成功 ----

    public static <T> R<T> ok() {
        R<T> r = new R<>();
        r.code = ResultCode.SUCCESS.getCode();
        r.message = ResultCode.SUCCESS.getMessage();
        return r;
    }

    public static <T> R<T> ok(T data) {
        R<T> r = ok();
        r.data = data;
        return r;
    }

    public static <T> R<T> ok(String message, T data) {
        R<T> r = ok(data);
        r.message = message;
        return r;
    }

    // ---- 失败 ----

    public static <T> R<T> fail(String message) {
        R<T> r = new R<>();
        r.code = ResultCode.FAIL.getCode();
        r.message = message;
        return r;
    }

    public static <T> R<T> fail(int code, String message) {
        R<T> r = new R<>();
        r.code = code;
        r.message = message;
        return r;
    }

    public static <T> R<T> fail(ResultCode resultCode) {
        R<T> r = new R<>();
        r.code = resultCode.getCode();
        r.message = resultCode.getMessage();
        return r;
    }

    // ---- 未授权 ----

    public static <T> R<T> unauthorized(String message) {
        return fail(ResultCode.UNAUTHORIZED.getCode(), message);
    }

    public static <T> R<T> forbidden(String message) {
        return fail(ResultCode.FORBIDDEN.getCode(), message);
    }

    // ---- 判断 ----

    public boolean isSuccess() {
        return ResultCode.SUCCESS.getCode().equals(this.code);
    }
}
