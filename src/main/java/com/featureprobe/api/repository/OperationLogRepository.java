package com.featureprobe.api.repository;

import com.featureprobe.api.entity.OperationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Long>,
        JpaSpecificationExecutor<OperationLog> {
}
