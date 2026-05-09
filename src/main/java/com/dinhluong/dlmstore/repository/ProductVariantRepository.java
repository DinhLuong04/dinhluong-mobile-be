package com.dinhluong.dlmstore.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import com.dinhluong.dlmstore.entity.ProductVariant;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    Optional<ProductVariant> findBySku(String sku);

    List<ProductVariant> findTop10ByStockQuantityLessThanOrderByStockQuantityAsc(Integer threshold);

    List<ProductVariant> findByProductIdIn(List<Long> productIds);
}