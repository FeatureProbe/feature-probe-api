package com.featureprobe.api.service

import com.featureprobe.api.auth.UserPasswordAuthenticationProvider
import com.featureprobe.api.auth.UserPasswordAuthenticationToken
import com.featureprobe.api.base.enums.RoleEnum
import com.featureprobe.api.entity.Member
import com.featureprobe.api.repository.MemberRepository
import org.springframework.security.core.Authentication
import spock.lang.Specification


class UserPasswordAuthenticationSpec extends Specification{

    def MemberRepository memberRepository

    def MemberService memberService

    def UserPasswordAuthenticationProvider userPasswordAuthenticationProvider

    def UserPasswordAuthenticationToken token

    def setup() {
        this.memberRepository = Mock(MemberRepository)
        this.memberService = Mock(MemberService)
        this.userPasswordAuthenticationProvider = new UserPasswordAuthenticationProvider(memberRepository, memberService)
        token = new UserPasswordAuthenticationToken("admin", "abc12345")
    }

    def "user password is pass"() {
        when:
        def authenticate = userPasswordAuthenticationProvider.authenticate(token)
        then:
        1 * memberRepository.findByAccount("admin") >>
                Optional.of(new Member(account: "Admin",
                        password: "\$2a\$10\$jeJ25nROU8APkG2ixK6zyecwzIJ8oHz0ZNqBDiwMXcy9lo9S3YGma",
                        role: RoleEnum.ADMIN))
        1 * memberService.updateVisitedTime("admin");
        with(authenticate) {
            "Admin" == ((UserPasswordAuthenticationToken)authenticate).getAccount()
        }
    }

}

