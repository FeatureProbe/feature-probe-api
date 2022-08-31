package com.featureprobe.api.service;

import com.featureprobe.api.auth.TokenHelper;
import com.featureprobe.api.auth.tenant.TenantContext;
import com.featureprobe.api.base.constants.MessageKey;
import com.featureprobe.api.base.enums.ResourceType;
import com.featureprobe.api.base.enums.RoleEnum;
import com.featureprobe.api.base.exception.ForbiddenException;
import com.featureprobe.api.base.exception.ResourceNotFoundException;
import com.featureprobe.api.dto.MemberCreateRequest;
import com.featureprobe.api.dto.MemberModifyPasswordRequest;
import com.featureprobe.api.dto.MemberResponse;
import com.featureprobe.api.dto.MemberSearchRequest;
import com.featureprobe.api.dto.MemberUpdateRequest;
import com.featureprobe.api.entity.Member;
import com.featureprobe.api.entity.Organization;
import com.featureprobe.api.entity.OrganizationMember;
import com.featureprobe.api.mapper.MemberMapper;
import com.featureprobe.api.repository.MemberRepository;
import com.featureprobe.api.repository.OrganizationRepository;
import com.featureprobe.api.repository.OrganizationMemberRepository;
import com.featureprobe.api.service.aspect.ExcludeTenant;
import com.featureprobe.api.util.PageRequestUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.Predicate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;


@ExcludeTenant
@Slf4j
@Service
@AllArgsConstructor
public class MemberService {

    private MemberRepository memberRepository;

    private MemberIncludeDeletedService memberIncludeDeletedService;

    private OrganizationRepository organizationRepository;

    private OrganizationMemberRepository organizationMemberRepository;

    @PersistenceContext
    public EntityManager entityManager;

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional(rollbackFor = Exception.class)
    public List<MemberResponse> create(MemberCreateRequest createRequest) {
        List<Member> savedMembers = memberRepository.saveAll(newNumbers(createRequest));
        return savedMembers.stream().map(item ->
                MemberMapper.INSTANCE.entityToResponse(item)).collect(Collectors.toList());
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
        OrganizationMember organizationMember = organizationMemberRepository
                .findByOrganizationIdAndMemberId(Long.parseLong(TenantContext.getCurrentTenant()), member.getId())
                .orElseThrow(()  -> new ResourceNotFoundException(ResourceType.ORGANIZATION_MEMBER,
                        account + "_" + TenantContext.getCurrentTenant()));
        organizationMemberRepository.delete(organizationMember);
        member.setDeleted(true);
        return MemberMapper.INSTANCE.entityToResponse(memberRepository.save(member));
    }

    private List<Member> newNumbers(MemberCreateRequest createRequest) {
        return createRequest.getAccounts()
                .stream()
                .filter(account -> memberIncludeDeletedService.validateAccountIncludeDeleted(account))
                .map(account -> newMember(account, createRequest.getPassword())).collect(Collectors.toList());
    }

    private Member newMember(String account, String password) {
        Member member = new Member();
        member.setAccount(account);
        member.setRole(RoleEnum.MEMBER);
        member.setPassword(new BCryptPasswordEncoder().encode(password));
        Organization organization = organizationRepository.findById(TenantContext.getCurrentOrganization()
                .getOrganizationId()).get();
        member.setOrganizations(Arrays.asList(organization));
        return member;
    }

    private void verifyPassword(String oldPassword, String newPassword) {
        if (!passwordEncoder.matches(oldPassword, newPassword)) {
            throw new IllegalArgumentException(MessageKey.INVALID_OLD_PASSWORD);
        }
    }

    public Optional<Member> findByAccount(String account) {
        return memberRepository.findByAccount(account);
    }

    private void verifyAdminPrivileges() {
        if (!TokenHelper.isAdmin()) {
            throw new ForbiddenException();
        }
    }

    public Page<MemberResponse> query(MemberSearchRequest searchRequest) {
        Pageable pageable = PageRequestUtil.toPageable(searchRequest, Sort.Direction.DESC, "createdTime");
        Specification<OrganizationMember> spec = (root, query, cb) -> {
            Predicate p1 = cb.equal(root.get("organizationId"), TenantContext.getCurrentOrganization()
                    .getOrganizationId());
            return query.where(cb.and(p1)).groupBy(root.get("memberId"))
                    .getRestriction();
        };
        Page<OrganizationMember> organizationMembers = organizationMemberRepository.findAll(spec, pageable);
        List<Long> memberIds = organizationMembers.getContent().stream().map(OrganizationMember::getMemberId)
                .collect(Collectors.toList());
        Map<Long, Member> memberMap = memberRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(Member::getId, Function.identity()));
        return organizationMembers.map(item ->
                MemberMapper.INSTANCE.entityToResponse(memberMap.get(item.getMemberId())));
    }

    public MemberResponse queryByAccount(String account) {
        Member member = findMemberByAccount(account, true);
        return MemberMapper.INSTANCE.entityToResponse(member);
    }

    private Member findLoggedInMember() {
        return findMemberByAccount(TokenHelper.getAccount());
    }

    private Member findMemberByAccount(String account) {
        return findMemberByAccount(account, false);
    }

    private Member findMemberByAccount(String account, boolean includeDeleted) {
        Optional<Member> member = includeDeleted ? memberIncludeDeletedService
                .queryMemberByAccountIncludeDeleted(account) : memberRepository.findByAccount(account);
        return member.orElseThrow(() -> new ResourceNotFoundException(ResourceType.MEMBER, account));
    }
}
