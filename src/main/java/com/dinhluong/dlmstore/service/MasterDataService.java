package com.dinhluong.dlmstore.service;

import com.dinhluong.dlmstore.dto.requests.MasterDataRequest;
import com.dinhluong.dlmstore.dto.responses.BrandResponse;
import com.dinhluong.dlmstore.dto.responses.CategoryResponse;
import com.dinhluong.dlmstore.entity.Brand;
import com.dinhluong.dlmstore.entity.Category;
import com.dinhluong.dlmstore.exception.DataConstraintException;
import com.dinhluong.dlmstore.repository.BrandRepository;
import com.dinhluong.dlmstore.repository.CategoryRepository;
import com.dinhluong.dlmstore.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MasterDataService {

    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;
    // ================= CATEGORY =================

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryResponse saveCategory(Long id, MasterDataRequest request) {
        Category category = (id != null) 
                ? categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy Category")) 
                : new Category();

        category.setName(request.getName());
        category.setSlug(request.getSlug());
        category.setDescription(request.getDescription());
        category.setThumbnailUrl(request.getThumbnailUrl());
        category.setLevel(request.getLevel() != null ? request.getLevel() : 1);

        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Parent Category"));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        Category savedCategory = categoryRepository.save(category);
        return mapToCategoryResponse(savedCategory);
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (productRepository.existsByCategoryId(id)) {
            throw new DataConstraintException("Không thể xóa! Danh mục này đang chứa sản phẩm.");
        }
        categoryRepository.deleteById(id);
    }

    // Hàm phụ trợ map Entity -> DTO cho Category
    private CategoryResponse mapToCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .slug(category.getSlug())
                .thumbnailUrl(category.getThumbnailUrl())
                .level(category.getLevel())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .build();
    }


    // ================= BRAND =================

    public List<BrandResponse> getAllBrands() {
        return brandRepository.findAll().stream()
                .map(this::mapToBrandResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public BrandResponse saveBrand(Long id, MasterDataRequest request) {
        Brand brand = (id != null) 
                ? brandRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy Brand")) 
                : new Brand();

        brand.setName(request.getName());
        brand.setSlug(request.getSlug());
        brand.setDescription(request.getDescription());
        brand.setThumbnailUrl(request.getThumbnailUrl());

        Brand savedBrand = brandRepository.save(brand);
        return mapToBrandResponse(savedBrand);
    }

    @Transactional
    public void deleteBrand(Long id) {

        if (productRepository.existsByBrandId(id)) {
            throw new DataConstraintException("Không thể xóa! Hãng này đang có sản phẩm.");
        }
        brandRepository.deleteById(id);
    }

    // Hàm phụ trợ map Entity -> DTO cho Brand
    private BrandResponse mapToBrandResponse(Brand brand) {
        return BrandResponse.builder()
                .id(brand.getId())
                .name(brand.getName())
                .description(brand.getDescription())
                .slug(brand.getSlug())
                .thumbnailUrl(brand.getThumbnailUrl())
                .build();
    }
}