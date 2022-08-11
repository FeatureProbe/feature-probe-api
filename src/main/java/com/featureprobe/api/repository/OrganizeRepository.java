package com.featureprobe.api.repository;

import com.featureprobe.api.entity.Organize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizeRepository extends JpaRepository<Organize, Long>, JpaSpecificationExecutor<Organize> {

}
