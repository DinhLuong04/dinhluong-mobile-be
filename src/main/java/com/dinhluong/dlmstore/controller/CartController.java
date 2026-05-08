package com.dinhluong.dlmstore.controller;

import com.dinhluong.dlmstore.dto.ApiResponse;
import com.dinhluong.dlmstore.dto.requests.AddToCartRequest;
import com.dinhluong.dlmstore.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/add/{userId}")
    public ApiResponse<?> addToCart(
            @PathVariable Long userId,
            @RequestBody AddToCartRequest request) {

        cartService.addToCart(userId, request);
        return ApiResponse.success("Thêm vào giỏ hàng thành công", null);
    }

    @GetMapping("/{userId}")
    public ApiResponse<?> getCart(@PathVariable Long userId) {
        return ApiResponse.success("Lấy giỏ hàng thành công", cartService.getCartByUserId(userId));
    }

    @PutMapping("/update/{cartItemId}")
    public ApiResponse<?> updateQuantity(
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity) {

        cartService.updateQuantity(cartItemId, quantity);
        return ApiResponse.success("Cập nhật số lượng thành công", null);
    }

    @DeleteMapping("/remove/{cartItemId}")
    public ApiResponse<?> removeItem(@PathVariable Long cartItemId) {
        cartService.removeItem(cartItemId);
        return ApiResponse.success("Xóa sản phẩm thành công", null);
    }
}