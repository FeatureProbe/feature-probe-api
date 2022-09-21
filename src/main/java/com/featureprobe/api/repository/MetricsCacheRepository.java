package com.featureprobe.api.repository;

import com.featureprobe.api.base.enums.MetricsCacheTypeEnum;
import com.featureprobe.api.entity.MetricsCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface MetricsCacheRepository extends JpaRepository<MetricsCache, Long>,
        JpaSpecificationExecutor<MetricsCache> {

    Optional<MetricsCache> findBySdkKeyAndToggleKeyAndStartDateAndEndDateAndType(String sdkKey, String toggleKey,
                                                                                 Date startDate, Date endDate,
                                                                                 MetricsCacheTypeEnum type);

    void deleteBySdkKeyAndToggleKey(String sdkKey, String toggleKey);

}
