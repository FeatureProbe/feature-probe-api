package com.featureprobe.api.service

import com.featureprobe.api.base.enums.ValidateTypeEnum
import com.featureprobe.api.base.exception.ResourceConflictException
import com.featureprobe.api.dto.ProjectCreateRequest
import com.featureprobe.api.dto.ProjectQueryRequest
import com.featureprobe.api.dto.ProjectUpdateRequest
import com.featureprobe.api.entity.Environment
import com.featureprobe.api.entity.Project
import com.featureprobe.api.repository.ProjectRepository
import com.featureprobe.sdk.server.FeatureProbe
import org.hibernate.internal.SessionImpl
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.util.CollectionUtils
import spock.lang.Specification
import javax.persistence.EntityManager

class ProjectServiceSpec extends Specification {

    ProjectService projectService

    ProjectRepository projectRepository

    ProjectQueryRequest queryRequest

    ProjectCreateRequest createRequest

    ProjectUpdateRequest projectUpdateRequest

    FeatureProbe featureProbe

    EntityManager entityManager

    def projectKey
    def projectName
    def keyword

    def setup() {
        projectName = "test_project_name"
        projectKey = "test_project"
        keyword = "feature"
        projectRepository = Mock(ProjectRepository)
        entityManager = Mock(SessionImpl)
        featureProbe = new FeatureProbe("_")
        projectService = new ProjectService(projectRepository, featureProbe, entityManager)
        queryRequest = new ProjectQueryRequest(keyword: keyword)
        createRequest = new ProjectCreateRequest(name: projectName, key: projectKey)
        projectUpdateRequest = new ProjectUpdateRequest(name: "project_test_update", description: projectKey)
        setAuthContext("Admin", "ADMIN")
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
        1 * projectRepository.existsByKey(projectKey) >> false
        1 * projectRepository.existsByName(projectName) >> false
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
        1 * projectRepository.existsByName(projectUpdateRequest.name) >> false
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
        1 * projectRepository.existsByKey(projectKey) >> true
        then:
        thrown ResourceConflictException
    }

    def "check project name" () {
        when:
        projectService.validateExists(ValidateTypeEnum.NAME, projectName)
        then:
        1 * projectRepository.existsByName(projectName) >> true
        then:
        thrown ResourceConflictException
    }

    private setAuthContext(String account, String role) {
        SecurityContextHolder.setContext(new SecurityContextImpl(
                new JwtAuthenticationToken(new Jwt.Builder("21212").header("a","a")
                        .claim("role", role).claim("account", account).build())))
    }

}

