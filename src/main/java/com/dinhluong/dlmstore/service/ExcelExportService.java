package com.dinhluong.dlmstore.service;

import java.io.ByteArrayInputStream;

public interface ExcelExportService<T> {
    
    /**
     * Xuất dữ liệu ra luồng byte để tải về
     * @param data Dữ liệu cần xuất
     * @param extraParam Tham số phụ (VD: mốc thời gian, tên trạng thái...)
     * @return Luồng dữ liệu file Excel
     */
    ByteArrayInputStream exportToExcel(T data, String extraParam);
}
