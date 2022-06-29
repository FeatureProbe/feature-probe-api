package com.featureprobe.api.service

import com.featureprobe.api.base.enums.ValidateTypeEnum
import com.featureprobe.api.base.exception.ResourceConflictException
import com.featureprobe.api.dto.ServerResponse
import com.featureprobe.api.dto.ToggleCreateRequest
import com.featureprobe.api.dto.ToggleSearchRequest
import com.featureprobe.api.dto.ToggleUpdateRequest
import com.featureprobe.api.entity.Environment
import com.featureprobe.api.entity.Event
import com.featureprobe.api.entity.Project
import com.featureprobe.api.entity.Segment
import com.featureprobe.api.entity.Tag
import com.featureprobe.api.entity.Targeting
import com.featureprobe.api.entity.Toggle
import com.featureprobe.api.entity.ToggleTagRelation
import com.featureprobe.api.repository.EnvironmentRepository
import com.featureprobe.api.repository.EventRepository
import com.featureprobe.api.repository.SegmentRepository
import com.featureprobe.api.repository.TagRepository
import com.featureprobe.api.repository.TargetingRepository
import com.featureprobe.api.repository.ToggleRepository
import com.featureprobe.api.repository.ToggleTagRepository
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import spock.lang.Specification
import spock.lang.Title

@Title("Toggle Unit Test")
class ToggleServiceSpec extends Specification {

    ToggleService toggleService

    ToggleRepository toggleRepository

    SegmentRepository segmentRepository

    TagRepository tagRepository

    ToggleTagRepository toggleTagRepository

    TargetingRepository targetingRepository

    EnvironmentRepository environmentRepository

    EventRepository eventRepository

    def projectKey
    def environmentKey
    def toggleKey
    def toggleName
    def sdkKey
    def rules
    def segmentRules

    def setup() {
        toggleRepository = Mock(ToggleRepository)
        segmentRepository = Mock(SegmentRepository)
        tagRepository = Mock(TagRepository)
        toggleTagRepository = Mock(ToggleTagRepository)
        targetingRepository = Mock(TargetingRepository)
        environmentRepository = Mock(EnvironmentRepository)
        eventRepository = Mock(EventRepository)
        toggleService = new ToggleService(toggleRepository, segmentRepository, tagRepository, toggleTagRepository,
                targetingRepository, environmentRepository, eventRepository)
        projectKey = "feature_probe"
        environmentKey = "test"
        toggleKey = "feature_toggle_unit_test"
        toggleName = "test_toggle"
        sdkKey = "server-123456"
        rules = "{\"rules\":[{\"conditions\":[{\"type\":\"string\",\"subject\":\"city\",\"predicate\":\"is one of\"," +
                "\"objects\":[\"Paris\"]},{\"type\":\"segment\",\"subject\":\"\",\"predicate\":\"is in\"," +
                "\"objects\":[\"test_segment\"]},{\"type\":\"number\",\"subject\":\"age\",\"predicate\":\"=\"," +
                "\"objects\":[\"20\"]},{\"type\":\"datetime\",\"subject\":\"\",\"predicate\":\"before\"," +
                "\"objects\":[\"2022/06/27 16:08:10+08:00\"]},{\"type\":\"semver\",\"subject\":\"\"," +
                "\"predicate\":\"before\",\"objects\":[\"1.0.1\"]}],\"name\":\"Paris women show 50% red buttons, 50% blue\"," +
                "\"serve\":{\"split\":[5000,5000,0]}}],\"disabledServe\":{\"select\":1},\"defaultServe\":{\"select\":1}," +
                "\"variations\":[{\"value\":\"red\",\"name\":\"Red Button\",\"description\":\"Set button color to Red\"}," +
                "{\"value\":\"blue\",\"name\":\"Blue Button\",\"description\":\"Set button color to Blue\"}]}"
        segmentRules = "[{\"conditions\":[{\"type\":\"string\",\"subject\":\"userId\",\"predicate\":\"is one of\"," +
                "\"objects\":[\"zhangsan\",\"wangwu\",\"lishi\",\"miss\"]},{\"type\":\"string\",\"subject\":\"userId\"," +
                "\"predicate\":\"is one of\",\"objects\":[\"huahau\",\"kaka\",\"dada\"]}],\"name\":\"\"}]"
    }

    def "query toggle by key"() {
        when:
        def ret = toggleService.queryByKey(projectKey, toggleKey)

        then:
        1 * toggleRepository.findByProjectKeyAndKey(projectKey, toggleKey) >> Optional.of(new Toggle(key: toggleKey, projectKey: projectKey))
        with(ret) {
            toggleKey == it.key
        }
    }

