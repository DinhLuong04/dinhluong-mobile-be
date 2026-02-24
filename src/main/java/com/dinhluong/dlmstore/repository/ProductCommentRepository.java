package com.dinhluong.dlmstore.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dinhluong.dlmstore.entity.ProductComment;
import com.dinhluong.dlmstore.entity.ProductComment.CommentStatus;

import java.util.List;

@Repository
public interface ProductCommentRepository extends JpaRepository<ProductComment, Long> {
    
    // 1. Lấy các bình luận GỐC (không phải reply) đã được duyệt, phân trang
    Page<ProductComment> findByProductIdAndParentIdIsNullAndStatusOrderByCreatedAtDesc(
            Long productId, CommentStatus status, Pageable pageable);

    // --- ĐÂY LÀ HÀM MỚI THÊM VÀO ĐỂ LỌC THEO SAO ---
    // 2. Lấy bình luận GỐC đã được duyệt, có lọc theo đúng số sao (rating), phân trang
    Page<ProductComment> findByProductIdAndParentIdIsNullAndStatusAndRatingOrderByCreatedAtDesc(
            Long productId, CommentStatus status, Integer rating, Pageable pageable);

    // 3. Lấy tất cả các Reply của danh sách bình luận gốc
    List<ProductComment> findByParentIdInAndStatusOrderByCreatedAtAsc(
            List<Long> parentIds, CommentStatus status);

    // 4. Tính toán số lượng sao (Breakdown)
    @Query("SELECT r.rating as star, COUNT(r) as count FROM ProductComment r " +
           "WHERE r.productId = :productId AND r.parentId IS NULL AND r.status = 'APPROVED' " +
           "GROUP BY r.rating")
    List<Object[]> countRatingsByProductId(@Param("productId") Long productId);
}