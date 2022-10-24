package com.featureprobe.api.dao.repository;

import com.featureprobe.api.dao.entity.Environment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface EnvironmentRepository extends JpaRepository<Environment, Long> {

    List<Environment> findAllByProjectKey(String projectKey);

    List<Environment> findByKeyIn(Set<String> key);

    List<Environment> findAllByProjectKeyAndArchived(String projectKey, Boolean archived);

    long countByProjectKey(String projectKey);

    Optional<Environment> findByServerSdkKey(String serverSdkKey);

    Optional<Environment> findByServerSdkKeyOrClientSdkKey(String serverSdkKey, String clientSdkKey);

    boolean existsByProjectKeyAndKey(String projectKey, String key);

    boolean existsByProjectKeyAndName(String projectKey, String name);

    Optional<Environment> findByProjectKeyAndKey(String projectKey, String key);

    Optional<Environment> findByProjectKeyAndKeyAndArchived(String projectKey, String key, Boolean archived);

    List<Environment> findAllByArchivedAndDeleted(boolean archived, boolean deleted);
}
