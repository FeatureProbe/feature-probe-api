package com.featureprobe.api.service;

import com.featureprobe.api.auth.TokenHelper;
import com.featureprobe.api.base.enums.ResourceType;
import com.featureprobe.api.base.exception.ResourceConflictException;
import com.featureprobe.api.base.exception.ResourceNotFoundException;
import com.featureprobe.api.base.exception.ResourceOverflowException;
import com.featureprobe.api.dto.EnvironmentCreateRequest;
import com.featureprobe.api.dto.EnvironmentQueryRequest;
import com.featureprobe.api.dto.EnvironmentResponse;
import com.featureprobe.api.dto.EnvironmentUpdateRequest;
import com.featureprobe.api.entity.Environment;
import com.featureprobe.api.entity.Project;
import com.featureprobe.api.entity.Targeting;
import com.featureprobe.api.entity.Toggle;
import com.featureprobe.api.mapper.EnvironmentMapper;
import com.featureprobe.api.model.TargetingContent;
import com.featureprobe.api.repository.EnvironmentRepository;
import com.featureprobe.api.repository.ProjectRepository;
import com.featureprobe.api.repository.TargetingRepository;
import com.featureprobe.api.repository.ToggleRepository;
import com.featureprobe.api.service.aspect.Archived;
import com.featureprobe.api.util.SdkKeyGenerateUtil;
import com.featureprobe.sdk.server.FPUser;
import com.featureprobe.sdk.server.FeatureProbe;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class EnvironmentService {

    private EnvironmentRepository environmentRepository;

    private ProjectRepository projectRepository;

    private ToggleRepository toggleRepository;

    private TargetingRepository targetingRepository;

    private FeatureProbe featureProbe;

    @PersistenceContext
    public EntityManager entityManager;

    private static final String LIMITER_TOGGLE_KEY = "FeatureProbe_env_limiter";

    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value="all_sdk_key_map", allEntries=true)
    public EnvironmentResponse create(String projectKey, EnvironmentCreateRequest createRequest) {
        validateLimit(projectKey);
        Project project = projectRepository.findByKey(projectKey).get();
        Environment environment = EnvironmentMapper.INSTANCE.requestToEntity(createRequest);
        environment.setServerSdkKey(SdkKeyGenerateUtil.getServerSdkKey());
        environment.setClientSdkKey(SdkKeyGenerateUtil.getClientSdkKey());
        environment.setProject(project);
        initEnvironmentTargeting(projectKey, createRequest.getKey());
        return EnvironmentMapper.INSTANCE.entityToResponse(environmentRepository.save(environment));
    }

    @Archived
    @Transactional(rollbackFor = Exception.class)
    public EnvironmentResponse update(String projectKey, String environmentKey,
                                      EnvironmentUpdateRequest updateRequest) {
        boolean archived = updateRequest.getArchived() == null  ? false : !updateRequest.getArchived();
        Environment environment = environmentRepository.findByProjectKeyAndKeyAndArchived(projectKey,
                        environmentKey, archived).orElseThrow(() ->
                new ResourceNotFoundException(ResourceType.ENVIRONMENT, environmentKey));
        if (!StringUtils.equals(environment.getName(), updateRequest.getName())) {
            validateEnvironmentByName(projectKey, updateRequest.getName());
        }
        EnvironmentMapper.INSTANCE.mapEntity(updateRequest, environment);
        if (updateRequest.isResetServerSdk()) {
            environment.setServerSdkKey(SdkKeyGenerateUtil.getServerSdkKey());
        }
        if (updateRequest.isResetClientSdk()) {
            environment.setClientSdkKey(SdkKeyGenerateUtil.getClientSdkKey());
        }
        return EnvironmentMapper.INSTANCE.entityToResponse(environmentRepository.save(environment));
    }

    public EnvironmentResponse query(String projectKey, String environmentKey) {
        Environment environment = environmentRepository.findByProjectKeyAndKey(projectKey, environmentKey)
                .orElseThrow(() -> new ResourceNotFoundException(ResourceType.ENVIRONMENT, environmentKey));
        return EnvironmentMapper.INSTANCE.entityToResponse(environment);
    }

    @Archived
    public List<EnvironmentResponse> list(String projectKey, EnvironmentQueryRequest queryRequest) {
        List<Environment> environments = environmentRepository
                .findAllByProjectKeyAndArchived(projectKey, queryRequest.isArchived());
        return environments.stream().map(environment -> EnvironmentMapper.INSTANCE.entityToResponse(environment))
                .collect(Collectors.toList());
    }

    private void validateLimit(String projectKey) {
        long total = environmentRepository.countByProjectKey(projectKey);
        FPUser user = new FPUser(String.valueOf(TokenHelper.getUserId()));
        user.with("account", TokenHelper.getAccount());
        double limitNum = featureProbe.numberValue(LIMITER_TOGGLE_KEY, user , -1);
        if (limitNum > 0 && total >= limitNum) {
            throw new ResourceOverflowException(ResourceType.ENVIRONMENT);
        }
    }

    private void initEnvironmentTargeting(String projectKey, String environmentKey) {
        List<Toggle> toggles = toggleRepository.findAllByProjectKey(projectKey);
        List<Targeting> targetingList = toggles.stream().map(toggle -> createDefaultTargeting(toggle, environmentKey))
                .collect(Collectors.toList());
        targetingRepository.saveAll(targetingList);
    }

    private Targeting createDefaultTargeting(Toggle toggle, String environmentKey) {
        Targeting targeting = new Targeting();
        targeting.setDeleted(false);
        targeting.setVersion(1L);
        targeting.setProjectKey(toggle.getProjectKey());
        targeting.setDisabled(true);
        targeting.setContent(TargetingContent.newDefault(toggle).toJson());
        targeting.setToggleKey(toggle.getKey());
        targeting.setEnvironmentKey(environmentKey);
        return targeting;
    }

    private void validateEnvironmentByName(String projectKey, String name) {
        if (environmentRepository.existsByProjectKeyAndName(projectKey, name)) {
            throw new ResourceConflictException(ResourceType.ENVIRONMENT);
        }
    }
}
