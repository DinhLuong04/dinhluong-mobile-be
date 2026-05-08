package com.dinhluong.dlmstore.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dinhluong.dlmstore.entity.Product;
import com.dinhluong.dlmstore.entity.Enums.ProductStatus;
import com.dinhluong.dlmstore.entity.Enums.ProductType;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

       Optional<Product> findBySlug(String slug);

       @Query("""
                         SELECT p FROM Product p
                         WHERE p.status = 'ACTIVE' AND p.productType='MAIN'
                         AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                         LOWER(p.searchKeywords) LIKE LOWER(CONCAT('%', :keyword, '%')))
                     """)
       Page<Product> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

       List<Product> findBySlugIn(List<String> slugs);

       // CẬP NHẬT QUERY NÀY
       @Query("SELECT p FROM Product p WHERE " +
                     "(:productType IS NULL OR p.productType = :productType) AND " +
                     "(:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
                     "(:status IS NULL OR p.status = :status) AND " +
                     "(:brandId IS NULL OR p.brand.id = :brandId) AND " + 
                     "(:categoryId IS NULL OR p.category.id = :categoryId) AND " + 
                     "p.isDeleted = false") 
       Page<Product> findWithFilters(
                     @Param("productType") ProductType productType,
                     @Param("keyword") String keyword,
                     @Param("status") ProductStatus status,
                     @Param("brandId") Long brandId, // <--- Thêm tham số
                     @Param("categoryId") Long categoryId, // <--- Thêm tham số
                     Pageable pageable);

       long countByIsFeaturedTrue();

       @Query("SELECT p FROM Product p " +
                     "WHERE p.isFeatured = true " +
                     "AND p.status = 'ACTIVE' " +
                     "ORDER BY p.soldQuantity DESC")
       List<Product> findFeaturedProducts(Pageable pageable);

}