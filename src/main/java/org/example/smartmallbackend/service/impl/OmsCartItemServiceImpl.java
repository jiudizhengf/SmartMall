package org.example.smartmallbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.smartmallbackend.common.BusinessException;
import org.example.smartmallbackend.common.Result;
import org.example.smartmallbackend.context.UserContext;
import org.example.smartmallbackend.entity.OmsCartItem;
import org.example.smartmallbackend.entity.PmsSku;
import org.example.smartmallbackend.entity.PmsSpu;
import org.example.smartmallbackend.mapper.OmsCartItemMapper;
import org.example.smartmallbackend.service.OmsCartItemService;
import org.example.smartmallbackend.service.PmsSkuService;
import org.example.smartmallbackend.service.PmsSpuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OmsCartItemServiceImpl extends ServiceImpl<OmsCartItemMapper, OmsCartItem>
        implements OmsCartItemService {
    @Autowired
    private PmsSpuService spuService;

    @Autowired
    private PmsSkuService skuService;

    @Override
    public boolean addCart(OmsCartItem cartItem) {
        Long userId = UserContext.getUserId();
        if (cartItem.getSkuId() == null || cartItem.getQuantity() == null) {
            throw new BusinessException("参数不合法");
        }
        // 校验 购物车是否有该商品
        OmsCartItem existingItem = this.getOne(new LambdaQueryWrapper<OmsCartItem>()
                .eq(OmsCartItem::getUserId, userId)
                .eq(OmsCartItem::getSkuId, cartItem.getSkuId()));
        if (existingItem != null) {
            // 有则增加数量
            existingItem.setQuantity(existingItem.getQuantity() + cartItem.getQuantity());
            this.updateById(existingItem);
            return true;
        } else {
            // 无则新增
            // 填充其他商品信息
            PmsSku sku = skuService.getById(cartItem.getSkuId());
            if (sku == null) {
                return false;
            }
            PmsSpu spu = spuService.getById(cartItem.getSpuId());
            OmsCartItem newItem = new OmsCartItem();
            newItem.setUserId(userId);
            newItem.setSkuId(sku.getId());
            newItem.setSpuId(sku.getSpuId());
            // 组合商品名称
            String fullName = (spu != null ? spu.getName() + " " : "") + sku.getName();
            newItem.setSpuName(fullName);
            newItem.setSkuPic(sku.getPicUrl());
            newItem.setSkuPrice(sku.getPrice());
            newItem.setQuantity(cartItem.getQuantity());
            newItem.setSkuAttrs(sku.getSpecData());
            return this.save(newItem);
        }
    }

    @Override
    public void clearCart() {
        Long userId = UserContext.getUserId();
        this.remove(new LambdaQueryWrapper<OmsCartItem>().eq(OmsCartItem::getUserId, userId));
    }

    @Override
    public List<OmsCartItem> getCartItems() {
        Long userId = UserContext.getUserId();
        return this.list(new LambdaQueryWrapper<OmsCartItem>()
                .eq(OmsCartItem::getUserId, userId)
                .orderByDesc(OmsCartItem::getCreateTime));
    }

    @Override
    public boolean updateCartItemQuantity(Long cartItemId, Integer quantity) {
        Long userId = UserContext.getUserId();
        OmsCartItem cartItem = this.getById(cartItemId);
        //权限校验
        if (cartItem == null || !cartItem.getUserId().equals(userId)) {
            return false;
        }
        cartItem.setQuantity(quantity);
        return this.updateById(cartItem);
    }

    @Override
    public boolean deleteCartItem(Long cartItemId) {
        Long userId = UserContext.getUserId();
        OmsCartItem cartItem = this.getById(cartItemId);
        //权限校验
        if (cartItem == null || !cartItem.getUserId().equals(userId)) {
            return false;
        }
        return this.removeById(cartItemId);
    }
}
