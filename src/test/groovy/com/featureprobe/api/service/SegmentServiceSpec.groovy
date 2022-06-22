package com.featureprobe.api.service

import com.featureprobe.api.base.enums.ValidateTypeEnum
import com.featureprobe.api.base.exception.ResourceConflictException
import com.featureprobe.api.dto.PaginationRequest
import com.featureprobe.api.dto.SegmentCreateRequest
import com.featureprobe.api.dto.SegmentSearchRequest
import com.featureprobe.api.dto.SegmentUpdateRequest
import com.featureprobe.api.entity.Environment
import com.featureprobe.api.entity.Segment
import com.featureprobe.api.entity.Targeting
import com.featureprobe.api.entity.TargetingSegment
import com.featureprobe.api.entity.Toggle
import com.featureprobe.api.model.ConditionValue
import com.featureprobe.api.model.SegmentRuleModel
import com.featureprobe.api.repository.EnvironmentRepository
import com.featureprobe.api.repository.SegmentRepository
import com.featureprobe.api.repository.TargetingRepository
import com.featureprobe.api.repository.TargetingSegmentRepository
import com.featureprobe.api.repository.ToggleRepository
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import spock.lang.Specification
import spock.lang.Title

@Title("Segment Unit Test")
class SegmentServiceSpec extends Specification{

    SegmentRepository segmentRepository

    TargetingSegmentRepository targetingSegmentRepository

    TargetingRepository targetingRepository

    ToggleRepository toggleRepository

    EnvironmentRepository environmentRepository

    SegmentService segmentService

    def projectKey
    def segmentKey
    def segmentName
    def rules

    def setup() {
        segmentRepository = Mock(SegmentRepository)
        targetingSegmentRepository = Mock(TargetingSegmentRepository)
        targetingRepository = Mock(TargetingRepository)
        toggleRepository = Mock(ToggleRepository)
        environmentRepository = Mock(EnvironmentRepository)
        segmentService = new SegmentService(segmentRepository, targetingSegmentRepository, targetingRepository,
                toggleRepository, environmentRepository)

        projectKey = "feature_probe"
        segmentKey = "test_segment_key"
        segmentName = "test_segment"
        rules = "[{\"subject\":\"userId\",\"predicate\":\"withsend\",\"objects\":[\"test2\"]}]"
    }

    def "create a segment" () {
        when:
        def created = segmentService.create(projectKey, new SegmentCreateRequest(name: segmentName,
                key: segmentKey, rules: [new SegmentRuleModel(name: "rule1",
                conditions: [new ConditionValue(type: "string", subject: "userId", predicate: "withs end",
                        objects: ["test"])])]))
        then:
        1 * segmentRepository.save(_) >> new Segment(name: segmentName, key: segmentKey, rules: rules)
        with(created) {
            segmentName == created.name
            segmentKey == created.key
            projectKey == created.projectKey
            0 < created.rules.size()
        }

    }

    def "update a segment" () {
        when:
        def updated = segmentService.update(projectKey, segmentKey,
                new SegmentUpdateRequest(name: "segment_test_update", rules:  [new SegmentRuleModel(name: "rule1",
                        conditions: [new ConditionValue(type: "string", subject: "userId", predicate: "withs end",
                                objects: ["test"])])]))
        then:
        1 * segmentRepository.findByProjectKeyAndKey(projectKey, segmentKey) >>
                new Segment(name: segmentName, key: segmentKey, rules: rules)
        1 * segmentRepository.countByNameIncludeDeleted(projectKey, "segment_test_update") >> 0
        1 * segmentRepository.save(_) >> new Segment(name: segmentName, key: segmentKey, rules: rules)
        with(updated) {
            segmentName == updated.name
            segmentKey == updated.key
            projectKey == updated.projectKey
            0 < updated.rules.size()
        }
    }

    def "query a segment by key" () {
        when:
        def segment = segmentService.queryByKey(projectKey, segmentKey)
        then:
        1 *  segmentRepository.findByProjectKeyAndKey(projectKey, segmentKey) >>
                new Segment(name: segmentName, key: segmentKey, rules: rules)
        with(segment) {
            segmentName == segment.name
            segmentKey == segment.key
            projectKey == segment.projectKey
            0 < segment.rules.size()
        }
    }


    def "check segment key" () {
        when:
        segmentService.validateExists(projectKey, ValidateTypeEnum.KEY, segmentKey)
        then:
        1 * segmentRepository.countByKeyIncludeDeleted(projectKey, segmentKey) >> 1
        then:
        thrown ResourceConflictException
    }

    def "check segment name" () {
        when:
        segmentService.validateExists(projectKey, ValidateTypeEnum.NAME, segmentName)
        then:
        1 * segmentRepository.countByNameIncludeDeleted(projectKey, segmentName) >> 1
        then:
        thrown ResourceConflictException
    }

    def "delete a segment" () {
        when:
        def deleted = segmentService.delete(projectKey, segmentKey)
        then:
        1 * targetingSegmentRepository.countByProjectKeyAndSegmentKey(projectKey, segmentKey) >> 0
        1 * segmentRepository.findByProjectKeyAndKey(projectKey, segmentKey) >> new Segment(name: segmentName,
                key: segmentKey, rules: rules)
        1 * segmentRepository.save(_) >> new Segment(name: segmentName, key: segmentKey, rules: rules)
        with(deleted) {
            segmentName == deleted.name
            segmentKey == deleted.key
            projectKey == deleted.projectKey
            0 < deleted.rules.size()
        }

    }

    def "delete a using segment" () {
        when:
        def deleted = segmentService.delete(projectKey, segmentKey)
        then:
        1 * targetingSegmentRepository.countByProjectKeyAndSegmentKey(projectKey, segmentKey) >> 1
        then:
        thrown(IllegalArgumentException)
    }

    def "list of toggles using segment" () {
        when:
        def toggles = segmentService.usingToggles(projectKey, segmentKey,
                new PaginationRequest(pageIndex: 0, pageSize: 5))
        then:
        1 * targetingSegmentRepository.findByProjectKeyAndSegmentKey(projectKey, segmentKey) >>
                [new TargetingSegment(projectKey: projectKey, targetingId: 1, segmentKey: segmentKey)]
        1 * targetingRepository.findAll(_, _) >> new PageImpl<>([new Targeting(toggleKey: "test_toggle",
                projectKey: projectKey, environmentKey: "test", disabled: true)], Pageable.ofSize(1), 1)
        1 * toggleRepository.findByProjectKeyAndKey(projectKey, "test_toggle") >>
                Optional.of(new Toggle(name: "test_toggle", key: "test_toggle_key", desc: "this is a test toggle"))
        1 * environmentRepository
                .findByProjectKeyAndKey(projectKey, "test") >> Optional.of(new Environment(name: "test", key: "test"))
        with(toggles) {
            1 == toggles.size
        }
    }

    def "list toggles"() {
        when:
        def segments = segmentService.list(projectKey,
                new SegmentSearchRequest(pageIndex: 0, pageSize: 5))

        then:
        1 * segmentRepository.findAll(_, _) >> new PageImpl<>([new Segment(key: "test_segment")],
                Pageable.ofSize(1), 1)
        1 == segments.size()
    }


}

