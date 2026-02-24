package com.dinhluong.dlmstore.controller;

import com.dinhluong.dlmstore.dto.ApiResponse;
import com.dinhluong.dlmstore.dto.responses.ReviewResponse;
import com.dinhluong.dlmstore.security.CustomUserPrincipal;
import com.dinhluong.dlmstore.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    // 1. LẤY BÌNH LUẬN (CÓ THỂ LỌC THEO SAO)
    @GetMapping("/products/{slug}")
    public ResponseEntity<ApiResponse<ReviewResponse>> getReviews(
            @PathVariable String slug,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) Integer rating,
        @AuthenticationPrincipal CustomUserPrincipal currentUser) { 
        
        Long currentUserId = (currentUser != null) ? currentUser.getId() : null;

    // Truyền thêm currentUserId vào hàm service
    ReviewResponse response = reviewService.getProductReviewsBySlug(slug, rating, page, limit, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đánh giá thành công", response));
    }

    // 2. GỬI BÌNH LUẬN GỐC HOẶC PHẢN HỒI (REPLY)
    @PostMapping("")
    public ResponseEntity<ApiResponse<Object>> createReview(
            @RequestParam("product_slug") String productSlug,
            @RequestParam(value = "rating", required = false) Integer rating,
            @RequestParam("content") String content,
            @RequestParam(value = "parent_id", required = false) Long parentId,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal CustomUserPrincipal currentUser
    ) {
        try {
            Long currentUserId = currentUser.getId(); 

            reviewService.submitReviewBySlug(productSlug, rating, content, currentUserId, files, parentId);
            
            return ResponseEntity.ok(ApiResponse.success("Đánh giá của bạn đã được gửi thành công!", null));
            
        } catch (Exception e) {
            e.printStackTrace(); 
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Lỗi khi gửi đánh giá: " + e.getMessage()));
        }
    }
}