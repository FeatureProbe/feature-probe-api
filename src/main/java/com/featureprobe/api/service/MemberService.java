package com.featureprobe.api.service;

import com.featureprobe.api.auth.UserPasswordAuthenticationToken;
import com.featureprobe.api.base.constants.MessageKey;
import com.featureprobe.api.base.enums.ResourceType;
import com.featureprobe.api.base.enums.RoleEnum;
import com.featureprobe.api.base.exception.ForbiddenException;
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
import com.featureprobe.api.util.PageRequestUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Service
public class MemberService {

    private MemberRepository memberRepository;

    private PasswordEncoder passwordEncoder;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Transactional(rollbackFor = Exception.class)
    public List<MemberResponse> create(MemberCreateRequest createRequest) {
        List<Member> savedMembers = memberRepository.saveAll(newNumbers(createRequest));

        return savedMembers.stream().map(item ->
                MemberMapper.INSTANCE.entityToResponse(item)).collect(Collectors.toList());
    }

    private List<Member> newNumbers(MemberCreateRequest createRequest) {
        return createRequest.getAccounts()
                .stream()
                .filter(this::notExistsAccount)
                .map(account -> newMember(account, createRequest.getPassword())).collect(Collectors.toList());
    }

    private boolean notExistsAccount(String account) {
        if (memberRepository.findByAccountIncludeDeleted(account).isPresent()) {
            throw new ResourceConflictException(ResourceType.MEMBER);
        }
        return true;
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
        verifyAdminPrivileges();

        Member member = findMemberByAccount(updateRequest.getAccount());
        MemberMapper.INSTANCE.mapEntity(updateRequest, member);
        return MemberMapper.INSTANCE.entityToResponse(memberRepository.save(member));
    }

    @Transactional(rollbackFor = Exception.class)
    public MemberResponse modifyPassword(MemberModifyPasswordRequest modifyPasswordRequest) {
        Member member = findLoggedInMember();
        verifyPassword(modifyPasswordRequest.getOldPassword(), member.getPassword());

        member.setPassword(passwordEncoder.encode(modifyPasswordRequest.getNewPassword()));
        return MemberMapper.INSTANCE.entityToResponse(memberRepository.save(member));
    }

    private void verifyPassword(String oldPassword, String newPassword) {
        if (!passwordEncoder.matches(oldPassword, newPassword)) {
            throw new IllegalArgumentException(MessageKey.INVALID_OLD_PASSWORD);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateVisitedTime(String account) {
        Member member = findMemberByAccount(account);

        member.setVisitedTime(new Date());
        memberRepository.save(member);
    }

    @Transactional(rollbackFor = Exception.class)
    public MemberResponse delete(String account) {
        verifyAdminPrivileges();

        Member member = findMemberByAccount(account);
        member.setDeleted(true);
        return MemberMapper.INSTANCE.entityToResponse(memberRepository.save(member));
    }

    private void verifyAdminPrivileges() {
        UserPasswordAuthenticationToken authentication =
                (UserPasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAdmin()) {
            throw new ForbiddenException();
        }
    }

    public Page<MemberResponse> query(MemberSearchRequest searchRequest) {
        Pageable pageable = PageRequestUtil.toPageable(searchRequest, Sort.Direction.DESC, "createdTime");
        Page<Member> members = memberRepository.findAll(accountLike(searchRequest.getKeyword()), pageable);

        return members.map(item -> MemberMapper.INSTANCE.entityToResponse(item));
    }

    private Specification<Member> accountLike(String account) {
        if (StringUtils.isBlank(account)) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("account"),
                "%" + account + "%");
    }

    public MemberResponse queryByAccount(String account) {
        Member member = findMemberByAccount(account, true);
        return MemberMapper.INSTANCE.entityToResponse(member);
    }

    private Member findLoggedInMember() {
        UserPasswordAuthenticationToken authentication =
                (UserPasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        return findMemberByAccount(authentication.getAccount());
    }

    private Member findMemberByAccount(String account) {
        return findMemberByAccount(account, false);
    }

    private Member findMemberByAccount(String account, boolean includeDeleted) {
        Optional<Member> member = includeDeleted ? memberRepository.findByAccountIncludeDeleted(account) :
                memberRepository.findByAccount(account);
        return member.orElseThrow(() -> new ResourceNotFoundException(ResourceType.MEMBER, account));
    }
}
