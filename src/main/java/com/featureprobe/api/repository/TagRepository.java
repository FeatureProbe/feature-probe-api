package com.featureprobe.api.repository;

import com.featureprobe.api.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {


    @Query(value = "SELECT t.* FROM tag t INNER JOIN toggle_tag tt ON t.id = tt.tag_id "
            + "WHERE tt.toggle_key = ?1 AND t.deleted = 0", nativeQuery = true)
    List<Tag> selectTagsByToggleKey(String toggleKey);

    List<Tag> findByProjectKey(String projectKey);

    Tag findByProjectKeyAndName(String projectKey, String name);

    List<Tag> findByProjectKeyAndNameIn(String key, String[] tagNames);
}
