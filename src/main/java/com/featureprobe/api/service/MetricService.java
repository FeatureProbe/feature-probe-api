package com.featureprobe.api.service;

import com.featureprobe.api.base.constants.MetricType;
import com.featureprobe.api.dto.MetricResponse;
import com.featureprobe.api.entity.Environment;
import com.featureprobe.api.entity.Event;
import com.featureprobe.api.entity.Targeting;
import com.featureprobe.api.entity.TargetingVersion;
import com.featureprobe.api.entity.VariationHistory;
import com.featureprobe.api.mapper.TargetingVersionMapper;
import com.featureprobe.api.model.AccessEventPoint;
import com.featureprobe.api.model.TargetingContent;
import com.featureprobe.api.model.Variation;
import com.featureprobe.api.model.VariationAccessCounter;
import com.featureprobe.api.repository.EnvironmentRepository;
import com.featureprobe.api.repository.EventRepository;
import com.featureprobe.api.repository.TargetingRepository;
import com.featureprobe.api.repository.TargetingVersionRepository;
import com.featureprobe.api.repository.VariationHistoryRepository;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class MetricService {

    private EnvironmentRepository environmentRepository;
    private EventRepository eventRepository;
    private VariationHistoryRepository variationHistoryRepository;
    private TargetingVersionRepository targetingVersionRepository;
    private TargetingRepository targetingRepository;

    private static final int MAX_QUERY_HOURS = 12 * 24;
    private static final int MAX_QUERY_POINT_COUNT = 12;
    private static final int GROUP_BY_DAY_HOURS = 24;

    public MetricResponse query(String projectKey, String environmentKey, String toggleKey, MetricType metricType,
                                int lastHours) {
        int queryLastHours = Math.min(lastHours, MAX_QUERY_HOURS);
        String serverSdkKey = queryEnvironmentServerSdkKey(projectKey, environmentKey);
        Long targetingId = queryTargetingId(projectKey, environmentKey, toggleKey);
        Map<String, VariationHistory> variationVersionMap = buildVariationVersionMap(projectKey,
                environmentKey, toggleKey);
        List<AccessEventPoint> accessEventPoints = queryAccessEventPoints(serverSdkKey, toggleKey, targetingId,
                queryLastHours);
        List<AccessEventPoint> aggregatedAccessEventPoints = aggregatePointByMetricType(variationVersionMap,
                accessEventPoints, metricType);

        List<VariationAccessCounter> accessCounters = summaryAccessEvents(aggregatedAccessEventPoints);
        Targeting latestTargeting = targetingRepository.findByProjectKeyAndEnvironmentKeyAndToggleKey(projectKey,
                environmentKey, toggleKey).get();
        appendLatestVariations(accessCounters, latestTargeting, metricType);

        return new MetricResponse(accessEventPoints, sortAccessCounters(accessCounters));
    }

    private Map<String, VariationHistory> buildVariationVersionMap(String projectKey, String environmentKey,
                                                                   String toggleKey) {
        List<VariationHistory> variationHistories
                = variationHistoryRepository.findByProjectKeyAndEnvironmentKeyAndToggleKey(projectKey,
                environmentKey, toggleKey);

        return variationHistories.stream().collect(Collectors.toMap(this::toIndexValue, Function.identity()));
    }

    protected List<AccessEventPoint> aggregatePointByMetricType(Map<String, VariationHistory> variationVersionMap,
                                                              List<AccessEventPoint> accessEventPoints,
                                                              MetricType metricType) {

        accessEventPoints.forEach(accessEventPoint -> {
            List<VariationAccessCounter> variationAccessCounters = accessEventPoint.getValues();

            variationAccessCounters.forEach(variationAccessCounter -> {
                VariationHistory variationHistory = variationVersionMap.get(variationAccessCounter.getValue());
                if (variationHistory != null) {
                    variationAccessCounter.setValue(metricType.isNameType()
                            ? variationHistory.getName() : variationHistory.getValue());
                }
            });
            Map<String, Long> variationCounts =
                    accessEventPoint.getValues().stream().collect(Collectors.toMap(VariationAccessCounter::getValue,
                            VariationAccessCounter::getCount, Long::sum));

            List<VariationAccessCounter> values = variationCounts.entrySet().stream().map(e ->
                            new VariationAccessCounter(e.getKey(), e.getValue()))
                    .collect(Collectors.toList());
            accessEventPoint.setValues(values);
        });
        return accessEventPoints;
    }

    private List<AccessEventPoint> queryAccessEventPoints(String serverSdkKey, String toggleKey,
                                                          Long targetId, int lastHours) {
        int pointIntervalCount = getPointIntervalCount(lastHours);
        int pointCount = lastHours / pointIntervalCount;

        LocalDateTime pointStartTime = getQueryStartDateTime(lastHours);
        String pointNameFormat = getPointNameFormat(lastHours);

        List<AccessEventPoint> accessEventPoints = Lists.newArrayList();
        for (int i = 0; i < pointCount; i++) {
            LocalDateTime pointEndTime = pointStartTime.plusHours(pointIntervalCount);
            AccessEventPoint accessEventPoint = queryAccessEventPoint(serverSdkKey, toggleKey, targetId,
                    pointNameFormat, pointStartTime, pointEndTime);

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

    protected AccessEventPoint queryAccessEventPoint(String serverSdkKey, String toggleKey, Long targetingId,
                                                     String pointNameFormat,
                                                     LocalDateTime pointStartTime,
                                                     LocalDateTime pointEndTime) {
        List<VariationAccessCounter> accessEvents = queryAccessEvents(serverSdkKey,
                toggleKey, pointStartTime, pointEndTime);
        String pointName = String.format("%s", pointEndTime.format(DateTimeFormatter.ofPattern(pointNameFormat)));
        Long lastTargetingVersion = queryLastTargetingVersion(targetingId, pointStartTime, pointEndTime);
        return new AccessEventPoint(pointName, accessEvents, lastTargetingVersion);
    }

    private Long queryLastTargetingVersion(Long targetingId, LocalDateTime pointStartTime, LocalDateTime pointEndTime) {
        List<TargetingVersion> targetingVersions = targetingVersionRepository
                .findAllByTargetingIdAndCreatedTimeGreaterThanEqualAndCreatedTimeLessThanEqualOrderByCreatedTimeDesc(
                        targetingId, toDate(pointStartTime), toDate(pointEndTime));
        if (CollectionUtils.isNotEmpty(targetingVersions)) {
            return targetingVersions.get(0).getVersion();
        }
        return null;
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

    protected List<VariationAccessCounter> toAccessEvent(List<Event> events) {
        if (CollectionUtils.isEmpty(events)) {
            return Collections.emptyList();
        }
        Map<String, Long> variationCounts =
                events.stream().collect(Collectors.toMap(this::toIndexValue, Event::getCount, Long::sum));
        return variationCounts.entrySet().stream().map(e ->
                new VariationAccessCounter(e.getKey(), e.getValue())).collect(Collectors.toList());
    }

    private String toIndexValue(Event event) {
        return event.getToggleVersion() + "_" + event.getValueIndex();
    }

    private String toIndexValue(VariationHistory variationHistory) {
        return variationHistory.getToggleVersion() + "_" + variationHistory.getValueIndex();
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

    protected void appendLatestVariations(List<VariationAccessCounter> accessCounters, Targeting latestTargeting,
                                        MetricType metricType) {
        TargetingContent targetingContent = TargetingVersionMapper.INSTANCE.toTargetingContent(
                latestTargeting.getContent());

        if (CollectionUtils.isEmpty(targetingContent.getVariations())) {
            return;
        }
        List<String> latestVariations = targetingContent.getVariations()
                .stream()
                .map(metricType.isNameType() ? Variation::getName : Variation::getValue)
                .collect(Collectors.toList());

        setVariationDeletedIfNotInLatest(accessCounters, latestVariations);
        appendVariationIfInLatest(accessCounters, latestVariations);
    }


    private void setVariationDeletedIfNotInLatest(List<VariationAccessCounter> accessCounters,
                                                  List<String> namesOrValues) {
        accessCounters.stream()
                .forEach(accessCounter -> accessCounter.setDeleted(!namesOrValues.contains(accessCounter.getValue())));
    }

    private void appendVariationIfInLatest(List<VariationAccessCounter> accessCounters, List<String> namesOrValues) {
        namesOrValues.forEach(value -> {
            if (!accessCounters.stream()
                    .filter(accessCounter -> StringUtils.equals(accessCounter.getValue(), value))
                    .findFirst()
                    .isPresent()) {
                accessCounters.add(new VariationAccessCounter(value, 0L));
            }
        });
    }

    protected List<VariationAccessCounter> sortAccessCounters(List<VariationAccessCounter> accessCounters) {
        Collections.sort(accessCounters, Comparator.comparingLong(VariationAccessCounter::getCount).reversed());

        List<VariationAccessCounter> deletedCounters = Lists.newArrayList();
        new ArrayList<>(accessCounters).forEach(accessCounter -> {
            if (BooleanUtils.isTrue(accessCounter.getDeleted())) {
                deletedCounters.add(accessCounter);
                accessCounters.remove(accessCounter);
            }
        });
        accessCounters.addAll(deletedCounters);
        return accessCounters;
    }


    private String queryEnvironmentServerSdkKey(String projectKey, String environmentKey) {
        Environment environment = this.environmentRepository.findByProjectKeyAndKey(projectKey, environmentKey).get();
        return environment.getServerSdkKey();
    }

    private Long queryTargetingId(String projectKey, String environmentKey, String toggleKey) {
        Targeting targeting = targetingRepository.findByProjectKeyAndEnvironmentKeyAndToggleKey(projectKey,
                environmentKey, toggleKey).get();
        return targeting.getId();
    }

    protected boolean isGroupByDay(int queryLastHours) {
        return queryLastHours > GROUP_BY_DAY_HOURS;
    }

    private Date toDate(LocalDateTime pointStartTime) {
        return Date.from(pointStartTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
