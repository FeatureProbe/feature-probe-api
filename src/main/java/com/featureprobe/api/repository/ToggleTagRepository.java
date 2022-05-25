package com.featureprobe.api.repository;

import com.featureprobe.api.entity.ToggleTagRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ToggleTagRepository extends JpaRepository<ToggleTagRelation, Long> {

    @Query(value = "SELECT tt.* FROM tag t INNER JOIN toggle_tag tt ON t.id = tt.tag_id "
            + "WHERE t.name in (?1) AND t.deleted = 0", nativeQuery = true)
    List<ToggleTagRelation> findByNames(List<String> names);

}
