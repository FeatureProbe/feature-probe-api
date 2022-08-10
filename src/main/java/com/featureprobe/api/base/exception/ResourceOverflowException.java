package com.featureprobe.api.base.exception;

import com.featureprobe.api.base.enums.ResourceType;

public class ResourceOverflowException extends RuntimeException {

    ResourceType resourceType;

    public ResourceOverflowException(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

}
