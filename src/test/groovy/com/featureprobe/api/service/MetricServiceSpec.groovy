package com.featureprobe.api.service

import com.featureprobe.api.entity.Event
import com.featureprobe.api.entity.TargetingVersion
import com.featureprobe.api.model.AccessEventPoint
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
