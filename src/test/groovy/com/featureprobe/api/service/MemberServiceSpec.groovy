package com.featureprobe.api.service

import com.featureprobe.api.auth.UserPasswordAuthenticationToken
import com.featureprobe.api.base.enums.RoleEnum
import com.featureprobe.api.dto.MemberCreateRequest
import com.featureprobe.api.dto.MemberModifyPasswordRequest
import com.featureprobe.api.dto.MemberResponse
import com.featureprobe.api.dto.MemberSearchRequest
import com.featureprobe.api.dto.MemberUpdateRequest
import com.featureprobe.api.entity.Member
import com.featureprobe.api.repository.MemberRepository
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import spock.lang.Specification

class MemberServiceSpec extends Specification{

    def MemberRepository memberRepository

    def MemberService memberService

    def setup() {
        memberRepository = Mock(MemberRepository)
        memberService = new MemberService(memberRepository)
    }

    def "create a member" () {
        when:
        def create = memberService.create(
                new MemberCreateRequest(accounts: ["root"], password: "root"))
        then:
        1 * memberRepository.findByAccountContainerDeleted("root") >> Optional.empty()
        1 * memberRepository.saveAll(_) >> [new Member(account: "root", password: "password", role: RoleEnum.MEMBER)]
        with(create) {
            1 == create.size()
        }
    }

    def "update a member" () {
        when:
        SecurityContextHolder.setContext(new SecurityContextImpl(
                new UserPasswordAuthenticationToken("Admin", "ADMIN", null)))
        def update = memberService.update(new MemberUpdateRequest(account: "root", password: "root"))
        then:
        1 * memberRepository.findByAccount("root") >>
                Optional.of(new Member(account: "root", password: "root", role: "MEMBER"))
        1 *  memberRepository.save(_) >> new Member(account: "root", password: "root", role: "MEMBER")
        with(update) {
            "root" == update.account
            "MEMBER" == update.role
        }

    }

    def "modify member password"() {
        when:
        SecurityContextHolder.setContext(new SecurityContextImpl(
                new UserPasswordAuthenticationToken("test", "MEMBER", null)))
        def modify = memberService.modifyPassword(new MemberModifyPasswordRequest(newPassword: "root",
                oldPassword: "Pass1234"))
        then:
        1 * memberRepository.findByAccount("test") >>
                Optional.of(new Member(account: "test",
                        password: "\$2a\$10\$WO5tC7A/nsPe5qmVmjTIPeKD0R/Tm2YsNiVP0geCerT0hIRLBCxZ6", role: "MEMBER"))
        1 *  memberRepository.save(_) >> new Member(account: "root", password: "323232", role: "MEMBER")
        with(modify) {
            "root" == modify.account
            "MEMBER" == modify.role
        }
    }

    def "delete a member"() {
        when:
        SecurityContextHolder.setContext(new SecurityContextImpl(
                new UserPasswordAuthenticationToken("Admin", "ADMIN", null)))
        def delete = memberService.delete("root")
        then:
        1 * memberRepository.findByAccount("root") >>
                Optional.of(new Member(account: "root", password: "root", role: "MEMBER"))
        1 * memberRepository.save(_) >> new Member(account: "root", password: "root", role: "MEMBER")
        with(delete) {
            "root" == delete.account
            "MEMBER" == delete.role
        }
    }

    def "query member list"() {
        when:
        def list = memberService.list(new MemberSearchRequest(keyword: "root",
                pageIndex: 0, pageSize: 10))
        then:
        1 * memberRepository.findAll(_, _) >> new PageImpl<>([new Member(account: "root", password: "root",
                role: RoleEnum.MEMBER)], Pageable.ofSize(10), 1)
        with(list) {
            1 == list.size()
        }
    }

    def "query single member" () {
        when:
        def query = memberService.query("root")
        then:
        1 * memberRepository.findByAccountContainerDeleted("root") >> Optional.of(
                new Member(account: "root", role: RoleEnum.MEMBER))
        with (query) {
            "root" == query.account
        }
    }
}

