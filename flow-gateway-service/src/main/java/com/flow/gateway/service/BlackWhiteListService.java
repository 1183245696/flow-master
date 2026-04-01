package com.flow.gateway.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flow.gateway.entity.BlackWhiteListEntity;
import com.flow.gateway.mapper.BlackWhiteListMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlackWhiteListService extends ServiceImpl<BlackWhiteListMapper, BlackWhiteListEntity> {

    private static final String REDIS_WHITELIST_KEY = "gateway:whitelist";
    private static final String REDIS_BLACKLIST_KEY = "gateway:blacklist";

    private final StringRedisTemplate redisTemplate;

    /**
     * Sync DB → Redis every 60 seconds
     */
    @Scheduled(fixedRate = 60_000)
    public void syncToRedis() {
        List<BlackWhiteListEntity> all = list();

        redisTemplate.delete(REDIS_WHITELIST_KEY);
        redisTemplate.delete(REDIS_BLACKLIST_KEY);

        for (BlackWhiteListEntity e : all) {
            String key = "whitelist".equals(e.getListType()) ? REDIS_WHITELIST_KEY : REDIS_BLACKLIST_KEY;
            redisTemplate.opsForSet().add(key, e.getIpAddress());
        }
    }

    @Transactional
    public BlackWhiteListEntity addEntry(BlackWhiteListEntity entity) {
        save(entity);
        syncToRedis();
        return entity;
    }

    @Transactional
    public void removeEntry(Long id) {
        removeById(id);
        syncToRedis();
    }

    public Page<BlackWhiteListEntity> pageByType(String listType, int pageNum, int pageSize) {
        return page(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<BlackWhiteListEntity>()
                        .eq(BlackWhiteListEntity::getListType, listType)
                        .orderByDesc(BlackWhiteListEntity::getCreateTime));
    }
}
