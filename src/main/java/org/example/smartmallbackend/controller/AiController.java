package org.example.smartmallbackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.example.smartmallbackend.common.Result;
import org.example.smartmallbackend.entity.PmsSpu;
import org.example.smartmallbackend.service.AiSearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {
    private final AiSearchService aiSearchService;
    @Operation(summary = "AI搜索接口",description = "输入自然语言（如：适合送给女朋友的礼物），返回推荐商品")
    @GetMapping("/search")
    public Result<List<PmsSpu>> search(
            @Parameter(description = "搜索关键词",required = true,example = "适合送给女朋友的礼物")
            @RequestParam String query) {
        List<PmsSpu> recommendedProducts = aiSearchService.search(query);
        return Result.success(recommendedProducts);
    }
}
