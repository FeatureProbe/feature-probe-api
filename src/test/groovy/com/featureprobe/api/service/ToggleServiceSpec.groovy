package com.featureprobe.api.service

import com.featureprobe.api.base.enums.ValidateTypeEnum
import com.featureprobe.api.base.enums.VisitFilter
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
import com.featureprobe.api.repository.TargetingVersionRepository
import com.featureprobe.api.repository.ToggleRepository
import com.featureprobe.api.repository.ToggleTagRepository
import com.featureprobe.api.repository.VariationHistoryRepository
import com.featureprobe.sdk.server.FeatureProbe
import org.hibernate.internal.SessionImpl
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import spock.lang.Specification
import spock.lang.Title

import javax.persistence.EntityManager

@Title("Toggle Unit Test")
class ToggleServiceSpec extends Specification {

    ToggleService toggleService

    ToggleIncludeDeletedService toggleIncludeDeletedService

    ToggleRepository toggleRepository

    TagRepository tagRepository

    ToggleTagRepository toggleTagRepository

    TargetingRepository targetingRepository

    EnvironmentRepository environmentRepository

    EventRepository eventRepository

    TargetingVersionRepository targetingVersionRepository

    VariationHistoryRepository variationHistoryRepository

    FeatureProbe featureProbe

    EntityManager entityManager

    def projectKey
    def environmentKey
    def toggleKey
    def toggleName
    def sdkKey
    def rules
    def segmentRules

    def setup() {
        toggleRepository = Mock(ToggleRepository)
        tagRepository = Mock(TagRepository)
        toggleTagRepository = Mock(ToggleTagRepository)
        targetingRepository = Mock(TargetingRepository)
        environmentRepository = Mock(EnvironmentRepository)
        eventRepository = Mock(EventRepository)
        targetingVersionRepository = Mock(TargetingVersionRepository)
        variationHistoryRepository = Mock(VariationHistoryRepository)
        featureProbe = new FeatureProbe("_")
        entityManager = Mock(SessionImpl)
        toggleIncludeDeletedService = new ToggleIncludeDeletedService(toggleRepository, entityManager)
        toggleService = new ToggleService(toggleRepository, tagRepository, targetingRepository,
                environmentRepository, eventRepository, targetingVersionRepository,
                variationHistoryRepository, toggleIncludeDeletedService, featureProbe, entityManager)
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
        setAuthContext("Admin", "ADMIN")
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
                new ToggleSearchRequest(visitFilter: VisitFilter.IN_WEEK_VISITED, disabled: false,
                        tags: ["test"], keyword: "test", environmentKey: environmentKey)
        when:
        def page = toggleService.query(projectKey, toggleSearchRequest)

        then:
        1 * environmentRepository.findByProjectKeyAndKey(projectKey, environmentKey) >>
                Optional.of(new Environment(key: environmentKey, serverSdkKey: "1234", clientSdkKey: "5678"))
        1 * targetingRepository.findAllByProjectKeyAndEnvironmentKeyAndDisabled(projectKey, environmentKey,
                false) >> [new Targeting(toggleKey: toggleKey)]
        1 * tagRepository.findByNameIn(["test"]) >> [new Tag(name: "test", toggles: [new Toggle(key: toggleKey)])]
        1 * eventRepository.findAll(_) >> [new Event(toggleKey: toggleKey)]
        1 * toggleRepository.findAll(_, _) >> new PageImpl<>([new Toggle(key: toggleKey, projectKey: projectKey)],
                Pageable.ofSize(1), 1)
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
        1 * toggleRepository.existsByProjectKeyAndKey(projectKey, toggleKey) >> false
        1 * toggleRepository.existsByProjectKeyAndName(projectKey, "toggle1") >> false
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
        1 * toggleRepository.existsByProjectKeyAndName(projectKey, "toggle2") >> false
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
        toggleIncludeDeletedService.validateExists(projectKey, ValidateTypeEnum.KEY, toggleKey)
        then:
        toggleRepository.existsByProjectKeyAndKey(projectKey, toggleKey) >> true
        then:
        thrown ResourceConflictException
    }

    def "check toggle name" () {
        when:
        toggleIncludeDeletedService.validateExists(projectKey, ValidateTypeEnum.NAME, toggleName)
        then:
        1 * toggleRepository.existsByProjectKeyAndName(projectKey, toggleName) >> true
        then:
        thrown ResourceConflictException
    }

    private setAuthContext(String account, String role) {
        SecurityContextHolder.setContext(new SecurityContextImpl(
                new JwtAuthenticationToken(new Jwt.Builder("21212").header("a","a")
                        .claim("role", role).claim("account", account).build())))
    }
}