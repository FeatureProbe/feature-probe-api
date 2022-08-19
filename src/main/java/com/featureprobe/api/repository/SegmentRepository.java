package com.featureprobe.api.repository;

import com.featureprobe.api.entity.Segment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SegmentRepository extends JpaRepository<Segment, Long>, JpaSpecificationExecutor<Segment> {

    Segment findByProjectKeyAndKey(String projectKey, String key);

    List<Segment> findAllByProjectKeyAndOrganizationId(String projectKey, Long organizationId);

    boolean existsByProjectKeyAndKey(String projectKey, String key);

    boolean existsByProjectKeyAndName(String projectKey, String name);

}
