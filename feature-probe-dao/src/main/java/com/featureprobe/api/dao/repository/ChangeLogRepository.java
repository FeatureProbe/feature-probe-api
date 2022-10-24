package com.featureprobe.api.dao.repository;

import com.featureprobe.api.dao.entity.ChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ChangeLogRepository extends JpaRepository<ChangeLog, Long>, JpaSpecificationExecutor<ChangeLog> {

    List<ChangeLog> findAllByIdGreaterThanOrderByIdAsc(long id);

}
