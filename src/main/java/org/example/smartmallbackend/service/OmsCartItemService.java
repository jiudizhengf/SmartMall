package org.example.smartmallbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.smartmallbackend.entity.OmsCartItem;

import java.util.List;

public interface OmsCartItemService extends IService<OmsCartItem> {
    boolean addCart(OmsCartItem cartItem);
    void clearCart();
    List<OmsCartItem> getCartItems();
    boolean updateCartItemQuantity(Long cartItemId, Integer quantity);
    boolean deleteCartItem(Long cartItemId);
}
