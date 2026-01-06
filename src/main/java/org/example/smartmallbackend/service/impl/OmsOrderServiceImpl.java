package org.example.smartmallbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.smartmallbackend.entity.OmsOrder;
import org.example.smartmallbackend.mapper.OmsOrderMapper;
import org.example.smartmallbackend.service.OmsOrderService;
import org.springframework.stereotype.Service;

/**
 * 订单 Service实现类
 *
 * @author smart-mall-backend
 * @description 针对表【oms_order(订单主表)】的数据库操作Service实现
 * @createDate 2026-01-06
 */
@Service
public class OmsOrderServiceImpl extends ServiceImpl<OmsOrderMapper, OmsOrder>
        implements OmsOrderService {

}




