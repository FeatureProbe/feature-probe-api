package com.featureprobe.api.auth;

import com.featureprobe.api.base.enums.OperationType;
import com.featureprobe.api.entity.Member;
import com.featureprobe.api.entity.OperationLog;
import com.featureprobe.api.service.MemberService;
import com.featureprobe.api.service.OperationLogService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.Optional;

@Component
@AllArgsConstructor
public class UserPasswordAuthenticationProvider implements AuthenticationProvider {

    private MemberService memberService;

    private OperationLogService operationLogService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        UserPasswordAuthenticationToken token = (UserPasswordAuthenticationToken) authentication;
        if (StringUtils.isNotBlank(token.getAccount()) && StringUtils.isNotBlank(token.getPassword())) {
            Optional<Member> member = memberService.findByAccount(token.getAccount());
            OperationLog log = new OperationLog(OperationType.LOGIN.name() + "_" + token.getSource(),
                    token.getAccount());
            if (member.isPresent()
                    && new BCryptPasswordEncoder().matches(token.getPassword(), member.get().getPassword())) {
                memberService.updateVisitedTime(token.getAccount());
                operationLogService.save(log);
                return new UserPasswordAuthenticationToken(member.get(),
                        Arrays.asList(new SimpleGrantedAuthority(member.get().getRole().name())));
            }
        }
        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (UserPasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }

}
