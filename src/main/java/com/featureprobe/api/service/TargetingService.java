package com.featureprobe.api.service;

import com.featureprobe.api.base.enums.ResourceType;
import com.featureprobe.api.base.exception.ResourceNotFoundException;
import com.featureprobe.api.dto.AfterTargetingVersionResponse;
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
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@AllArgsConstructor
@Service
public class TargetingService {

    private static final Pattern dateTimeRegex = Pattern.compile("[0-9]{3}[0-9]-[0-1][0-9]-[0-3][0-9]T[0-2][0-9]" +
            "(:[0-6][0-9]){2}\\+[0-2][0-9]:[0-1][0-9]");

    private static final Pattern versionRegex = Pattern.compile("^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)" +
            "(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))" +
            "?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$");

    private TargetingRepository targetingRepository;

    private SegmentRepository segmentRepository;

    private TargetingSegmentRepository targetingSegmentRepository;

    private TargetingVersionRepository targetingVersionRepository;

    private VariationHistoryRepository variationHistoryRepository;

    @Transactional(rollbackFor = Exception.class)
    public TargetingResponse update(String projectKey, String environmentKey,
                                    String toggleKey, TargetingRequest targetingRequest) {
        validateTargetingContent(projectKey, targetingRequest.getContent());
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
        targetingVersion.setProjectKey(targeting.getProjectKey());
        targetingVersion.setEnvironmentKey(targeting.getEnvironmentKey());
        targetingVersion.setToggleKey(targeting.getToggleKey());
        targetingVersion.setContent(targeting.getContent());
        targetingVersion.setDisabled(targeting.getDisabled());
        targetingVersion.setVersion(targeting.getVersion());
        targetingVersion.setComment(comment);
        return targetingVersion;
    }

    private void saveTargetingVersion(TargetingVersion targetingVersion) {
        targetingVersionRepository.save(targetingVersion);
    }

    public Page<TargetingVersionResponse> queryVersions(String projectKey, String environmentKey, String toggleKey,
                                                        TargetingVersionRequest targetingVersionRequest) {
        Page<TargetingVersion> targetingVersions ;
        if(Objects.isNull(targetingVersionRequest.getVersion())) {
            targetingVersions = targetingVersionRepository
                    .findAllByProjectKeyAndEnvironmentKeyAndToggleKey(projectKey, environmentKey, toggleKey,
                            PageRequestUtil.toCreatedTimeDescSortPageable(targetingVersionRequest));
        } else {
            targetingVersions = targetingVersionRepository
                    .findAllByProjectKeyAndEnvironmentKeyAndToggleKeyAndVersionLessThanOrderByVersionDesc(
                            projectKey, environmentKey, toggleKey, targetingVersionRequest.getVersion(),
                            PageRequestUtil.toCreatedTimeDescSortPageable(targetingVersionRequest));
        }
        return targetingVersions.map(targetingVersion ->
                TargetingVersionMapper.INSTANCE.entityToResponse(targetingVersion));
    }

    public AfterTargetingVersionResponse queryAfterVersion(String projectKey, String environmentKey, String toggleKey,
                                                           Long version) {
        List<TargetingVersion> targetingVersions = targetingVersionRepository
                .findAllByProjectKeyAndEnvironmentKeyAndToggleKeyAndVersionGreaterThanEqualOrderByVersionDesc(
                        projectKey, environmentKey, toggleKey, version);
        List<TargetingVersionResponse> versions = targetingVersions.stream().map(targetingVersion ->
                TargetingVersionMapper.INSTANCE.entityToResponse(targetingVersion)).collect(Collectors.toList());
        long total = targetingVersionRepository.countByProjectKeyAndEnvironmentKeyAndToggleKey(projectKey,
                environmentKey, toggleKey);
        return new AfterTargetingVersionResponse(total, versions);
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

    private void validateTargetingContent(String projectKey, TargetingContent content) {
        if (CollectionUtils.isEmpty(content.getRules())) {
            return;
        }
        content.getRules()
                .stream()
                .filter(BaseRule::isNotEmptyConditions)
                .forEach(toggleRule -> {
                    validateRuleRefSegmentExists(projectKey, toggleRule);
                    validateNumber(toggleRule);
                    validateDatetime(toggleRule);
                    validateSemVer(toggleRule);
                });
    }

    private void validateRuleRefSegmentExists(String projectKey, ToggleRule toggleRule) {
        toggleRule.getConditions().stream().filter(ConditionValue::isSegmentType)
                .forEach(conditionValue -> conditionValue.getObjects().stream().forEach(segmentKey -> {
                    if (!segmentRepository.existsByProjectKeyAndKey(projectKey, segmentKey)) {
                        throw new ResourceNotFoundException(ResourceType.SEGMENT, segmentKey);
                    }
                }));
    }

    private void validateNumber(ToggleRule toggleRule) {
        toggleRule.getConditions().stream().filter(ConditionValue::isNumberType)
                .forEach(conditionValue -> conditionValue.getObjects().stream().forEach(number -> {
                    try {
                        Double.parseDouble(number);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("validate.number_format_error");
                    }
                }));
    }

    private void validateDatetime(ToggleRule toggleRule) {
        toggleRule.getConditions().stream().filter(ConditionValue::isDatetimeType)
                .forEach(conditionValue -> conditionValue.getObjects().stream().forEach(datetime -> {
                    if (!dateTimeRegex.matcher(datetime).matches()) {
                        throw new IllegalArgumentException("validate.datetime_format_error");
                    }
                }));
    }

    private void validateSemVer(ToggleRule toggleRule) {
        toggleRule.getConditions().stream().filter(ConditionValue::isSemVerType)
                .forEach(conditionValue -> conditionValue.getObjects().stream().forEach(semVer -> {
                    if (!versionRegex.matcher(semVer).matches()) {
                        throw new IllegalArgumentException("validate.version_format_error");
                    }
                }));
    }

}
