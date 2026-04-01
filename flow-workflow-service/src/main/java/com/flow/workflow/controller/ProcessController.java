package com.flow.workflow.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.flow.base.response.R;
import com.flow.workflow.entity.ProcessDefinitionVersion;
import com.flow.workflow.entity.SysProcessApproverConfig;
import com.flow.workflow.entity.SysProcessCategory;
import com.flow.workflow.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "流程管理")
@RestController
@RequestMapping("/v1/workflow")
@RequiredArgsConstructor
public class ProcessController {

    private final ProcessService processService;
    private final ProcessCategoryService categoryService;
    private final ProcessInstanceService instanceService;
    private final ApproverConfigService approverConfigService;

    // ── 流程定义 ──────────────────────────────────────────────────────────────

    @Operation(summary = "流程列表（每个 key 只返回最新版本）")
    @GetMapping("/definitions")
    public R<List<ProcessDefinitionVersion>> listLatest() {
        return R.ok(processService.listLatest());
    }

    @Operation(summary = "流程详情 + 历史版本")
    @GetMapping("/definitions/{processKey}")
    public R<Map<String, Object>> detail(
            @PathVariable String processKey,
            @RequestParam(defaultValue = "1") int historyPage,
            @RequestParam(defaultValue = "10") int historySize) {
        ProcessDefinitionVersion latest = processService.getLatest(processKey);
        Page<ProcessDefinitionVersion> history = processService.historyVersions(processKey, historyPage, historySize);
        return R.ok(Map.of("latest", latest, "history", history));
    }

    @Operation(summary = "新增/编辑流程（保存 BPMN + 部署新版本）")
    @PostMapping("/definitions")
    public R<ProcessDefinitionVersion> deploy(@RequestBody ProcessDefinitionVersion req) {
        return R.ok(processService.deployProcess(req));
    }

    @Operation(summary = "固定/取消固定流程（最多 6 个）")
    @PutMapping("/definitions/{id}/pin")
    public R<Void> pin(@PathVariable Long id, @RequestParam boolean pin) {
        processService.togglePin(id, pin);
        return R.ok();
    }

    @Operation(summary = "获取固定的流程（首页展示）")
    @GetMapping("/definitions/pinned")
    public R<List<ProcessDefinitionVersion>> pinned() {
        return R.ok(processService.listPinned());
    }

    // ── 流程分类 ──────────────────────────────────────────────────────────────

    @Operation(summary = "分类列表")
    @GetMapping("/categories")
    public R<List<SysProcessCategory>> listCategories() {
        return R.ok(categoryService.list());
    }

    @Operation(summary = "新增分类")
    @PostMapping("/categories")
    public R<SysProcessCategory> createCategory(@RequestBody SysProcessCategory category) {
        categoryService.save(category);
        return R.ok(category);
    }

    @Operation(summary = "修改分类")
    @PutMapping("/categories/{id}")
    public R<Void> updateCategory(@PathVariable Long id, @RequestBody SysProcessCategory category) {
        category.setId(id);
        categoryService.updateById(category);
        return R.ok();
    }

    @Operation(summary = "删除分类（有流程绑定时拒绝）")
    @DeleteMapping("/categories/{id}")
    public R<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return R.ok();
    }

    // ── 审批人配置 ────────────────────────────────────────────────────────────

    @Operation(summary = "保存审批人配置")
    @PostMapping("/approver-configs")
    public R<SysProcessApproverConfig> saveApproverConfig(@RequestBody SysProcessApproverConfig config) {
        approverConfigService.saveConfig(config);
        return R.ok(config);
    }

    @Operation(summary = "按流程 key 查询审批人配置")
    @GetMapping("/approver-configs/{processKey}")
    public R<List<SysProcessApproverConfig>> getApproverConfigs(@PathVariable String processKey) {
        return R.ok(approverConfigService.listByProcessKey(processKey));
    }

    // ── 流程实例 ──────────────────────────────────────────────────────────────

    @Operation(summary = "发起流程")
    @PostMapping("/instances")
    public R<String> startProcess(@RequestBody StartProcessRequest req,
                                  @RequestHeader("X-User-Id") Long userId) {
        ProcessInstance instance = instanceService.startProcess(
                req.getProcessKey(), userId, req.getVariables());
        return R.ok(instance.getId());
    }

    @Operation(summary = "我的待办任务")
    @GetMapping("/tasks/my-pending")
    public R<List<Task>> myPendingTasks(@RequestHeader("X-User-Id") Long userId) {
        return R.ok(instanceService.getMyPendingTasks(userId));
    }

    @Operation(summary = "审批通过")
    @PostMapping("/tasks/{taskId}/approve")
    public R<Void> approve(@PathVariable String taskId,
                            @RequestBody ApprovalRequest req,
                            @RequestHeader("X-User-Id") Long userId) {
        instanceService.approveTask(taskId, userId, req.getComment(), req.getVariables());
        return R.ok();
    }

    @Operation(summary = "审批驳回")
    @PostMapping("/tasks/{taskId}/reject")
    public R<Void> reject(@PathVariable String taskId,
                           @RequestBody ApprovalRequest req,
                           @RequestHeader("X-User-Id") Long userId) {
        instanceService.rejectTask(taskId, userId, req.getComment());
        return R.ok();
    }

    @Data
    public static class StartProcessRequest {
        private String processKey;
        private Map<String, Object> variables;
    }

    @Data
    public static class ApprovalRequest {
        private String comment;
        private Map<String, Object> variables;
    }
}
