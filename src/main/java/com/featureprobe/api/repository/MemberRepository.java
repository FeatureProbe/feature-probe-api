package com.featureprobe.api.repository;


import com.featureprobe.api.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> , JpaSpecificationExecutor<Member> {

    Optional<Member> findByAccount(String account);

    @Query(value = "SELECT * FROM member WHERE account = ?1", nativeQuery = true)
    Optional<Member> findByAccountIncludeDeleted(String account);
    
}
