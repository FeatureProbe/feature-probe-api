package com.featureprobe.api.auth;

import com.featureprobe.api.base.enums.RoleEnum;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class UserPasswordAuthenticationToken extends AbstractAuthenticationToken {

    private String account;

    private String source;

    private String password;

    private AuthenticatedMember principal;

    public UserPasswordAuthenticationToken(String account, String source, String password) {
        super(null);
        this.account = account;
        this.source = source;
        this.password = password;
        super.setAuthenticated(false);
    }

    public UserPasswordAuthenticationToken(AuthenticatedMember principal,
                                           Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.account = principal.getName();
        super.setAuthenticated(true);
    }

    public boolean isAdmin() {
        return RoleEnum.ADMIN.name().equals(getRole());
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public AuthenticatedMember getPrincipal() {
        return principal;
    }

    public String getAccount() {
        return account;
    }

    public String getSource() {
        return source;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        if (principal == null) {
            return null;
        }
        return principal.getRole();
    }

}
