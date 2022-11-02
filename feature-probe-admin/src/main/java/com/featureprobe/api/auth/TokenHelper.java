package com.featureprobe.api.auth;

import com.featureprobe.api.base.enums.RoleEnum;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class TokenHelper {

    private static final String ACCOUNT_KEY = "account";
    private static final String USER_ID_KEY = "userId";
    private static final String ROLE_KEY = "role";

    public static final Long getUserId() {
        JwtAuthenticationToken authentication = (JwtAuthenticationToken) SecurityContextHolder.
                getContext().getAuthentication();
        return (Long) authentication.getTokenAttributes().get(USER_ID_KEY);
    }

    public static final String getAccount() {
        JwtAuthenticationToken authentication = (JwtAuthenticationToken) SecurityContextHolder.
                getContext().getAuthentication();
        return (String) authentication.getTokenAttributes().get(ACCOUNT_KEY);
    }

    public static final String getRole() {
        JwtAuthenticationToken authentication = (JwtAuthenticationToken) SecurityContextHolder.
                getContext().getAuthentication();
        return (String) authentication.getTokenAttributes().get(ROLE_KEY);
    }

    public static final boolean isAdmin() {
        JwtAuthenticationToken authentication = (JwtAuthenticationToken) SecurityContextHolder.
                getContext().getAuthentication();
        return RoleEnum.ADMIN.name().equals((String) authentication.getTokenAttributes().get(ROLE_KEY));
    }
}
