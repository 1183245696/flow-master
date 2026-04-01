package com.user.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flow.base.exception.BusinessException;
import com.user.user.entity.SysUser;
import com.user.user.entity.SysUserTransferHistory;
import com.user.user.event.UserCreatedEvent;
import com.user.user.mapper.SysUserMapper;
import com.user.user.mapper.SysUserTransferMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<SysUserMapper, SysUser> {

    private static final String USER_EVENTS_EXCHANGE = "user.events";
    private static final String PERM_KEY_PREFIX      = "perm:user:";

    private final SysUserTransferMapper transferMapper;
    private final RabbitTemplate rabbitTemplate;
    private final StringRedisTemplate redisTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public SysUser createUser(SysUser user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(1);
        save(user);
        user.setUserId(user.getId());
        updateById(user);
        UserCreatedEvent event = new UserCreatedEvent(user.getId(), user.getUsername(), user.getDeptId(), user.getPositionId());
        rabbitTemplate.convertAndSend(USER_EVENTS_EXCHANGE, "user.created", event);
        return user;
    }

    @Transactional
    public SysUser updateUser(SysUser user) { updateById(user); return user; }

    @Transactional
    public void deleteUser(Long id) { removeById(id); }

    public Page<SysUser> pageUsers(int pageNum, int pageSize, String keyword) {
        return page(new Page<>(pageNum, pageSize),
            new LambdaQueryWrapper<SysUser>()
                .like(keyword != null && !keyword.isBlank(), SysUser::getRealName, keyword)
                .orderByDesc(SysUser::getCreateTime));
    }

    @Transactional
    public void transferUser(Long userId, Long toDeptId, Long toPositionId, String reason, Long operatorId) {
        SysUser user = getById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        SysUserTransferHistory h = new SysUserTransferHistory();
        h.setUserId(userId); h.setFromDeptId(user.getDeptId()); h.setToDeptId(toDeptId);
        h.setFromPositionId(user.getPositionId()); h.setToPositionId(toPositionId);
        h.setReason(reason); h.setOperatorId(operatorId);
        transferMapper.insert(h);
        user.setDeptId(toDeptId); user.setPositionId(toPositionId);
        updateById(user);
    }

    public Page<SysUserTransferHistory> pageTransferHistory(Long userId, int pageNum, int pageSize) {
        return transferMapper.selectPage(new Page<>(pageNum, pageSize),
            new LambdaQueryWrapper<SysUserTransferHistory>()
                .eq(SysUserTransferHistory::getUserId, userId)
                .orderByDesc(SysUserTransferHistory::getCreateTime));
    }

    public List<String> getPermissionsByUserId(Long userId) {
        Set<String> cached = redisTemplate.opsForSet().members(PERM_KEY_PREFIX + userId);
        if (cached != null && !cached.isEmpty()) return List.copyOf(cached);
        List<String> perms = baseMapper.selectPermissionsByUserId(userId);
        if (!perms.isEmpty()) redisTemplate.opsForSet().add(PERM_KEY_PREFIX + userId, perms.toArray(new String[0]));
        return perms;
    }
}
