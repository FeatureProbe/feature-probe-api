package com.featureprobe.api.service;

import com.featureprobe.api.base.enums.ResourceType;
import com.featureprobe.api.base.enums.ValidateTypeEnum;
import com.featureprobe.api.base.exception.ResourceConflictException;
import com.featureprobe.api.dto.SegmentResponse;
import com.featureprobe.api.entity.Segment;
import com.featureprobe.api.mapper.SegmentMapper;
import com.featureprobe.api.repository.SegmentRepository;
import com.featureprobe.api.service.aspect.IncludeDeleted;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.Predicate;

@Service
@AllArgsConstructor
public class SegmentIncludeDeletedService {

    private SegmentRepository segmentRepository;

    @PersistenceContext
    public EntityManager entityManager;

    public void validateExistsIncludeDeleted(String projectKey, ValidateTypeEnum type, String value) {
        switch (type) {
            case KEY:
                validateKeyIncludeDeleted(projectKey, value);
                break;
            case NAME:
                validateNameIncludeDeleted(projectKey, value);
                break;
            default:
                break;
        }
    }

    public void validateKeyIncludeDeleted(String projectKey, String key) {
        if (segmentRepository.existsByProjectKeyAndKey(projectKey, key)) {
            throw new ResourceConflictException(ResourceType.SEGMENT);
        }
    }

    public void validateNameIncludeDeleted(String projectKey, String name) {
        if (segmentRepository.existsByProjectKeyAndName(projectKey, name)) {
            throw new ResourceConflictException(ResourceType.SEGMENT);
        }
    }

    @IncludeDeleted
    public Page<SegmentResponse> findAllByKeywordIncludeDeleted(String projectKey, String keyword, Pageable pageable) {
        Specification<Segment> spec = buildQuerySpec(projectKey, keyword);
        return findPagingBySpec(spec, pageable);
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
