package com.featureprobe.api.service;

import com.featureprobe.api.base.enums.ResourceType;
import com.featureprobe.api.base.enums.ValidateTypeEnum;
import com.featureprobe.api.base.enums.VisitFilter;
import com.featureprobe.api.base.exception.ResourceConflictException;
import com.featureprobe.api.base.exception.ResourceNotFoundException;
import com.featureprobe.api.dto.ToggleCreateRequest;
import com.featureprobe.api.dto.ToggleItemResponse;
import com.featureprobe.api.dto.ServerResponse;
import com.featureprobe.api.dto.ToggleResponse;
import com.featureprobe.api.dto.ToggleSearchRequest;
import com.featureprobe.api.dto.ToggleUpdateRequest;
import com.featureprobe.api.entity.Environment;
import com.featureprobe.api.entity.Event;
import com.featureprobe.api.entity.Segment;
import com.featureprobe.api.entity.Tag;
import com.featureprobe.api.entity.Targeting;
import com.featureprobe.api.entity.TargetingVersion;
import com.featureprobe.api.entity.Toggle;
import com.featureprobe.api.entity.ToggleTagRelation;
import com.featureprobe.api.entity.VariationHistory;
import com.featureprobe.api.mapper.JsonMapper;
import com.featureprobe.api.mapper.ToggleMapper;
import com.featureprobe.api.model.ServerSegmentBuilder;
import com.featureprobe.api.model.TargetingContent;
import com.featureprobe.api.model.ServerToggleBuilder;
import com.featureprobe.api.model.Variation;
import com.featureprobe.api.repository.EnvironmentRepository;
import com.featureprobe.api.repository.EventRepository;
import com.featureprobe.api.repository.SegmentRepository;
import com.featureprobe.api.repository.TagRepository;
import com.featureprobe.api.repository.TargetingRepository;
import com.featureprobe.api.repository.TargetingVersionRepository;
import com.featureprobe.api.repository.ToggleRepository;
import com.featureprobe.api.repository.ToggleTagRepository;
import com.featureprobe.api.repository.VariationHistoryRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@AllArgsConstructor
public class ToggleService {

    private ToggleRepository toggleRepository;

    private SegmentRepository segmentRepository;

    private TagRepository tagRepository;

    private ToggleTagRepository toggleTagRepository;

    private TargetingRepository targetingRepository;

    private EnvironmentRepository environmentRepository;

    private EventRepository eventRepository;

    private TargetingVersionRepository targetingVersionRepository;

    private VariationHistoryRepository variationHistoryRepository;

    @Transactional(rollbackFor = Exception.class)
    public ToggleResponse create(String projectKey, ToggleCreateRequest createRequest) {
        Toggle toggle = createToggle(projectKey, createRequest);
        createDefaultTargetingEntities(projectKey, toggle);
        return ToggleMapper.INSTANCE.entityToResponse(toggle);
    }

    protected Toggle createToggle(String projectKey, ToggleCreateRequest createRequest) {
        validateKey(projectKey, createRequest.getKey());
        validateName(projectKey, createRequest.getName());
        Toggle toggle = ToggleMapper.INSTANCE.requestToEntify(createRequest);
        toggle.setProjectKey(projectKey);
        toggle.setDeleted(false);
        toggle.setArchived(false);
        setToggleTagRefs(toggle, createRequest.getTags());
        toggleRepository.save(toggle);
        return toggle;
    }

    private void createDefaultTargetingEntities(String projectKey, Toggle toggle) {
        List<Environment> environments = environmentRepository.findAllByProjectKey(projectKey);
        if (CollectionUtils.isEmpty(environments)) {
            log.info("{} environment is empty, ignore create targeting", projectKey);
            return;
        }
        List<Targeting> targetingList = environments.stream().map(environment ->
                createDefaultTargeting(toggle, environment)).collect(Collectors.toList());
        List<Targeting> savedTargetingList = targetingRepository.saveAll(targetingList);
        for (Targeting targeting : savedTargetingList) {
            saveTargetingVersion(buildTargetingVersion(targeting, ""));
            saveVariationHistory(targeting);
        }
    }

    private void saveTargetingVersion(TargetingVersion targetingVersion) {
        targetingVersionRepository.save(targetingVersion);
    }

