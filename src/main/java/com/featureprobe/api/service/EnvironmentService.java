package com.featureprobe.api.service;

import com.featureprobe.api.auth.TokenHelper;
import com.featureprobe.api.base.enums.ResourceType;
import com.featureprobe.api.base.enums.ValidateTypeEnum;
import com.featureprobe.api.base.exception.ResourceConflictException;
import com.featureprobe.api.base.exception.ResourceNotFoundException;
import com.featureprobe.api.base.exception.ResourceOverflowException;
import com.featureprobe.api.dto.EnvironmentCreateRequest;
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

    public static final String ALL_SDK_KEYS_CACHE_KEY = ServerService.ALL_SDK_KEYS_CACHE_KEY;

    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames="all_sdk_key_map", key = "#root.target.ALL_SDK_KEYS_CACHE_KEY")
    public EnvironmentResponse create(String projectKey, EnvironmentCreateRequest createRequest) {
        validateLimit(projectKey);
        Project project = projectRepository.findByKey(projectKey).get();
        validateKey(projectKey, createRequest.getKey());
        validateName(projectKey, createRequest.getName());
        Environment environment = EnvironmentMapper.INSTANCE.requestToEntity(createRequest);
        environment.setServerSdkKey(SdkKeyGenerateUtil.getServerSdkKey());
        environment.setClientSdkKey(SdkKeyGenerateUtil.getClientSdkKey());
        environment.setProject(project);
        initEnvironmentTargeting(projectKey, createRequest.getKey());
        return EnvironmentMapper.INSTANCE.entityToResponse(environmentRepository.save(environment));
    }

    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames="all_sdk_key_map", key = "#root.target.ALL_SDK_KEYS_CACHE_KEY")
    public EnvironmentResponse update(String projectKey, String environmentKey,
                                      EnvironmentUpdateRequest updateRequest) {
        Environment environment = environmentRepository.findByProjectKeyAndKey(projectKey, environmentKey).get();
        if (!StringUtils.equals(environment.getName(), updateRequest.getName())) {
            validateName(projectKey, updateRequest.getName());
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

    public void validateExists(String projectKey, ValidateTypeEnum type, String value) {
        switch (type) {
            case KEY:
                validateKey(projectKey, value);
                break;
            case NAME:
                validateName(projectKey, value);
                break;
            default:
                break;
        }
    }

    private void validateKey(String projectKey, String key) {
        if (environmentRepository.existsByProjectKeyAndKey(projectKey, key)) {
            throw new ResourceConflictException(ResourceType.ENVIRONMENT);
        }
    }

    private void validateName(String projectKey, String name) {
        if (environmentRepository.existsByProjectKeyAndName(projectKey, name)) {
            throw new ResourceConflictException(ResourceType.ENVIRONMENT);
        }
    }
}
