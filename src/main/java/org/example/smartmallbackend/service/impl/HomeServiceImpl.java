package org.example.smartmallbackend.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.smartmallbackend.entity.PmsSpu;
import org.example.smartmallbackend.entity.SmsHomeAdvertise;
import org.example.smartmallbackend.mapper.PmsSpuMapper;
import org.example.smartmallbackend.mapper.SmsHomeAdvertiseMapper;
import org.example.smartmallbackend.service.HomeService;
import org.example.smartmallbackend.service.PmsSpuService;
import org.example.smartmallbackend.service.SmsHomeAdvertiseService;
import org.example.smartmallbackend.vo.HomeContentResultVO;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {
    private final SmsHomeAdvertiseService advertiseService;
    private final PmsSpuService spuService;
    @Override
    @Cacheable(value = "home:content", key = "'homeContent'", unless = "#result == null")
    public HomeContentResultVO getHomeContent() {
        HomeContentResultVO resultVO = new HomeContentResultVO();
        // 获取广告数据
        List<SmsHomeAdvertise> ads= advertiseService.list(new LambdaQueryWrapper<SmsHomeAdvertise>()
                .eq(SmsHomeAdvertise::getStatus, 1)
                .orderByDesc(SmsHomeAdvertise::getSort));
        resultVO.setAdvertiseList(ads);
        //获取新品推荐
        List<PmsSpu> newProducts = spuService.list(new LambdaQueryWrapper<PmsSpu>()
                .eq(PmsSpu::getPublishStatus, 1)
                .orderByDesc(PmsSpu::getCreateTime)
                .last("LIMIT 4"));
        resultVO.setNewProductList(newProducts);
        //获取热门推荐(价格为高的商品)
        List<PmsSpu> hotProducts = spuService.list(new LambdaQueryWrapper<PmsSpu>()
                .eq(PmsSpu::getPublishStatus, 1)
                .orderByDesc(PmsSpu::getPrice)
                .last("LIMIT 4"));
        resultVO.setRecommendProductList(hotProducts);
        return resultVO;
    }
}
