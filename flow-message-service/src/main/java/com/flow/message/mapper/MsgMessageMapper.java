package com.flow.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flow.message.entity.MsgMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MsgMessageMapper extends BaseMapper<MsgMessage> {

    @Select("SELECT COUNT(*) FROM msg_message WHERE user_id = #{userId} AND is_read = 0 AND is_deleted = 0")
    int countUnread(@Param("userId") Long userId);
}
