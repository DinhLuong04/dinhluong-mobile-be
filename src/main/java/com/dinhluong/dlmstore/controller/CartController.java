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

    // 1. Thêm sản phẩm vào giỏ hàng
    @PostMapping("/add/{userId}")
    public ApiResponse<?> addToCart(
            @PathVariable Long userId, // Hứng userId từ URL
            @RequestBody AddToCartRequest request) {
        
        cartService.addToCart(userId, request);
        return ApiResponse.success("Thêm vào giỏ hàng thành công", null);
    }

    // 2. Lấy danh sách sản phẩm trong giỏ hàng
    @GetMapping("/{userId}")
    public ApiResponse<?> getCart(@PathVariable Long userId) {
        // Bạn sẽ cần tạo CartResponse DTO, ở đây mình trả thẳng ra Object từ Service
        return ApiResponse.success("Lấy giỏ hàng thành công", cartService.getCartByUserId(userId));
    }

    // 3. Cập nhật số lượng sản phẩm (ấn + / -)
    @PutMapping("/update/{cartItemId}")
    public ApiResponse<?> updateQuantity(
            @PathVariable Long cartItemId, 
            @RequestParam Integer quantity) {
        
        cartService.updateQuantity(cartItemId, quantity);
        return ApiResponse.success("Cập nhật số lượng thành công", null);
    }

    // 4. Xóa sản phẩm khỏi giỏ hàng
    @DeleteMapping("/remove/{cartItemId}")
    public ApiResponse<?> removeItem(@PathVariable Long cartItemId) {
        cartService.removeItem(cartItemId);
        return ApiResponse.success("Xóa sản phẩm thành công", null);
    }
}