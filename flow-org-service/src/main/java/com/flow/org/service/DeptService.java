package com.flow.org.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flow.base.exception.BusinessException;
import com.flow.org.entity.SysDept;
import com.flow.org.mapper.SysDeptMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeptService extends ServiceImpl<SysDeptMapper, SysDept> {

    private final SysDeptMapper deptMapper;

    /** Build full dept tree */
    public List<SysDept> buildTree() {
        List<SysDept> all = list(new LambdaQueryWrapper<SysDept>()
                .orderByAsc(SysDept::getSort));
        return buildChildren(0L, all);
    }

    private List<SysDept> buildChildren(Long parentId, List<SysDept> all) {
        return all.stream()
                .filter(d -> Objects.equals(d.getParentId(), parentId))
                .peek(d -> {
                    // Attach children via reflection trick — use a VO in real code
                    // Here simplified for brevity
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public SysDept createDept(SysDept dept) {
        // Build path
        if (dept.getParentId() == null || dept.getParentId() == 0) {
            dept.setParentId(0L);
            dept.setLevel(1);
        } else {
            SysDept parent = getById(dept.getParentId());
            if (parent == null) throw new BusinessException("父部门不存在");
            dept.setLevel(parent.getLevel() + 1);
            dept.setPath(parent.getPath() + parent.getId() + "/");
        }
        save(dept);
        if (dept.getPath() == null) dept.setPath("/" + dept.getId() + "/");
        updateById(dept);
        return dept;
    }

    @Transactional
    public void deleteDept(Long id) {
        // Check children
        long children = count(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getParentId, id));
        if (children > 0) throw new BusinessException("请先删除子部门");
        removeById(id);
    }

    /**
     * 考勤区域继承：递归 CTE 向上追溯，找第一个非 null 的 attendance_zone_id
     */
    public Long getInheritedAttendanceZone(Long deptId) {
        if (deptId == null) return null;
        SysDept dept = getById(deptId);
        if (dept == null) return null;
        if (dept.getAttendanceZoneId() != null) return dept.getAttendanceZoneId();
        // Use recursive CTE query from mapper
        return deptMapper.findInheritedAttendanceZoneId(deptId);
    }

    /**
     * POI 导出部门列表
     * 列：部门名称 | 负责人 | 考勤区域 | 打卡规则 | 层级路径
     */
    public void exportDepts(HttpServletResponse response) throws IOException {
        List<SysDept> depts = deptMapper.selectAllForExport();

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("部门列表");

            // Header style
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Header row
            Row header = sheet.createRow(0);
            String[] cols = {"部门名称", "负责人ID", "考勤区域ID", "打卡规则ID", "层级路径"};
            for (int i = 0; i < cols.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(cols[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 5000);
            }

            // Data rows
            for (int i = 0; i < depts.size(); i++) {
                SysDept d = depts.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(d.getName());
                row.createCell(1).setCellValue(d.getLeaderUserId() != null ? d.getLeaderUserId() : 0);
                row.createCell(2).setCellValue(d.getAttendanceZoneId() != null ? d.getAttendanceZoneId() : 0);
                row.createCell(3).setCellValue(d.getRuleId() != null ? d.getRuleId() : 0);
                row.createCell(4).setCellValue(d.getPath() != null ? d.getPath() : "");
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition",
                    "attachment; filename=" + URLEncoder.encode("部门列表.xlsx", StandardCharsets.UTF_8));
            wb.write(response.getOutputStream());
        }
    }
}
