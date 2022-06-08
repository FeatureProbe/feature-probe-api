package com.featureprobe.api.service;

import com.featureprobe.api.base.enums.ResourceType;
import com.featureprobe.api.base.enums.ValidateTypeEnum;
import com.featureprobe.api.base.exception.ResourceConflictException;
import com.featureprobe.api.dto.ProjectCreateRequest;
import com.featureprobe.api.dto.ProjectQueryRequest;
import com.featureprobe.api.dto.ProjectResponse;
import com.featureprobe.api.dto.ProjectUpdateRequest;
import com.featureprobe.api.entity.Environment;
import com.featureprobe.api.entity.Project;
import com.featureprobe.api.mapper.ProjectMapper;
import com.featureprobe.api.repository.ProjectRepository;
import com.featureprobe.api.util.SdkKeyGenerateUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class ProjectService {

    private ProjectRepository projectRepository;

    @Transactional(rollbackFor = Exception.class)
    public ProjectResponse create(ProjectCreateRequest createRequest) {
        return ProjectMapper.INSTANCE.entityToResponse(createProject(createRequest));
    }

    @Transactional(rollbackFor = Exception.class)
    public ProjectResponse update(String projectKey, ProjectUpdateRequest updateRequest) {
        if (StringUtils.isNotBlank(updateRequest.getName())) {
            validateName(updateRequest.getName());
        }
        Project project = projectRepository.findByKey(projectKey).get();
        ProjectMapper.INSTANCE.mapEntity(updateRequest, project);
        return ProjectMapper.INSTANCE.entityToResponse(projectRepository.save(project));
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
        Project project = projectRepository.findByKey(key).get();
        return ProjectMapper.INSTANCE.entityToResponse(project);
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
        if (projectRepository.countByKeyIncludeDeleted(key) > 0) {
            throw new ResourceConflictException(ResourceType.PROJECT);
        }
    }

    private void validateName(String name) {
        if (projectRepository.countByNameIncludeDeleted(name) > 0) {
            throw new ResourceConflictException(ResourceType.PROJECT);
        }
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

}
