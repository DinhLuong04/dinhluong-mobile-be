package com.dinhluong.dlmstore.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dinhluong.dlmstore.entity.ProductHighlightSpec;

@Repository
public interface ProductHighlightSpecRepository
        extends JpaRepository<ProductHighlightSpec, Long> {

    List<ProductHighlightSpec> findByProductIdIn(List<Long> productIds);
}