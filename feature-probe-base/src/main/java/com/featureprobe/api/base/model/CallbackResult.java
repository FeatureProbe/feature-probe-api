package com.featureprobe.api.base.model;

import lombok.Data;

@Data
public class CallbackResult {

    private boolean isSuccess;

    private int statusCode;

    private String requestBody;

    private String responseBody;

    private String errorMessage;

}