    def "search toggles by filter params"() {
        def toggleSearchRequest =
                new ToggleSearchRequest(isVisited: true, disabled: false,
                        tags: ["test"], keyword: "test", environmentKey: environmentKey)
        when:
        def page = toggleService.query(projectKey, toggleSearchRequest)

        then:
        1 * environmentRepository.findByProjectKeyAndKey(projectKey, environmentKey) >>
                Optional.of(new Environment(key: environmentKey, serverSdkKey: "1234", clientSdkKey: "5678"))
        1 * targetingRepository.findAllByProjectKeyAndEnvironmentKeyAndDisabled(projectKey, environmentKey,
                false) >> [new Targeting(toggleKey: toggleKey)]
        1 * toggleTagRepository.findByNames(["test"]) >> [new ToggleTagRelation(toggleKey: toggleKey)]
        1 * eventRepository.findAll(_) >> [new Event(toggleKey: toggleKey)]
        1 * toggleRepository.findAll(_, _) >> new PageImpl<>([new Toggle(key: toggleKey, projectKey: projectKey)],
                Pageable.ofSize(1), 1)
        1 * tagRepository.selectTagsByToggleKey(toggleKey) >> [new Tag(name: "tag")]
        1 * targetingRepository.findByProjectKeyAndEnvironmentKeyAndToggleKey(projectKey, environmentKey, toggleKey) >>
                Optional.of(new Targeting(toggleKey: toggleKey, environmentKey: environmentKey,
                        projectKey: projectKey, disabled: true))
        1 * environmentRepository.findByProjectKeyAndKey(projectKey, environmentKey) >>
                Optional.of(new Environment(key: environmentKey, serverSdkKey: "123", clientSdkKey: "123"))
        1 * eventRepository.findAll(_, _) >> new PageImpl<>([new Event(toggleKey: toggleKey, sdkKey: "123",
                startDate: new Date())],
                Pageable.ofSize(1), 1)
        with(page) {
            1 == it.size
            it.getContent().get(0).visitedTime != null
        }
    }

    def "create toggle success"() {
        Toggle savedToggle
        List<Targeting> savedTargetingList

        when:
        def response = toggleService.create(projectKey,
                new ToggleCreateRequest(name: "toggle1", key: toggleKey, tags: ["tg1", "tg2"]))

        then:
        response
        1 * toggleRepository.countByKeyIncludeDeleted(projectKey, toggleKey) >> 0
        1 * toggleRepository.countByNameIncludeDeleted(projectKey, "toggle1") >> 0
        1 * environmentRepository.findAllByProjectKey(projectKey) >> [new Environment(key: "test"), new Environment(key: "online")]
        1 * toggleRepository.save(_ as Toggle) >> { it -> savedToggle = it[0] }
        1 * targetingRepository.saveAll(_ as List<Targeting>) >> { it -> savedTargetingList = it[0] }
        1 * tagRepository.findByProjectKeyAndNameIn(projectKey, ["tg1", "tg2"]) >> [new Tag(name: "tg1"), new Tag(name: "tg2")]
        with(savedToggle) {
            toggleKey == key
            "tg1" == tags[0].name
        }
        2 == savedTargetingList.size()
    }

    def "update toggle success"() {
        Toggle updatedToggle

        when:
        def response = toggleService.update(projectKey, toggleKey,
                new ToggleUpdateRequest(name: "toggle2", tags: ["tg1", "tg2"], desc: "updated"))

        then:
        response
        1 * toggleRepository.findByProjectKeyAndKey(projectKey, toggleKey) >> Optional.of(new Toggle(projectKey: projectKey,
                key: toggleKey, name: "toggle1", desc: "init"))
        1 * toggleRepository.countByNameIncludeDeleted(projectKey, "toggle2") >> 0
        1 * toggleRepository.save(_ as Toggle) >> { it -> updatedToggle = it[0] }
        1 * tagRepository.findByProjectKeyAndNameIn(projectKey, ["tg1", "tg2"]) >> [new Tag(name: "tg1")]
        with(updatedToggle) {
            toggleKey == key
            "updated" == desc
            "toggle2" == name
            1 == tags.size()
        }
    }

    def "check toggle key" () {
        when:
        toggleService.validateExists(projectKey, ValidateTypeEnum.KEY, toggleKey)
        then:
        toggleRepository.countByKeyIncludeDeleted(projectKey, toggleKey) >> 1
        then:
        thrown ResourceConflictException
    }

    def "check toggle name" () {
        when:
        toggleService.validateExists(projectKey, ValidateTypeEnum.NAME, toggleName)
        then:
        1 * toggleRepository.countByNameIncludeDeleted(projectKey, toggleName) >> 1
        then:
        thrown ResourceConflictException
    }

    def "query server toggles by server sdkKey"() {
        when:
        def serverResponse = toggleService.queryServerTogglesByServerSdkKey(sdkKey)
        then:
        2 * environmentRepository.findByServerSdkKey(sdkKey) >>
                new Environment(project: new Project(key: projectKey), key: environmentKey)
        2 * segmentRepository.findAllByProjectKey(projectKey) >>
                [new Segment(projectKey: projectKey, key: "test_segment",
                        uniqueKey: projectKey + "\$test_segment", rules: segmentRules)]
        1 * toggleRepository.findAllByProjectKey(projectKey) >>
                [new Toggle(projectKey: projectKey, key: toggleKey, returnType: "string", clientAvailability: false)]
        1 * targetingRepository.findAllByProjectKeyAndEnvironmentKey(projectKey, environmentKey) >>
                [new Targeting(projectKey: projectKey, environmentKey: environmentKey,
                        toggleKey: toggleKey, content: rules, disabled: false)]
        with(serverResponse) {
            1 == toggles.size()
            1 == segments.size()
        }
    }
}