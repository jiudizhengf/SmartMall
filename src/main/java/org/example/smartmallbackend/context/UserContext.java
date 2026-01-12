package org.example.smartmallbackend.context;

import org.example.smartmallbackend.common.BusinessException;
import org.example.smartmallbackend.common.ResultCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 全局用户上下文工具类
 * 可以在任何地方获取当前登录用户ID
 */
public class UserContext {
    /**
     * 获取当前登录用户ID
     * @return
     */
    public static Long getUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null||!authentication.isAuthenticated()||"anonymousUser".equals(authentication.getPrincipal())){
            throw new BusinessException(ResultCode.UNAUTHORIZED,"用户未登录");
        }
        try{
            return (Long) authentication.getPrincipal();

        }catch (Exception e){
            throw new BusinessException(ResultCode.FORBIDDEN,"用户信息解析失败");
        }
    }
}
