package org.example.smartmallbackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.smartmallbackend.common.Result;
import org.example.smartmallbackend.service.impl.SeckillServiceImpl;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "秒杀活动接口", description = "秒杀活动管理")
@RestController
@RequestMapping("/api/seckill")
@RequiredArgsConstructor
public class SeckillController {
    private final SeckillServiceImpl seckillService;

    @Operation(summary = "秒杀活动预热", description = "预热即将开始的秒杀活动")
    @PostMapping("/prepare")
    public Result<?> prepareStock(@RequestParam Long skuId, @RequestParam int count) {
        seckillService.prepareStock(skuId, count);
        return Result.success("库存预热成功,Redis Key: seckill:stock:" + skuId);
    }
    @Operation(summary = "参与秒杀活动", description = "用户参与秒杀活动")
    @PostMapping("/seckill")
    public Result<String> seckill(@RequestParam Long skuId, @RequestParam Long userId) {
        //方便接口调试，实际上应该在UserContext中获取当前用户ID
        return seckillService.seckill(skuId, userId);
    }
}
