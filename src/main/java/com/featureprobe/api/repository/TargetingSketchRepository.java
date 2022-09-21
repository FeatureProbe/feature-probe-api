package com.featureprobe.api.repository;

import com.featureprobe.api.base.enums.SketchStatusEnum;
import com.featureprobe.api.entity.TargetingSketch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface TargetingSketchRepository extends JpaRepository<TargetingSketch, Long>,
        JpaSpecificationExecutor<TargetingSketch> {

    Optional<TargetingSketch> findByApprovalId(Long approvalId);

    List<TargetingSketch> findByApprovalIdIn(Set<Long> approvalIds);

    List<TargetingSketch> findByProjectKeyAndEnvironmentKeyAndStatusAndToggleKeyIn(String projectKey,
                                                                                   String environmentKey,
                                                                                   SketchStatusEnum status,
                                                                                   List<String> toggleKeys);
    
}
