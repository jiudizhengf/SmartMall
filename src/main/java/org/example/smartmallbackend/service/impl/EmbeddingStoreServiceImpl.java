package org.example.smartmallbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.smartmallbackend.entity.EmbeddingStore;
import org.example.smartmallbackend.mapper.EmbeddingStoreMapper;
import org.example.smartmallbackend.service.EmbeddingStoreService;
import org.springframework.stereotype.Service;

/**
 * 向量存储 Service实现类
 *
 * @author smart-mall-backend
 * @description 针对表【embedding_store(向量知识库)】的数据库操作Service实现
 * @createDate 2026-01-06
 */
@Service
public class EmbeddingStoreServiceImpl extends ServiceImpl<EmbeddingStoreMapper, EmbeddingStore>
        implements EmbeddingStoreService {

}




