package org.example.smartmallbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.smartmallbackend.entity.UmsUser;
import org.example.smartmallbackend.mapper.UmsUserMapper;
import org.example.smartmallbackend.service.UmsUserService;
import org.springframework.stereotype.Service;

@Service
public class UmsUserServiceImpl extends ServiceImpl<UmsUserMapper, UmsUser> implements UmsUserService {
}
