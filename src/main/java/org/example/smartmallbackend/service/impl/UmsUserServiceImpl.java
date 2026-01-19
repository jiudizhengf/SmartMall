package org.example.smartmallbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.smartmallbackend.entity.UmsUser;
import org.example.smartmallbackend.mapper.UmsUserMapper;
import org.example.smartmallbackend.service.UmsUserService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class UmsUserServiceImpl extends ServiceImpl<UmsUserMapper, UmsUser> implements UmsUserService {
    // 2. 根据 ID 查询 (高频：过滤器里解析 Token 后查用户)
    @Override
    @Cacheable(value = "ums:user", key = "#id", unless = "#result == null")
    public UmsUser getById(java.io.Serializable id) {
        return super.getById(id);
    }

    // 3. 修改用户信息时，删除缓存
    @Override
    @CacheEvict(value = "ums:user", key = "#entity.id")
    public boolean updateById(UmsUser entity) {
        return super.updateById(entity);
    }
}
