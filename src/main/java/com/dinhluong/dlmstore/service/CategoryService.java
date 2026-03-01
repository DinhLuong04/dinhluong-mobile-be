package com.dinhluong.dlmstore.service;

import com.dinhluong.dlmstore.dto.CategoryDTO;
import com.dinhluong.dlmstore.entity.Category;
import com.dinhluong.dlmstore.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // Lấy toàn bộ danh mục dưới dạng CÂY (Tree)
    @Transactional(readOnly = true)
    public List<CategoryDTO> getCategoryTree() {
        List<Category> rootCategories = categoryRepository.findByParentIsNull();
        return rootCategories.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // Lấy danh sách dạng PHẲNG (để đổ vào thẻ <Select> khi chọn danh mục cha)
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategoriesFlat() {
        return categoryRepository.findAll().stream().map(c -> {
            CategoryDTO dto = new CategoryDTO();
            dto.setId(c.getId());
            dto.setName(c.getName());
            dto.setLevel(c.getLevel());
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public Category createCategory(CategoryDTO dto) {
        Category category = new Category();
        category.setName(dto.getName());
        category.setSlug(dto.getSlug());
        category.setDescription(dto.getDescription());
        category.setThumbnailUrl(dto.getThumbnailUrl());

        if (dto.getParentId() != null) {
            Category parent = categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục cha"));
            category.setParent(parent);
            category.setLevel(parent.getLevel() != null ? parent.getLevel() + 1 : 1);
        } else {
            category.setLevel(0); // Danh mục gốc
        }

        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateCategory(Long id, CategoryDTO dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));

        category.setName(dto.getName());
        category.setSlug(dto.getSlug());
        category.setDescription(dto.getDescription());
        category.setThumbnailUrl(dto.getThumbnailUrl());

        if (dto.getParentId() != null) {
            // Chống lỗi cha nhận con làm cha (Cyclic)
            if (dto.getParentId().equals(id)) {
                throw new RuntimeException("Danh mục không thể làm cha của chính nó!");
            }
            Category parent = categoryRepository.findById(dto.getParentId()).orElseThrow();
            category.setParent(parent);
            category.setLevel(parent.getLevel() != null ? parent.getLevel() + 1 : 1);
        } else {
            category.setParent(null);
            category.setLevel(0);
        }

        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    // Hàm đệ quy map Entity -> DTO
    private CategoryDTO mapToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setSlug(category.getSlug());
        dto.setDescription(category.getDescription());
        dto.setThumbnailUrl(category.getThumbnailUrl());
        dto.setLevel(category.getLevel());
        
        if (category.getParent() != null) {
            dto.setParentId(category.getParent().getId());
            dto.setParentName(category.getParent().getName());
        }

        // Đệ quy gọi map các con
        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            dto.setChildren(category.getChildren().stream().map(this::mapToDTO).collect(Collectors.toList()));
        }
        return dto;
    }
}