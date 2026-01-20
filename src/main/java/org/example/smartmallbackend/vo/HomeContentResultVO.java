package org.example.smartmallbackend.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.example.smartmallbackend.entity.PmsSpu;
import org.example.smartmallbackend.entity.SmsHomeAdvertise;

import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "首页内容结果VO")
public class HomeContentResultVO implements Serializable {
    @Schema(description = "轮播广告列表")
    private List<SmsHomeAdvertise> advertiseList;

    @Schema(description = "新品推荐列表")
    private List<PmsSpu> newProductList;

    @Schema(description = "热门推荐列表")
    private List<PmsSpu> recommendProductList;
}
