package com.featureprobe.api.repository;

import com.featureprobe.api.entity.Toggle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ToggleRepository extends JpaRepository<Toggle, Long>, JpaSpecificationExecutor<Toggle> {

    Optional<Toggle> findByProjectKeyAndKey(String projectKey, String key);

    List<Toggle> findAllByProjectKey(String projectKey);

    boolean existsByProjectKeyAndKey(String projectKey, String key);

    boolean existsByProjectKeyAndName(String projectKey, String key);

}
