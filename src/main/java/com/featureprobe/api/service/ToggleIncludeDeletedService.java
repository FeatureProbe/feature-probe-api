package com.featureprobe.api.service;

import com.featureprobe.api.base.enums.ResourceType;
import com.featureprobe.api.base.enums.ValidateTypeEnum;
import com.featureprobe.api.base.exception.ResourceConflictException;
import com.featureprobe.api.repository.ToggleRepository;
import com.featureprobe.api.service.aspect.IncludeDeleted;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Service
@AllArgsConstructor
public class ToggleIncludeDeletedService {

    private ToggleRepository toggleRepository;

    @PersistenceContext
    public EntityManager entityManager;

    public void validateExists(String projectKey, ValidateTypeEnum type, String  value) {
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
        if (toggleRepository.existsByProjectKeyAndKey(projectKey, key)) {
            throw new ResourceConflictException(ResourceType.TOGGLE);
        }
    }

    @IncludeDeleted
    public void validateNameIncludeDeleted(String projectKey, String name) {
        if (toggleRepository.existsByProjectKeyAndName(projectKey, name)) {
            throw new ResourceConflictException(ResourceType.TOGGLE);
        }
    }

}
