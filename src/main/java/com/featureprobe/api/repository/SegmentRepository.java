package com.featureprobe.api.repository;

import com.featureprobe.api.entity.Segment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SegmentRepository extends JpaRepository<Segment, Long>, JpaSpecificationExecutor<Segment> {
}
