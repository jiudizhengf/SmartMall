package org.example.smartmallbackend.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 向量存储实体类
 * 用于LangChain4j向量知识库存储
 *
 * @TableName embedding_store
 */
@TableName(value = "embedding_store")
@Data
public class EmbeddingStore implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 向量ID
     */
    @TableId
    private String embeddingId;

    /**
     * 向量数据（JSON格式）
     */
    private String embedding;

    /**
     * 文本内容
     */
    private String text;

    /**
     * 元数据（JSON格式）
     */
    private String metadata;
}