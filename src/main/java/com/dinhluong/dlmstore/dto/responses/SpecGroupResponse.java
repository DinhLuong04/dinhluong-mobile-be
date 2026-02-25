package com.dinhluong.dlmstore.dto.responses;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class SpecGroupResponse {
    private Long id;
    private String name;
    private Integer sortOrder;
    
    // Trả về kèm danh sách thuộc tính con luôn để Frontend dễ render form
    private List<SpecAttributeResponse> attributes; 
}