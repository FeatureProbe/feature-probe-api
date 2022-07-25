package com.featureprobe.api.base.config;

import com.featureprobe.api.auth.UserPasswordAuthenticationToken;
import com.featureprobe.api.entity.Member;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;
import java.util.Optional;

@Configuration
public class AuditingConfig implements AuditorAware {

    @Override
    public Optional<Member> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof UserPasswordAuthenticationToken)) {
            return null;
        }
        UserPasswordAuthenticationToken userPasswordAuthenticationToken =
                (UserPasswordAuthenticationToken) authentication;
        return Optional.of(userPasswordAuthenticationToken.getPrincipal());
    }

}
