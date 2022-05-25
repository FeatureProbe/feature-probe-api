package com.featureprobe.api.repository;

import com.featureprobe.api.entity.Targeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TargetingRepository extends JpaRepository<Targeting, Long> {

    Optional<Targeting> findByProjectKeyAndEnvironmentKeyAndToggleKey(String projectKey, String environmentKey,
                                                                      String toggleKey);

    List<Targeting> findAllByProjectKeyAndEnvironmentKeyAndDisabled(String projectKey, String environmentKey,
                                                                    boolean disabled);

    List<Targeting> findAllByProjectKeyAndEnvironmentKey(String projectKey, String environmentKey);
}
