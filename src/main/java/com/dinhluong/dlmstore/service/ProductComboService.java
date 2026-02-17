package com.dinhluong.dlmstore.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.dinhluong.dlmstore.dto.responses.ProductComboDto;
import com.dinhluong.dlmstore.entity.Product;
import com.dinhluong.dlmstore.entity.ProductCombo;
import com.dinhluong.dlmstore.repository.ProductComboRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductComboService {

    private final ProductComboRepository comboRepository;

    // Hàm tiện ích format tiền tệ (Nên tách ra Utils riêng)
    private String formatCurrency(BigDecimal value) {
        if (value == null) return "0đ";
        return String.format("%,.0fđ", value).replace(",", ".");
    }

    @Transactional
    public List<ProductComboDto> getCombosBySlug(String slug) {
        // 1. Lấy danh sách từ DB
        List<ProductCombo> combos = comboRepository.findByMainProductSlug(slug);

        // 2. Convert sang DTO
        return combos.stream().map(combo -> {
            Product related = combo.getRelatedProduct();
            
            // Giá gốc của sản phẩm phụ
            BigDecimal originalPrice = related.getDisplayPrice();
            
            // Số tiền được giảm
            BigDecimal discount = combo.getDiscountAmount();
            
            // Giá sau khi mua kèm = Giá gốc - Giảm giá
            BigDecimal finalPrice = originalPrice.subtract(discount);

            // Mapping
            ProductComboDto dto = new ProductComboDto();
            dto.setId(combo.getId());
            dto.setRelatedProductId(related.getId());
            dto.setName(related.getName());
            dto.setImage(related.getThumbnailUrl());
            
            // Format hiển thị
            dto.setOldPrice(formatCurrency(originalPrice)); // VD: 500.000đ
            dto.setPrice(formatCurrency(finalPrice));       // VD: 450.000đ
            dto.setSaving("Tiết kiệm: " + formatCurrency(discount)); // VD: Tiết kiệm: 50.000đ
            
            // Data thô
            dto.setRawPrice(finalPrice);
            dto.setRawDiscount(discount);

            return dto;
        }).collect(Collectors.toList());
    }
}
