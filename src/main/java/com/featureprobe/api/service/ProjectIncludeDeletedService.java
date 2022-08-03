package com.featureprobe.api.service;

import com.featureprobe.api.base.enums.ResourceType;
import com.featureprobe.api.base.enums.ValidateTypeEnum;
import com.featureprobe.api.base.exception.ResourceConflictException;
import com.featureprobe.api.repository.ProjectRepository;
import com.featureprobe.api.service.aspect.IncludeDeleted;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Service
@AllArgsConstructor
public class ProjectIncludeDeletedService {

    private ProjectRepository projectRepository;

    @PersistenceContext
    public EntityManager entityManager;

    @IncludeDeleted
    public void validateExists(ValidateTypeEnum type, String value) {

        switch (type) {
            case KEY:
                validateKeyIncludeDeleted(value);
                break;
            case NAME:
                validateNameIncludeDeleted(value);
                break;
            default:
                break;
        }

    }

    @IncludeDeleted
    public void validateKeyIncludeDeleted(String key) {
        if (projectRepository.existsByKey(key)) {
            throw new ResourceConflictException(ResourceType.PROJECT);
        }
    }

    @IncludeDeleted
    public void validateNameIncludeDeleted(String name) {
        if (projectRepository.existsByName(name)) {
            throw new ResourceConflictException(ResourceType.PROJECT);
        }
    }

}
