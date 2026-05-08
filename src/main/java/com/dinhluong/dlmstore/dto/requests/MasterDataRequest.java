package com.dinhluong.dlmstore.dto.requests;

import lombok.Data;

@Data
public class MasterDataRequest {
    private String name;
    private String description;
    private String slug;
    private String thumbnailUrl;
    private Long parentId;
    private Integer level;
}