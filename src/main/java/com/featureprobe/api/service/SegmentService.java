package com.featureprobe.api.service;

import com.featureprobe.api.base.constants.MessageKey;
import com.featureprobe.api.base.enums.ResourceType;
import com.featureprobe.api.base.enums.ValidateTypeEnum;
import com.featureprobe.api.base.exception.ResourceConflictException;
import com.featureprobe.api.dto.PaginationRequest;
import com.featureprobe.api.dto.SegmentCreateRequest;
import com.featureprobe.api.dto.SegmentResponse;
import com.featureprobe.api.dto.SegmentSearchRequest;
import com.featureprobe.api.dto.SegmentUpdateRequest;
import com.featureprobe.api.dto.ToggleSegmentResponse;
import com.featureprobe.api.entity.Environment;
import com.featureprobe.api.entity.Segment;
import com.featureprobe.api.entity.Targeting;
import com.featureprobe.api.entity.TargetingSegment;
import com.featureprobe.api.entity.Toggle;
import com.featureprobe.api.mapper.SegmentMapper;
import com.featureprobe.api.repository.EnvironmentRepository;
import com.featureprobe.api.repository.SegmentRepository;
import com.featureprobe.api.repository.TargetingRepository;
import com.featureprobe.api.repository.TargetingSegmentRepository;
import com.featureprobe.api.repository.ToggleRepository;
import com.featureprobe.api.util.PageRequestUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.Predicate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class SegmentService {

    private SegmentRepository segmentRepository;

    private TargetingSegmentRepository targetingSegmentRepository;

    private TargetingRepository targetingRepository;

    private ToggleRepository toggleRepository;

    private EnvironmentRepository environmentRepository;

    private SegmentIncludeDeletedService segmentIncludeDeletedService;

    @PersistenceContext
    public EntityManager entityManager;

    public Page<SegmentResponse> list(String projectKey, SegmentSearchRequest searchRequest) {
        if (searchRequest.getIncludeDeleted()) {
            Pageable pageable = PageRequestUtil.toPageable(searchRequest, Sort.Direction.DESC, "created_time");
            return segmentIncludeDeletedService.findAllByKeywordIncludeDeleted(projectKey,
                    searchRequest.getKeyword(), pageable);
        }
        Specification<Segment> spec = buildQuerySpec(projectKey, searchRequest.getKeyword());
        return findPagingBySpec(spec, PageRequestUtil.toCreatedTimeDescSortPageable(searchRequest));
    }

    public SegmentResponse create(String projectKey, SegmentCreateRequest createRequest) {
        segmentIncludeDeletedService.validateKeyIncludeDeleted(projectKey, createRequest.getKey());
        segmentIncludeDeletedService.validateNameIncludeDeleted(projectKey, createRequest.getName());
        Segment segment = SegmentMapper.INSTANCE.requestToEntity(createRequest);
        segment.setProjectKey(projectKey);
        segment.setUniqueKey(StringUtils.join(projectKey, "$", createRequest.getKey()));
        return SegmentMapper.INSTANCE.entityToResponse(segmentRepository.save(segment));
    }

    public SegmentResponse update(String projectKey, String segmentKey, SegmentUpdateRequest updateRequest) {
        Segment segment = segmentRepository.findByProjectKeyAndKey(projectKey, segmentKey);
        if (!StringUtils.equals(segment.getName(), updateRequest.getName())) {
            segmentIncludeDeletedService.validateNameIncludeDeleted(projectKey, updateRequest.getName());
        }
        SegmentMapper.INSTANCE.mapEntity(updateRequest, segment);
        return SegmentMapper.INSTANCE.entityToResponse(segmentRepository.save(segment));
    }

    public SegmentResponse delete(String projectKey, String segmentKey) {
        if (targetingSegmentRepository.countByProjectKeyAndSegmentKey(projectKey, segmentKey) > 0) {
            throw new IllegalArgumentException(MessageKey.USING);
        }
        Segment segment = segmentRepository.findByProjectKeyAndKey(projectKey, segmentKey);
        segment.setDeleted(true);
        return SegmentMapper.INSTANCE.entityToResponse(segmentRepository.save(segment));
    }

    public Page<ToggleSegmentResponse> usingToggles(String projectKey, String segmentKey,
                                                    PaginationRequest paginationRequest) {
        List<TargetingSegment> targetingSegments = targetingSegmentRepository
                .findByProjectKeyAndSegmentKey(projectKey, segmentKey);
        Set<Long> targetingIds = targetingSegments.stream().map(TargetingSegment::getTargetingId)
                .collect(Collectors.toSet());
        Pageable pageable = PageRequest.of(paginationRequest.getPageIndex(), paginationRequest.getPageSize(),
                Sort.Direction.DESC, "createdTime");
        Specification<Targeting> spec = (root, query, cb) -> {
            Predicate p0 = root.get("id").in(targetingIds);
            query.where(cb.and(p0));
            return query.getRestriction();
        };
        Page<Targeting> targetingPage = targetingRepository.findAll(spec, pageable);
        Page<ToggleSegmentResponse> res = targetingPage.map(targeting -> {
            Optional<Toggle> toggleOptional = toggleRepository
                    .findByProjectKeyAndKey(projectKey, targeting.getToggleKey());
            ToggleSegmentResponse toggleSegmentResponse = SegmentMapper.INSTANCE
                    .toggleToToggleSegment(toggleOptional.get());
            toggleSegmentResponse.setDisabled(targeting.getDisabled());
            Optional<Environment> environment = environmentRepository
                    .findByProjectKeyAndKey(projectKey, targeting.getEnvironmentKey());
            toggleSegmentResponse.setEnvironmentName(environment.get().getName());
            toggleSegmentResponse.setEnvironmentKey(environment.get().getKey());
            return toggleSegmentResponse;
        });
        return res;
    }

    public SegmentResponse queryByKey(String projectKey, String segmentKey) {
        Segment segment = segmentRepository.findByProjectKeyAndKey(projectKey, segmentKey);
        return SegmentMapper.INSTANCE.entityToResponse(segment);
    }

    private Specification<Segment> buildQuerySpec(String projectKey, String keyword) {
        return (root, query, cb) -> {
            Predicate p3 = cb.equal(root.get("projectKey"), projectKey);
            if (StringUtils.isNotBlank(keyword)) {
                Predicate p0 = cb.like(root.get("name"), "%" + keyword + "%");
                Predicate p1 = cb.like(root.get("key"), "%" + keyword + "%");
                Predicate p2 = cb.like(root.get("description"), "%" + keyword + "%");
                query.where(cb.or(p0, p1, p2), cb.and(p3));
            } else {
                query.where(p3);
            }
            return query.getRestriction();
        };
    }

    private Page<SegmentResponse> findPagingBySpec(Specification<Segment> spec, Pageable pageable) {
        Page<Segment> segments = segmentRepository.findAll(spec, pageable);
        return segments.map(segment -> SegmentMapper.INSTANCE.entityToResponse(segment));
    }

}
