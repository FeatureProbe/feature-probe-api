package com.featureprobe.api.repository;

import com.featureprobe.api.entity.Project;
import com.featureprobe.api.validate.ResourceKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    Optional<Project> findByKey(String key);

    boolean existsByKey(String key);

    List<Project> findAllByNameContainsIgnoreCaseOrDescriptionContainsIgnoreCase(String keywordName,
                                                                                 String keywordDesc);

    @Query(value = "SELECT * FROM project WHERE `key` = ?1", nativeQuery = true)
    List<Project> findByKeyIncludeDeleted(String key);

    @Query(value = "SELECT * FROM project WHERE name = ?1", nativeQuery = true)
    List<Project> findByNameIncludeDeleted(String name);
}