    private void saveVariationHistory(Targeting targeting) {
        List<Variation> variations = JsonMapper.toObject(targeting.getContent(), TargetingContent.class)
                .getVariations();

        List<VariationHistory> variationHistories = IntStream.range(0, variations.size())
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

    private Targeting createDefaultTargeting(Toggle toggle, Environment environment) {
        Targeting targeting = new Targeting();
        targeting.setDeleted(false);
        targeting.setVersion(1L);
        targeting.setProjectKey(toggle.getProjectKey());
        targeting.setDisabled(true);
        targeting.setContent(TargetingContent.newDefault(toggle).toJson());
        targeting.setToggleKey(toggle.getKey());
        targeting.setEnvironmentKey(environment.getKey());

        return targeting;
    }

    @Transactional(rollbackFor = Exception.class)
    public ToggleResponse update(String projectKey, String toggleKey, ToggleUpdateRequest updateRequest) {
        Toggle toggle = toggleRepository.findByProjectKeyAndKey(projectKey, toggleKey).get();
        if(!StringUtils.equals(toggle.getName(), updateRequest.getName())) {
            validateName(projectKey, updateRequest.getName());
        }
        ToggleMapper.INSTANCE.mapEntity(updateRequest, toggle);
        setToggleTagRefs(toggle, updateRequest.getTags());

        toggleRepository.save(toggle);

        return ToggleMapper.INSTANCE.entityToResponse(toggle);
    }

    private void setToggleTagRefs(Toggle toggle, String[] tagNames) {
        List<Tag> tags = tagRepository.findByProjectKeyAndNameIn(toggle.getProjectKey(), tagNames);
        toggle.setTags(tags);
    }

    public Page<ToggleItemResponse> query(String projectKey, ToggleSearchRequest searchRequest) {
        Environment environment = environmentRepository
                .findByProjectKeyAndKey(projectKey, searchRequest.getEnvironmentKey())
                .orElseThrow(() ->
                        new ResourceNotFoundException(ResourceType.ENVIRONMENT, searchRequest.getEnvironmentKey()));
        Set<String> toggleKeys = new TreeSet<>();
        boolean isPrecondition = false;
        if (Objects.nonNull(searchRequest.getDisabled())) {
            isPrecondition = true;
            Set<String> keys = queryToggleKeysByDisabled(projectKey, searchRequest.getEnvironmentKey(),
                    searchRequest.getDisabled());
            retainAllKeys(toggleKeys, keys);
        }
        if (!CollectionUtils.isEmpty(searchRequest.getTags())) {
            isPrecondition = true;
            Set<String> keys = queryToggleKeysByTags(searchRequest.getTags());
            retainAllKeys(toggleKeys, keys);
        }
        if (Objects.nonNull(searchRequest.getVisitFilter())) {
            isPrecondition = true;
            Set<String> keys = queryToggleKeysByVisited(searchRequest.getVisitFilter(), projectKey, environment);
            retainAllKeys(toggleKeys, keys);
        }
        Page<Toggle> togglePage = compoundQuery(projectKey, searchRequest, toggleKeys, isPrecondition);
        Page<ToggleItemResponse> toggleItemPage = togglePage.map(item ->
                entityToItemResponse(item, projectKey, searchRequest.getEnvironmentKey()));
        return toggleItemPage;
    }

    private Page<Toggle> compoundQuery(String projectKey, ToggleSearchRequest searchRequest, Set<String> toggleKeys,
                                       boolean isPrecondition) {
        Specification<Toggle> resultSpec = new Specification<Toggle>() {
            @Override
            public Predicate toPredicate(Root<Toggle> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Predicate p1 = cb.like(root.get("name"), "%" + searchRequest.getKeyword() + "%");
                Predicate p2 = cb.like(root.get("key"), "%" + searchRequest.getKeyword() + "%");
                Predicate p3 = cb.like(root.get("desc"), "%" + searchRequest.getKeyword() + "%");
                Predicate p4 = root.get("key").in(toggleKeys);
                Predicate p5 = cb.equal(root.get("projectKey"), projectKey);
                Predicate p6 = cb.equal(root.get("archived"), 0);
                if (StringUtils.isNotBlank(searchRequest.getKeyword())) {
                    if (isPrecondition) {
                        return query.where(cb.or(p1, p2, p3), cb.and(p4, p5, p6)).getRestriction();
                    }
                    return query.where(cb.or(p1, p2, p3), cb.and(p5, p6)).getRestriction();
                }
                if (isPrecondition) {
                    return query.where(cb.and(p4, p5, p6)).getRestriction();
                }
                return query.where(cb.and(p5, p6)).getRestriction();
            }
        };
        Pageable pageable = PageRequest.of(searchRequest.getPageIndex(), searchRequest.getPageSize(),
                Sort.Direction.DESC, "createdTime");
        return toggleRepository.findAll(resultSpec, pageable);
    }

    private void retainAllKeys(Set<String> keys, Set<String> targets) {
        if (keys.isEmpty()) {
            keys.addAll(targets);
        } else {
            keys.retainAll(targets);
        }
    }

    private Set<String> queryToggleKeysByDisabled(String projectKey, String environmentKey, Boolean disabled) {
        List<Targeting> targetingList = targetingRepository.findAllByProjectKeyAndEnvironmentKeyAndDisabled(projectKey,
                environmentKey, disabled);
        return targetingList.stream().map(Targeting::getToggleKey).collect(Collectors.toSet());
    }

    private Set<String> queryToggleKeysByTags(List<String> tags) {
        List<ToggleTagRelation> toggleTagRelations = toggleTagRepository.findByNames(tags);
        return toggleTagRelations.stream().map(ToggleTagRelation::getToggleKey).collect(Collectors.toSet());
    }

    private Set<String> queryToggleKeysByVisited(VisitFilter visitFilter, String projectKey, Environment environment) {
        switch (visitFilter) {
            case IN_WEEK_VISITED:
                return weekVisitedToggleKeys(environment);
            case OUT_WEEK_VISITED:
                return lastVisitBeforeWeekToggleKeys(environment);
            case NOT_VISITED:
                return neverVisited(projectKey, environment);
            default:
                return new HashSet();
        }
    }
    
    private Set<String> allVisitedToggleKeys(Environment environment) {
        Specification<Event> spec = (root, query, cb) -> {
            Predicate p1 = cb.equal(root.get("sdkKey"), environment.getServerSdkKey());
            Predicate p2 = cb.equal(root.get("sdkKey"), environment.getClientSdkKey());
            return query.where(cb.or(p1, p2)).groupBy(root.get("toggleKey"))
                    .getRestriction();
        };
        List<Event> events = eventRepository.findAll(spec);
        return events.stream().map(Event::getToggleKey).collect(Collectors.toSet());
    }

    private Set<String> weekVisitedToggleKeys(Environment environment) {
        Date lastWeek = Date.from(LocalDateTime.now().minusDays(7).atZone(ZoneId.systemDefault()).toInstant());
        Specification<Event> spec = (root, query, cb) -> {
            Predicate p1 = cb.equal(root.get("sdkKey"), environment.getServerSdkKey());
            Predicate p2 = cb.equal(root.get("sdkKey"), environment.getClientSdkKey());
            Predicate p3 = cb.greaterThanOrEqualTo(root.get("endDate"), lastWeek);
            return query.where(cb.or(p1, p2), cb.and(p3)).groupBy(root.get("toggleKey"))
                    .getRestriction();
        };
        List<Event> events = eventRepository.findAll(spec);
        return events.stream().map(Event::getToggleKey).collect(Collectors.toSet());
    }

    private Set<String> lastVisitBeforeWeekToggleKeys(Environment environment) {
        Set<String> allVisitedKeys = allVisitedToggleKeys(environment);
        Set<String> weekVisitedKeys = weekVisitedToggleKeys(environment);
        allVisitedKeys.removeAll(weekVisitedKeys);
        return allVisitedKeys;
    }

    private Set<String> neverVisited(String projectKey, Environment environment) {
        Set<String> allVisitedKeys = allVisitedToggleKeys(environment);
        Specification<Toggle> notSpec = (root, query, cb) -> {
            Predicate p1 = cb.equal(root.get("projectKey"), projectKey);
            Predicate p2 = root.get("key").in(allVisitedKeys).not();
            return query.where(cb.and(p1, p2)).getRestriction();
        };
        List<Toggle> toggles = toggleRepository.findAll(notSpec);
        return toggles.stream().map(Toggle::getKey).collect(Collectors.toSet());
    }


    private ToggleItemResponse entityToItemResponse(Toggle toggle, String projectKey, String environmentKey) {
        ToggleItemResponse toggleItem = ToggleMapper.INSTANCE.entityToItemResponse(toggle);
        List<Tag> tags = tagRepository.selectTagsByToggleKey(toggle.getKey());
        toggleItem.setTags(tags.stream().map(Tag::getName).collect(Collectors.toList()));
        Targeting targeting = targetingRepository.findByProjectKeyAndEnvironmentKeyAndToggleKey(projectKey,
                environmentKey, toggle.getKey()).get();
        toggleItem.setDisabled(targeting.getDisabled());
        toggleItem.setVisitedTime(queryToggleVisited(projectKey, environmentKey, toggle.getKey()));
        return toggleItem;
    }

    private Date queryToggleVisited(String projectKey, String environmentKey, String toggleKey) {
        Environment environment = environmentRepository.findByProjectKeyAndKey(projectKey, environmentKey).get();
        Pageable pageable = PageRequest.of(0, 1,
                Sort.Direction.DESC, "startDate");
        Specification<Event> spec = new Specification<Event>() {
            @Override
            public Predicate toPredicate(Root<Event> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Predicate p0 = cb.equal(root.get("toggleKey"), toggleKey);
                Predicate p1 = cb.equal(root.get("sdkKey"), environment.getServerSdkKey());
                Predicate p2 = cb.equal(root.get("sdkKey"), environment.getClientSdkKey());
                return query.where(cb.and(p0), cb.or(p1, p2)).getRestriction();
            }
        };
        Page<Event> events = eventRepository.findAll(spec, pageable);
        if (!CollectionUtils.isEmpty(events.getContent())) {
            return events.getContent().get(0).getStartDate();
        }
        return null;
    }

    public ToggleResponse queryByKey(String projectKey, String toggleKey) {
        Toggle toggle = toggleRepository.findByProjectKeyAndKey(projectKey, toggleKey).get();
        return ToggleMapper.INSTANCE.entityToResponse(toggle);
    }

    public ServerResponse queryServerTogglesByServerSdkKey(String serverSdkKey) {
        return new ServerResponse(queryTogglesBySdkKey(serverSdkKey), querySegmentsBySdkKey(serverSdkKey));
    }

    private List<com.featureprobe.sdk.server.model.Segment> querySegmentsBySdkKey(String serverSdkKey) {
        Environment environment = environmentRepository.findByServerSdkKey(serverSdkKey).get();
        if (Objects.isNull(environment)) {
            return Collections.emptyList();
        }
        List<Segment> segments = segmentRepository.findAllByProjectKey(environment.getProject().getKey());
        return segments.stream().map(segment -> {
            try {
                return new ServerSegmentBuilder().builder()
                        .uniqueId(segment.getUniqueKey())
                        .version(segment.getVersion())
                        .rules(segment.getRules())
                        .build();
            } catch (Exception e) {
                log.error("Build server segment failed, server sdk key: {}, segment key: {}",
                        serverSdkKey, segment.getKey(), e);
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private List<com.featureprobe.sdk.server.model.Toggle> queryTogglesBySdkKey(String serverSdkKey) {
        Environment environment = environmentRepository.findByServerSdkKey(serverSdkKey).get();
        if (Objects.isNull(environment)) {
            return Collections.emptyList();
        }
        List<Segment> segments = segmentRepository.findAllByProjectKey(environment.getProject().getKey());
        List<Toggle> toggles = toggleRepository.findAllByProjectKey(environment.getProject().getKey());
        Map<String, Targeting> targetingByKey = targetingRepository.findAllByProjectKeyAndEnvironmentKey(
                environment.getProject().getKey(),
                environment.getKey()).stream().collect(Collectors.toMap(Targeting::getToggleKey, Function.identity()));
        return toggles.stream().map(toggle -> {
            Targeting targeting = targetingByKey.get(toggle.getKey());
            try {
                return new ServerToggleBuilder().builder()
                        .key(toggle.getKey())
                        .disabled(targeting.getDisabled())
                        .version(targeting.getVersion())
                        .returnType(toggle.getReturnType())
                        .forClient(toggle.getClientAvailability())
                        .rules(targeting.getContent())
                        .segments(segments.stream().collect(Collectors.toMap(Segment::getKey, Function.identity())))
                        .build();
            } catch (Exception e) {
                log.warn("Build server toggle failed, server sdk key: {}, toggle key: {}, env key: {}",
                        serverSdkKey, targeting.getToggleKey(), targeting.getEnvironmentKey(), e);
                return null;
            }

        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public void validateExists(String projectKey, ValidateTypeEnum type, String  value) {
        switch (type) {
            case KEY:
                validateKey(projectKey, value);
                break;
            case NAME:
                validateName(projectKey, value);
                break;
            default:
                break;
        }
    }


    private void validateKey(String projectKey, String key) {
        if (toggleRepository.countByKeyIncludeDeleted(projectKey, key) > 0) {
            throw new ResourceConflictException(ResourceType.TOGGLE);
        }
    }

    private void validateName(String projectKey, String name) {
        if (toggleRepository.countByNameIncludeDeleted(projectKey, name) > 0) {
            throw new ResourceConflictException(ResourceType.TOGGLE);
        }
    }

}
