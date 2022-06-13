package com.featureprobe.api.service;

import com.featureprobe.api.dto.SegmentResponse;
import com.featureprobe.api.dto.SegmentSearchRequest;
import com.featureprobe.api.entity.Segment;
import com.featureprobe.api.mapper.SegmentMapper;
import com.featureprobe.api.repository.SegmentRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;

@Slf4j
@AllArgsConstructor
@Service
public class SegmentService {

    private SegmentRepository segmentRepository;

    public Page<SegmentResponse> list(String projectKey, SegmentSearchRequest searchRequest) {
        Pageable pageable = PageRequest.of(searchRequest.getPageIndex(), searchRequest.getPageSize(),
                Sort.Direction.DESC, "createdTime");
        Specification<Segment> spec = (root, query, cb) -> {
            Predicate p2 = cb.equal(root.get("projectKey"), projectKey);
            if (StringUtils.isNotBlank(searchRequest.getKeyword())) {
                Predicate p0 = cb.like(root.get("name"), "%" + searchRequest.getKeyword() + "%");
                Predicate p1 = cb.like(root.get("key"), "%" + searchRequest.getKeyword() + "%");
                query.where(cb.or(p0, p1), cb.and(p2));
            } else {
                query.where(p2);
            }
            return query.getRestriction();
        };
        Page<Segment> segments = segmentRepository.findAll(spec, pageable);
        return segments.map(segment -> SegmentMapper.INSTANCE.entityToResponse(segment));
    }
}
