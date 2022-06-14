package com.featureprobe.api.base.exception;

import com.featureprobe.api.base.enums.ResourceType;

public class ResourceUsingException extends RuntimeException {

    ResourceType resourceType;

    public ResourceUsingException(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

}
