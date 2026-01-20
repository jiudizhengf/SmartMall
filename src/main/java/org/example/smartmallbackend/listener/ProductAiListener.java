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
import org.example.smartmallbackend.service.EmbeddingStoreService;
import org.example.smartmallbackend.service.PmsSkuService;
import org.example.smartmallbackend.service.PmsSpuService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

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
        //构建prompt
        String skuInfo = skuList.stream()
                .map(sku->String.format("%s (价格：%.2f)", sku.getName(),sku.getPrice()))
                .collect(Collectors.joining("; "));
        String textToEmbed = String.format("商品名称：%s\n副标题：%s\n商品描述：%s\n可选规格：%s",
                spu.getName(), spu.getSubTitle(), spu.getDescription(), skuInfo);
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
    public void handleProductOffShelf(ProductOnShelfEvent event) {
        Long spuId = event.getSpuId();
        log.info("开始处理商品下架向量删除spuId:{}",spuId);
        // 从向量库删除
        // 利用 metadata 字段进行模糊匹配删除
        // 存储时的 metadata 类似于: {"spuId": "1001", "brand": "Xiaomi"...}
        boolean removed=embeddingStoreService.remove(new LambdaQueryWrapper<org.example.smartmallbackend.entity.EmbeddingStore>()
                .like(org.example.smartmallbackend.entity.EmbeddingStore::getMetadata, "\"spuId\":\"" + spuId + "\""));
        if(removed){
            log.info("完成商品下架向量删除spuId:{}",spuId);
        }else{
            log.warn("未找到对应商品向量进行删除，spuId:{}", spuId);
        }
    }
}
