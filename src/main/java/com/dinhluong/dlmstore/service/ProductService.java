package com.dinhluong.dlmstore.service;


import com.dinhluong.dlmstore.dto.responses.ProductCardResponse;
import com.dinhluong.dlmstore.dto.responses.ProductDetailResponse;
import com.dinhluong.dlmstore.entity.Product;
import com.dinhluong.dlmstore.entity.Enums.OsType;
import com.dinhluong.dlmstore.entity.Enums.ProductStatus;
import com.dinhluong.dlmstore.mapper.ProductMapper;
import com.dinhluong.dlmstore.repository.ProductRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    @Autowired
    private ProductMapper productMapper;

    // 1. Lấy danh sách sản phẩm (Trả về ProductCardResponse chuẩn JSON Frontend cần)
    @Transactional(readOnly = true)
    public Page<ProductCardResponse> getAllProducts(
            String brand,
            String osType,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String search,
            Pageable pageable) {

        // A. Ưu tiên tìm kiếm Fulltext nếu có từ khóa
        if (StringUtils.hasText(search)) {
            return productRepository.searchByKeyword(search, pageable)
                    .map(productMapper::toCardResponse); // Gọi mapper xử lý logic gom màu/variant
        }

        // B. Nếu không tìm kiếm -> Dùng bộ lọc Specification
        Specification<Product> spec = Specification.where(null);

        // 1. Lọc theo Brand (Tên hãng)
        if (StringUtils.hasText(brand)) {
            spec = spec.and((root, query, cb) -> 
                cb.equal(root.get("brand").get("name"), brand));
        }

        // 2. Lọc theo OS (IOS, ANDROID...)
        if (StringUtils.hasText(osType)) {
            try {
                // Chuyển string sang Enum an toàn
                OsType osEnum = OsType.valueOf(osType.toUpperCase());
                spec = spec.and((root, query, cb) -> cb.equal(root.get("osType"), osEnum));
            } catch (IllegalArgumentException e) {
                // Nếu client gửi osType linh tinh (vd: "windowphone") -> Bỏ qua hoặc return rỗng
                // Ở đây chọn cách bỏ qua filter này
            }
        }

        // 3. Lọc theo khoảng giá (>= minPrice)
        if (minPrice != null) {
            spec = spec.and((root, query, cb) -> 
                cb.greaterThanOrEqualTo(root.get("displayPrice"), minPrice));
        }

        // 4. Lọc theo khoảng giá (<= maxPrice)
        if (maxPrice != null) {
            spec = spec.and((root, query, cb) -> 
                cb.lessThanOrEqualTo(root.get("displayPrice"), maxPrice));
        }
        
        // 5. Chỉ lấy sản phẩm đang ACTIVE (Quan trọng)
        spec = spec.and((root, query, cb) -> 
             cb.equal(root.get("status"), ProductStatus.ACTIVE));

        // C. Query và Map sang DTO
        return productRepository.findAll(spec, pageable)
                .map(productMapper::toCardResponse); // MapStruct sẽ tự chạy logic @AfterMapping tính toán discount/color
    }

    // 2. Lấy chi tiết sản phẩm theo Slug (Giữ nguyên)
    @Transactional(readOnly = true)
    public ProductDetailResponse getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại: " + slug));

        return productMapper.toDetailResponse(product);
    }

    public List<ProductDetailResponse> getProductsBySlugs(List<String> slugs) {
    // Giả sử bạn dùng JPA Repository
    List<Product> products = productRepository.findBySlugInList(slugs);
    
    // Map từ Entity sang DTO
    return products.stream()
            .map(productMapper::toDetailResponse)
            .collect(Collectors.toList());
}
}