package com.dinhluong.dlmstore.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dinhluong.dlmstore.entity.ProductCombo;

@Repository
public interface ProductComboRepository extends JpaRepository<ProductCombo, Long> {

    @Query("SELECT pc FROM ProductCombo pc " +
           "JOIN FETCH pc.relatedProduct rp " +
           "WHERE pc.mainProduct.slug = :slug " +
           "AND rp.status = 'ACTIVE'") // Chỉ lấy sản phẩm đang kinh doanh
    List<ProductCombo> findByMainProductSlug(@Param("slug") String slug);

  @Query("""
    SELECT pc
    FROM ProductCombo pc
    JOIN FETCH pc.relatedProduct rp
    WHERE pc.mainProduct.id IN :ids
""")
List<ProductCombo> findByMainProductIds(@Param("ids") List<Long> ids);

// ==============================================================
    // CÁC HÀM BỔ SUNG CHO TRANG QUẢN TRỊ ADMIN (COMBO MANAGER)
    // ==============================================================

    /**
     * Lấy tất cả combo của 1 sản phẩm chính (Dùng trong Modal quản lý combo)
     * Kèm JOIN FETCH để lấy tên và giá của Phụ kiện mà không bị N+1
     */
    @Query("SELECT pc FROM ProductCombo pc " +
           "JOIN FETCH pc.relatedProduct " +
           "WHERE pc.mainProduct.id = :mainProductId")
    List<ProductCombo> findByMainProductId(@Param("mainProductId") Long mainProductId);

    /**
     * Kiểm tra xem 1 phụ kiện đã được thêm vào combo của 1 máy chưa 
     * (Để ngăn Admin thêm trùng lặp cùng 1 phụ kiện)
     */
    boolean existsByMainProductIdAndRelatedProductId(Long mainProductId, Long relatedProductId);
}
