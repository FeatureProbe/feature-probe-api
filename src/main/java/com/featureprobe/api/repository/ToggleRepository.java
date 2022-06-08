package com.featureprobe.api.repository;

import com.featureprobe.api.entity.Toggle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ToggleRepository extends JpaRepository<Toggle, Long>, JpaSpecificationExecutor<Toggle> {

    Optional<Toggle> findByProjectKeyAndKey(String projectKey, String key);

    boolean existsByProjectKeyAndKey(String projectKey, String key);

    @Query(
            value = "SELECT * FROM toggle WHERE ( if(?2 != '', name like concat('%',?2,'%'), 1=1) "
                    + "OR if(?2 != '', `key` like concat('%',?2,'%'), 1=1) "
                    + "OR if(?2 != '', description like concat('%',?2,'%'), 1=1) )"
                    + "AND if(?3 != '', `key` IN (?3), 1=1) AND project_key = ?1 AND deleted = 0 AND archived = 0 "
                    + "ORDER BY created_time DESC",
            countQuery = "SELECT count(*) FROM toggle WHERE ( if(?2 != null, name like concat('%',?2,'%'), 1=1) "
                    + "OR if(?2 != '', `key` like concat('%',?2,'%'), 1=1) "
                    + "OR if(?2 != '', description like concat('%',?2,'%'), 1=1) )"
                    + "AND if(?3 != '', `key` IN (?3), 1=1) AND project_key = ?1 AND deleted = 0 AND archived = 0 ",
            nativeQuery = true
    )
    Page<Toggle> findAllByKeyword(String projectKey, String keyword,
                                  Set<String> toggleKeys, Pageable pageable);

    List<Toggle> findAllByProjectKey(String projectKey);

    @Query(value = "SELECT * FROM toggle WHERE project_key = ?1 AND `key` = ?2", nativeQuery = true)
    List<Toggle> findByKeyIncludeDeleted(String projectKey, String key);

    @Query(value = "SELECT * FROM toggle WHERE project_key = ?1 AND name = ?2", nativeQuery = true)
    List<Toggle> findByNameIncludeDeleted(String projectKey, String name);
}
