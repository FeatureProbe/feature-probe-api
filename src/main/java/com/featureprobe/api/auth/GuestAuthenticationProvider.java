package com.featureprobe.api.auth;

import com.featureprobe.api.entity.Member;
import com.featureprobe.api.service.GuestService;
import com.featureprobe.api.service.MemberService;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
@AllArgsConstructor
public class GuestAuthenticationProvider implements AuthenticationProvider {

    private MemberService memberService;

    private GuestService guestService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        GuestAuthenticationToken token = (GuestAuthenticationToken) authentication;
        Optional<Member> member = memberService.findByAccount(token.getAccount());
        if (member.isPresent()) {
            memberService.updateVisitedTime(token.getAccount());
            return new UserPasswordAuthenticationToken(member.get(),
                    Arrays.asList(new SimpleGrantedAuthority(member.get().getRole().name())));
        } else {
            Member newMember = guestService.initGuest(token.getAccount());
            return new UserPasswordAuthenticationToken(newMember,
                    Arrays.asList(new SimpleGrantedAuthority(newMember.getRole().name())));
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (GuestAuthenticationToken.class.isAssignableFrom(authentication));
    }

}
