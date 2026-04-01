package com.flow.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flow.workflow.entity.ProcessDefinitionVersion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ProcessDefinitionVersionMapper extends BaseMapper<ProcessDefinitionVersion> {

    @Select("SELECT * FROM sys_process_definition_version WHERE is_latest = 1 AND is_deleted = 0 ORDER BY created_at DESC")
    List<ProcessDefinitionVersion> selectLatestVersions();

    @Select("SELECT * FROM sys_process_definition_version WHERE process_key = #{processKey} AND is_deleted = 0 ORDER BY version DESC")
    List<ProcessDefinitionVersion> selectVersionHistory(@Param("processKey") String processKey);

    @Update("UPDATE sys_process_definition_version SET is_latest = 0 WHERE process_key = #{processKey}")
    void clearLatestFlag(@Param("processKey") String processKey);

    @Select("SELECT COUNT(*) FROM sys_process_definition_version WHERE is_pinned = 1 AND is_deleted = 0")
    int countPinned();
}
