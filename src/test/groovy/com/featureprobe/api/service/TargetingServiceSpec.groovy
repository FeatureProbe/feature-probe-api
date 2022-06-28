package com.featureprobe.api.service

import com.featureprobe.api.base.exception.ResourceNotFoundException
import com.featureprobe.api.dto.TargetingRequest
import com.featureprobe.api.dto.TargetingVersionRequest
import com.featureprobe.api.entity.Targeting
import com.featureprobe.api.entity.TargetingVersion
import com.featureprobe.api.mapper.JsonMapper
import com.featureprobe.api.model.TargetingContent
import com.featureprobe.api.repository.SegmentRepository
import com.featureprobe.api.repository.TargetingRepository
import com.featureprobe.api.repository.TargetingSegmentRepository
import com.featureprobe.api.repository.TargetingVersionRepository
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
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
    def numberErrorContent
    def datetimeErrorContent
    def semVerErrorContent

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
                "\"objects\":[\"Paris\"]},{\"type\":\"segment\",\"subject\":\"\",\"predicate\":\"is in\"," +
                "\"objects\":[\"test_segment\"]},{\"type\":\"number\",\"subject\":\"age\",\"predicate\":\"=\"," +
                "\"objects\":[\"20\"]},{\"type\":\"datetime\",\"subject\":\"\",\"predicate\":\"before\"," +
                "\"objects\":[\"2022/06/27 16:08:10+08:00\"]},{\"type\":\"semver\",\"subject\":\"\"," +
                "\"predicate\":\"before\",\"objects\":[\"1.0.1\"]}],\"name\":\"Paris women show 50% red buttons, 50% blue\"," +
                "\"serve\":{\"split\":[5000,5000,0]}}],\"disabledServe\":{\"select\":1},\"defaultServe\":{\"select\":1}," +
                "\"variations\":[{\"value\":\"red\",\"name\":\"Red Button\",\"description\":\"Set button color to Red\"}," +
                "{\"value\":\"blue\",\"name\":\"Blue Button\",\"description\":\"Set button color to Blue\"}]}"
        numberErrorContent = "{\"rules\":[{\"conditions\":[{\"type\":\"string\",\"subject\":\"city\"," +
                "\"predicate\":\"is one of\",\"objects\":[\"Paris\"]},{\"type\":\"segment\",\"subject\":\"\"," +
                "\"predicate\":\"is in\",\"objects\":[\"test_segment\"]},{\"type\":\"number\",\"subject\":\"age\"," +
                "\"predicate\":\"=\",\"objects\":[\"20\",\"abc\"]},{\"type\":\"datetime\",\"subject\":\"\"," +
                "\"predicate\":\"before\",\"objects\":[\"2022/06/27 16:08:10+08:00\"]},{\"type\":\"semver\"," +
                "\"subject\":\"\",\"predicate\":\"before\",\"objects\":[\"1.0.1\"]}]," +
                "\"name\":\"Paris women show 50% red buttons, 50% blue\",\"serve\":{\"split\":[5000,5000,0]}}]," +
                "\"disabledServe\":{\"select\":1},\"defaultServe\":{\"select\":1},\"variations\":[{\"value\":\"red\"," +
                "\"name\":\"Red Button\",\"description\":\"Set button color to Red\"},{\"value\":\"blue\"," +
                "\"name\":\"Blue Button\",\"description\":\"Set button color to Blue\"}]}"
        datetimeErrorContent = "{\"rules\":[{\"conditions\":[{\"type\":\"string\",\"subject\":\"city\"," +
                "\"predicate\":\"is one of\",\"objects\":[\"Paris\"]},{\"type\":\"segment\",\"subject\":\"\"," +
                "\"predicate\":\"is in\",\"objects\":[\"test_segment\"]},{\"type\":\"number\",\"subject\":\"age\"," +
                "\"predicate\":\"=\",\"objects\":[\"20\",\"abc\"]},{\"type\":\"datetime\",\"subject\":\"\"," +
                "\"predicate\":\"before\",\"objects\":[\"2022/06/27 16:08:10+08:00\",\"2022/06/27 80:08:10+08:00\"]}," +
                "{\"type\":\"semver\",\"subject\":\"\",\"predicate\":\"before\",\"objects\":[\"1.0.1\"]}]," +
                "\"name\":\"Paris women show 50% red buttons, 50% blue\",\"serve\":{\"split\":[5000,5000,0]}}]," +
                "\"disabledServe\":{\"select\":1},\"defaultServe\":{\"select\":1},\"variations\":[{\"value\":\"red\"," +
                "\"name\":\"Red Button\",\"description\":\"Set button color to Red\"},{\"value\":\"blue\"," +
                "\"name\":\"Blue Button\",\"description\":\"Set button color to Blue\"}]}"
        semVerErrorContent = "{\"rules\":[{\"conditions\":[{\"type\":\"string\",\"subject\":\"city\"," +
                "\"predicate\":\"is one of\",\"objects\":[\"Paris\"]},{\"type\":\"segment\",\"subject\":\"\"," +
                "\"predicate\":\"is in\",\"objects\":[\"test_segment\"]},{\"type\":\"number\",\"subject\":\"age\"," +
                "\"predicate\":\"=\",\"objects\":[\"20\",\"abc\"]},{\"type\":\"datetime\",\"subject\":\"\"," +
                "\"predicate\":\"before\",\"objects\":[\"2022/06/27 16:08:10+08:00\",\"2022/06/27 80:08:10+08:00\"]}," +
                "{\"type\":\"semver\",\"subject\":\"\",\"predicate\":\"before\",\"objects\":[\"1.0.1\",\"1.1\"]}]," +
                "\"name\":\"Paris women show 50% red buttons, 50% blue\",\"serve\":{\"split\":[5000,5000,0]}}]," +
                "\"disabledServe\":{\"select\":1},\"defaultServe\":{\"select\":1},\"variations\":[{\"value\":\"red\"," +
                "\"name\":\"Red Button\",\"description\":\"Set button color to Red\"},{\"value\":\"blue\"," +
                "\"name\":\"Blue Button\",\"description\":\"Set button color to Blue\"}]}"
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

    def "update targeting number format error" () {
        when:
        TargetingRequest targetingRequest = new TargetingRequest()
        TargetingContent targetingContent = JsonMapper.toObject(numberErrorContent, TargetingContent.class);
        targetingRequest.setContent(targetingContent)
        targetingService.update(projectKey, environmentKey, toggleKey, targetingRequest)
        then:
        1 * segmentRepository.existsByProjectKeyAndKey(projectKey, _) >> true
        thrown(IllegalArgumentException)
    }

    def "update targeting datetime format error" () {
        when:
        TargetingRequest targetingRequest = new TargetingRequest()
        TargetingContent targetingContent = JsonMapper.toObject(datetimeErrorContent, TargetingContent.class);
        targetingRequest.setContent(targetingContent)
        targetingService.update(projectKey, environmentKey, toggleKey, targetingRequest)
        then:
        1 * segmentRepository.existsByProjectKeyAndKey(projectKey, _) >> true
        thrown(IllegalArgumentException)
    }

    def "update targeting semVer format error" () {
        when:
        TargetingRequest targetingRequest = new TargetingRequest()
        TargetingContent targetingContent = JsonMapper.toObject(semVerErrorContent, TargetingContent.class);
        targetingRequest.setContent(targetingContent)
        targetingService.update(projectKey, environmentKey, toggleKey, targetingRequest)
        then:
        1 * segmentRepository.existsByProjectKeyAndKey(projectKey, _) >> true
        thrown(IllegalArgumentException)
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

    def "query targeting version history"() {
        when:
        def versions = targetingService.versions(projectKey, environmentKey,
                new TargetingVersionRequest())
        then:
        1 * targetingVersionRepository
                .findAllByProjectKeyAndEnvironmentKey(projectKey, environmentKey, _) >>
                new PageImpl<>([new TargetingVersion()], Pageable.ofSize(1), 1)
        1 == versions.size

    }

}

