package com.featureprobe.api.repository;

import com.featureprobe.api.entity.TargetingVersion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TargetingVersionRepository extends JpaRepository<TargetingVersion, Long>,
        JpaSpecificationExecutor<TargetingVersion> {

    Page<TargetingVersion> findAllByProjectKeyAndEnvironmentKey(String projectKey, String environmentKey,
                                                                 Pageable pageable);
}
