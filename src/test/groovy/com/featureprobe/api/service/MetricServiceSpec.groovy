package com.featureprobe.api.service

import com.featureprobe.api.base.constants.MetricType
import com.featureprobe.api.dto.MetricResponse
import com.featureprobe.api.entity.Environment
import com.featureprobe.api.entity.Event
import com.featureprobe.api.entity.Targeting
import com.featureprobe.api.entity.VariationHistory
import com.featureprobe.api.entity.TargetingVersion
import com.featureprobe.api.model.AccessEventPoint
import com.featureprobe.api.model.TargetingContent
import com.featureprobe.api.model.Variation
import com.featureprobe.api.model.VariationAccessCounter
import com.featureprobe.api.repository.EnvironmentRepository
import com.featureprobe.api.repository.EventRepository
import com.featureprobe.api.repository.TargetingRepository
import com.featureprobe.api.repository.TargetingVersionRepository
import com.featureprobe.api.repository.VariationHistoryRepository
import spock.lang.Specification

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MetricServiceSpec extends Specification {

    MetricService metricService
    EnvironmentRepository environmentRepository
    EventRepository eventRepository
    VariationHistoryRepository variationHistoryRepository
    TargetingVersionRepository targetingVersionRepository
    TargetingRepository targetingRepository

    def setup() {
        environmentRepository = Mock(EnvironmentRepository)
        eventRepository = Mock(EventRepository)
        variationHistoryRepository = Mock(VariationHistoryRepository)
        targetingVersionRepository = Mock(TargetingVersionRepository)
        targetingRepository = Mock(TargetingRepository)
        metricService = new MetricService(environmentRepository, eventRepository, variationHistoryRepository,
                targetingVersionRepository, targetingRepository)
    }

    def "test find the last 3 hours of data by metric type"() {
        given:
        def toggleKey = "myToggle"
        def envKey = "test"
        def projectKey = "prj-key"
        def serverSdkKey = "sdkKey-001"

        when:
        MetricResponse response = metricService.query("prj-key",
                "test", "myToggle", MetricType.NAME, 3)

        then:
        1 * environmentRepository.findByProjectKeyAndKey(projectKey, envKey) >> Optional.of(new Environment(serverSdkKey: serverSdkKey))
        1 * targetingRepository.findByProjectKeyAndEnvironmentKeyAndToggleKey(projectKey, envKey, toggleKey) >> Optional.of(new Targeting(id: 1))
        3 * eventRepository.findBySdkKeyAndToggleKeyAndStartDateGreaterThanEqualAndEndDateLessThanEqual(serverSdkKey, toggleKey,
                _, _) >> []
        1 * variationHistoryRepository.findByProjectKeyAndEnvironmentKeyAndToggleKey(projectKey, envKey, toggleKey) >> []
        1 * targetingRepository.findByProjectKeyAndEnvironmentKeyAndToggleKey(projectKey, envKey, toggleKey) >> Optional.of(new Targeting(content: "{}"))

        0 == response.summary.size()
    }

    def "test query access event points when event is empty"() {
        given:
        def lastHours = 10

        when:
        List<AccessEventPoint> accessEventPoints = metricService.queryAccessEventPoints("test-sdk-key",
                "my_toggle1", 2, lastHours)

        then:
        10 * eventRepository.findBySdkKeyAndToggleKeyAndStartDateGreaterThanEqualAndEndDateLessThanEqual(_, _, _, _) >> []
        lastHours == accessEventPoints.size()
    }

    def "test append latest variations"() {
        given:
        List<VariationAccessCounter> accessCounters = [
                new VariationAccessCounter("red", 10),
                new VariationAccessCounter("blue", 10)
        ]
        Targeting latestTargeting = new Targeting(content: new TargetingContent(variations: [new Variation(name: "red"),
                                                                                             new Variation(name: "green")]).toJson())

        when:
        metricService.appendLatestVariations(accessCounters, latestTargeting, MetricType.NAME)

        then:
        3 == accessCounters.size()
    }

    def "test event entities convert to access counter"() {
        when:
        def accessCounters = metricService.toAccessEvent([
                new Event(valueIndex: 0, toggleVersion: 10, count: 10),
                new Event(valueIndex: 1, toggleVersion: 11, count: 20),
                new Event(valueIndex: 0, toggleVersion: 10, count: 30)
        ])

        then:
        2 == accessCounters.size()
        with(accessCounters.find { it.value == '10_0' }) {
            40 == count
        }
    }

    def "test aggregate point by metric type "() {
        when:
        List<AccessEventPoint> accessEventPoints = metricService.aggregatePointByMetricType([
                "1_10": new VariationHistory(id: 3, name: "blue"),
                "1_11": new VariationHistory(id: 3, name: "blue")],
                [new AccessEventPoint("10", [new VariationAccessCounter(value: "1_10", count: 15),
                                             new VariationAccessCounter(value: "1_11", count: 5)], 1)], MetricType.NAME)

        then:
        1 == accessEventPoints.size()
        "blue" == accessEventPoints[0].values[0].value
        20 == accessEventPoints[0].values[0].count
    }

    def "test `isGroupByDay`"() {
        expect:
        groupByDay == metricService.isGroupByDay(lastHours)

        where:
        groupByDay | lastHours
        false      | 10
        false      | 24
        true       | 48
        true       | 49
    }


    def "test `getPointIntervalCount`"() {
        expect:
        intervalCount == metricService.getPointIntervalCount(lastHours)

        where:
        intervalCount | lastHours
        1             | 9
        2             | 24
        24            | 48
    }

    def "test `getPointNameFormat`"() {
        expect:
        formatName == metricService.getPointNameFormat(lastHours)

        where:
        formatName | lastHours
        "HH"       | 9
        "HH"       | 24
        "MM/dd"    | 49
    }

    def "test `getQueryStartDateTime`"() {
        expect:
        dateTime == metricService.getQueryStartDateTime(now, lastHours)
                .format(DateTimeFormatter.ofPattern("MM/dd HH"))

        where:
        dateTime   | now                                      | lastHours
        "03/01 07" | LocalDateTime.of(2022, 3, 1, 10, 10, 10) | 4
        "03/01 11" | LocalDateTime.of(2022, 3, 2, 10, 10, 10) | 24
    }

    def "test `summaryAccessEvents`"() {
        when:
        def events = metricService.summaryAccessEvents([new AccessEventPoint("10", [new VariationAccessCounter(value: "true", count: 1)], null),
                                                        new AccessEventPoint("11", [new VariationAccessCounter(value: "false", count: 4),
                                                                                    new VariationAccessCounter(value: "true", count: 9)], null),
                                                        new AccessEventPoint("12", [new VariationAccessCounter(value: "true", count: 2)], null),
        ])

        then:
        2 == events.size()
        with(events.find { it.value == 'true' }) {
            12 == it.count
        }
        with(events.find { it.value == 'false' }) {
            4 == it.count
        }
    }

    def "test `queryAccessEventPoint`"() {
        def sdkKey = "sdk-key1"
        def toggleName = "t1"
        def startTime = LocalDateTime.of(2022, 3, 1, 10, 10, 10)
        def endTime = LocalDateTime.of(2022, 3, 1, 11, 10, 10)

        when:
        AccessEventPoint accessEventPoint = metricService.queryAccessEventPoint(sdkKey, toggleName, 1, "HH",
                startTime, endTime)


        then:
        1 * eventRepository.findBySdkKeyAndToggleKeyAndStartDateGreaterThanEqualAndEndDateLessThanEqual(sdkKey,
                toggleName, _, _) >> [new Event(valueIndex: 0, toggleVersion: 1, count: 10),
                                      new Event(valueIndex: 1, toggleVersion: 1, count: 11)]
        1 * targetingVersionRepository
                .findAllByTargetingIdAndCreatedTimeGreaterThanEqualAndCreatedTimeLessThanEqualOrderByCreatedTimeDesc(
                        1, _, _) >> [new TargetingVersion(version: 10)]
        with(accessEventPoint) {
            "11" == it.name
            10 == it.lastChangeVersion
            2 == it.values.size()
        }
    }
}
