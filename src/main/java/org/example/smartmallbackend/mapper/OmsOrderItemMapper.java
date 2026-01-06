package org.example.smartmallbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.smartmallbackend.entity.OmsOrderItem;

/**
 * 订单明细 Mapper接口
 *
 * @author smart-mall-backend
 * @description 针对表【oms_order_item(订单明细快照表)】的数据库操作Mapper
 * @createDate 2026-01-06
 * @Entity org.example.smartmallbackend.entity.OmsOrderItem
 */
@Mapper
public interface OmsOrderItemMapper extends BaseMapper<OmsOrderItem> {

}




