package org.example.smartmallbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.smartmallbackend.entity.PmsSpu;
import org.springframework.cache.annotation.Cacheable;

/**
 * 商品SPU Service接口
 *
 * @author smart-mall-backend
 * @description 针对表【pms_spu(商品SPU表)】的数据库操作Service
 * @createDate 2026-01-06
 */
public interface PmsSpuService extends IService<PmsSpu> {

    @Cacheable(value = "pmsSpuCache", key = "#id",unless = "#result == null")
    PmsSpu getById(Long id);

    boolean removeById(Long id);
}
