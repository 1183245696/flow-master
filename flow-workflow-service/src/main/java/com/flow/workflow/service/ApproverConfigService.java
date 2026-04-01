package com.flow.workflow.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flow.workflow.entity.SysProcessApproverConfig;
import com.flow.workflow.mapper.SysProcessApproverConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApproverConfigService extends ServiceImpl<SysProcessApproverConfigMapper, SysProcessApproverConfig> {

    @Transactional
    public void saveConfig(SysProcessApproverConfig config) {
        // Upsert: remove existing config for same processKey+nodeId, then insert
        remove(new LambdaQueryWrapper<SysProcessApproverConfig>()
                .eq(SysProcessApproverConfig::getProcessKey, config.getProcessKey())
                .eq(SysProcessApproverConfig::getNodeId, config.getNodeId()));
        save(config);
    }

    public List<SysProcessApproverConfig> listByProcessKey(String processKey) {
        return list(new LambdaQueryWrapper<SysProcessApproverConfig>()
                .eq(SysProcessApproverConfig::getProcessKey, processKey));
    }
}
