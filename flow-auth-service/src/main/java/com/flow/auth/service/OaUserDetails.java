package com.flow.auth.service;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class OaUserDetails implements UserDetails {

    private final Long userId;
    private final String username;
    private final String password;
    private final List<String> roles;
    private final boolean enabled;

    public OaUserDetails(Long userId, String username, String password,
                         List<String> roles, boolean enabled) {
        this.userId   = userId;
        this.username = username;
        this.password = password;
        this.roles    = roles;
        this.enabled  = enabled;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    @Override public String getPassword()  { return password; }
    @Override public String getUsername()  { return username; }
    @Override public boolean isAccountNonExpired()   { return true; }
    @Override public boolean isAccountNonLocked()    { return true; }
    @Override public boolean isCredentialsNonExpired(){ return true; }
    @Override public boolean isEnabled()   { return enabled; }
}
