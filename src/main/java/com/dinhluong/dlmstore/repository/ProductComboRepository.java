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
}
