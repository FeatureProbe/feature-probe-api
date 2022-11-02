package com.featureprobe.api.service


import com.featureprobe.api.base.enums.OrganizationRoleEnum
import com.featureprobe.api.base.enums.RoleEnum
import com.featureprobe.api.base.exception.ForbiddenException
import com.featureprobe.api.base.model.OrganizationMemberModel
import com.featureprobe.api.base.tenant.TenantContext
import com.featureprobe.api.dao.exception.ResourceConflictException
import com.featureprobe.api.dao.exception.ResourceNotFoundException
import com.featureprobe.api.dto.MemberCreateRequest
import com.featureprobe.api.dto.MemberModifyPasswordRequest
import com.featureprobe.api.dto.MemberSearchRequest
import com.featureprobe.api.dto.MemberUpdateRequest
import com.featureprobe.api.dao.entity.Member
import com.featureprobe.api.dao.entity.Organization
import com.featureprobe.api.dao.entity.OrganizationMember
import com.featureprobe.api.dao.repository.MemberRepository
import com.featureprobe.api.dao.repository.OrganizationRepository
import com.featureprobe.api.dao.repository.OrganizationMemberRepository
import org.hibernate.internal.SessionImpl
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import spock.lang.Specification

import javax.persistence.EntityManager

class MemberServiceSpec extends Specification {

    MemberRepository memberRepository
    MemberIncludeDeletedService memberIncludeDeletedService
    OrganizationRepository organizationRepository
    OrganizationMemberRepository organizationMemberRepository
    MemberService memberService
    EntityManager entityManager


    def setup() {
        memberRepository = Mock(MemberRepository)
        organizationRepository = Mock(OrganizationRepository)
        organizationMemberRepository = Mock(OrganizationMemberRepository)
        entityManager = Mock(SessionImpl)
        memberIncludeDeletedService = new MemberIncludeDeletedService(memberRepository, entityManager)
        memberService = new MemberService(memberRepository, memberIncludeDeletedService, organizationRepository,
                organizationMemberRepository, entityManager)
        TenantContext.setCurrentOrganization(new OrganizationMemberModel(1, "organization", OrganizationRoleEnum.OWNER))
    }

    def "create a member success"() {
        when:
        def savedMember = memberService.create(
                new MemberCreateRequest(accounts: ["root"], password: "root"))

        then:
        1 * memberRepository.existsByAccount("root") >> false
        1 * organizationRepository.findById(_) >> Optional.of(new Organization(id: 1, name: "organization name"))
        1 * memberRepository.saveAll(_) >> [new Member(account: "root", password: "password", role: RoleEnum.MEMBER)]
        with(savedMember) {
            1 == savedMember.size()
        }
    }

    def "create a member failed when member existed"() {
        given:
        memberRepository.existsByAccount("root") >> true

        when:
        memberService.create(new MemberCreateRequest(accounts: ["root"], password: "root"))

        then:
        thrown(ResourceConflictException)
    }

    def "update a member"() {
        given:
        setAuthContext("Admin", "ADMIN")

        when:
        def response = memberService.update(new MemberUpdateRequest(account: "root", password: "root"))

        then:
        1 * memberRepository.findByAccount("root") >>
                Optional.of(new Member(account: "root", password: "root", role: "MEMBER"))
        1 * memberRepository.save(_) >> new Member(account: "root", password: "root", role: "MEMBER")
        with(response) {
            "root" == account
            "MEMBER" == role
        }

    }

    def "modify member password success"() {
        given:
        setAuthContext("test", "MEMBER")

        when:
        def modify = memberService.modifyPassword(new MemberModifyPasswordRequest(newPassword: "root",
                oldPassword: "Pass1234"))

        then:
        1 * memberRepository.findByAccount("test") >>
                Optional.of(new Member(account: "test",
                        password: "\$2a\$10\$WO5tC7A/nsPe5qmVmjTIPeKD0R/Tm2YsNiVP0geCerT0hIRLBCxZ6", role: "MEMBER"))
        1 * memberRepository.save(_) >> new Member(account: "root", password: "323232", role: "MEMBER")
        with(modify) {
            "root" == account
            "MEMBER" == role
        }
    }

