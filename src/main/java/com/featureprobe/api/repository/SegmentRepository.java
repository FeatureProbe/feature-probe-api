package com.featureprobe.api.repository;

import com.featureprobe.api.entity.Segment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SegmentRepository extends JpaRepository<Segment, Long>, JpaSpecificationExecutor<Segment> {

    Segment findByProjectKeyAndKey(String projectKey, String key);

    boolean existsByProjectKeyAndKey(String projectKey, String key);

    @Query(value = "SELECT count(id) FROM segment WHERE project_key = ?1 AND `key` = ?2", nativeQuery = true)
    int countByKeyIncludeDeleted(String projectKey, String key);

    @Query(value = "SELECT count(id) FROM segment WHERE project_key = ?1 AND name = ?2", nativeQuery = true)
    int countByNameIncludeDeleted(String projectKey, String name);

}
