package org.example.smartmallbackend.service.impl;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.smartmallbackend.entity.PmsSpu;
import org.example.smartmallbackend.service.AiSearchService;
import org.example.smartmallbackend.service.PmsSpuService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiSearchServiceImpl implements AiSearchService {
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final PmsSpuService spuService;
    private static final double MIN_SCORE = 0.75;
    @Override
    public List<PmsSpu> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }
        log.info("搜索上架商品，关键词：{}", keyword);
        // 生成关键词的向量表示
        Embedding queryEmbedding = embeddingModel.embed(keyword).content();
        // 构建搜索请求，需要指定 maxResults (例如 10)
        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(20)
                .build();
        List<EmbeddingMatch<TextSegment>> matches= embeddingStore.search(request).matches();
        if (matches.isEmpty()) {
            log.info("未找到匹配的向量结果");
            return Collections.emptyList();
        }
        // 2. 过滤掉分数太低的结果
        List<Long> spuIds = matches.stream()
                .filter(match -> {
                    // 如果分数低于阈值，直接丢弃
                    boolean pass = match.score() >= MIN_SCORE;
                    if (!pass) {
                        log.info("丢弃低相关度结果: spuId={} score={}", match.embedded().metadata().get("spuId"), match.score());
                    }
                    return pass;
                })
                .map(match -> match.embedded().metadata().get("spuId"))
                .filter(id -> id != null && !id.isEmpty())
                .map(Long::valueOf)
                .distinct()
                .limit(10) // 过滤完后再取前 10 个
                .collect(Collectors.toList());

        if (spuIds.isEmpty()) {
            return Collections.emptyList();
        }
        log.info("向量检索命中 SPU ID: {}", spuIds);

        // 【回查】去 MySQL 查询完整的商品信息
        List<PmsSpu> spuList = spuService.listByIds(spuIds);
        // 【重排】数据库查出来的顺序是乱的，我们需要按向量相似度重新排序
        // 建立 id -> spu 的映射
        Map<Long, PmsSpu> spuMap = spuList.stream()
                .collect(Collectors.toMap(PmsSpu::getId, Function.identity()));
        // 按 spuIds (相似度高 -> 低) 的顺序组装结果
        List<PmsSpu> sortedList = new ArrayList<>();
        for (Long id : spuIds) {
            if (spuMap.containsKey(id)) {
                sortedList.add(spuMap.get(id));
            }
        }
        return sortedList;

    }
}
