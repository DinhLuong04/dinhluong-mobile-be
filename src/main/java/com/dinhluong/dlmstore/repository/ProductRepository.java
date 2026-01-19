package com.dinhluong.dlmstore.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import com.dinhluong.dlmstore.entity.Product;
@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Optional<Product> findBySlug(String slug);

    // Tìm kiếm Fulltext (Nếu DB đã đánh index FULLTEXT)
    @Query(value = "SELECT * FROM products p WHERE MATCH(p.name, p.search_keywords) AGAINST (?1)", nativeQuery = true)
    Page<Product> searchByKeyword(String keyword, Pageable pageable);
}
