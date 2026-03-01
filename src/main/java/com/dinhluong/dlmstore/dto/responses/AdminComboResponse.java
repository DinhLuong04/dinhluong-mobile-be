package com.dinhluong.dlmstore.dto.responses;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class AdminComboResponse {
    private Long id;
    private Long mainProductId;
    private Long relatedProductId;
    private String relatedProductThumbnail;
    private String relatedProductName; // Cần tên phụ kiện để hiển thị lên bảng
    private BigDecimal discountAmount;
    private String note;
}