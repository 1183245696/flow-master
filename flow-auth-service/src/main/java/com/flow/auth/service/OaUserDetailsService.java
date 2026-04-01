package com.flow.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.flow.auth.entity.SysUserAuth;
import com.flow.auth.mapper.SysUserAuthMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OaUserDetailsService implements UserDetailsService {

    private final SysUserAuthMapper userAuthMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUserAuth user = userAuthMapper.selectOne(
                new LambdaQueryWrapper<SysUserAuth>()
                        .eq(SysUserAuth::getUsername, username)
                        .eq(SysUserAuth::getIsDeleted, 0));
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        return new OaUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                List.of(user.getRoles().split(",")),
                user.getStatus() == 1);
    }
}
