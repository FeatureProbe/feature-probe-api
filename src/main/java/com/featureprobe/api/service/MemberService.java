package com.featureprobe.api.service;

import com.featureprobe.api.auth.UserPasswordAuthenticationToken;
import com.featureprobe.api.base.enums.ResourceType;
import com.featureprobe.api.base.enums.RoleEnum;
import com.featureprobe.api.base.exception.PasswordErrorException;
import com.featureprobe.api.base.exception.ResourceConflictException;
import com.featureprobe.api.base.exception.ResourceNotFoundException;
import com.featureprobe.api.dto.MemberCreateRequest;
import com.featureprobe.api.dto.MemberModifyPasswordRequest;
import com.featureprobe.api.dto.MemberResponse;
import com.featureprobe.api.dto.MemberSearchRequest;
import com.featureprobe.api.dto.MemberUpdateRequest;
import com.featureprobe.api.entity.Member;
import com.featureprobe.api.mapper.MemberMapper;
import com.featureprobe.api.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@AllArgsConstructor
@Service
public class MemberService {

    private MemberRepository memberRepository;

    @Transactional(rollbackFor = Exception.class)
    public List<MemberResponse> create(MemberCreateRequest createRequest) {
        List<Member> members = new ArrayList<>();
        createRequest.getAccounts().stream().forEach(account -> {
            if (memberRepository.findByAccountContainerDeleted(account).isPresent()) {
                throw new ResourceConflictException(ResourceType.MEMBER);
            }
            members.add(newMember(account, createRequest.getPassword()));
        });
        List<Member> saveMembers = memberRepository.saveAll(members);
        return saveMembers.stream().map(item ->
                MemberMapper.INSTANCE.entityToResponse(item)).collect(Collectors.toList());
    }

    private Member newMember(String account, String password) {
        Member member = new Member();
        member.setAccount(account);
        member.setPassword(new BCryptPasswordEncoder().encode(password));
        member.setRole(RoleEnum.MEMBER);
        return member;
    }

    @Transactional(rollbackFor = Exception.class)
    public MemberResponse update(MemberUpdateRequest updateRequest) {
        UserPasswordAuthenticationToken authentication =
                (UserPasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        Member member = memberRepository.findByAccount(updateRequest.getAccount())
                .orElseThrow(() -> new ResourceNotFoundException(ResourceType.MEMBER, updateRequest.getAccount()));
        MemberMapper.INSTANCE.mapEntity(updateRequest, member);
        if (authentication.isAdmin()) {
            return MemberMapper.INSTANCE.entityToResponse(memberRepository.save(member));
        }
        return null;
    }

    @Transactional(rollbackFor = Exception.class)
    public MemberResponse modifyPassword(MemberModifyPasswordRequest modifyPasswordRequest) {
        UserPasswordAuthenticationToken authentication =
                (UserPasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        Member member = memberRepository.findByAccount(authentication.getAccount())
                .orElseThrow(() -> new ResourceNotFoundException(ResourceType.MEMBER, authentication.getAccount()));
        if (new BCryptPasswordEncoder().matches(modifyPasswordRequest.getOldPassword(), member.getPassword())) {
            member.setPassword(new BCryptPasswordEncoder().encode(modifyPasswordRequest.getNewPassword()));
            return MemberMapper.INSTANCE.entityToResponse(memberRepository.save(member));
        } else {
            throw new PasswordErrorException();
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateVisitedTime(String account) {
        Member member = memberRepository.findByAccount(account)
                .orElseThrow(() -> new ResourceNotFoundException(ResourceType.MEMBER, account));
        member.setVisitedTime(new Date());
        memberRepository.save(member);
    }

    @Transactional(rollbackFor = Exception.class)
    public MemberResponse delete(String account) {
        UserPasswordAuthenticationToken authentication =
                (UserPasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        Member member = memberRepository.findByAccount(account).orElseThrow(() ->
                new ResourceNotFoundException(ResourceType.MEMBER, account));
        member.setDeleted(true);
        if (authentication.isAdmin()) {
            return MemberMapper.INSTANCE.entityToResponse(memberRepository.save(member));
        }
        return null;
    }

    public Page<MemberResponse> list(MemberSearchRequest searchRequest) {
        Pageable pageable = PageRequest.of(searchRequest.getPageIndex(), searchRequest.getPageSize(),
                Sort.Direction.DESC, "createdTime");
        Specification<Member> spec = new Specification<Member>() {
            @Override
            public Predicate toPredicate(Root<Member> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                if (StringUtils.isNotBlank(searchRequest.getKeyword())) {
                    Predicate p0 = cb.like(root.get("account"), "%" + searchRequest.getKeyword() + "%");
                    query.where(p0);
                }
                return query.getRestriction();
            }
        };
        Page<Member> members = memberRepository.findAll(spec, pageable);
        return members.map(item -> MemberMapper.INSTANCE.entityToResponse(item));
    }

    public MemberResponse query(String account) {
        Member member = memberRepository.findByAccountContainerDeleted(account)
                .orElseThrow(() -> new ResourceNotFoundException(ResourceType.MEMBER, account));
        return MemberMapper.INSTANCE.entityToResponse(member);
    }

}
