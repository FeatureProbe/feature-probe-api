package com.featureprobe.api.repository;

import com.featureprobe.api.entity.ToggleTagRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ToggleTagRepository extends JpaRepository<ToggleTagRelation, Long> {

}
