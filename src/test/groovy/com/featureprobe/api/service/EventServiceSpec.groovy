package com.featureprobe.api.service

import com.featureprobe.api.dto.EventCreateRequest
import com.featureprobe.api.model.VariationAccessCounter
import com.featureprobe.api.model.AccessSummary

import com.featureprobe.api.repository.EventRepository
import spock.lang.Specification

class EventServiceSpec extends Specification {

    EventService eventService
    EventRepository eventRepository

    def setup() {
        this.eventRepository = Mock(EventRepository)
        this.eventService = new EventService(eventRepository)
    }

    def "test create access events"() {
        def savedEvents

        when:
        this.eventService.create("server-key-test", [new EventCreateRequest(access: new AccessSummary(
                startTime: 10010101010,
                endTime: 202020202020,
                counters: [
                    "key1" :  [new VariationAccessCounter(value: "true", count: 100),
                               new VariationAccessCounter(value: "false", count: 10)]
                ]
        ))])

        then:
        1 * eventRepository.saveAll(_) >> { it -> savedEvents = it[0] }
        savedEvents
        2 == savedEvents.size()

        with(savedEvents[0]) {
            "access" == it.type
        }
    }

}
