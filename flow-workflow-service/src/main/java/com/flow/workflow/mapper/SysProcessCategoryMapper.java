package com.flow.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flow.workflow.entity.SysProcessCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysProcessCategoryMapper extends BaseMapper<SysProcessCategory> {

    @Select("SELECT COUNT(*) FROM sys_process_definition_version WHERE category_code = #{code} AND is_deleted = 0")
    int countBoundProcesses(@Param("code") String code);
}
