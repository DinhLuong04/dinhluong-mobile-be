package com.dinhluong.dlmstore.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dinhluong.dlmstore.entity.ProductImage;

@Repository
public interface ProductImageRepository
        extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProductIdIn(List<Long> productIds);
}
