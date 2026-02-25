package com.dinhluong.dlmstore.controller.Admin;

import com.dinhluong.dlmstore.dto.ApiResponse;
import com.dinhluong.dlmstore.dto.requests.AdminReplyRequest;
import com.dinhluong.dlmstore.dto.responses.AdminCommentResponse;
import com.dinhluong.dlmstore.entity.ProductComment;
import com.dinhluong.dlmstore.service.AdminReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {

    private final AdminReviewService adminReviewService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AdminCommentResponse>>> getReviews(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ProductComment.CommentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminCommentResponse> reviews = adminReviewService.getAdminReviews(keyword, status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách bình luận thành công", reviews));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<String>> updateStatus(
            @PathVariable Long id, 
            @RequestParam ProductComment.CommentStatus status) {
        try {
            adminReviewService.updateReviewStatus(id, status);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái thành công", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @PostMapping("/{id}/reply")
    public ResponseEntity<ApiResponse<String>> replyToReview(
            @PathVariable Long id, 
            @RequestBody AdminReplyRequest request) {
        try {
            adminReviewService.replyToReview(id, request);
            return ResponseEntity.ok(ApiResponse.success("Phản hồi bình luận thành công", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteReview(@PathVariable Long id) {
        try {
            adminReviewService.deleteReview(id);
            return ResponseEntity.ok(ApiResponse.success("Xóa bình luận thành công", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }
}