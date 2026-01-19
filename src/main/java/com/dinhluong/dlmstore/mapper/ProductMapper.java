package com.dinhluong.dlmstore.mapper;

import com.dinhluong.dlmstore.dto.responses.ProductCardResponse;
import com.dinhluong.dlmstore.dto.responses.ProductDetailResponse;
import com.dinhluong.dlmstore.entity.Product;
import com.dinhluong.dlmstore.entity.ProductHighlightSpec;
import com.dinhluong.dlmstore.entity.ProductImage;
import com.dinhluong.dlmstore.entity.ProductSpecGroup;
import com.dinhluong.dlmstore.entity.ProductSpecItem;
import com.dinhluong.dlmstore.entity.ProductVariant;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    // ====================================================
    // 1. MAPPING CHO CARD (DANH SÁCH)
    // ====================================================
    @Mapping(source = "slug", target = "id")
    @Mapping(source = "thumbnailUrl", target = "image")
    @Mapping(source = "displayPrice", target = "price")
    @Mapping(source = "highlightSpecs", target = "specs")
    @Mapping(target = "discountNote", ignore = true)
    @Mapping(target = "colors", ignore = true)
    @Mapping(target = "variants", ignore = true)
    @Mapping(target = "promotions", ignore = true)
    @Mapping(target = "promotionText", ignore = true)
    ProductCardResponse toCardResponse(Product product);

    // Map: Highlight Spec -> DTO
    @Mapping(source = "iconUrl", target = "icon")
    @Mapping(source = "value", target = "subLabel")
    ProductCardResponse.SpecDto toSpecDto(ProductHighlightSpec spec);

    // --- LOGIC XỬ LÝ RIÊNG CHO CARD (BẠN ĐANG THIẾU CÁI NÀY) ---
    @AfterMapping
    default void calculateCardFields(Product product, @MappingTarget ProductCardResponse response) {
        // 1. Tính Discount Note
        if (product.getOriginalPrice() != null && product.getDisplayPrice() != null) {
            BigDecimal diff = product.getOriginalPrice().subtract(product.getDisplayPrice());
            if (diff.compareTo(BigDecimal.ZERO) > 0) {
                DecimalFormat formatter = new DecimalFormat("#,###");
                response.setDiscountNote("Giảm " + formatter.format(diff) + "đ");
            }
        }

        // 2. Xử lý Variants & Colors
        if (product.getVariants() != null) {
            // Lấy list màu (unique)
            List<ProductCardResponse.ColorDto> distinctColors = product.getVariants().stream()
                    .filter(distinctByKey(ProductVariant::getColorHex))
                    .map(v -> {
                        ProductCardResponse.ColorDto c = new ProductCardResponse.ColorDto();
                        c.setHex(v.getColorHex());
                        return c;
                    })
                    .collect(Collectors.toList());
            response.setColors(distinctColors);

            // Lấy list ROM (unique)
            List<ProductCardResponse.VariantDto> distinctRoms = product.getVariants().stream()
                    .filter(distinctByKey(ProductVariant::getRom))
                    .map(v -> {
                        ProductCardResponse.VariantDto dto = new ProductCardResponse.VariantDto();
                        dto.setLabel(v.getRom());
                        dto.setActive(false);
                        return dto;
                    })
                    .collect(Collectors.toList());
            if (!distinctRoms.isEmpty()) distinctRoms.get(0).setActive(true);
            response.setVariants(distinctRoms);
        }

        // 3. Fake Promotions cho Card
        // 3. PROMOTIONS (Đã sửa: Chỉ dùng Momo & VNPay)
        List<String> promoLogos = new ArrayList<>();
        
        // Logo Momo (Link mẫu, bạn nên tải về up lên CDN của bạn)
        promoLogos.add("https://homepage.momocdn.net/fileuploads/svg/momo-file-240411162904.svg");
        
        // Logo VNPay
        promoLogos.add("https://vnpay.vn/assets/images/logo-icon/logo-primary.svg");
        
        response.setPromotions(promoLogos);
        response.setPromotionText("Giảm thêm 5% tối đa 200k qua VNPay/Momo");
    }


    // ====================================================
    // 2. MAPPING CHO DETAIL (CHI TIẾT)
    // ====================================================
    @Mapping(source = "displayPrice", target = "price")
    @Mapping(source = "highlightSpecs", target = "highlightSpecs")
    @Mapping(source = "specGroups", target = "specsData")
    @Mapping(target = "productImages", ignore = true)
    @Mapping(target = "storageOptions", ignore = true)
    @Mapping(target = "colorOptions", ignore = true)
    @Mapping(target = "variants", ignore = true)
    @Mapping(target = "promotions", ignore = true)
    @Mapping(target = "policies", ignore = true)
    @Mapping(target = "discountNote", ignore = true)
    ProductDetailResponse toDetailResponse(Product product);

    @Mapping(source = "items", target = "items")
    ProductDetailResponse.SpecGroupDto toSpecGroupDto(ProductSpecGroup group);

    ProductDetailResponse.SpecItemDto toSpecItemDto(ProductSpecItem item);

    @Mapping(source = "iconUrl", target = "icon")
    ProductDetailResponse.HighlightSpecDto toHighlightDto(ProductHighlightSpec spec);

    // --- LOGIC XỬ LÝ RIÊNG CHO DETAIL ---
    @AfterMapping
    default void mapDetailFields(Product product, @MappingTarget ProductDetailResponse response) {
        
        // 1. Map Product Images
        if (product.getImages() != null) {
            response.setProductImages(product.getImages().stream()
                    .sorted(Comparator.comparingInt(ProductImage::getSortOrder))
                    .map(ProductImage::getImageUrl)
                    .collect(Collectors.toList()));
        }

        // 2. Discount Note
        if (product.getOriginalPrice() != null && product.getDisplayPrice() != null) {
            BigDecimal diff = product.getOriginalPrice().subtract(product.getDisplayPrice());
            if (diff.compareTo(BigDecimal.ZERO) > 0) {
                DecimalFormat formatter = new DecimalFormat("#,###");
                response.setDiscountNote("Giảm " + formatter.format(diff) + "đ");
            }
        }

        // 3. Variants logic
        if (product.getVariants() != null) {
            // Full variants info
            List<ProductDetailResponse.VariantDetailDto> variantDtos = product.getVariants().stream()
                    .map(v -> {
                        ProductDetailResponse.VariantDetailDto dto = new ProductDetailResponse.VariantDetailDto();
                        dto.setSku(v.getSku());
                        dto.setRom(v.getRom());
                        dto.setColorName(v.getColorName());
                        dto.setPrice(v.getPrice());
                        dto.setStock(v.getStockQuantity());
                        return dto;
                    }).toList();
            response.setVariants(variantDtos);

            // Storage Options
            List<String> roms = product.getVariants().stream()
                    .map(ProductVariant::getRom)
                    .distinct().sorted().collect(Collectors.toList());
            response.setStorageOptions(roms);

            // Color Options
            List<ProductDetailResponse.ColorOptionDto> colors = product.getVariants().stream()
                    .filter(distinctByKey(ProductVariant::getColorName))
                    .map(v -> {
                        ProductDetailResponse.ColorOptionDto c = new ProductDetailResponse.ColorOptionDto();
                        c.setName(v.getColorName());
                        c.setHex(v.getColorHex());
                        c.setImg(v.getImageUrl());
                        return c;
                    }).collect(Collectors.toList());
            response.setColorOptions(colors);
        }

        // 4. Fake Promotions & Policies
        List<String> promos = new ArrayList<>();
        promos.add("Giảm ngay 2% tối đa 300.000đ khi thanh toán qua Momo");
        promos.add("Nhập mã VNPAYDLM giảm 5% tối đa 500.000đ");
        promos.add("Trả góp 0% qua thẻ tín dụng");
        response.setPromotions(promos);

        List<ProductDetailResponse.PolicyDto> policies = new ArrayList<>();
        ProductDetailResponse.PolicyDto p1 = new ProductDetailResponse.PolicyDto();
        p1.setIcon("https://cdn2.fptshop.com.vn/svg/Type_Bao_hanh_chinh_hang_4afa1cb34d.svg");
        p1.setText("Hàng chính hãng - Bảo hành 12 tháng");
        policies.add(p1);
        response.setPolicies(policies);
    }

    // Helper lọc trùng (Dùng chung cho cả 2 hàm)
    static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
}