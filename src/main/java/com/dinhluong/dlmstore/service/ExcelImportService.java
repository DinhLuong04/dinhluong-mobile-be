package com.dinhluong.dlmstore.service;

import org.springframework.web.multipart.MultipartFile;

public interface ExcelImportService<T> {
    /**
     * Đọc file Excel và lưu vào Database
     * @param file File tải lên từ Client
     * @return Kết quả import (Có thể là String báo cáo, hoặc đối tượng T tùy ý)
     */
    T importFromExcel(MultipartFile file);
}