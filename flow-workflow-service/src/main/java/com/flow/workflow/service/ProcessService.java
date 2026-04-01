package com.flow.workflow.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flow.base.exception.BusinessException;
import com.flow.workflow.entity.ProcessDefinitionVersion;
import com.flow.workflow.mapper.ProcessDefinitionVersionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessService extends ServiceImpl<ProcessDefinitionVersionMapper, ProcessDefinitionVersion> {

    private static final int MAX_PINNED = 6;

    private final ProcessDefinitionVersionMapper versionMapper;
    private final RepositoryService repositoryService;

    /** Returns only latest version per process_key */
    public List<ProcessDefinitionVersion> listLatest() {
        return versionMapper.selectLatestVersions();
    }

    /** Paginated history versions (read-only) for a given process_key */
    public Page<ProcessDefinitionVersion> historyVersions(String processKey, int pageNum, int pageSize) {
        Page<ProcessDefinitionVersion> p = new Page<>(pageNum, pageSize);
        List<ProcessDefinitionVersion> list = versionMapper.selectVersionHistory(processKey);
        p.setRecords(list);
        p.setTotal(list.size());
        return p;
    }

    /** Latest version detail + history */
    public ProcessDefinitionVersion getLatest(String processKey) {
        List<ProcessDefinitionVersion> list = versionMapper.selectVersionHistory(processKey);
        return list.isEmpty() ? null : list.get(0);
    }

    @Transactional
    public ProcessDefinitionVersion deployProcess(ProcessDefinitionVersion req) {
        // Determine next version number
        List<ProcessDefinitionVersion> history = versionMapper.selectVersionHistory(req.getProcessKey());
        int nextVersion = history.isEmpty() ? 1 : history.get(0).getVersion() + 1;

        // Mark all previous as not-latest — NEVER delete old ProcessDefinitions
        versionMapper.clearLatestFlag(req.getProcessKey());

        // Deploy to Flowable engine
        Deployment deployment = repositoryService.createDeployment()
                .name(req.getName())
                .category(req.getCategoryCode())
                .addInputStream(
                        req.getProcessKey() + ".bpmn20.xml",
                        new ByteArrayInputStream(req.getBpmnXml().getBytes(StandardCharsets.UTF_8)))
                .deploy();

        // Fetch the created ProcessDefinition ID
        String pdId = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .singleResult()
                .getId();

        // Save version record
        ProcessDefinitionVersion version = new ProcessDefinitionVersion();
        version.setProcessKey(req.getProcessKey());
        version.setVersion(nextVersion);
        version.setDeploymentId(deployment.getId());
        version.setProcessDefinitionId(pdId);
        version.setName(req.getName());
        version.setCategoryCode(req.getCategoryCode());
        version.setDescription(req.getDescription());
        version.setIcon(req.getIcon());
        version.setBpmnXml(req.getBpmnXml());
        version.setIsLatest(true);
        version.setIsPinned(false);
        version.setDeployedAt(LocalDateTime.now());
        save(version);

        log.info("Deployed process: key={}, version={}, deploymentId={}",
                req.getProcessKey(), nextVersion, deployment.getId());
        return version;
    }

    /**
     * Pin/unpin a process.
     * MUST enforce max 6 pinned — throws BusinessException if >6.
     */
    @Transactional
    public void togglePin(Long versionId, boolean pin) {
        ProcessDefinitionVersion v = getById(versionId);
        if (v == null) throw new BusinessException("流程版本不存在");

        if (pin) {
            int currentPinned = versionMapper.countPinned();
            if (currentPinned >= MAX_PINNED) {
                throw new BusinessException("最多只能固定 " + MAX_PINNED + " 个流程，请先取消其他固定");
            }
        }
        v.setIsPinned(pin);
        updateById(v);
    }

    public List<ProcessDefinitionVersion> listPinned() {
        return versionMapper.selectLatestVersions().stream()
                .filter(v -> Boolean.TRUE.equals(v.getIsPinned()))
                .toList();
    }
}
