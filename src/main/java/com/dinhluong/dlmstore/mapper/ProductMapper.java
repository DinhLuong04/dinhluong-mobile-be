package com.dinhluong.dlmstore.mapper;

import com.dinhluong.dlmstore.dto.responses.ProductCardResponse;
import com.dinhluong.dlmstore.dto.responses.ProductDetailResponse;
import com.dinhluong.dlmstore.entity.Product;
import com.dinhluong.dlmstore.entity.ProductHighlightSpec;
import com.dinhluong.dlmstore.entity.ProductImage;
import com.dinhluong.dlmstore.entity.ProductVariant;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class ProductMapper {

    @Autowired
    protected ObjectMapper objectMapper;

    // MAPPING CHO CARD (DANH SÁCH)

    @Mapping(source = "id", target = "id")
    @Mapping(source = "slug", target = "slug")
    @Mapping(source = "thumbnailUrl", target = "image")
    @Mapping(source = "displayPrice", target = "price")
    @Mapping(source = "highlightSpecs", target = "specs")
    @Mapping(target = "discountNote", ignore = true)
    @Mapping(target = "colors", ignore = true)
    @Mapping(target = "variants", ignore = true)
    @Mapping(target = "promotions", ignore = true)
    @Mapping(target = "promotionText", ignore = true)
    public abstract ProductCardResponse toCardResponse(Product product);

    @Mapping(source = "iconUrl", target = "icon")
    @Mapping(source = "value", target = "subLabel")
    public abstract ProductCardResponse.SpecDto toSpecDto(ProductHighlightSpec spec);

    @AfterMapping
    protected void calculateCardFields(Product product, @MappingTarget ProductCardResponse response) {
        if (product.getOriginalPrice() != null && product.getDisplayPrice() != null) {
            BigDecimal diff = product.getOriginalPrice().subtract(product.getDisplayPrice());
            if (diff.compareTo(BigDecimal.ZERO) > 0) {
                DecimalFormat formatter = new DecimalFormat("#,###");
                response.setDiscountNote("Giảm " + formatter.format(diff) + "đ");
            }
        }

        if (product.getVariants() != null && !product.getVariants().isEmpty()) {

            List<ProductCardResponse.ColorDto> distinctColors = product.getVariants().stream()
                    .filter(v -> v.getColorHex() != null)
                    .filter(distinctByKey(ProductVariant::getColorHex))
                    .map(v -> {
                        ProductCardResponse.ColorDto c = new ProductCardResponse.ColorDto();
                        c.setHex(v.getColorHex());
                        return c;
                    })
                    .collect(Collectors.toList());
            response.setColors(distinctColors);

            List<ProductCardResponse.VariantDto> distinctRoms = product.getVariants().stream()
                    .filter(v -> v.getRom() != null)
                    .filter(distinctByKey(ProductVariant::getRom))
                    .map(v -> {
                        ProductCardResponse.VariantDto dto = new ProductCardResponse.VariantDto();
                        dto.setLabel(v.getRom());
                        dto.setActive(false);
                        return dto;
                    })
                    .collect(Collectors.toList());

            if (!distinctRoms.isEmpty())
                distinctRoms.get(0).setActive(true);
            response.setVariants(distinctRoms);
        }

        List<String> promoLogos = new ArrayList<>();
        promoLogos.add("https://homepage.momocdn.net/fileuploads/svg/momo-file-240411162904.svg");
        promoLogos.add("https://vnpay.vn/assets/images/logo-icon/logo-primary.svg");

        response.setPromotions(promoLogos);
        response.setPromotionText("Giảm thêm 5% tối đa 200k qua VNPay/Momo");
    }

    // MAPPING DETAIL

    @Mapping(source = "displayPrice", target = "price")
    @Mapping(source = "highlightSpecs", target = "highlightSpecs")
    @Mapping(source = "thumbnailUrl", target = "thumbnail")
    @Mapping(target = "specsData", expression = "java(mapJsonToSpecs(product.getSpecificationsJson()))")
    @Mapping(target = "productImages", ignore = true)
    @Mapping(target = "storageOptions", ignore = true)
    @Mapping(target = "colorOptions", ignore = true)
    @Mapping(target = "variants", ignore = true)
    @Mapping(target = "promotions", ignore = true)
    @Mapping(target = "discountNote", ignore = true)
    public abstract ProductDetailResponse toDetailResponse(Product product);

    @Mapping(source = "iconUrl", target = "icon")
    public abstract ProductDetailResponse.HighlightSpecDto toHighlightDto(ProductHighlightSpec spec);

    protected List<ProductDetailResponse.SpecGroupDto> mapJsonToSpecs(JsonNode jsonNode) {
        if (jsonNode == null)
            return new ArrayList<>();
        try {
            return objectMapper.convertValue(jsonNode, new TypeReference<List<ProductDetailResponse.SpecGroupDto>>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @AfterMapping
    protected void mapDetailFields(Product product, @MappingTarget ProductDetailResponse response) {
        if (product.getImages() != null) {

            response.setProductImages(product.getImages().stream()
                    .sorted(Comparator.comparingInt(ProductImage::getSortOrder))
                    .map(ProductImage::getImageUrl)
                    .collect(Collectors.toList()));
        }

        if (product.getOriginalPrice() != null && product.getDisplayPrice() != null) {
            BigDecimal diff = product.getOriginalPrice().subtract(product.getDisplayPrice());
            if (diff.compareTo(BigDecimal.ZERO) > 0) {
                DecimalFormat formatter = new DecimalFormat("#,###");
                response.setDiscountNote("Giảm " + formatter.format(diff) + "đ");
            }
        }

        if (product.getVariants() != null) {
            List<ProductDetailResponse.VariantDetailDto> variantDtos = product.getVariants().stream()
                    .map(v -> {
                        ProductDetailResponse.VariantDetailDto dto = new ProductDetailResponse.VariantDetailDto();
                        dto.setId(v.getId());
                        dto.setSku(v.getSku());
                        dto.setRom(v.getRom());
                        dto.setColorName(v.getColorName());
                        dto.setPrice(v.getPrice());
                        dto.setStock(v.getStockQuantity());
                        return dto;
                    }).toList();
            response.setVariants(variantDtos);

            if (product.getAvailableRoms() != null && !product.getAvailableRoms().isEmpty()) {
                response.setStorageOptions(product.getAvailableRoms());
            } else {

                List<String> roms = product.getVariants().stream()
                        .map(ProductVariant::getRom)
                        .filter(Objects::nonNull)
                        .distinct()
                        .sorted()
                        .collect(Collectors.toList());
                response.setStorageOptions(roms);
            }

            List<ProductDetailResponse.ColorOptionDto> colors = product.getVariants().stream()
                    .filter(v -> v.getColorName() != null)
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

        List<String> promos = new ArrayList<>();
        promos.add("Giảm ngay 2% tối đa 300.000đ khi thanh toán qua Momo");
        promos.add("Nhập mã VNPAYDLM giảm 5% tối đa 500.000đ");
        promos.add("Trả góp 0% qua thẻ tín dụng");
        response.setPromotions(promos);
    }

    protected <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> {
            Object key = keyExtractor.apply(t);

            return key != null && seen.add(key);
        };
    }
}