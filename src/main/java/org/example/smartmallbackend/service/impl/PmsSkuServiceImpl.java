package org.example.smartmallbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.smartmallbackend.entity.PmsSku;
import org.example.smartmallbackend.mapper.PmsSkuMapper;
import org.example.smartmallbackend.service.PmsSkuService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 商品SKU Service实现类
 *
 * @author smart-mall-backend
 * @description 针对表【pms_sku(商品SKU表)】的数据库操作Service实现
 * @createDate 2026-01-06
 */
@Service
public class PmsSkuServiceImpl extends ServiceImpl<PmsSkuMapper, PmsSku>
        implements PmsSkuService {

    @Cacheable(value = "pms:sku", key = "#id", unless = "#result == null")
    @Override
    public PmsSku getById(Long id) {
        return super.getById(id);
    }

    @Override
    @CacheEvict(value = "pms:sku", key = "#entity.id")
    public boolean updateById(PmsSku entity) {
        // 更新缓存
        return super.updateById(entity);
    }

    @Override
    @CacheEvict(value = "pms:sku", key = "#id")
    public boolean removeById(Long id) {
        return super.removeById(id);
    }

}




