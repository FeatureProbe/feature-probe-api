package com.featureprobe.api.base.config;

import com.featureprobe.api.auth.UserPasswordAuthenticationToken;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;
import java.util.Optional;

@Configuration
public class AuditingConfig implements AuditorAware {

    private static final String ANONYMOUS_OPERATE = "Anonymous";

    private static final String SYSTEM_OPERATE = "System";


    @Override
    public Optional getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (Objects.isNull(authentication)) {
            return Optional.of(SYSTEM_OPERATE);
        } else if (authentication instanceof AnonymousAuthenticationToken) {
            return Optional.of(ANONYMOUS_OPERATE);
        } else {
            UserPasswordAuthenticationToken userPasswordAuthenticationToken =
                    (UserPasswordAuthenticationToken) authentication;
            return Optional.of(userPasswordAuthenticationToken.getAccount());
        }
    }

}
