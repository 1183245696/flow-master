package com.flow.attendance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flow.attendance.entity.AttRecord;
import com.flow.attendance.entity.AttRule;
import com.flow.attendance.entity.AttZone;
import com.flow.attendance.mapper.AttRecordMapper;
import com.flow.attendance.mapper.AttRuleMapper;
import com.flow.attendance.mapper.AttZoneMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceService extends ServiceImpl<AttRecordMapper, AttRecord> {

    private final AttZoneMapper zoneMapper;
    private final AttRuleMapper ruleMapper;
    private final AttRecordMapper recordMapper;

    // ─── Zone & Rule CRUD ────────────────────────────────────────────────────

    public List<AttZone> listZones() {
        return zoneMapper.selectList(null);
    }

    public AttZone createZone(AttZone zone) {
        zoneMapper.insert(zone);
        return zone;
    }

    public void updateZone(AttZone zone) {
        zoneMapper.updateById(zone);
    }

    public void deleteZone(Long id) {
        zoneMapper.deleteById(id);
    }

    public List<AttRule> listRules() {
        return ruleMapper.selectList(null);
    }

    public AttRule createRule(AttRule rule) {
        ruleMapper.insert(rule);
        return rule;
    }

    public void updateRule(AttRule rule) {
        ruleMapper.updateById(rule);
    }

    public void deleteRule(Long id) {
        ruleMapper.deleteById(id);
    }

    // ─── Clock-in ────────────────────────────────────────────────────────────

    /**
     * 打卡逻辑：
     * 1. 通过 deptId 找考勤区域（含继承）
     * 2. 计算距离
     * 3. distance ≤ radius → isValid=true
     * 4. 保存记录
     */
    @Transactional
    public AttRecord clockIn(Long userId, Long deptId, Double lat, Double lng, String type) {
        // 1. Find zone (with inheritance) — zone lookup done by caller passing zoneId
        //    Here we look up by deptId using zone stored in dept (via Dubbo in real scenario)
        //    For standalone mode: find zone configured for dept
        AttZone zone = findZoneForDept(deptId);

        AttRecord record = new AttRecord();
        record.setUserId(userId);
        record.setDeptId(deptId);
        record.setClockTime(LocalDateTime.now());
        record.setLat(lat);
        record.setLng(lng);
        record.setType(type);

        if (zone != null) {
            int distanceMeters = calculateDistanceMeters(lat, lng, zone.getLat(), zone.getLng());
            record.setZoneId(zone.getId());
            record.setDistanceMeters(distanceMeters);
            record.setIsValid(distanceMeters <= zone.getRadiusMeters());
        } else {
            record.setDistanceMeters(0);
            record.setIsValid(false);
            log.warn("No attendance zone found for deptId={}", deptId);
        }

        save(record);
        log.info("Clock-in saved: userId={}, type={}, isValid={}, distance={}m",
                userId, type, record.getIsValid(), record.getDistanceMeters());
        return record;
    }

    /**
     * 请假时长计算（排除非工作时间）
     * 支持：双休/大小周/自定义
     */
    public double calculateLeaveDuration(Long userId, LocalDateTime startTime, LocalDateTime endTime, Long ruleId) {
        if (ruleId == null) {
            // Default: 8 hours per day, Mon–Fri
            return calculateDefaultLeaveDuration(startTime, endTime);
        }

        AttRule rule = ruleMapper.selectById(ruleId);
        if (rule == null) {
            return calculateDefaultLeaveDuration(startTime, endTime);
        }

        LocalTime workStart = LocalTime.parse(rule.getWorkStart());
        LocalTime workEnd   = LocalTime.parse(rule.getWorkEnd());
        double totalHours   = 0.0;

        LocalDate cursor = startTime.toLocalDate();
        LocalDate lastDay = endTime.toLocalDate();

        while (!cursor.isAfter(lastDay)) {
            if (isWorkDay(cursor, rule)) {
                LocalDateTime dayStart = LocalDateTime.of(cursor, workStart);
                LocalDateTime dayEnd   = LocalDateTime.of(cursor, workEnd);

                // Intersect with requested leave period
                LocalDateTime from = startTime.isAfter(dayStart) ? startTime : dayStart;
                LocalDateTime to   = endTime.isBefore(dayEnd) ? endTime : dayEnd;

                if (from.isBefore(to)) {
                    totalHours += ChronoUnit.MINUTES.between(from, to) / 60.0;
                }
            }
            cursor = cursor.plusDays(1);
        }
        return totalHours;
    }

    private boolean isWorkDay(LocalDate date, AttRule rule) {
        int dayOfWeek = date.getDayOfWeek().getValue(); // 1=Mon, 7=Sun
        String workDays = rule.getWorkDays();

        switch (rule.getType()) {
            case "双休" -> { return dayOfWeek >= 1 && dayOfWeek <= 5; }
            case "大小周" -> {
                // Week 1: Mon-Sat, Week 2: Mon-Fri, alternating
                long weekOfYear = date.toEpochDay() / 7;
                if (weekOfYear % 2 == 0) return dayOfWeek >= 1 && dayOfWeek <= 6;
                else return dayOfWeek >= 1 && dayOfWeek <= 5;
            }
            case "自定义" -> {
                if (workDays == null) return dayOfWeek >= 1 && dayOfWeek <= 5;
                return workDays.contains(String.valueOf(dayOfWeek));
            }
            default -> { return dayOfWeek >= 1 && dayOfWeek <= 5; }
        }
    }

    private double calculateDefaultLeaveDuration(LocalDateTime start, LocalDateTime end) {
        LocalTime ws = LocalTime.of(9, 0);
        LocalTime we = LocalTime.of(18, 0);
        double total = 0.0;
        LocalDate cursor = start.toLocalDate();
        while (!cursor.isAfter(end.toLocalDate())) {
            int dow = cursor.getDayOfWeek().getValue();
            if (dow >= 1 && dow <= 5) {
                LocalDateTime ds = LocalDateTime.of(cursor, ws);
                LocalDateTime de = LocalDateTime.of(cursor, we);
                LocalDateTime from = start.isAfter(ds) ? start : ds;
                LocalDateTime to   = end.isBefore(de) ? end : de;
                if (from.isBefore(to)) total += ChronoUnit.MINUTES.between(from, to) / 60.0;
            }
            cursor = cursor.plusDays(1);
        }
        return total;
    }

    // ─── Haversine distance (meters) ────────────────────────────────────────

    public int calculateDistanceMeters(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371000; // Earth radius in meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return (int) (R * c);
    }

    private AttZone findZoneForDept(Long deptId) {
        // In a real scenario this calls OrgRpcService.getAttendanceZoneByDeptId
        // then fetches the zone. Simplified: return first available zone.
        return zoneMapper.selectList(new LambdaQueryWrapper<AttZone>().last("LIMIT 1"))
                .stream().findFirst().orElse(null);
    }

    // ─── Records ─────────────────────────────────────────────────────────────

    public Page<AttRecord> myRecords(Long userId, LocalDateTime from, LocalDateTime to,
                                      int pageNum, int pageSize) {
        return page(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<AttRecord>()
                        .eq(AttRecord::getUserId, userId)
                        .ge(from != null, AttRecord::getClockTime, from)
                        .le(to != null, AttRecord::getClockTime, to)
                        .orderByDesc(AttRecord::getClockTime));
    }

    public Page<AttRecord> allRecords(Long deptId, Long userId,
                                       LocalDateTime from, LocalDateTime to,
                                       int pageNum, int pageSize) {
        return page(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<AttRecord>()
                        .eq(deptId != null, AttRecord::getDeptId, deptId)
                        .eq(userId != null, AttRecord::getUserId, userId)
                        .ge(from != null, AttRecord::getClockTime, from)
                        .le(to != null, AttRecord::getClockTime, to)
                        .orderByDesc(AttRecord::getClockTime));
    }

    public int getTodayClockCount(Long userId) {
        LocalDateTime dayStart = LocalDate.now().atStartOfDay();
        LocalDateTime dayEnd   = dayStart.plusDays(1);
        return recordMapper.countTodayByUser(userId, dayStart, dayEnd);
    }
}
