package com.featureprobe.api.service;


import com.featureprobe.api.base.enums.ResourceType;
import com.featureprobe.api.base.exception.ResourceNotFoundException;
import com.featureprobe.api.dto.EventCreateRequest;
import com.featureprobe.api.entity.Environment;
import com.featureprobe.api.entity.Event;
import com.featureprobe.api.model.VariationAccessCounter;
import com.featureprobe.api.repository.EnvironmentRepository;
import com.featureprobe.api.repository.EventRepository;
import com.featureprobe.api.service.aspect.ExcludeTenant;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
@ExcludeTenant
public class EventService {

    private EventRepository eventRepository;
    private EnvironmentRepository environmentRepository;


    public void create(String serverSdkKey, List<EventCreateRequest> requests) {
        Environment environment = environmentRepository.findByServerSdkKey(serverSdkKey)
                .orElseThrow(() -> new ResourceNotFoundException(ResourceType.ENVIRONMENT, serverSdkKey));

        requests.forEach(request -> {
            if (request.getAccess() == null) {
                return;
            }
            List<Event> events = Optional.of(request.getAccess().getCounters())
                    .orElse(Collections.emptyMap())
                    .entrySet()
                    .stream()
                    .flatMap(entry -> createEventEntities(entry).stream())
                    .map(event -> wrapEvent(event, environment, request))
                    .collect(Collectors.toList());

            if (!events.isEmpty()) {
                eventRepository.saveAll(events);
            }
        });
    }


    private List<Event> createEventEntities(Map.Entry<String, List<VariationAccessCounter>> toggleToAccessCounter) {
        String toggleKey = toggleToAccessCounter.getKey();

        return Optional.of(toggleToAccessCounter.getValue())
                .orElse(Collections.emptyList())
                .stream()
                .map(accessEvent -> this.createEventEntity(toggleKey, accessEvent))
                .collect(Collectors.toList());
    }

    private Event createEventEntity(String toggleKey, VariationAccessCounter accessCounter) {
        Event event = new Event();
        event.setToggleKey(toggleKey);
        event.setCount(accessCounter.getCount());
        event.setValueIndex(accessCounter.getIndex());
        event.setToggleVersion(accessCounter.getVersion());

        return event;
    }

    private Event wrapEvent(Event event, Environment environment, EventCreateRequest request) {
        if (request.getAccess() == null) {
            return event;
        }
        event.setSdkKey(environment.getServerSdkKey());
        event.setProjectKey(environment.getProject().getKey());
        event.setEnvironmentKey(environment.getKey());
        event.setType("access");
        event.setStartDate(new Date(request.getAccess().getStartTime()));
        event.setEndDate(new Date(request.getAccess().getEndTime()));
        return event;
    }
}
