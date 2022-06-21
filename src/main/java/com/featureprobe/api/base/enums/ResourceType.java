package com.featureprobe.api.base.enums;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public enum ResourceType {
    PROJECT("projectKey"), TOGGLE("toggleKey"), ENVIRONMENT("environmentKey"),
    MEMBER("account"),SEGMENT("segment");

    private String paramName;

    ResourceType(String paramName) {
        this.paramName = paramName;
    }

    public String getParamName() {
        return paramName;
    }

    public static ResourceType of(String paramName) {
        return Arrays.stream(ResourceType.values()).filter(resourceType -> StringUtils.equals(resourceType.paramName,
                paramName)).findFirst().orElse(null);
    }
}
