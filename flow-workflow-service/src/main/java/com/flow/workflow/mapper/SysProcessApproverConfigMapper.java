package com.flow.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flow.workflow.entity.SysProcessApproverConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysProcessApproverConfigMapper extends BaseMapper<SysProcessApproverConfig> {

    @Select("SELECT * FROM sys_process_approver_config WHERE process_key = #{processKey} AND node_id = #{nodeId} AND is_deleted = 0")
    List<SysProcessApproverConfig> selectByProcessKeyAndNodeId(@Param("processKey") String processKey,
                                                                @Param("nodeId") String nodeId);
}
