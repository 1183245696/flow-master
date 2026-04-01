package com.user.user.rpc;
import com.flow.base.util.RedisTokenUtil;
import com.user.user.entity.SysUser;
import com.user.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import java.util.List;
@DubboService(version = "1.0.0")
@RequiredArgsConstructor
public class UserRpcServiceImpl implements UserRpcService {
    private final UserService userService;
    private final RedisTokenUtil redisTokenUtil;
    @Override public SysUser getById(Long userId) { return userService.getById(userId); }
    @Override public List<String> getPermissionsByUserId(Long userId) { return userService.getPermissionsByUserId(userId); }
    @Override public boolean validateToken(Long userId, String token) {
        try { return redisTokenUtil.validateAccessToken(userId, token); }
        catch (Exception e) { return false; }
    }
}
