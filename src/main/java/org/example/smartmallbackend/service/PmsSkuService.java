package org.example.smartmallbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.smartmallbackend.entity.PmsSku;
import org.springframework.cache.annotation.Cacheable;

/**
 * 商品SKU Service接口
 *
 * @author smart-mall-backend
 * @description 针对表【pms_sku(商品SKU表)】的数据库操作Service
 * @createDate 2026-01-06
 */
public interface PmsSkuService extends IService<PmsSku> {

    @Cacheable(value = "pms:sku", key = "#id", unless = "#result == null")
    PmsSku getById(Long id);

    boolean removeById(Long id);
}