    def "modify member password failed when old password error"() {
        given:
        setAuthContext("test", "MEMBER")
        memberRepository.findByAccount("test") >> Optional.of(
                new Member(account: "test", password: "abcdefg", role: "MEMBER"))

        when:
        memberService.modifyPassword(new MemberModifyPasswordRequest(newPassword: "root", oldPassword: "Pass1234"))

        then:
        thrown(IllegalArgumentException)
    }

    def "delete a member"() {
        given:
        setAuthContext("Admin", "ADMIN")
        TenantContext.setCurrentTenant("1")
        when:
        def response = memberService.delete("root")

        then:
        1 * memberRepository.findByAccount("root") >>
                Optional.of(new Member(id: 1, account: "root", password: "root", role: "MEMBER"))
        1 * organizationMemberRepository.findByOrganizationIdAndMemberId(1, 1) >>
                Optional.of(new OrganizationMember())
        1 * organizationMemberRepository.delete(_)
        1 * memberRepository.save(_) >> new Member(account: "root", password: "root", role: "MEMBER")
        with(response) {
            "root" == account
            "MEMBER" == role
        }
    }

    def "delete member failed when logged user is not admin"() {
        given:
        setAuthContext("user", "MEMBER")

        when:
        memberService.delete("s1")

        then:
        thrown(ForbiddenException)
    }

    def "update member visited time by account"() {
        when:
        memberService.updateVisitedTime("test")

        then:
        1 * memberRepository.findByAccount("test") >>
                Optional.of(new Member(account: "test", password: "test", role: "MEMBER"))
        1 * memberRepository.save(_)
    }

    def "query member list"() {
        when:
        def list = memberService.list(new MemberSearchRequest(keyword: "root",
                pageIndex: 0, pageSize: 10))

        then:
        1 * organizationMemberRepository.findAll(_, _) >> new PageImpl<>([new OrganizationMember(memberId: 1)],
                PageRequest.of(1, 10), 1)
        1 * memberRepository.findAllById([1]) >> [new Member()]
        with(list) {
            1 == size()
        }
    }

    def "query member by account"() {
        when:
        def response = memberService.queryByAccount("root")

        then:
        1 * memberRepository.findByAccount("root") >> Optional.of(new Member(account: "root", role: RoleEnum.ADMIN))
        with(response) {
            "root" == account
        }
    }

    def "query member failed by account when account not exists"() {
        when:
        memberService.queryByAccount("abc")

        then:
        1 * memberRepository.findByAccount("abc") >> Optional.empty()
        thrown(ResourceNotFoundException)
    }

    def " member validate pass"() {
        when:
        def validate = memberIncludeDeletedService.validateAccountIncludeDeleted("Admin")
        then:
        1 * memberRepository.existsByAccount("Admin") >> false
        true == validate
    }

    def " member validate conflict"() {
        when:
        def validate = memberIncludeDeletedService.validateAccountIncludeDeleted("Admin")
        then:
        1 * memberRepository.existsByAccount("Admin") >> true
        thrown(ResourceConflictException)
    }

    def "query include deleted service"() {
        when:
        def member = memberIncludeDeletedService.queryMemberByAccountIncludeDeleted("Admin")
        then:
        1 * memberRepository.findByAccount("Admin") >> Optional.of(new Member())
        true == member.isPresent()
    }

    private setAuthContext(String account, String role) {
        SecurityContextHolder.setContext(new SecurityContextImpl(
                new JwtAuthenticationToken(new Jwt.Builder("21212").header("a", "a")
                        .claim("role", role).claim("account", account).build())))
    }

}

