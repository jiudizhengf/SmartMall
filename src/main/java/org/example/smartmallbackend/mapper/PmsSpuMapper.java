package org.example.smartmallbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.smartmallbackend.entity.PmsSpu;

/**
 * 商品SPU Mapper接口
 *
 * @author smart-mall-backend
 * @description 针对表【pms_spu(商品SPU表)】的数据库操作Mapper
 * @createDate 2026-01-06
 * @Entity org.example.smartmallbackend.entity.PmsSpu
 */
@Mapper
public interface PmsSpuMapper extends BaseMapper<PmsSpu> {

}




