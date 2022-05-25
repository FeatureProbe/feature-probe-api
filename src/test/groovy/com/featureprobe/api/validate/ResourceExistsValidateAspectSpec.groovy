package com.featureprobe.api.validate

import com.featureprobe.api.base.enums.ResourceType
import com.featureprobe.api.base.exception.ResourceNotFoundException
import com.featureprobe.api.repository.EnvironmentRepository
import com.featureprobe.api.repository.ProjectRepository
import com.featureprobe.api.repository.ToggleRepository
import spock.lang.Specification

class ResourceExistsValidateAspectSpec extends Specification {

    ProjectRepository projectRepository
    EnvironmentRepository environmentRepository
    ToggleRepository toggleRepository

    ResourceExistsValidateAspect resourceExistsValidateAspect
    def projectKey = "prj_test_key"
    def toggleKey = "toggle_key_test"
    def envKey = "env_key_test"

    def setup() {
        projectRepository = Mock(ProjectRepository)
        environmentRepository = Mock(EnvironmentRepository)
        toggleRepository = Mock(ToggleRepository)

        resourceExistsValidateAspect = new ResourceExistsValidateAspect(projectRepository, toggleRepository,
                environmentRepository)
    }



    def "validate project exists"() {
        given:
        projectRepository.existsByKey(projectKey) >> true

        when:
        resourceExistsValidateAspect.validateProjectExists(new ResourceKey(ResourceType.PROJECT, projectKey))

        then:
        noExceptionThrown()
    }

    def "validate project not exists"() {
        given:
        projectRepository.existsByKey(projectKey) >> false

        when:
        resourceExistsValidateAspect.validateProjectExists(new ResourceKey(ResourceType.PROJECT, projectKey))

        then:
        thrown(ResourceNotFoundException)
    }

    def "validate toggle exists"() {
        given:
        toggleRepository.existsByProjectKeyAndKey(projectKey, toggleKey) >> true

        when:
        resourceExistsValidateAspect.validateToggleExists(new ResourceKey(ResourceType.PROJECT, projectKey),
                [new ResourceKey(ResourceType.TOGGLE, toggleKey)])

        then:
        noExceptionThrown()
    }

    def "validate toggle not exists"() {
        given:
        toggleRepository.existsByProjectKeyAndKey(projectKey, toggleKey) >> false

        when:
        resourceExistsValidateAspect.validateToggleExists(new ResourceKey(ResourceType.PROJECT, projectKey),
                [new ResourceKey(ResourceType.TOGGLE, toggleKey)])

        then:
        thrown(ResourceNotFoundException)
    }

    def "validate environment exists"() {
        given:
        environmentRepository.existsByProjectKeyAndKey(projectKey, envKey) >> true

        when:
        resourceExistsValidateAspect.validateEnvironmentExists(new ResourceKey(ResourceType.PROJECT, projectKey),
                [new ResourceKey(ResourceType.ENVIRONMENT, envKey)])

        then:
        noExceptionThrown()
    }

    def "validate environment not exists"() {
        given:
        environmentRepository.existsByProjectKeyAndKey(projectKey, envKey) >> false

        when:
        resourceExistsValidateAspect.validateEnvironmentExists(new ResourceKey(ResourceType.PROJECT, projectKey),
                [new ResourceKey(ResourceType.ENVIRONMENT, envKey)])

        then:
        thrown(ResourceNotFoundException)
    }

}
