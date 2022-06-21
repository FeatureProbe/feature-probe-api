package com.featureprobe.api.service;

import com.featureprobe.api.base.enums.ResourceType;
import com.featureprobe.api.base.exception.ResourceNotFoundException;
import com.featureprobe.api.dto.TargetingRequest;
import com.featureprobe.api.dto.TargetingResponse;
import com.featureprobe.api.entity.Targeting;
import com.featureprobe.api.entity.TargetingSegment;
import com.featureprobe.api.mapper.TargetingMapper;
import com.featureprobe.api.model.TargetingContent;
import com.featureprobe.api.repository.SegmentRepository;
import com.featureprobe.api.repository.TargetingRepository;
import com.featureprobe.api.repository.TargetingSegmentRepository;
import com.featureprobe.sdk.server.model.ConditionType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Service
public class TargetingService {

    private TargetingRepository targetingRepository;

    private SegmentRepository segmentRepository;

    private TargetingSegmentRepository targetingSegmentRepository;

    @Transactional(rollbackFor = Exception.class)
    public TargetingResponse update(String projectKey, String environmentKey,
                                    String toggleKey, TargetingRequest targetingRequest) {
        validateTargetingContent(projectKey, targetingRequest.getContent());
        Targeting targeting = targetingRepository.findByProjectKeyAndEnvironmentKeyAndToggleKey(projectKey,
                environmentKey, toggleKey).get();
        TargetingMapper.INSTANCE.mapEntity(targetingRequest, targeting);
        targetingSegmentRepository.deleteByTargetingId(targeting.getId());
        saveTargetingSegmentRefs(projectKey, targeting, targetingRequest);
        return TargetingMapper.INSTANCE.entityToResponse(targetingRepository.save(targeting));
    }

    private void saveTargetingSegmentRefs(String projectKey, Targeting targeting, TargetingRequest targetingRequest) {
        List<TargetingSegment> targetingSegmentList = getTargetingSegments(projectKey, targeting, targetingRequest);
        if (!CollectionUtils.isEmpty(targetingSegmentList)) {
            targetingSegmentRepository.saveAll(targetingSegmentList);
        }
    }

    private List<TargetingSegment> getTargetingSegments(String projectKey, Targeting targeting,
                                                          TargetingRequest targetingRequest) {
        Set<String> segmentKeys = new TreeSet<>();
        targetingRequest.getContent().getRules().forEach(toggleRule -> {
            toggleRule.getConditions().forEach(conditionValue -> {
                if (StringUtils.equals(ConditionType.SEGMENT.toValue(), conditionValue.getType())) {
                    segmentKeys.addAll(conditionValue.getObjects());
                }
            });
        });
        return segmentKeys.stream().map(segmentKey -> new TargetingSegment(targeting.getId(), segmentKey, projectKey))
                .collect(Collectors.toList());
    }

    public TargetingResponse queryByKey(String projectKey, String environmentKey, String toggleKey) {
        Targeting targeting = targetingRepository.findByProjectKeyAndEnvironmentKeyAndToggleKey(projectKey,
                environmentKey, toggleKey).get();
        return TargetingMapper.INSTANCE.entityToResponse(targeting);
    }

    private void validateTargetingContent(String projectKey, TargetingContent content) {
        if (CollectionUtils.isEmpty(content.getRules())) return;
        content.getRules().stream().forEach(toggleRule -> {
            if (CollectionUtils.isEmpty(toggleRule.getConditions())) return;
            toggleRule.getConditions().stream().forEach(conditionValue -> {
                if (!StringUtils.equals(ConditionType.SEGMENT.toValue(), conditionValue.getType())) return;
                conditionValue.getObjects().stream().forEach(segmentKey -> {
                    if (!segmentRepository.existsByProjectKeyAndKey(projectKey, segmentKey)) {
                        throw new ResourceNotFoundException(ResourceType.SEGMENT, segmentKey);
                    }
                });
            });
        });

    }

}
