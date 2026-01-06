package org.example.smartmallbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.smartmallbackend.entity.EmbeddingStore;

/**
 * 向量存储 Mapper接口
 *
 * @author smart-mall-backend
 * @description 针对表【embedding_store(向量知识库)】的数据库操作Mapper
 * @createDate 2026-01-06
 * @Entity org.example.smartmallbackend.entity.EmbeddingStore
 */
@Mapper
public interface EmbeddingStoreMapper extends BaseMapper<EmbeddingStore> {

}




