package org.example.smartmallbackend.listener;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.example.smartmallbackend.entity.PmsSku;
import org.example.smartmallbackend.entity.PmsSpu;
import org.example.smartmallbackend.event.ProductOnShelfEvent;
import org.example.smartmallbackend.event.ProductionOffShelfEvent;
import org.example.smartmallbackend.service.EmbeddingStoreService;
import org.example.smartmallbackend.service.PmsSkuService;
import org.example.smartmallbackend.service.PmsSpuService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ProductAiListener {
    @Autowired
    private PmsSpuService pmsSpuService;
    @Autowired
    private PmsSkuService pmsSkuService;

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    @Autowired
    private EmbeddingStoreService embeddingStoreService;

    @Async
    @EventListener
    public void handleProductOnShelf(ProductOnShelfEvent event) {
        Long spuId = event.getSpuId();
        log.info("开始处理商品向量化spuId:{}",spuId);
        // 获取商品信息
        PmsSpu spu= pmsSpuService.getById(spuId);
        if (spu == null) {
            log.warn("未找到商品SPU，spuId:{}", spuId);
            return;
        }
        List<PmsSku> skuList=pmsSkuService.list(new LambdaQueryWrapper<PmsSku>().eq(PmsSku::getSpuId,spuId));
        // 1. 计算价格区间 (让AI知道这个商品大概多少钱，防止把几块钱的手机壳推荐给搜手机的人)
        BigDecimal minPrice = skuList.stream().map(PmsSku::getPrice).min(BigDecimal::compareTo).orElse(spu.getPrice());
        BigDecimal maxPrice = skuList.stream().map(PmsSku::getPrice).max(BigDecimal::compareTo).orElse(spu.getPrice());

        // 2. 提取规格特征 (把所有SKU的特性都拼进来，比如 "黑色 256G", "白色 512G")
        String skuKeywords = skuList.stream()
                .map(sku -> sku.getName() + " " + sku.getSpecData()) // 这里把 specData JSON 也拼进去，增加特征
                .collect(Collectors.joining("; "));

        // 3. 🔥 核心优化：构建高密度的语义文本模板
        // 格式化文本，像写 SEO 文章一样，把品牌、分类、特性都显式列出来
        String textToEmbed = String.format("""
                商品类型：电商商品
                品牌：%s
                商品名称：%s
                核心卖点：%s
                详细描述：%s
                价格范围：%.0f - %.0f 元
                包含规格：%s
                """,
                spu.getBrandName(),
                spu.getName(),
                spu.getSubTitle(),
                spu.getDescription(), // 这里的描述越详细，搜索越准
                minPrice, maxPrice,
                skuKeywords
        );
        //准备元数据
        Metadata metadata = new Metadata();
        metadata.add("spuId", spuId.toString());
        metadata.add("brand", spu.getBrandName());
        metadata.add("category", spu.getCategoryId().toString());
        //生成向量
        TextSegment textSegment = TextSegment.from(textToEmbed,metadata);
        Embedding embedding=embeddingModel.embed(textSegment).content();

        //存入向量库
        embeddingStore.add(embedding,textSegment);
        log.info("完成商品向量化存储spuId:{}",spuId);

    }

    @Async
    @EventListener
    public void handleProductOffShelf(ProductionOffShelfEvent event) {
        Long spuId = event.getSpuId();
        log.info("开始处理商品下架向量删除spuId:{}",spuId);
        // 从向量库删除
        // 利用 metadata 字段进行模糊匹配删除
        // 存储时的 metadata 类似于: {"spuId": "1001", "brand": "Xiaomi"...}
        String jsonCondition = String.format("{\"spuId\": \"%s\"}", spuId);
        boolean removed=embeddingStoreService.remove(
                new LambdaQueryWrapper<org.example.smartmallbackend.entity.EmbeddingStore>()
                .apply("metadata @> {0}::jsonb", jsonCondition)
        );
        if(removed){
            log.info("完成商品下架向量删除spuId:{}",spuId);
        }else{
            log.warn("未找到对应商品向量进行删除，spuId:{}", spuId);
        }
    }
}
