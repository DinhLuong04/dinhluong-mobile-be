package com.dinhluong.dlmstore.dto;

import lombok.Data;
import java.util.List;

@Data
public class CategoryDTO {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String thumbnailUrl;
    private Integer level;
    private Long parentId;
    private String parentName;
    private List<CategoryDTO> children; // Để render Tree Table
}