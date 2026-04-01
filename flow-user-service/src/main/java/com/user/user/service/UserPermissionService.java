package com.user.user.service;

import com.user.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

/**
 * 加载用户权限集合（格式 METHOD:/path）并缓存到 Redis
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserPermissionService {

    private static final String PERM_KEY_PREFIX = "perm:user:";
    private static final Duration PERM_TTL = Duration.ofHours(8);

    private final SysUserMapper userMapper;
    private final StringRedisTemplate redisTemplate;

    public Set<String> loadPermissions(Long userId) {
        String key = PERM_KEY_PREFIX + userId;
        Set<String> cached = redisTemplate.opsForSet().members(key);
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }

        // Load from DB via native query joining sys_user→sys_position→sys_position_menu→sys_menu
        Set<String> perms = userMapper.selectPermissionsByUserId(userId);
        if (perms == null) perms = new HashSet<>();

        // Always grant access to own profile
        perms.add("GET:/v1/users/me");
        perms.add("PUT:/v1/users/me");

        if (!perms.isEmpty()) {
            redisTemplate.opsForSet().add(key, perms.toArray(new String[0]));
            redisTemplate.expire(key, PERM_TTL);
        }
        return perms;
    }

    public void evictCache(Long userId) {
        redisTemplate.delete(PERM_KEY_PREFIX + userId);
    }
}
