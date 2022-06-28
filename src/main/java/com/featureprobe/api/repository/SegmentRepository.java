package com.featureprobe.api.repository;

import com.featureprobe.api.entity.Segment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SegmentRepository extends JpaRepository<Segment, Long>, JpaSpecificationExecutor<Segment> {

    Segment findByProjectKeyAndKey(String projectKey, String key);

    List<Segment> findAllByProjectKey(String projectKey);

    boolean existsByProjectKeyAndKey(String projectKey, String key);

    @Query(value = "SELECT count(id) FROM segment WHERE project_key = ?1 AND `key` = ?2", nativeQuery = true)
    int countByKeyIncludeDeleted(String projectKey, String key);

    @Query(value = "SELECT count(id) FROM segment WHERE project_key = ?1 AND name = ?2", nativeQuery = true)
    int countByNameIncludeDeleted(String projectKey, String name);

    @Query(
            value = "SELECT * FROM segment WHERE ( if(?2 != '', name like concat('%',?2,'%'), 1=1) "
                    + "OR if(?2 != '', `key` like concat('%',?2,'%'), 1=1) "
                    + "OR if(?2 != '', description like concat('%',?2,'%'), 1=1) )"
                    + "AND project_key = ?1 "
                    + "ORDER BY created_time DESC",
            countQuery = "SELECT count(*) FROM segment WHERE ( if(?2 != null, name like concat('%',?2,'%'), 1=1) "
                    + "OR if(?2 != '', `key` like concat('%',?2,'%'), 1=1) "
                    + "OR if(?2 != '', description like concat('%',?2,'%'), 1=1) )"
                    + "AND project_key = ?1 ",
            nativeQuery = true
    )
    Page<Segment> findAllByKeywordIncludeDeleted(String projectKey, String keyword, Pageable pageable);

}
