package com.flow.org.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flow.org.entity.SysPosition;
import com.flow.org.mapper.SysPositionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PositionService extends ServiceImpl<SysPositionMapper, SysPosition> {

    public Page<SysPosition> pageByDept(Long deptId, int pageNum, int pageSize) {
        return page(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SysPosition>()
                        .eq(deptId != null, SysPosition::getDeptId, deptId)
                        .orderByAsc(SysPosition::getSort));
    }

    public SysPosition createPosition(SysPosition position) {
        save(position);
        return position;
    }

    public void deletePosition(Long id) {
        removeById(id);
    }
}
