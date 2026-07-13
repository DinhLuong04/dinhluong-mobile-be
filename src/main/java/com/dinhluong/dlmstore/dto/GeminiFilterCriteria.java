package com.dinhluong.dlmstore.dto;

import java.math.BigDecimal;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiFilterCriteria {
    // Intent của người dùng: Thêm "OUT_OF_DOMAIN"
    private String intent; // "SEARCH", "DETAIL", "COMPARE", "CHAT", "OUT_OF_DOMAIN"

    // Các tiêu chí lọc (cho intent SEARCH)
    private List<String> brands;
    private List<String> osTypes;
    private List<String> networks;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer minBattery;
    private Double minScreenSize;
    private String searchKeyword;
    
    // THÊM 2 TRƯỜNG NÀY ĐỂ BẮT CẤU HÌNH SÂU
    private String ram; // VD: "8GB"
    private String rom; // VD: "256GB"

    // Các tiêu chí cụ thể (cho intent DETAIL / COMPARE)
    private String targetProductName;
    private List<String> compareProductNames; // Giữ nguyên List của bạn vì nó rất chuẩn
}