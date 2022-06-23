package com.featureprobe.api.service

import com.featureprobe.api.base.exception.ResourceNotFoundException
import com.featureprobe.api.dto.TargetingRequest
import com.featureprobe.api.entity.Targeting
import com.featureprobe.api.mapper.JsonMapper
import com.featureprobe.api.model.TargetingContent
import com.featureprobe.api.repository.SegmentRepository
import com.featureprobe.api.repository.TargetingRepository
import com.featureprobe.api.repository.TargetingSegmentRepository
import com.featureprobe.api.repository.TargetingVersionRepository
import spock.lang.Specification
import spock.lang.Title

@Title("Targeting Unit Test")
class TargetingServiceSpec extends Specification {

    TargetingService targetingService;

    TargetingRepository targetingRepository
    SegmentRepository segmentRepository
    TargetingSegmentRepository targetingSegmentRepository
    TargetingVersionRepository targetingVersionRepository

    def projectKey
    def environmentKey
    def toggleKey
    def content

    def setup() {
        targetingRepository = Mock(TargetingRepository)
        segmentRepository = Mock(SegmentRepository)
        targetingSegmentRepository = Mock(TargetingSegmentRepository)
        targetingVersionRepository = Mock(TargetingVersionRepository)
        targetingService = new TargetingService(targetingRepository, segmentRepository,
                targetingSegmentRepository, targetingVersionRepository)
        projectKey = "feature_probe"
        environmentKey = "test"
        toggleKey = "feature_toggle_unit_test"
        content = "{\"rules\":[{\"conditions\":[{\"type\":\"string\",\"subject\":\"city\",\"predicate\":\"is one of\"," +
                "\"objects\":[\"Paris\"]},{\"type\":\"segment\",\"predicate\":\"in\",\"objects\":[\"jjj\"," +
                "\"test_users\",\"snapshot_users\"]}],\"name\":\"Users in Paris\",\"serve\":{\"select\":2}}," +
                "{\"conditions\":[{\"type\":\"string\",\"subject\":\"city\",\"predicate\":\"is one of\"," +
                "\"objects\":[\"Lille\"]}],\"name\":\"Users in Lille\",\"serve\":{\"select\":1}}]," +
                "\"disabledServe\":{\"select\":0},\"defaultServe\":{\"select\":0},\"variations\":[{\"value\":\"7\"," +
                "\"name\":\"discount 0.7\",\"description\":\"\"},{\"value\":\"8\",\"name\":\"discount 0.8\"," +
                "\"description\":\"\"},{\"value\":\"9\",\"name\":\"discount 0.9\",\"description\":\"\"}]}"
    }

    def "update targeting"() {
        when:
        TargetingRequest targetingRequest = new TargetingRequest()
        TargetingContent targetingContent = JsonMapper.toObject(content, TargetingContent.class);
        targetingRequest.setContent(targetingContent)
        targetingRequest.setDisabled(false)
        def ret = targetingService.update(projectKey, environmentKey, toggleKey, targetingRequest)
        then:
        segmentRepository.existsByProjectKeyAndKey(projectKey, _) >> true
        1 * targetingRepository.findByProjectKeyAndEnvironmentKeyAndToggleKey(projectKey, environmentKey, toggleKey) >>
                Optional.of(new Targeting(id: 1, toggleKey: toggleKey, environmentKey: environmentKey,
                        content: "", disabled: true, version: 1))
        1 * targetingSegmentRepository.deleteByTargetingId(1)
        1 * targetingSegmentRepository.saveAll(_)
        1 * targetingRepository.save(_) >> new Targeting(toggleKey: toggleKey, environmentKey: environmentKey,
                content: content, disabled: false, version: 2)
        1 * targetingVersionRepository.save(_)
        with(ret) {
            content == it.content
            false == it.disabled
        }
    }

    def "update targeting segment not found" () {
        when:
        TargetingRequest targetingRequest = new TargetingRequest()
        TargetingContent targetingContent = JsonMapper.toObject(content, TargetingContent.class);
        targetingRequest.setContent(targetingContent)
        targetingRequest.setDisabled(false)
        targetingService.update(projectKey, environmentKey, toggleKey, targetingRequest)
        then:
        segmentRepository.existsByProjectKeyAndKey(projectKey, _) >> false
        then:
        thrown(ResourceNotFoundException)
    }


    def "query targeting by key"() {
        when:
        def ret = targetingService.queryByKey(projectKey, environmentKey, toggleKey)
        then:
        1 * targetingRepository.findByProjectKeyAndEnvironmentKeyAndToggleKey(projectKey, environmentKey, toggleKey) >>
                Optional.of(new Targeting(toggleKey: toggleKey, environmentKey: environmentKey,
                        content: content, disabled: false))
        with(ret) {
            content == it.content
            false == it.disabled
        }
    }

}

