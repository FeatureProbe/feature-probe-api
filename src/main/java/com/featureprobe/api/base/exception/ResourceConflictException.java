package com.featureprobe.api.base.exception;

import com.featureprobe.api.base.enums.ResourceType;

public class ResourceConflictException extends RuntimeException {
    ResourceType resourceType;

    public ResourceConflictException(ResourceType resourceType) {
        this.resourceType = resourceType;
    }
}
