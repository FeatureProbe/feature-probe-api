package com.featureprobe.api.service;

import com.featureprobe.api.base.enums.ResourceType;
import com.featureprobe.api.base.exception.ResourceNotFoundException;
import com.featureprobe.api.dto.EnvironmentCreateRequest;
import com.featureprobe.api.dto.EnvironmentResponse;
import com.featureprobe.api.dto.EnvironmentUpdateRequest;
import com.featureprobe.api.dto.SdkKeyResponse;
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
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class EnvironmentService {

    private EnvironmentRepository environmentRepository;

    private ProjectRepository projectRepository;

    private ToggleRepository toggleRepository;

    private TargetingRepository targetingRepository;

    private EnvironmentIncludeDeletedService environmentIncludeDeletedService;

    public SdkKeyResponse queryAllSdkKeys() {
        SdkKeyResponse sdkKeyResponse = new SdkKeyResponse();
        List<Environment> environments = environmentRepository.findAll();
        environments.stream().forEach(environment -> sdkKeyResponse.put(environment.getClientSdkKey(),
                environment.getServerSdkKey()));
        return sdkKeyResponse;
    }

    @Transactional(rollbackFor = Exception.class)
    public EnvironmentResponse create(String projectKey, EnvironmentCreateRequest createRequest) {
        Project project = projectRepository.findByKey(projectKey).get();
        environmentIncludeDeletedService.validateKeyIncludeDeleted(projectKey, createRequest.getKey());
        environmentIncludeDeletedService.validateNameIncludeDeleted(projectKey, createRequest.getName());
        Environment environment = EnvironmentMapper.INSTANCE.requestToEntity(createRequest);
        environment.setServerSdkKey(SdkKeyGenerateUtil.getServerSdkKey());
        environment.setClientSdkKey(SdkKeyGenerateUtil.getClientSdkKey());
        environment.setProject(project);
        initEnvironmentTargeting(projectKey, createRequest.getKey());
        return EnvironmentMapper.INSTANCE.entityToResponse(environmentRepository.save(environment));
    }

    @Transactional(rollbackFor = Exception.class)
    public EnvironmentResponse update(String projectKey, String environmentKey,
                                      EnvironmentUpdateRequest updateRequest) {
        Environment environment = environmentRepository.findByProjectKeyAndKey(projectKey, environmentKey).get();
        if (!StringUtils.equals(environment.getName(), updateRequest.getName())) {
            environmentIncludeDeletedService.validateNameIncludeDeleted(projectKey, updateRequest.getName());
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

    public String getSdkServerKey(String serverKeyOrClientKey) {
        return environmentRepository.findByServerSdkKeyOrClientSdkKey(serverKeyOrClientKey, serverKeyOrClientKey)
                .orElseThrow(() -> new ResourceNotFoundException(ResourceType.ENVIRONMENT, serverKeyOrClientKey))
                .getServerSdkKey();
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

}
