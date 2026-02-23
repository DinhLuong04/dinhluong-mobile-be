package com.dinhluong.dlmstore.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dinhluong.dlmstore.dto.ApiResponse;
import com.dinhluong.dlmstore.dto.responses.ProductCardResponse;
import com.dinhluong.dlmstore.dto.responses.ProductComboDto;
import com.dinhluong.dlmstore.dto.responses.ProductDetailResponse;
import com.dinhluong.dlmstore.service.ProductComboService;
import com.dinhluong.dlmstore.service.ProductService;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    @Autowired
    private ProductService productService;
    @Autowired
    private ProductComboService comboService;
    @GetMapping
    public ApiResponse<Page<ProductCardResponse>> getProducts(
            // 1. Tìm kiếm & Cơ bản
            @RequestParam(required = false) String search,
            @RequestParam(name = "category", required = false) String categorySlug,

            // 2. Bộ lọc danh sách (Multi-select Checkbox) -> Dùng List<String>
            @RequestParam(required = false) List<String> brands, // VD: ?brands=Samsung&brands=Oppo
            @RequestParam(required = false) List<String> os, // VD: ?os=Android
            @RequestParam(required = false) List<String> roms, // VD: ?roms=128 GB
            @RequestParam(required = false) List<String> rams, // VD: ?rams=8 GB
            @RequestParam(required = false) List<String> networks, // VD: ?networks=5G

            // 3. Bộ lọc khoảng (Ranges)
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer minBattery,
            @RequestParam(required = false) Integer maxBattery,     // <-- THÊM MỚI
            @RequestParam(required = false) Double minScreenSize,
            @RequestParam(required = false) Double maxScreenSize,   // <-- THÊM MỚI
            @RequestParam(required = false) Integer minRefreshRate,
            @RequestParam(required = false) Integer maxRefreshRate, // <-- THÊM MỚI (Cho range nếu cần)
            // 4. Phân trang & Sắp xếp
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {
        // Xử lý Sort
        Sort.Direction direction = Sort.Direction.DESC;
        if (sort.length > 1 && sort[1].equalsIgnoreCase("asc")) {
            direction = Sort.Direction.ASC;
        }

        // Lưu ý: sort[0] phải khớp với tên biến trong Entity (ví dụ: displayPrice thay
        // vì display_price)
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));

        // Gọi Service với đầy đủ tham số mới
        Page<ProductCardResponse> result = productService.getAllProducts(
                categorySlug,
                brands,
                os,
                roms,
                rams,
                networks,
                minPrice,
                maxPrice,
                minBattery, maxBattery,         
                minScreenSize, maxScreenSize,   
                minRefreshRate, maxRefreshRate, 
                search,
                pageable);

        return ApiResponse.success("Lấy danh sách sản phẩm thành công", result);
    }

    /**
     * API: Xem chi tiết sản phẩm
     * GET /api/v1/products/{slug}
     */
    @GetMapping("/{slug}")
    public ApiResponse<ProductDetailResponse> getProductDetail(@PathVariable String slug) {
        ProductDetailResponse result = productService.getProductBySlug(slug);

        return ApiResponse.success("Lấy chi tiết sản phẩm thành công", result);
    }

    @GetMapping("/{slug}/combos")
    public ApiResponse<List<ProductComboDto>> getProductCombo(@PathVariable String slug) {
        List<ProductComboDto> result = comboService.getCombosBySlug(slug);
        return ApiResponse.success("Danh sách combo đi kèm", result);
    }


    @GetMapping("/batch")
    public ApiResponse<List<ProductDetailResponse>> getProductsBySlugs(@RequestParam List<String> slugs) {
        List<ProductDetailResponse> results = productService.getProductsBySlugs(slugs);
        return ApiResponse.success("Lấy danh sách sản phẩm thành công", results);
    }

    @GetMapping("/update-keywordSearch")
    public ApiResponse<?> UpdateKeyWordSeacrh() {
        productService.updateAllProductKeywords();
        return ApiResponse.success("Cập nhật thành công danh sách sản phẩm thành công", null);
    }
}
