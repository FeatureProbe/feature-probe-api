package com.featureprobe.api.service

import com.featureprobe.api.entity.Environment
import com.featureprobe.api.entity.Project
import com.featureprobe.api.entity.Segment
import com.featureprobe.api.entity.Targeting
import com.featureprobe.api.entity.Toggle
import com.featureprobe.api.repository.EnvironmentRepository
import com.featureprobe.api.repository.SegmentRepository
import com.featureprobe.api.repository.TargetingRepository
import com.featureprobe.api.repository.ToggleRepository
import org.hibernate.internal.SessionImpl
import spock.lang.Specification

import javax.persistence.EntityManager

class ServerServiceSpec extends Specification {

    EnvironmentRepository environmentRepository
    SegmentRepository segmentRepository
    ToggleRepository toggleRepository
    TargetingRepository targetingRepository
    ServerService serverService
    EntityManager entityManager

    def projectKey
    def environmentKey
    def toggleKey
    def toggleName
    def sdkKey
    def rules
    def segmentRules

    def setup() {
        environmentRepository = Mock(EnvironmentRepository)
        segmentRepository = Mock(SegmentRepository)
        toggleRepository = Mock(ToggleRepository)
        targetingRepository = Mock(TargetingRepository)
        entityManager = Mock(SessionImpl)
        serverService = new ServerService(environmentRepository, segmentRepository, toggleRepository, targetingRepository, entityManager)

        projectKey = "feature_probe"
        environmentKey = "test"
        toggleKey = "feature_toggle_unit_test"
        toggleName = "test_toggle"
        sdkKey = "server-123456"
        rules = "{\"rules\":[{\"conditions\":[{\"type\":\"string\",\"subject\":\"city\",\"predicate\":\"is one of\"," +
                "\"objects\":[\"Paris\"]},{\"type\":\"segment\",\"subject\":\"\",\"predicate\":\"is in\"," +
                "\"objects\":[\"test_segment\"]},{\"type\":\"number\",\"subject\":\"age\",\"predicate\":\"=\"," +
                "\"objects\":[\"20\"]},{\"type\":\"datetime\",\"subject\":\"\",\"predicate\":\"before\"," +
                "\"objects\":[\"2022-06-27T16:08:10+08:00\"]},{\"type\":\"semver\",\"subject\":\"\"," +
                "\"predicate\":\"before\",\"objects\":[\"1.0.1\"]}],\"name\":\"Paris women show 50% red buttons, 50% blue\"," +
                "\"serve\":{\"split\":[5000,5000,0]}}],\"disabledServe\":{\"select\":1},\"defaultServe\":{\"select\":1}," +
                "\"variations\":[{\"value\":\"red\",\"name\":\"Red Button\",\"description\":\"Set button color to Red\"}," +
                "{\"value\":\"blue\",\"name\":\"Blue Button\",\"description\":\"Set button color to Blue\"}]}"
        segmentRules = "[{\"conditions\":[{\"type\":\"string\",\"subject\":\"userId\",\"predicate\":\"is one of\"," +
                "\"objects\":[\"zhangsan\",\"wangwu\",\"lishi\",\"miss\"]},{\"type\":\"string\",\"subject\":\"userId\"," +
                "\"predicate\":\"is one of\",\"objects\":[\"huahau\",\"kaka\",\"dada\"]}],\"name\":\"\"}]"
    }

    def "test get sdk server key"() {
        given:
        environmentRepository.findByServerSdkKeyOrClientSdkKey("key1", "key1") >>
                Optional.of(new Environment(serverSdkKey: "key001"))

        when:
        def sdkServerKey = serverService.getSdkServerKey("key1")

        then:
        "key001" == sdkServerKey

    }

    def "query server toggles by server sdkKey"() {
        when:
        def serverResponse = serverService.queryServerTogglesByServerSdkKey(sdkKey)

        then:
        1 * environmentRepository.findByServerSdkKeyOrClientSdkKey(_, _) >>
                Optional.of(new Environment(project: new Project(key: projectKey, organizationId: 1), key: environmentKey, serverSdkKey: sdkKey))
        2 * environmentRepository.findByServerSdkKey(sdkKey) >>
                Optional.of(new Environment(project: new Project(key: projectKey, organizationId: 1), key: environmentKey))
        2 * segmentRepository.findAllByProjectKeyAndOrganizationId(projectKey, 1) >>
                [new Segment(projectKey: projectKey, key: "test_segment",
                        uniqueKey: projectKey + "\$test_segment", rules: segmentRules)]
        1 * toggleRepository.findAllByProjectKeyAndOrganizationId(projectKey, 1) >>
                [new Toggle(projectKey: projectKey, key: toggleKey, returnType: "string", clientAvailability: false)]
        1 * targetingRepository.findAllByProjectKeyAndEnvironmentKeyAndOrganizationId(projectKey, environmentKey, 1) >>
                [new Targeting(projectKey: projectKey, environmentKey: environmentKey,
                        toggleKey: toggleKey, content: rules, disabled: false)]
        with(serverResponse) {
            1 == toggles.size()
            1 == segments.size()
        }
    }

}
