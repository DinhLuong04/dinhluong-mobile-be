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

       @Query("SELECT COUNT(p) FROM Product p WHERE p.status = 'ACTIVE' AND p.isDeleted = false")
long countActiveProducts();

@Query("SELECT COUNT(p) FROM Product p WHERE p.status = 'INACTIVE' AND p.isDeleted = false")
long countInactiveProducts();

       // Trong ProductRepository.java
@Query("SELECT p FROM Product p WHERE " +
       "(:productType IS NULL OR p.productType = :productType) AND " +
       "(:status IS NULL OR p.status = :status) AND " +
       "(:brandId IS NULL OR p.brand.id = :brandId) AND " +
       "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
       "(:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
List<Product> findAllWithFilters(@Param("productType") ProductType productType, 
                                 @Param("keyword") String keyword, 
                                 @Param("status") ProductStatus status, 
                                 @Param("brandId") Long brandId, 
                                 @Param("categoryId") Long categoryId);
       Optional<Product> findBySlug(String slug);

       @Query("""
                         SELECT p FROM Product p
                         WHERE p.status = 'ACTIVE' AND p.productType='MAIN'
                         AND p.isDeleted = false
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
              
    @Query("""
    SELECT DISTINCT p
    FROM Product p
    LEFT JOIN FETCH p.specValues sv
    LEFT JOIN FETCH sv.attribute a
    LEFT JOIN FETCH a.group
""")
List<Product> findAllForRebuild();
      boolean existsBySlug(String slug);
       boolean existsByCategoryId(Long categoryId);
       boolean existsByBrandId(Long brandId);
Optional<Product> findByName(String name);

Optional<Product> findBySlugAndStatusAndIsDeletedFalse(String slug, ProductStatus status);

List<Product> findBySlugInAndStatusAndIsDeletedFalse(List<String> slugs, ProductStatus status);
}