package com.dinhluong.dlmstore.dto.responses;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SpecAttributeResponse {
    private Long id;
    private String name;
    private Integer sortOrder;
    private String dataType;
    private Long groupId;
    private String groupName;
}