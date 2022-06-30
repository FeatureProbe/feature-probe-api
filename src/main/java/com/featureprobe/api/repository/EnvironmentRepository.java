package com.featureprobe.api.repository;

import com.featureprobe.api.entity.Environment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnvironmentRepository extends JpaRepository<Environment, Long> {

    Optional<Environment> findByKey(String key);

    List<Environment> findAllByProjectKey(String projectKey);

    Optional<Environment> findByServerSdkKey(String serverSdkKey);

    Optional<Environment> findByServerSdkKeyOrClientSdkKey(String serverSdkKey, String clientSdkKey);

    boolean existsByProjectKeyAndKey(String projectKey, String key);

    Optional<Environment> findByProjectKeyAndKey(String projectKey, String key);

    @Query(value = "SELECT count(id) FROM environment WHERE project_key = ?1 AND `key` = ?2", nativeQuery = true)
    int countByKeyIncludeDeleted(String projectKey, String key);

    @Query(value = "SELECT count(id) FROM environment WHERE project_key = ?1 AND name = ?2", nativeQuery = true)
    int countByNameIncludeDeleted(String projectKey, String name);
}
