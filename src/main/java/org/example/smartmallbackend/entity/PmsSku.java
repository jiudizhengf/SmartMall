package org.example.smartmallbackend.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品SKU实体类
 * SKU（Stock Keeping Unit）库存量单位，商品库存和规格信息
 *
 * @TableName pms_sku
 */
@TableName(value = "pms_sku")
@Data
public class PmsSku implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * SPU ID
     */
    private Long spuId;

    /**
     * SKU编码
     */
    private String skuCode;

    /**
     * SKU名称
     */
    private String name;

    /**
     * SKU价格
     */
    private BigDecimal price;

    /**
     * 库存数量
     */
    private Integer stock;

    /**
     * 规格数据（JSON格式）
     */
    private String specData;

    /**
     * 商品图片URL
     */
    private String picUrl;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    @TableLogic
    private Integer isDeleted;
}