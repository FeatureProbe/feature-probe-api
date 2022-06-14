package com.featureprobe.api.repository;

import com.featureprobe.api.entity.TargetingSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TargetingSegmentRepository extends JpaRepository<TargetingSegment, Long>,
        JpaSpecificationExecutor<TargetingSegment> {

    List<TargetingSegment> findByProjectKeyAndSegmentKey(String projectKey, String segmentKey);

}
