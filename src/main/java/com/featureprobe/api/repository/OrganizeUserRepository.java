package com.featureprobe.api.repository;

import com.featureprobe.api.entity.OrganizeUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizeUserRepository extends JpaRepository<OrganizeUser, Long>,
        JpaSpecificationExecutor<OrganizeUser> {

    Optional<OrganizeUser> findByOrganizeIdAndUserId(Long organizeId, Long userId);

}
