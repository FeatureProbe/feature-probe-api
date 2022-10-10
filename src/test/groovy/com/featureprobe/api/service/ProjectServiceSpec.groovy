package com.featureprobe.api.service

import com.featureprobe.api.base.enums.ValidateTypeEnum
import com.featureprobe.api.base.exception.ResourceConflictException
import com.featureprobe.api.dto.ApprovalSettings
import com.featureprobe.api.dto.PreferenceCreateRequest
import com.featureprobe.api.dto.ProjectCreateRequest
import com.featureprobe.api.dto.ProjectQueryRequest
import com.featureprobe.api.dto.ProjectUpdateRequest
import com.featureprobe.api.entity.Environment
import com.featureprobe.api.entity.Project
import com.featureprobe.api.repository.EnvironmentRepository
import com.featureprobe.api.repository.ProjectRepository
import com.featureprobe.api.repository.TargetingSketchRepository
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

    EnvironmentRepository environmentRepository

    TargetingSketchRepository targetingSketchRepository

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
        environmentRepository = Mock(EnvironmentRepository)
        targetingSketchRepository = Mock(TargetingSketchRepository)
        entityManager = Mock(SessionImpl)
        featureProbe = new FeatureProbe("_")
        projectService = new ProjectService(projectRepository, environmentRepository, targetingSketchRepository, featureProbe, entityManager)
        queryRequest = new ProjectQueryRequest(keyword: keyword)
        createRequest = new ProjectCreateRequest(name: projectName, key: projectKey)
        projectUpdateRequest = new ProjectUpdateRequest(name: "project_test_update", description: projectKey, archived: true)
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
                Optional.of(new Project(name: projectName, key: projectKey, environments: [new Environment()]))
        1 * environmentRepository.saveAll(_)
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

    def "validate projecte by name&key not exists"() {
        when:
        projectService.validateExists(ValidateTypeEnum.NAME, "name")
        projectService.validateExists(ValidateTypeEnum.KEY, "key")
        then:
        projectRepository.existsByName("name") >> false
        projectRepository.existsByKey("key") >> false
    }

    def "validate projecte by name is exists"() {
        when:
        projectService.validateExists(ValidateTypeEnum.NAME, "name")
        then:
        projectRepository.existsByName("name") >> true
        thrown ResourceConflictException
    }

    def "validate projecte by keu is exists"() {
        when:
        projectService.validateExists(ValidateTypeEnum.KEY, "key")
        then:
        projectRepository.existsByKey("key") >> true
        thrown ResourceConflictException
    }

    def "create preference"() {
        when:
        projectService.createPreference(projectKey, new PreferenceCreateRequest(approvalSettings: [new ApprovalSettings(environmentKey: "dev", enable: true, reviewers: ["Admin"])]))
        then:
        1 * environmentRepository.findAllByProjectKey(projectKey) >> [new Environment(key: "dev")]
        1 * environmentRepository.saveAll(_)
    }

    def "query approval settings list"(){
        when:
        def list = projectService.approvalSettingsList(projectKey)
        then:
        1 * environmentRepository.findAllByProjectKey(projectKey) >> [new Environment(key: "dev", enableApproval: true, reviewers: "[\"Admin\"]")]
        1 * targetingSketchRepository.existsByProjectKeyAndEnvironmentKeyAndStatus(_, _, _) >> true
        1 == list.size()
    }

    private setAuthContext(String account, String role) {
        SecurityContextHolder.setContext(new SecurityContextImpl(
                new JwtAuthenticationToken(new Jwt.Builder("21212").header("a","a")
                        .claim("role", role).claim("account", account).build())))
    }

}

