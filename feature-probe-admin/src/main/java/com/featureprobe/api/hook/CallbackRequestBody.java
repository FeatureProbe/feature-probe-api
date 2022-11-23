package com.featureprobe.api.hook;

import lombok.Data;

@Data
public class CallbackRequestBody {

    private String resource;

    private String action;

    private String operator;

    private Long timestamp;

    private String projectKey;

    private String environmentKey;

    private String toggleKey;

    private String segmentKey;

    private Object data;

}
