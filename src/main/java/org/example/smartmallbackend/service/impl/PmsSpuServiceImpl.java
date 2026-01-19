package org.example.smartmallbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.smartmallbackend.entity.PmsSpu;
import org.example.smartmallbackend.mapper.PmsSpuMapper;
import org.example.smartmallbackend.service.PmsSpuService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 商品SPU Service实现类
 *
 * @author smart-mall-backend
 * @description 针对表【pms_spu(商品SPU表)】的数据库操作Service实现
 * @createDate 2026-01-06
 */
@Service
public class PmsSpuServiceImpl extends ServiceImpl<PmsSpuMapper, PmsSpu>
        implements PmsSpuService {

    @Cacheable(value = "pms:spu", key = "#id", unless = "#result == null")
    @Override
    public PmsSpu getById(Long id) {
        return super.getById(id);
    }

    @Override
    @CacheEvict(value = "pms:spu", key = "#entity.id")
    public boolean updateById(PmsSpu entity) {
        // 更新缓存
        return super.updateById(entity);
    }

    @Override
    @CacheEvict(value = "pms:spu", key = "#id")
    public boolean removeById(Long id) {
        // 删除缓存
        return super.removeById(id);
    }
}




