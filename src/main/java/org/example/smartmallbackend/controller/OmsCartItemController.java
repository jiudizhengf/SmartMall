package org.example.smartmallbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.example.smartmallbackend.common.Result;
import org.example.smartmallbackend.entity.OmsCartItem;
import org.example.smartmallbackend.entity.PmsSku;
import org.example.smartmallbackend.entity.PmsSpu;
import org.example.smartmallbackend.service.OmsCartItemService;
import org.example.smartmallbackend.service.PmsSkuService;
import org.example.smartmallbackend.service.PmsSpuService;
import org.example.smartmallbackend.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "购物车管理", description = "购物车项的增删改查接口")
@RestController
@RequestMapping("/api/oms/cart")
@RequiredArgsConstructor
public class OmsCartItemController {
    private final OmsCartItemService omsCartItemService;
    private final PmsSpuService spuService;
    private final PmsSkuService skuService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "添加商品到购物车")
    @PostMapping("/add")
    public Result<?> add(@RequestBody OmsCartItem cartItem) {
        boolean flag= omsCartItemService.addCart(cartItem);
        return flag?Result.success("商品已添加到购物车"):Result.error("添加失败");
    }
    @GetMapping("/list")
    @Operation(summary = "获取用户购物车列表")
    public Result<List<OmsCartItem>> list() {
        List<OmsCartItem> cartItemsList =omsCartItemService.getCartItems();
        return Result.success(cartItemsList);
    }

    @PutMapping("/update/quantity")
    @Operation(summary = "更新购物车商品数量")
    public Result<?> updateQuantity(@RequestParam Long cartItemId, @RequestParam Integer quantity) {
        boolean updated= omsCartItemService.updateCartItemQuantity(cartItemId, quantity);
        return updated?Result.success("商品数量已更新"):Result.error("更新失败");
    }
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除购物车商品")
    public Result<?> delete(@PathVariable Long id) {
        boolean removed= omsCartItemService.deleteCartItem(id);
        return removed?Result.success("商品已从购物车删除"):Result.error("删除失败");
    }
    @Operation(summary = "清空购物车")
    @DeleteMapping("/clear")
    public Result<?> clear() {
        omsCartItemService.clearCart();
        return Result.success("购物车已清空");
    }

}
