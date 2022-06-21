package com.featureprobe.api.base.constants;

public enum ResponseCode {

    CONFLICT("conflict", MessageKey.CONFLICT),
    NOT_FOUND("not_found", MessageKey.NOT_FOUND),
    INVALID_REQUEST("invalid_request", MessageKey.INVALID_REQUEST);

    private String code;

    private String messageKey;

    ResponseCode(String code, String messageKey) {
        this.code = code;
        this.messageKey = messageKey;
    }

    public String code() {
        return this.code;
    }

    public String messageKey() {
        return this.messageKey;
    }
}
