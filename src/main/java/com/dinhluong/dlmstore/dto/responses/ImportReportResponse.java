package com.dinhluong.dlmstore.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportReportResponse {

    // ==========================================
    // PRODUCT
    // ==========================================

    private int totalAdded;

    private int totalUpdated;

    private int totalSkipped;

    private int totalProcessed;

    // ==========================================
    // VARIANT
    // ==========================================

    private int variantAdded;

    private int variantUpdated;

    // ==========================================
    // IMAGE
    // ==========================================

    private int imageAdded;

    // ==========================================
    // HIGHLIGHT SPEC
    // ==========================================

    private int highlightAdded;

    private int highlightUpdated;

    // ==========================================
    // SPEC VALUE
    // ==========================================

    private int specValueAdded;

    private int specValueUpdated;

    // ==========================================
    // MESSAGE & ERRORS
    // ==========================================

    private String message;

    // Đổi từ List<String> errorDetails sang List<ImportValidationError> errors
    private List<ImportValidationError> errors = new ArrayList<>();

}