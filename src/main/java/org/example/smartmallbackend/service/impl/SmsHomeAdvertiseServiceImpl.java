package org.example.smartmallbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.smartmallbackend.entity.SmsHomeAdvertise;
import org.example.smartmallbackend.mapper.SmsHomeAdvertiseMapper;
import org.example.smartmallbackend.service.SmsHomeAdvertiseService;
import org.springframework.stereotype.Service;

@Service
public class SmsHomeAdvertiseServiceImpl extends ServiceImpl<SmsHomeAdvertiseMapper, SmsHomeAdvertise>
    implements SmsHomeAdvertiseService {
}
