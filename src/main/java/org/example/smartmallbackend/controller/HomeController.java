package org.example.smartmallbackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.example.smartmallbackend.common.Result;
import org.example.smartmallbackend.service.HomeService;
import org.example.smartmallbackend.vo.HomeContentResultVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeController {
    private final HomeService homeService;

    @Operation(summary = "获取首页内容", description = "获取首页轮播图、推荐商品等内容")
    @GetMapping("/content")
    public Result<HomeContentResultVO> content(){
        HomeContentResultVO content = homeService.getHomeContent();
        return Result.success(content);
    }
}
