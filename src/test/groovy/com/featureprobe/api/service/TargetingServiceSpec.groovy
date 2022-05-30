package com.featureprobe.api.service

import com.featureprobe.api.dto.TargetingRequest
import com.featureprobe.api.entity.Targeting
import com.featureprobe.api.mapper.JsonMapper
import com.featureprobe.api.model.TargetingContent
import com.featureprobe.api.repository.TargetingRepository
import spock.lang.Specification
import spock.lang.Title

@Title("Targeting Unit Test")
class TargetingServiceSpec extends Specification {

    TargetingService targetingService;

    TargetingRepository targetingRepository

    def projectKey
    def environmentKey
    def toggleKey
    def content

    def setup() {
        targetingRepository = Mock(TargetingRepository)
        targetingService = new TargetingService(targetingRepository)
        projectKey = "feature_probe"
        environmentKey = "test"
        toggleKey = "feature_toggle_unit_test"
        content = "{\"default_serve\":{\"select\":3}," +
                "\"disabled_serve\":{\"select\":1},\"variations\":[{\"name\":\"open\",\"description\":\"打开开关\"," +
                "\"value\":\"true\"},{\"name\":\"close\",\"description\":\"关闭开关\",\"value\":\"false\"}]," +
                "\"rules\":[{\"name\":\"规则一1\",\"serve\":{\"select\":0}," +
                "\"conditions\":[{\"predicate\":\"in one of\",\"subject\":\"userId\"," +
                "\"objects\":[\"1\",\"2\",\"3\"],\"type\":\"string\"},{\"predicate\":\"is not any of\"," +
                "\"subject\":\"userId\",\"objects\":[\"3\",\"rrrr\"],\"type\":\"\"}]},{\"name\":\"规则二\"," +
                "\"serve\":{\"split\":[7000,3000]},\"conditions\":[{\"predicate\":\"starts with\"," +
                "\"subject\":\"userId\",\"objects\":[\"123\",\"32\"],\"type\":\"string\"}]}]}"
    }

    def "update targeting"() {
        when:
        TargetingRequest targetingRequest = new TargetingRequest()
        TargetingContent targetingContent = JsonMapper.toObject(content, TargetingContent.class);
        targetingRequest.setContent(targetingContent)
        targetingRequest.setDisabled(false)
        def ret = targetingService.update(projectKey, environmentKey, toggleKey, targetingRequest)
        then:
        1 * targetingRepository.findByProjectKeyAndEnvironmentKeyAndToggleKey(projectKey, environmentKey, toggleKey) >>
                new Optional<>(new Targeting(toggleKey: toggleKey, environmentKey: environmentKey,
                        content: "", disabled: true))
        1 * targetingRepository.save(_) >> new Targeting(toggleKey: toggleKey, environmentKey: environmentKey,
                content: content, disabled: false)
        with(ret) {
            content == it.content
            false == it.disabled
        }
    }


    def "query targeting by key"() {
        when:
        def ret = targetingService.queryByKey(projectKey, environmentKey, toggleKey)
        then:
        1 * targetingRepository.findByProjectKeyAndEnvironmentKeyAndToggleKey(projectKey, environmentKey, toggleKey) >>
                new Optional<>(new Targeting(toggleKey: toggleKey, environmentKey: environmentKey,
                        content: content, disabled: false))
        with(ret) {
            content == it.content
            false == it.disabled
        }
    }

}

