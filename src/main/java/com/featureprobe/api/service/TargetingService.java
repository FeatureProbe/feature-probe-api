package com.featureprobe.api.service;

import com.featureprobe.api.base.enums.ResourceType;
import com.featureprobe.api.base.exception.ResourceNotFoundException;
import com.featureprobe.api.dto.TargetingRequest;
import com.featureprobe.api.dto.TargetingResponse;
import com.featureprobe.api.dto.TargetingVersionRequest;
import com.featureprobe.api.dto.TargetingVersionResponse;
import com.featureprobe.api.entity.Targeting;
import com.featureprobe.api.entity.TargetingSegment;
import com.featureprobe.api.entity.TargetingVersion;
import com.featureprobe.api.entity.VariationHistory;
import com.featureprobe.api.mapper.TargetingMapper;
import com.featureprobe.api.mapper.TargetingVersionMapper;
import com.featureprobe.api.model.BaseRule;
import com.featureprobe.api.model.ConditionValue;
import com.featureprobe.api.model.TargetingContent;
import com.featureprobe.api.model.ToggleRule;
import com.featureprobe.api.model.Variation;
import com.featureprobe.api.repository.SegmentRepository;
import com.featureprobe.api.repository.TargetingRepository;
import com.featureprobe.api.repository.TargetingSegmentRepository;
import com.featureprobe.api.repository.TargetingVersionRepository;
import com.featureprobe.api.repository.VariationHistoryRepository;
import com.featureprobe.api.util.PageRequestUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@AllArgsConstructor
@Service
public class TargetingService {

    private TargetingRepository targetingRepository;

    private SegmentRepository segmentRepository;

    private TargetingSegmentRepository targetingSegmentRepository;

    private TargetingVersionRepository targetingVersionRepository;

    private VariationHistoryRepository variationHistoryRepository;

    @Transactional(rollbackFor = Exception.class)
    public TargetingResponse update(String projectKey, String environmentKey,
                                    String toggleKey, TargetingRequest targetingRequest) {
        validateTargetingRefSegmentsExists(projectKey, targetingRequest.getContent());

        Targeting existedTargeting = targetingRepository.findByProjectKeyAndEnvironmentKeyAndToggleKey(projectKey,
                environmentKey, toggleKey).get();
        long oldVersion = existedTargeting.getVersion();
        Targeting updatedTargeting = updateTargeting(existedTargeting, targetingRequest);

        if (updatedTargeting.getVersion() > oldVersion) {
            saveTargetingSegmentRefs(projectKey, updatedTargeting, targetingRequest.getContent());
            saveTargetingVersion(buildTargetingVersion(updatedTargeting, targetingRequest.getComment()));
            saveVariationHistory(updatedTargeting, targetingRequest.getContent());
        }
        return TargetingMapper.INSTANCE.entityToResponse(updatedTargeting);
    }

    private Targeting updateTargeting(Targeting currentTargeting, TargetingRequest updateTargetingRequest) {
        TargetingMapper.INSTANCE.mapEntity(updateTargetingRequest, currentTargeting);

        return targetingRepository.saveAndFlush(currentTargeting);
    }

    private TargetingVersion buildTargetingVersion(Targeting targeting, String comment) {
        TargetingVersion targetingVersion = new TargetingVersion();
        targetingVersion.setTargetingId(targeting.getId());
        targetingVersion.setProjectKey(targeting.getProjectKey());
        targetingVersion.setEnvironmentKey(targeting.getEnvironmentKey());
        targetingVersion.setContent(targeting.getContent());
        targetingVersion.setVersion(targeting.getVersion());
        targetingVersion.setComment(comment);
        return targetingVersion;
    }

    private void saveTargetingVersion(TargetingVersion targetingVersion) {
        targetingVersionRepository.save(targetingVersion);
    }

    public Page<TargetingVersionResponse> queryVersions(String projectKey, String environmentKey,
                                                   TargetingVersionRequest targetingVersionRequest) {
        Page<TargetingVersion> targetingVersions = targetingVersionRepository
                .findAllByProjectKeyAndEnvironmentKey(projectKey, environmentKey,
                        PageRequestUtil.toCreatedTimeDescSortPageable(targetingVersionRequest));
        return targetingVersions.map(targetingVersion ->
                TargetingVersionMapper.INSTANCE.entityToResponse(targetingVersion));
    }


    private void saveTargetingSegmentRefs(String projectKey, Targeting targeting, TargetingContent targetingContent) {
        targetingSegmentRepository.deleteByTargetingId(targeting.getId());

        List<TargetingSegment> targetingSegmentList = getTargetingSegments(projectKey, targeting, targetingContent);
        if (!CollectionUtils.isEmpty(targetingSegmentList)) {
            targetingSegmentRepository.saveAll(targetingSegmentList);
        }
    }

    private List<TargetingSegment> getTargetingSegments(String projectKey, Targeting targeting,
                                                        TargetingContent targetingContent) {
        Set<String> segmentKeys = new TreeSet<>();
        targetingContent.getRules().forEach(toggleRule -> toggleRule.getConditions()
                .stream()
                .filter(ConditionValue::isSegmentType)
                .forEach(conditionValue -> segmentKeys.addAll(conditionValue.getObjects())));

        return segmentKeys.stream().map(segmentKey -> new TargetingSegment(targeting.getId(), segmentKey, projectKey))
                .collect(Collectors.toList());
    }

    private void saveVariationHistory(Targeting targeting,
                                      TargetingContent targetingContent) {
        List<Variation> variations = targetingContent.getVariations();

        List<VariationHistory> variationHistories = IntStream.range(0, targetingContent
                .getVariations().size())
                .mapToObj(index -> convertVariationToEntity(targeting, index,
                        variations.get(index)))
                .collect(Collectors.toList());

        variationHistoryRepository.saveAll(variationHistories);
    }

    private VariationHistory convertVariationToEntity(Targeting targeting, int index, Variation variation) {
        VariationHistory variationHistory = new VariationHistory();

        variationHistory.setEnvironmentKey(targeting.getEnvironmentKey());
        variationHistory.setProjectKey(targeting.getProjectKey());
        variationHistory.setToggleKey(targeting.getToggleKey());
        variationHistory.setValue(variation.getValue());
        variationHistory.setName(variation.getName());
        variationHistory.setToggleVersion(targeting.getVersion());
        variationHistory.setValueIndex(index);

        return variationHistory;
    }

    public TargetingResponse queryByKey(String projectKey, String environmentKey, String toggleKey) {
        Targeting targeting = targetingRepository.findByProjectKeyAndEnvironmentKeyAndToggleKey(projectKey,
                environmentKey, toggleKey).get();
        return TargetingMapper.INSTANCE.entityToResponse(targeting);
    }

    private void validateTargetingRefSegmentsExists(String projectKey, TargetingContent content) {
        if (CollectionUtils.isEmpty(content.getRules())) {
            return;
        }
        content.getRules()
                .stream()
                .filter(BaseRule::isNotEmptyConditions)
                .forEach(validateRuleRefSegmentExists(projectKey));
    }

    private Consumer<ToggleRule> validateRuleRefSegmentExists(String projectKey) {
        return toggleRule -> toggleRule.getConditions().stream().filter(ConditionValue::isSegmentType)
                .forEach(conditionValue -> conditionValue.getObjects().stream().forEach(segmentKey -> {
                    if (!segmentRepository.existsByProjectKeyAndKey(projectKey, segmentKey)) {
                        throw new ResourceNotFoundException(ResourceType.SEGMENT, segmentKey);
                    }
                }));
    }

}
