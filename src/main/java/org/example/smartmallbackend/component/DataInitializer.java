package org.example.smartmallbackend.component;

import cn.hutool.core.util.RandomUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.example.smartmallbackend.controller.PmsSpuController;
import org.example.smartmallbackend.entity.*;
import org.example.smartmallbackend.service.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * 旗舰版数据初始化脚本
 * 特性：使用笛卡尔积算法自动生成大量多规格 SKU
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final PmsSpuService spuService;
    private final PmsSkuService skuService;
    private final UmsUserService userService;
    private final SmsHomeAdvertiseService advertiseService;
    private final PmsSpuController spuController;
    private final PasswordEncoder passwordEncoder;

    // 规格定义工具类
    @lombok.Data
    @lombok.AllArgsConstructor
    static class SpecOption {
        String key;         // 规格名，如 "color"
        String label;       // 规格显示名，如 "颜色"
        List<String> values;// 规格值，如 ["黑色", "白色"]
        BigDecimal priceAdd;// 该维度每升一级增加的价格
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(String... args) throws Exception {
        // 强制清理旧数据（慎用：为了演示效果，这里会先清空商品表）
        // if (spuService.count() > 0) { ... } // 如果不想清空，请恢复这个判断

        log.info(">>>> 🚀 开始生成旗舰版测试数据 (海量SKU模式)...");

        initBasicData();

        // 生成多规格商品
        initComplexProducts();

        log.info(">>>> ✅ 数据生成完毕！请查看数据库或前端页面。");
    }

    private void initComplexProducts() {
        // 1. iPhone 15 (颜色 x 存储 = 5x3 = 15个SKU)
        generateProductWithSpecs(
                "iPhone 15", "A16芯片 | 灵动岛", "Apple", 1L,
                "配备灵动岛，4800万像素主摄，2倍光学变焦。A16 仿生芯片，全天候电池续航。",
                new BigDecimal("5999"),
                Arrays.asList(
                        new SpecOption("color", "颜色", Arrays.asList("黑色", "蓝色", "绿色", "黄色", "粉色"), BigDecimal.ZERO),
                        new SpecOption("storage", "存储", Arrays.asList("128GB", "256GB", "512GB"), new BigDecimal("1000"))
                )
        );

        // 2. MacBook Pro 16 (芯片 x 内存 x 存储 = 2x2x2 = 8个SKU)
        generateProductWithSpecs(
                "MacBook Pro 16", "M3 Max | 极致生产力", "Apple", 1L,
                "地球上最强的笔记本电脑。Liquid 视网膜 XDR 显示屏，长达 22 小时电池续航。",
                new BigDecimal("19999"),
                Arrays.asList(
                        new SpecOption("chip", "芯片", Arrays.asList("M3 Pro", "M3 Max"), new BigDecimal("4000")),
                        new SpecOption("ram", "内存", Arrays.asList("18GB", "36GB"), new BigDecimal("3000")),
                        new SpecOption("storage", "硬盘", Arrays.asList("512GB", "1TB"), new BigDecimal("1500"))
                )
        );

        // 3. Nike Air Jordan 1 (颜色 x 尺码 = 2x6 = 12个SKU)
        generateProductWithSpecs(
                "Air Jordan 1 Low", "经典复刻 | 芝加哥配色", "Nike", 2L,
                "经典永不过时。采用真皮材质，后跟内嵌 Air 缓震配置，塑就轻盈舒适的迈步体验。",
                new BigDecimal("899"),
                Arrays.asList(
                        new SpecOption("color", "配色", Arrays.asList("芝加哥红", "北卡蓝"), BigDecimal.ZERO),
                        new SpecOption("size", "尺码", Arrays.asList("39", "40", "41", "42", "43", "44"), BigDecimal.ZERO)
                )
        );

        // 4. Keychron Q1 Pro (轴体 x 颜色 = 3x3 = 9个SKU)
        generateProductWithSpecs(
                "Keychron Q1 Pro", "铝坨坨 | 蓝牙双模", "Keychron", 3L,
                "全铝客制化机械键盘，支持 QMK/VIA 改键。Gasket 结构，手感软弹温润。",
                new BigDecimal("998"),
                Arrays.asList(
                        new SpecOption("color", "外壳", Arrays.asList("碳黑", "银灰", "海军蓝"), BigDecimal.ZERO),
                        new SpecOption("switch", "轴体", Arrays.asList("红轴", "茶轴", "香蕉轴"), new BigDecimal("50"))
                )
        );

        // 5. Uniqlo 卫衣 (颜色 x 尺码 = 4x4 = 16个SKU)
        generateProductWithSpecs(
                "Uniqlo 连帽卫衣", "重磅纯棉 | 宽松版型", "Uniqlo", 4L,
                "质感厚实的运动衫，内里起绒，肌肤触感舒适。落肩袖设计，更显休闲。",
                new BigDecimal("199"),
                Arrays.asList(
                        new SpecOption("color", "颜色", Arrays.asList("灰色", "黑色", "藏青", "米色"), BigDecimal.ZERO),
                        new SpecOption("size", "尺码", Arrays.asList("S", "M", "L", "XL"), BigDecimal.ZERO)
                )
        );

        // 6. Xiaomi 14 Ultra (颜色 x 存储 = 3x3 = 9个SKU)
        generateProductWithSpecs(
                "Xiaomi 14 Ultra", "徕卡四摄 | 骁龙8Gen3", "Xiaomi", 1L,
                "小米年度影像旗舰，一英寸无级可变光圈。双向卫星通信，小米龙铠架构。",
                new BigDecimal("6499"),
                Arrays.asList(
                        new SpecOption("color", "颜色", Arrays.asList("黑色", "白色", "龙晶蓝"), BigDecimal.ZERO),
                        new SpecOption("storage", "版本", Arrays.asList("12+256GB", "16+512GB", "16+1TB"), new BigDecimal("500"))
                )
        );
    }

    /**
     * 核心算法：生成多规格 SKU (笛卡尔积)
     */
    @SneakyThrows
    private void generateProductWithSpecs(String name, String subTitle, String brand, Long catId, String desc,
                                          BigDecimal basePrice, List<SpecOption> specs) {
        // 1. 创建 SPU
        Long spuId = createSpu(name, subTitle, brand, catId, desc, basePrice);

        // 2. 递归生成 SKU 列表
        List<Map<String, String>> skuCombinations = new ArrayList<>();
        generateCartesianProduct(specs, 0, new LinkedHashMap<>(), skuCombinations);

        // 3. 遍历组合，保存 SKU
        int index = 0;
        for (Map<String, String> combination : skuCombinations) {
            // 构建 SKU 名称 (如: iPhone 15 黑色 128GB)
            StringBuilder skuName = new StringBuilder(name);
            BigDecimal currentPrice = basePrice;

            // 计算价格：叠加每个规格的加价
            for (SpecOption spec : specs) {
                String value = combination.get(spec.key);
                skuName.append(" ").append(value);

                // 简单的价格算法：找到这个值是列表里的第几个，乘以增量
                int valIndex = spec.values.indexOf(value);
                if (valIndex > 0 && spec.priceAdd.compareTo(BigDecimal.ZERO) > 0) {
                    currentPrice = currentPrice.add(spec.priceAdd.multiply(new BigDecimal(valIndex)));
                }
            }

            // 序列化规格 JSON
            String specDataJson = new ObjectMapper().writeValueAsString(combination);

            // 图片 (模拟不同颜色用不同图，实际需真实URL)
            String pic = "https://via.placeholder.com/300x300?text=" + name.replaceAll(" ", "+") + "+" + index;

            createSku(spuId, "SKU-" + spuId + "-" + index, skuName.toString(), currentPrice,
                    RandomUtil.randomInt(10, 200), specDataJson, pic);
            index++;
        }

        log.info("商品 [{}] 生成完成，共 {} 个 SKU", name, index);

        // 4. 上架 (触发AI)
        safePublish(spuId);
    }

    /**
     * 递归实现笛卡尔积
     */
    private void generateCartesianProduct(List<SpecOption> specs, int depth,
                                          Map<String, String> current,
                                          List<Map<String, String>> result) {
        if (depth == specs.size()) {
            result.add(new LinkedHashMap<>(current)); // 必须拷贝
            return;
        }

        SpecOption currentSpec = specs.get(depth);
        for (String val : currentSpec.values) {
            current.put(currentSpec.key, val);
            generateCartesianProduct(specs, depth + 1, current, result);
            current.remove(currentSpec.key); // 回溯
        }
    }

    // --- 基础辅助方法 ---

    private void safePublish(Long spuId) {
        try {
            spuController.publish(spuId);
            Thread.sleep(100);
        } catch (Exception e) {
            log.error("上架失败", e);
        }
    }

    private Long createSpu(String name, String subTitle, String brand, Long catId, String desc, BigDecimal price) {
        PmsSpu spu = new PmsSpu();
        spu.setName(name);
        spu.setSubTitle(subTitle);
        spu.setBrandName(brand);
        spu.setCategoryId(catId);
        spu.setDescription(desc);
        spu.setPrice(price);
        spu.setPublishStatus(0);
        spuService.save(spu);
        return spu.getId();
    }

    private void createSku(Long spuId, String code, String name, BigDecimal price, Integer stock, String specs, String pic) {
        PmsSku sku = new PmsSku();
        sku.setSpuId(spuId);
        sku.setSkuCode(code);
        sku.setName(name);
        sku.setPrice(price);
        sku.setStock(stock);
        sku.setSpecData(specs);
        sku.setPicUrl(pic);
        skuService.save(sku);
    }

    private void initBasicData() {
        if (userService.count() == 0) {
            UmsUser user = new UmsUser();
            user.setUsername("test");
            user.setPassword(passwordEncoder.encode("123456"));
            user.setNickName("TestUser");
            userService.save(user);
        }
        if (advertiseService.count() == 0) {
            SmsHomeAdvertise ad = new SmsHomeAdvertise();
            ad.setName("开学季");
            ad.setPic("https://via.placeholder.com/800x400");
            ad.setStatus(1);
            advertiseService.save(ad);
        }
    }
}