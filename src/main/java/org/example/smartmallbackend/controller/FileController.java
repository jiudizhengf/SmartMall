package org.example.smartmallbackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.smartmallbackend.common.Result;
import org.example.smartmallbackend.service.OssService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
@Tag(name = "文件管理", description = "文件上传和下载接口")
public class FileController {
    private final OssService ossService;
    @Operation(summary = "文件上传", description = "上传文件到对象存储服务")
    @PostMapping("/upload")
    public Result<?> upload(@Parameter(description = "文件对象",required = true) @RequestParam("file") MultipartFile file) {
        String url = ossService.upload(file);
        return Result.success(url);
    }
}
