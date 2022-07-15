package com.featureprobe.api.repository;

import com.featureprobe.api.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    List<Event> findBySdkKeyAndToggleKeyAndStartDateGreaterThanEqualAndEndDateLessThanEqual(String sdkKey,
                                                                                            String toggleKey,
                                                                                            Date startDate,
                                                                                            Date endDate);

    boolean existsBySdkKeyAndToggleKey(String sdkKey, String toggleKey);

}
