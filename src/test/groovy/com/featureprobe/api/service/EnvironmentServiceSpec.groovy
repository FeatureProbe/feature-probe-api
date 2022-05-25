package com.featureprobe.api.service

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
import spock.lang.Specification

class EnvironmentServiceSpec extends Specification {


    ProjectRepository projectRepository

    EnvironmentRepository environmentRepository

    ToggleRepository toggleRepository

    TargetingRepository targetingRepository

    EnvironmentService environmentService

    EnvironmentCreateRequest createRequest

    EnvironmentUpdateRequest updateRequest

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
        environmentService = new EnvironmentService(environmentRepository, projectRepository,
                toggleRepository, targetingRepository)
        createRequest = new EnvironmentCreateRequest(name: environmentName, key: environmentKey)
        updateRequest = new EnvironmentUpdateRequest(name: environmentName)
    }

    def "Create environment"() {
        when:
        def ret = environmentService.create(projectKey, createRequest)
        then:
        1 * projectRepository.findByKey(projectKey) >>
                new Optional<>(new Project(name: projectName, key: projectKey))
        1 * environmentRepository.existsByProjectKeyAndKey(projectKey, environmentKey) >> false
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


    def "Create environment key exist"() {
        when:
        def ret = environmentService.create(projectKey, createRequest)
        then:
        1 * projectRepository.findByKey(projectKey) >>
                Optional.of(new Project(name: projectName, key: projectKey))
        1 * environmentRepository.existsByProjectKeyAndKey(projectKey, environmentKey) >> true
        then:
        thrown ResourceConflictException
    }


    def "Update environment"() {
        when:
        def ret = environmentService.update(projectKey, environmentKey, updateRequest)
        then:
        1 * environmentRepository.findByProjectKeyAndKey(projectKey, environmentKey) >>
                Optional.of(new Environment(name: environmentName, key: environmentKey))
        1 * environmentRepository.save(_) >> new Environment(name: environmentName, key: environmentKey,
                serverSdkKey: serverSdkKey, clientSdkKey: clientSdkKey)
        with(ret) {
            environmentName == it.name
            environmentKey == it.key
            serverSdkKey == it.serverSdkKey
            clientSdkKey == it.clientSdkKey
        }
    }

    def "test get sdk server key"() {
        given:
        environmentRepository.findByServerSdkKeyOrClientSdkKey("key1", "key1") >>
                Optional.of(new Environment(serverSdkKey: "key001"))

        when:
        def sdkServerKey = environmentService.getSdkServerKey("key1")

        then:
        "key001" == sdkServerKey

    }
}

