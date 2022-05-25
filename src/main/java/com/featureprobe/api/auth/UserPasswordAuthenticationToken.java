package com.featureprobe.api.auth;

import com.featureprobe.api.base.enums.RoleEnum;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class UserPasswordAuthenticationToken extends AbstractAuthenticationToken {

    private String account;

    private String password;

    private String role;

    public UserPasswordAuthenticationToken(String account, String password) {
        super(null);
        this.account = account;
        this.password = password;
        super.setAuthenticated(false);
    }

    public UserPasswordAuthenticationToken(String account, String role,
                                           Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.account = account;
        this.role = role;
        super.setAuthenticated(true);
    }

    public boolean isAdmin() {
        return RoleEnum.ADMIN.name().equals(role);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return "admin";
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
