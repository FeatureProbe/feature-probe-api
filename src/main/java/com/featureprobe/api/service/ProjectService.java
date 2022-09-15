package com.featureprobe.api.service;

import com.featureprobe.api.auth.TokenHelper;
import com.featureprobe.api.base.enums.ResourceType;
import com.featureprobe.api.base.enums.ValidateTypeEnum;
import com.featureprobe.api.base.exception.ResourceConflictException;
import com.featureprobe.api.base.exception.ResourceNotFoundException;
import com.featureprobe.api.base.exception.ResourceOverflowException;
import com.featureprobe.api.dto.ApprovalSettings;
import com.featureprobe.api.dto.PreferenceCreateRequest;
import com.featureprobe.api.dto.ProjectCreateRequest;
import com.featureprobe.api.dto.ProjectQueryRequest;
import com.featureprobe.api.dto.ProjectResponse;
import com.featureprobe.api.dto.ProjectUpdateRequest;
import com.featureprobe.api.entity.Environment;
import com.featureprobe.api.entity.Project;
import com.featureprobe.api.mapper.EnvironmentMapper;
import com.featureprobe.api.mapper.ProjectMapper;
import com.featureprobe.api.repository.EnvironmentRepository;
import com.featureprobe.api.repository.ProjectRepository;
import com.featureprobe.api.util.SdkKeyGenerateUtil;
import com.featureprobe.sdk.server.FPUser;
import com.featureprobe.sdk.server.FeatureProbe;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class ProjectService {

    private ProjectRepository projectRepository;

    private EnvironmentRepository environmentRepository;

    private FeatureProbe featureProbe;

    @PersistenceContext
    public EntityManager entityManager;

    private static final String LIMITER_TOGGLE_KEY = "FeatureProbe_project_limiter";

    public static final String ALL_SDK_KEYS_CACHE_KEY = ServerService.ALL_SDK_KEYS_CACHE_KEY;

    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames="all_sdk_key_map", key = "#root.target.ALL_SDK_KEYS_CACHE_KEY")
    public ProjectResponse create(ProjectCreateRequest createRequest) {
        validateLimit();
        return ProjectMapper.INSTANCE.entityToResponse(createProject(createRequest));
    }

    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames="all_sdk_key_map", key = "#root.target.ALL_SDK_KEYS_CACHE_KEY")
    public ProjectResponse update(String projectKey, ProjectUpdateRequest updateRequest) {
        Project project = projectRepository.findByKey(projectKey)
                .orElseThrow(() -> new ResourceNotFoundException(ResourceType.PROJECT, projectKey));
        if (updateRequest.getArchived() != null && updateRequest.getArchived()) {
            archiveAllEnvironments(project.getEnvironments());
        }
        if (!StringUtils.equals(project.getName(), updateRequest.getName())) {
            validateName(updateRequest.getName());
        }
        ProjectMapper.INSTANCE.mapEntity(updateRequest, project);
        return ProjectMapper.INSTANCE.entityToResponse(projectRepository.save(project));
    }

    @Transactional(rollbackFor = Exception.class)
    public void createPreference(String projectKey, PreferenceCreateRequest createRequest) {
        Map<String, ApprovalSettings> approvalSettingsMap = createRequest.getApprovalSettings().stream()
                .collect(Collectors.toMap(ApprovalSettings::getEnvironmentKey, Function.identity()));
        if (CollectionUtils.isNotEmpty(createRequest.getApprovalSettings())) {
            List<Environment> environments = environmentRepository.findAllByProjectKey(projectKey);
            environments.stream().forEach(environment -> EnvironmentMapper.INSTANCE
                    .mapEntity(approvalSettingsMap.get(environment.getKey()), environment));
            environmentRepository.saveAll(environments);
        }
    }

    public List<ApprovalSettings> approvalSettingsList(String projectKey) {
        List<Environment> environments = environmentRepository.findAllByProjectKey(projectKey);
        return environments.stream().map(environment ->
                EnvironmentMapper.INSTANCE.entityToApprovalSettings(environment)).collect(Collectors.toList());
    }

    private void archiveAllEnvironments(List<Environment> environments) {
        for(Environment environment : environments) {
            environment.setArchived(true);
        }
        environmentRepository.saveAll(environments);
    }

    private void validateLimit() {
        long total = projectRepository.count();
        FPUser user = new FPUser(String.valueOf(TokenHelper.getUserId()));
        user.with("account", TokenHelper.getAccount());
        double limitNum = featureProbe.numberValue(LIMITER_TOGGLE_KEY, user , -1);
        if (limitNum > 0 && total >= limitNum) {
            throw new ResourceOverflowException(ResourceType.PROJECT);
        }
    }

    private Project createProject(ProjectCreateRequest createRequest) {
        validateKey(createRequest.getKey());
        validateName(createRequest.getName());
        Project createProject = ProjectMapper.INSTANCE.requestToEntity(createRequest);
        createProject.setDeleted(false);
        createProject.setEnvironments(createDefaultEnv(createProject));
        Project project = projectRepository.save(createProject);
        return project;
    }

    public List<ProjectResponse> list(ProjectQueryRequest queryRequest) {
        List<Project> projects;
        if (StringUtils.isNotBlank(queryRequest.getKeyword())) {
            projects = projectRepository.findAllByNameContainsIgnoreCaseOrDescriptionContainsIgnoreCase(
                    queryRequest.getKeyword(), queryRequest.getKeyword());
        } else {
            projects = projectRepository.findAll();
        }
        return projects.stream().map(project ->
                ProjectMapper.INSTANCE.entityToResponse(project)).collect(Collectors.toList());
    }

    public ProjectResponse queryByKey(String key) {
        Project project = projectRepository.findByKey(key)
                .orElseThrow(() -> new ResourceNotFoundException(ResourceType.PROJECT, key));
        return ProjectMapper.INSTANCE.entityToResponse(project);
    }

    private List<Environment> createDefaultEnv(Project project) {
        List<Environment> environments = new ArrayList<>();
        environments.add(createOnlineEnv(project));
        return environments;
    }

    private Environment createOnlineEnv(Project project) {
        Environment onlineEnv = new Environment();
        onlineEnv.setName("online");
        onlineEnv.setKey("online");
        onlineEnv.setProject(project);
        onlineEnv.setClientSdkKey(SdkKeyGenerateUtil.getClientSdkKey());
        onlineEnv.setServerSdkKey(SdkKeyGenerateUtil.getServerSdkKey());
        return onlineEnv;
    }

    public void validateExists(ValidateTypeEnum type, String value) {

        switch (type) {
            case KEY:
                validateKey(value);
                break;
            case NAME:
                validateName(value);
                break;
            default:
                break;
        }

    }

    private void validateKey(String key) {
        if (projectRepository.existsByKey(key)) {
            throw new ResourceConflictException(ResourceType.PROJECT);
        }
    }

    private void validateName(String name) {
        if (projectRepository.existsByName(name)) {
            throw new ResourceConflictException(ResourceType.PROJECT);
        }
    }

}
