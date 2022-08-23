package com.featureprobe.api.service

import com.featureprobe.api.base.enums.ValidateTypeEnum
import com.featureprobe.api.base.exception.ResourceConflictException
import com.featureprobe.api.dto.EnvironmentCreateRequest
import com.featureprobe.api.dto.EnvironmentUpdateRequest
import com.featureprobe.api.entity.Environment
import com.featureprobe.api.entity.Project
import com.featureprobe.api.entity.Toggle
import com.featureprobe.api.repository.EnvironmentRepository
import com.featureprobe.api.repository.ProjectRepository
import com.featureprobe.api.repository.TargetingRepository
import com.featureprobe.api.repository.ToggleRepository
import com.featureprobe.sdk.server.FeatureProbe
import org.hibernate.internal.SessionImpl
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import spock.lang.Specification
import javax.persistence.EntityManager

class EnvironmentServiceSpec extends Specification {


    ProjectRepository projectRepository

    EnvironmentRepository environmentRepository

    ToggleRepository toggleRepository

    TargetingRepository targetingRepository

    EnvironmentService environmentService

    EnvironmentCreateRequest createRequest

    EnvironmentUpdateRequest updateRequest

    FeatureProbe featureProbe
    EntityManager entityManager

    def projectName
    def projectKey
    def environmentName
    def environmentKey
    def serverSdkKey
    def clientSdkKey
    def toggleName
    def toggleKey

    def setup() {
        projectName = "test_project_name"
        projectKey = "test_project"
        environmentName = "test_env_name"
        environmentKey = "test_env"
        serverSdkKey = "server-232323"
        clientSdkKey = "client-dasda3"
        toggleName = "test_toggle_name"
        toggleKey = "test_toggle_key"
        projectRepository = Mock(ProjectRepository)
        environmentRepository = Mock(EnvironmentRepository)
        toggleRepository = Mock(ToggleRepository)
        targetingRepository = Mock(TargetingRepository)
        featureProbe = new FeatureProbe("_")
        entityManager = Mock(SessionImpl)
        environmentService = new EnvironmentService(environmentRepository, projectRepository,
                toggleRepository, targetingRepository, featureProbe, entityManager)
        createRequest = new EnvironmentCreateRequest(name: environmentName, key: environmentKey)
        updateRequest = new EnvironmentUpdateRequest(name: "env_test_update")
        setAuthContext("Admin", "ADMIN")
    }

    def "Create environment"() {
        when:
        def ret = environmentService.create(projectKey, createRequest)
        then:
        1 * projectRepository.findByKey(projectKey) >>
                new Optional<>(new Project(name: projectName, key: projectKey))
        1 * environmentRepository.save(_) >> new Environment(name: environmentName, key: environmentKey,
                serverSdkKey: serverSdkKey, clientSdkKey: clientSdkKey)
        1 * toggleRepository.findAllByProjectKey(projectKey) >> [new Toggle(name: toggleName,
                key: toggleKey, projectKey: toggleKey)]
        1 * targetingRepository.saveAll(_)
        with(ret) {
            environmentName == it.name
            environmentKey == it.key
            serverSdkKey == it.serverSdkKey
            clientSdkKey == it.clientSdkKey
        }
    }


    def "Update environment"() {
        when:
        def ret = environmentService.update(projectKey, environmentKey, updateRequest)
        then:
        1 * environmentRepository.findByProjectKeyAndKeyAndArchived(projectKey, environmentKey, false) >>
                Optional.of(new Environment(name: environmentName, key: environmentKey))
        1 * environmentRepository.existsByProjectKeyAndName(projectKey, updateRequest.name) >> false
        1 * environmentRepository.save(_) >> new Environment(name: environmentName, key: environmentKey,
                serverSdkKey: serverSdkKey, clientSdkKey: clientSdkKey)
        with(ret) {
            environmentName == it.name
            environmentKey == it.key
            serverSdkKey == it.serverSdkKey
            clientSdkKey == it.clientSdkKey
        }
    }

    def "query a environment" () {
        when:
        def environment = environmentService.query(projectKey, environmentKey)
        then:
        1 * environmentRepository.findByProjectKeyAndKey(projectKey, environmentKey) >>
                Optional.of(new Environment(key: environmentKey, serverSdkKey: "server-123", clientSdkKey: "client-123"))
        environmentKey == environment.key
        "server-123" == environment.serverSdkKey
        "client-123" == environment.clientSdkKey
    }

    private setAuthContext(String account, String role) {
        SecurityContextHolder.setContext(new SecurityContextImpl(
                new JwtAuthenticationToken(new Jwt.Builder("21212").header("a","a")
                        .claim("role", role).claim("account", account).build())))
    }
}

