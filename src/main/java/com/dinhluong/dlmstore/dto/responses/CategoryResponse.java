package com.dinhluong.dlmstore.dto.responses;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryResponse {
    private Long id;
    private String name;
    private String description;
    private String slug;
    private String thumbnailUrl;
    private Integer level;
    
    // Chỉ trả về ID và Tên của danh mục cha thay vì bê nguyên cả object Parent
    private Long parentId;
    private String parentName; 
}