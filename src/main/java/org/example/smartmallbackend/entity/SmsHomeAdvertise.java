package org.example.smartmallbackend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 首页轮播广告实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sms_home_advertise")
@Schema(description = "首页轮播广告")
public class SmsHomeAdvertise extends BaseEntity {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "广告名称")
    private String name;

    @Schema(description = "图片地址")
    private String pic;

    @Schema(description = "跳转链接")
    private String url;

    @Schema(description = "状态：0-下线，1-上线")
    private Integer status;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "备注")
    private String note;
}