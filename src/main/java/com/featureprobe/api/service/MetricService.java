package com.featureprobe.api.service;

import com.featureprobe.api.dto.MetricResponse;
import com.featureprobe.api.entity.Environment;
import com.featureprobe.api.entity.Event;
import com.featureprobe.api.model.AccessEventPoint;
import com.featureprobe.api.model.VariationAccessCounter;
import com.featureprobe.api.repository.EnvironmentRepository;
import com.featureprobe.api.repository.EventRepository;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class MetricService {

    private EnvironmentRepository environmentRepository;
    private EventRepository eventRepository;

    private static final int MAX_QUERY_HOURS = 12 * 24;
    private static final int MAX_QUERY_POINT_COUNT = 12;
    private static final int GROUP_BY_DAY_HOURS = 24;

    public MetricResponse query(String projectKey, String environmentKey, String toggleKey, int lastHours) {
        int queryLastHours = Math.min(lastHours, MAX_QUERY_HOURS);
        String serverSdkKey = queryEnvironmentServerSdkKey(projectKey, environmentKey);

        List<AccessEventPoint> accessEventPoints = queryAccessEventPoints(serverSdkKey, toggleKey, queryLastHours);
        return new MetricResponse(accessEventPoints, summaryAccessEvents(accessEventPoints));
    }

    private List<AccessEventPoint> queryAccessEventPoints(String serverSdkKey, String toggleKey, int lastHours) {
        int pointIntervalCount = getPointIntervalCount(lastHours);
        int pointCount = lastHours / pointIntervalCount;

        LocalDateTime pointStartTime = getQueryStartDateTime(lastHours);
        String pointNameFormat = getPointNameFormat(lastHours);

        List<AccessEventPoint> accessEventPoints = Lists.newArrayList();
        for (int i = 0; i < pointCount; i++) {
            LocalDateTime pointEndTime = pointStartTime.plusHours(pointIntervalCount);
            AccessEventPoint accessEventPoint = queryAccessEventPoint(serverSdkKey, toggleKey, pointNameFormat,
                    pointStartTime, pointEndTime);

            accessEventPoints.add(accessEventPoint);
            pointStartTime = pointEndTime;
        }
        return accessEventPoints;
    }

    protected int getPointIntervalCount(int lastHours) {
        int pointIntervalCount;
        if (isGroupByDay(lastHours)) {
            pointIntervalCount = GROUP_BY_DAY_HOURS;
        } else {
            pointIntervalCount = lastHours <= MAX_QUERY_POINT_COUNT ? 1 : 2;
        }
        return pointIntervalCount;
    }

    protected AccessEventPoint queryAccessEventPoint(String serverSdkKey, String toggleKey, String pointNameFormat,
                                                     LocalDateTime pointStartTime,
                                                     LocalDateTime pointEndTime) {
        List<VariationAccessCounter> accessEvents = queryAccessEvents(serverSdkKey,
                toggleKey, pointStartTime, pointEndTime);
        String pointName = String.format("%s", pointEndTime.format(DateTimeFormatter.ofPattern(pointNameFormat)));

        return new AccessEventPoint(pointName, accessEvents);
    }

    private List<VariationAccessCounter> queryAccessEvents(String serverSdkKey,
                                                           String toggleKey,
                                                           LocalDateTime pointStartTime,
                                                           LocalDateTime pointEndTime) {
        List<Event> currentPointEvents
                = eventRepository.findBySdkKeyAndToggleKeyAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
                serverSdkKey,
                toggleKey,
                toDate(pointStartTime),
                toDate(pointEndTime));

        return toAccessEvent(currentPointEvents);
    }

    private List<VariationAccessCounter> toAccessEvent(List<Event> events) {
        if (CollectionUtils.isEmpty(events)) {
            return Collections.emptyList();
        }
        Map<String, Long> variationCounts =
                events.stream().collect(Collectors.toMap(Event::getVariation, Event::getCount, Long::sum));
        return variationCounts.entrySet().stream().map(e ->
                new VariationAccessCounter(e.getKey(), e.getValue())).collect(Collectors.toList());
    }

    protected String getPointNameFormat(int lastHours) {
        return isGroupByDay(lastHours) ? "MM/dd" : "HH";
    }

    protected LocalDateTime getQueryStartDateTime(LocalDateTime nowDateTime, int queryLastHours) {
        if (isGroupByDay(queryLastHours)) {
            nowDateTime = nowDateTime.withHour(23).withMinute(59).withSecond(59);
        } else {
            nowDateTime = nowDateTime.withMinute(0).withSecond(0).plusHours(1);
        }
        return nowDateTime.minusHours(queryLastHours);
    }

    private LocalDateTime getQueryStartDateTime(int queryLastHours) {
        return getQueryStartDateTime(LocalDateTime.now(), queryLastHours);
    }


    protected List<VariationAccessCounter> summaryAccessEvents(List<AccessEventPoint> accessEventPoints) {
        List<VariationAccessCounter> summaryEvents = Lists.newArrayList();
        accessEventPoints.forEach(accessEventPoint -> {
            Map<String, Long> variationCount = accessEventPoint.getValues().stream().collect(
                    Collectors.toMap(VariationAccessCounter::getValue, VariationAccessCounter::getCount));
            variationCount.keySet().forEach(key -> {
                VariationAccessCounter findingToggleCounter = summaryEvents.stream().filter(toggleCounter ->
                        StringUtils.equals(toggleCounter.getValue(), key)).findFirst().orElse(null);

                if (findingToggleCounter == null) {
                    summaryEvents.add(new VariationAccessCounter(key, variationCount.get(key)));
                } else {
                    findingToggleCounter.setCount(findingToggleCounter.getCount() + variationCount.get(key));
                }
            });
        });

        return summaryEvents;
    }

    private String queryEnvironmentServerSdkKey(String projectKey, String environmentKey) {
        Environment environment = this.environmentRepository.findByProjectKeyAndKey(projectKey, environmentKey).get();
        return environment.getServerSdkKey();
    }

    protected boolean isGroupByDay(int queryLastHours) {
        return queryLastHours > GROUP_BY_DAY_HOURS;
    }

    private Date toDate(LocalDateTime pointStartTime) {
        return Date.from(pointStartTime.atZone(ZoneId.systemDefault()).toInstant());
    }

}
