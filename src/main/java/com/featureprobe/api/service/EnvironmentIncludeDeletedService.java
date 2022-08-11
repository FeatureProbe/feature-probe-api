package com.featureprobe.api.service;

import com.featureprobe.api.base.enums.ResourceType;
import com.featureprobe.api.base.enums.ValidateTypeEnum;
import com.featureprobe.api.base.exception.ResourceConflictException;
import com.featureprobe.api.repository.EnvironmentRepository;
import com.featureprobe.api.service.aspect.IncludeDeleted;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EnvironmentIncludeDeletedService {

    private EnvironmentRepository environmentRepository;

    @IncludeDeleted
    public void validateExists(String projectKey, ValidateTypeEnum type, String value) {
        switch (type) {
            case KEY:
                validateKeyIncludeDeleted(projectKey, value);
                break;
            case NAME:
                validateNameIncludeDeleted(projectKey, value);
                break;
            default:
                break;
        }

    }

    @IncludeDeleted
    public void validateKeyIncludeDeleted(String projectKey, String key) {
        if (environmentRepository.existsByProjectKeyAndKey(projectKey, key)) {
            throw new ResourceConflictException(ResourceType.ENVIRONMENT);
        }
    }

    @IncludeDeleted
    public void validateNameIncludeDeleted(String projectKey, String name) {
        if (environmentRepository.existsByProjectKeyAndName(projectKey, name)) {
            throw new ResourceConflictException(ResourceType.ENVIRONMENT);
        }
    }

}
