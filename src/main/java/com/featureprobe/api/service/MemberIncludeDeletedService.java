package com.featureprobe.api.service;

import com.featureprobe.api.base.enums.ResourceType;
import com.featureprobe.api.base.exception.ResourceConflictException;
import com.featureprobe.api.entity.Member;
import com.featureprobe.api.repository.MemberRepository;
import com.featureprobe.api.service.aspect.IncludeDeleted;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Optional;

@Service
@AllArgsConstructor
public class MemberIncludeDeletedService {

    private MemberRepository memberRepository;

    @PersistenceContext
    public EntityManager entityManager;

    @IncludeDeleted
    public boolean validateAccountIncludeDeleted(String account) {
        if (memberRepository.existsByAccount(account)) {
            throw new ResourceConflictException(ResourceType.MEMBER);
        }
        return true;
    }

    @IncludeDeleted
    public Optional<Member> queryMemberByAccountIncludeDeleted(String account) {
        return memberRepository.findByAccount(account);
    }

}
