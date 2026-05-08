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

        // Lấy các bình luận GỐC (không phải reply) đã được duyệt, phân trang
        Page<ProductComment> findByProductIdAndParentIdIsNullAndStatusOrderByCreatedAtDesc(
                        Long productId, CommentStatus status, Pageable pageable);

        // --- ĐÂY LÀ HÀM MỚI THÊM VÀO ĐỂ LỌC THEO SAO ---
        // Lấy bình luận GỐC đã được duyệt, có lọc theo đúng số sao (rating), phân trang
        Page<ProductComment> findByProductIdAndParentIdIsNullAndStatusAndRatingOrderByCreatedAtDesc(
                        Long productId, CommentStatus status, Integer rating, Pageable pageable);

        // Lấy tất cả các Reply của danh sách bình luận gốc
        List<ProductComment> findByParentIdInAndStatusOrderByCreatedAtAsc(
                        List<Long> parentIds, CommentStatus status);

        // Tính toán số lượng sao (Breakdown)
        @Query("SELECT r.rating as star, COUNT(r) as count FROM ProductComment r " +
                        "WHERE r.productId = :productId AND r.parentId IS NULL AND r.status = 'APPROVED' " +
                        "GROUP BY r.rating")
        List<Object[]> countRatingsByProductId(@Param("productId") Long productId);

        // THÊM MỚI CHO ADMIN
        @Query("SELECT c FROM ProductComment c WHERE " +
                        "c.parentId IS NULL AND " +
                        "(:status IS NULL OR c.status = :status) AND " +
                        "(:keyword IS NULL OR :keyword = '' OR LOWER(c.content) LIKE LOWER(CONCAT('%', :keyword, '%')) "
                        +
                        "OR LOWER(c.authorName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                        "ORDER BY c.createdAt DESC")
        Page<ProductComment> searchAdminComments(@Param("keyword") String keyword,
                        @Param("status") CommentStatus status, Pageable pageable);

        // THÊM MỚI CHO ADMIN: Lấy tất cả reply của 1 list parent (không quan tâm
        // status)
        List<ProductComment> findByParentIdInOrderByCreatedAtAsc(List<Long> parentIds);
}