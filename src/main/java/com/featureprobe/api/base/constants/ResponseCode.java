package com.featureprobe.api.base.constants;

public enum ResponseCode {

    CONFLICT("conflict", "resource.error.conflict"),
    NOT_FOUND("not_found", "resource.error.not_found"),
    INVALID_REQUEST("invalid_request", "validate.invalid_request");

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
