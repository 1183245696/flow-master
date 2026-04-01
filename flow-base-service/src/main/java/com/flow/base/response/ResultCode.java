package com.flow.base.response;

import lombok.Getter;

@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),
    UNAUTHORIZED(401, "未认证，请先登录"),
    FORBIDDEN(403, "无接口请求权限"),
    NOT_FOUND(404, "资源不存在"),
    PARAM_ERROR(400, "参数校验失败"),
    SERVICE_UNAVAILABLE(503, "服务暂时不可用，请稍后重试"),
    BUSINESS_ERROR(422, "业务处理失败");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
