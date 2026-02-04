package com.dinhluong.dlmstore.dto;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiFilterCriteria {
    // Intent của người dùng
    private String intent; // "SEARCH", "DETAIL", "COMPARE", "CHAT"

    // Các tiêu chí lọc (cho intent SEARCH)
    private List<String> brands;
    private List<String> osTypes;
    private List<String> networks;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer minBattery;
    private Double minScreenSize;
    private String searchKeyword;

    // Các tiêu chí cụ thể (cho intent DETAIL / COMPARE)
    private String targetProductName;
    private List<String> compareProductNames;
}
