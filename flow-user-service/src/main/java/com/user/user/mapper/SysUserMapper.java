package com.user.user.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.user.user.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    @Select("SELECT permission_code FROM sys_user_permission WHERE user_id = #{userId}")
    List<String> selectPermissionsByUserId(Long userId);
}
