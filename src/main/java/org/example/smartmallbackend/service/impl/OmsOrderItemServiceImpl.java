package org.example.smartmallbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.smartmallbackend.entity.OmsOrderItem;
import org.example.smartmallbackend.mapper.OmsOrderItemMapper;
import org.example.smartmallbackend.service.OmsOrderItemService;
import org.springframework.stereotype.Service;

/**
 * 订单明细 Service实现类
 *
 * @author smart-mall-backend
 * @description 针对表【oms_order_item(订单明细快照表)】的数据库操作Service实现
 * @createDate 2026-01-06
 */
@Service
public class OmsOrderItemServiceImpl extends ServiceImpl<OmsOrderItemMapper, OmsOrderItem>
        implements OmsOrderItemService {

}




