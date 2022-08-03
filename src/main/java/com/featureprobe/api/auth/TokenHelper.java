package com.featureprobe.api.auth;


import com.featureprobe.api.base.enums.RoleEnum;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class TokenHelper {

    public static final Long getUserId() {
        JwtAuthenticationToken authentication = (JwtAuthenticationToken) SecurityContextHolder.
                getContext().getAuthentication();
        return (Long) authentication.getTokenAttributes().get("userId");
    }

    public static final String getAccount() {
        JwtAuthenticationToken authentication = (JwtAuthenticationToken) SecurityContextHolder.
                getContext().getAuthentication();
        return (String) authentication.getTokenAttributes().get("account");
    }

    public static final String getRole() {
        JwtAuthenticationToken authentication = (JwtAuthenticationToken) SecurityContextHolder.
                getContext().getAuthentication();
        return (String) authentication.getTokenAttributes().get("role");
    }

    public static final boolean isAdmin() {
        JwtAuthenticationToken authentication = (JwtAuthenticationToken) SecurityContextHolder.
                getContext().getAuthentication();
        return RoleEnum.ADMIN.name().equals((String) authentication.getTokenAttributes().get("role"));
    }
}
