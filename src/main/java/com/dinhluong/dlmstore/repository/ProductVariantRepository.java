package com.dinhluong.dlmstore.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import com.dinhluong.dlmstore.entity.ProductVariant;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    Optional<ProductVariant> findBySku(String sku);

    List<ProductVariant> findTop10ByStockQuantityLessThanOrderByStockQuantityAsc(Integer threshold);

    List<ProductVariant> findByProductIdIn(List<Long> productIds);

    @Query("SELECT COUNT(v) FROM ProductVariant v WHERE v.stockQuantity <= 0 OR v.stockQuantity IS NULL")
long countOutOfStockVariants();

@Query("SELECT COUNT(v) FROM ProductVariant v WHERE v.stockQuantity > 0 AND v.stockQuantity < 5")
long countLowStockVariants();
    boolean existsBySku(String sku);

    // 2. Dùng khi CẬP NHẬT: Check xem có SKU này chưa, nhưng BỎ QUA chính cái Variant đang sửa (dựa vào id)
    boolean existsBySkuAndIdNot(String sku, Long id);
    @Query("SELECT COUNT(pv) > 0 FROM ProductVariant pv WHERE pv.id = :id AND pv.product.isDeleted = false")
    boolean existsAndActive(@Param("id") Long id);
}