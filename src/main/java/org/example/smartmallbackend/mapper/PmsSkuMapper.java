package org.example.smartmallbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.smartmallbackend.entity.PmsSku;

/**
 * 商品SKU Mapper接口
 *
 * @author smart-mall-backend
 * @description 针对表【pms_sku(商品SKU表)】的数据库操作Mapper
 * @createDate 2026-01-06
 * @Entity org.example.smartmallbackend.entity.PmsSku
 */
@Mapper
public interface PmsSkuMapper extends BaseMapper<PmsSku> {

}




