package com.user.user.rpc;
import com.user.user.entity.SysUser;
import java.util.List;
public interface UserRpcService {
    SysUser getById(Long userId);
    List<String> getPermissionsByUserId(Long userId);
    boolean validateToken(Long userId, String token);
}
