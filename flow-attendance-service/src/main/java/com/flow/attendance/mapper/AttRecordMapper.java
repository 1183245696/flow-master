package com.flow.attendance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.flow.attendance.entity.AttRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface AttRecordMapper extends BaseMapper<AttRecord> {

    @Select("""
            SELECT COUNT(DISTINCT user_id)
            FROM att_record
            WHERE clock_time >= #{dayStart}
              AND clock_time < #{dayEnd}
              AND is_deleted = 0
            """)
    int countTodayClockUsers(@Param("dayStart") LocalDateTime dayStart,
                              @Param("dayEnd") LocalDateTime dayEnd);

    @Select("""
            SELECT COUNT(*)
            FROM att_record
            WHERE user_id = #{userId}
              AND clock_time >= #{dayStart}
              AND clock_time < #{dayEnd}
              AND is_deleted = 0
            """)
    int countTodayByUser(@Param("userId") Long userId,
                          @Param("dayStart") LocalDateTime dayStart,
                          @Param("dayEnd") LocalDateTime dayEnd);
}
