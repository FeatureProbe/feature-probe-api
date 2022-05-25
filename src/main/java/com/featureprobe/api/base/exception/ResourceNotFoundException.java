package com.featureprobe.api.base.exception;

import com.featureprobe.api.base.enums.ResourceType;

public class ResourceNotFoundException extends RuntimeException {
    ResourceType resourceType;
    String resourceKey;

    public ResourceNotFoundException(ResourceType resourceType, String resourceKey) {
        this.resourceKey = resourceKey;
        this.resourceType = resourceType;
    }

}
