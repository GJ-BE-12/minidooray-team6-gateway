package com.nhnacademy.gateway.config;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;


public class CustomUserPrincipal implements UserDetails {
    @Getter
    private final String username;
    private final String password;
    @Getter
    private final Long numericId;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserPrincipal(String username, String password, Long numericId, Collection<? extends GrantedAuthority> authorities) {
        this.username = username;
        this.password = password;
        this.numericId = numericId;
        this.authorities = authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }


    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }
}
