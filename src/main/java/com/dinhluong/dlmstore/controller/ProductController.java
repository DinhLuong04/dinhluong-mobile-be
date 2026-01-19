package com.dinhluong.dlmstore.controller;

import java.math.BigDecimal;

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
import com.dinhluong.dlmstore.dto.responses.ProductDetailResponse;
import com.dinhluong.dlmstore.service.ProductService;
import org.springframework.data.domain.Pageable;
@RestController
@RequestMapping("/api/products")
public class ProductController {
    @Autowired
    private  ProductService productService;
    @GetMapping
    public ApiResponse<Page<ProductCardResponse>> getProducts(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String os,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort
    ) {
        // Xử lý Sort
        Sort.Direction direction = Sort.Direction.DESC;
        if (sort.length > 1 && sort[1].equalsIgnoreCase("asc")) {
            direction = Sort.Direction.ASC;
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));

        // Gọi Service
        Page<ProductCardResponse> result = productService.getAllProducts(
                brand, os, minPrice, maxPrice, search, pageable
        );

        // Trả về format chuẩn
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
}
