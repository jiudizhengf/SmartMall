package org.example.smartmallbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.smartmallbackend.entity.OmsOrder;

/**
 * 订单 Mapper接口
 *
 * @author smart-mall-backend
 * @description 针对表【oms_order(订单主表)】的数据库操作Mapper
 * @createDate 2026-01-06
 * @Entity org.example.smartmallbackend.entity.OmsOrder
 */
@Mapper
public interface OmsOrderMapper extends BaseMapper<OmsOrder> {

}




