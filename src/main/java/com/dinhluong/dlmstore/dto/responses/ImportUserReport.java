package com.dinhluong.dlmstore.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImportUserReport {
    private int successCount;
    private int failCount;
    private List<UserImportError> errors;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserImportError {
        private int row;
        private String email;
        private String message;
    }
}