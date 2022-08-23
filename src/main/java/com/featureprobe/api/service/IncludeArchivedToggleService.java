package com.featureprobe.api.service;

import com.featureprobe.api.base.enums.ResourceType;
import com.featureprobe.api.base.enums.ValidateTypeEnum;
import com.featureprobe.api.base.exception.ResourceConflictException;
import com.featureprobe.api.repository.ToggleRepository;
import com.featureprobe.api.service.aspect.Archived;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@AllArgsConstructor
@Service
public class IncludeArchivedToggleService {

    private ToggleRepository toggleRepository;

    @PersistenceContext
    public EntityManager entityManager;

    @Archived
    public void validateIncludeArchivedToggle(String projectKey, ValidateTypeEnum type, String value) {
        switch (type) {
            case KEY:
                validateIncludeArchivedToggleByName(projectKey, value);
                break;
            case NAME:
                validateIncludeArchivedToggleByKey(projectKey, value);
                break;
            default:
                break;
        }
    }

    @Archived
    public void validateIncludeArchivedToggleByName(String projectKey, String name) {
        if (toggleRepository.existsByProjectKeyAndName(projectKey, name)) {
            throw new ResourceConflictException(ResourceType.ENVIRONMENT);
        }
    }

    @Archived
    public void validateIncludeArchivedToggleByKey(String projectKey, String key) {
        if(toggleRepository.existsByProjectKeyAndKey(projectKey, key)) {
            throw new ResourceConflictException(ResourceType.ENVIRONMENT);
        }
    }

}
