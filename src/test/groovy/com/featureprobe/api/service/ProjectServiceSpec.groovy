package com.featureprobe.api.service

import com.featureprobe.api.base.enums.ValidateTypeEnum
import com.featureprobe.api.base.exception.ResourceConflictException
import com.featureprobe.api.dto.ProjectCreateRequest
import com.featureprobe.api.dto.ProjectQueryRequest
import com.featureprobe.api.dto.ProjectUpdateRequest
import com.featureprobe.api.entity.Environment
import com.featureprobe.api.entity.Project
import com.featureprobe.api.repository.ProjectRepository
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.util.CollectionUtils
import spock.lang.Specification
import spock.lang.Title

@Title("Project Unit Test")
@SpringBootTest
class ProjectServiceSpec extends Specification {

    ProjectService projectService

    ProjectRepository projectRepository

    ProjectQueryRequest queryRequest

    ProjectCreateRequest createRequest

    ProjectUpdateRequest projectUpdateRequest

    def projectKey
    def projectName
    def keyword

    def setup() {
        projectName = "test_project_name"
        projectKey = "test_project"
        keyword = "feature"
        projectRepository = Mock(ProjectRepository)
        projectService = new ProjectService(projectRepository)
        queryRequest = new ProjectQueryRequest(keyword: keyword)
        createRequest = new ProjectCreateRequest(name: projectName, key: projectKey)
        projectUpdateRequest = new ProjectUpdateRequest(name: "project_test_update", description: projectKey)
    }

    def "project list"() {
        when:
        def ret = projectService.list(queryRequest)
        then:
        1 * projectRepository.findAllByNameContainsIgnoreCaseOrDescriptionContainsIgnoreCase(
                keyword, keyword) >> [new Project(name: projectName, key: projectKey)]
        with(ret) {
            !CollectionUtils.isEmpty(it)
        }
    }

    def "create project" () {
        when:
        def ret = projectService.create(createRequest)
        then:
        1 * projectRepository.countByKeyIncludeDeleted(projectKey) >> 0
        1 * projectRepository.countByNameIncludeDeleted(projectName) >> 0
        1 * projectRepository.save(_) >> new Project(name: projectName, key: projectKey,
                environments: [new Environment()])
        with(ret) {
            projectName == ret.name
            projectKey == ret.key
            1 == ret.environments.size()
        }
    }

    def "update project" () {
        when:
        def ret = projectService.update(projectKey, projectUpdateRequest)
        then:
        1 * projectRepository.findByKey(projectKey) >>
                Optional.of(new Project(name: projectName, key: projectKey))
        1 * projectRepository.countByNameIncludeDeleted(projectUpdateRequest.name) >> 0
        1 * projectRepository.save(_) >> new Project(name: projectName, key: projectKey)
        with(ret) {
            projectName == it.name
            projectKey == it.key
        }
    }

    def "check project key" () {
        when:
        projectService.validateExists(ValidateTypeEnum.KEY, projectKey)
        then:
        1 * projectRepository.countByKeyIncludeDeleted(projectKey) >> 1
        then:
        thrown ResourceConflictException
    }

    def "check project name" () {
        when:
        projectService.validateExists(ValidateTypeEnum.NAME, projectName)
        then:
        1 * projectRepository.countByNameIncludeDeleted(projectName) >> 1
        then:
        thrown ResourceConflictException
    }

}

