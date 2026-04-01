package com.flow.workflow.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flow.base.exception.BusinessException;
import com.flow.workflow.entity.SysProcessCategory;
import com.flow.workflow.mapper.SysProcessCategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProcessCategoryService extends ServiceImpl<SysProcessCategoryMapper, SysProcessCategory> {

    private final SysProcessCategoryMapper categoryMapper;

    /**
     * 删除前检查是否有流程绑定 — 有则抛异常
     */
    public void deleteCategory(Long id) {
        SysProcessCategory cat = getById(id);
        if (cat == null) throw new BusinessException("分类不存在");

        int bound = categoryMapper.countBoundProcesses(cat.getCode());
        if (bound > 0) {
            throw new BusinessException("该分类下存在 " + bound + " 个流程，请先移除后再删除");
        }
        removeById(id);
    }
}
