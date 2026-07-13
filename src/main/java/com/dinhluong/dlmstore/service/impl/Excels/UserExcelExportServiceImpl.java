package com.dinhluong.dlmstore.service.impl.Excels;

import com.dinhluong.dlmstore.entity.Users;
import com.dinhluong.dlmstore.repository.UserRepository;
import com.dinhluong.dlmstore.repository.projections.UserStatsProjection;
import com.dinhluong.dlmstore.service.ExcelExportService;

import lombok.RequiredArgsConstructor;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Service
@RequiredArgsConstructor
public class UserExcelExportServiceImpl implements ExcelExportService<List<UserStatsProjection>> {

    private final UserRepository userRepository;

    public ByteArrayInputStream generateExportData(String keyword, Boolean isEnabled) {
        // 1. Gọi Repository để lấy dữ liệu tổng hợp từ SQL (Cần Native Query như đã bàn)
        List<UserStatsProjection> stats = userRepository.searchUserStats(keyword, isEnabled);
        return this.exportToExcel(stats, null);
    }

    @Override
    public ByteArrayInputStream exportToExcel(List<UserStatsProjection> users, String extraParam) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // --- 1. TẠO SHEET TỔNG QUAN ---
            Sheet summarySheet = workbook.createSheet("Tổng quan");
            createSummarySheet(summarySheet, workbook, users);

            // --- 2. TẠO SHEET CHI TIẾT ---
            Sheet dataSheet = workbook.createSheet("Danh sách chi tiết");
            createDataSheet(dataSheet, workbook, users);

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Lỗi xuất file Excel khách hàng: " + e.getMessage());
        }
    }

    private void createSummarySheet(Sheet sheet, Workbook workbook, List<UserStatsProjection> users) {
        // --- 1. Logic tính toán số liệu ---
        long totalUsers = users.size();
        long activeUsers = users.stream().filter(u -> u.getIsEnabled() != null && u.getIsEnabled()).count();
        long lockedUsers = totalUsers - activeUsers;

        double totalRevenue = users.stream().mapToDouble(u -> u.getTotalSpent() != null ? u.getTotalSpent() : 0).sum();
        long totalSuccess = users.stream().mapToLong(u -> u.getSuccessOrders() != null ? u.getSuccessOrders() : 0).sum();
        long totalCancelled = users.stream().mapToLong(u -> u.getCancelledOrders() != null ? u.getCancelledOrders() : 0).sum();

        // --- 2. Styles ---
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleFont.setColor(IndexedColors.DARK_BLUE.getIndex());
        titleStyle.setFont(titleFont);

        CellStyle labelStyle = workbook.createCellStyle();
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        labelStyle.setFont(boldFont);

        CellStyle currencyStyle = workbook.createCellStyle();
        currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));

        // --- 3. Đổ dữ liệu ---
        Row titleRow = sheet.createRow(0);
        titleRow.createCell(0).setCellValue("BÁO CÁO TÌNH HÌNH KHÁCH HÀNG & KINH DOANH");
        titleRow.getCell(0).setCellStyle(titleStyle);

        int rowIdx = 2;
        // Nhóm Quản trị
        createRow(sheet, rowIdx++, "Tổng số khách hàng:", totalUsers, labelStyle, null);
        createRow(sheet, rowIdx++, "Đang hoạt động:", activeUsers, labelStyle, null);
        createRow(sheet, rowIdx++, "Tài khoản bị khóa:", lockedUsers, labelStyle, null);

        rowIdx++; // Cách 1 dòng

        // Nhóm Kinh doanh
        createRow(sheet, rowIdx++, "TỔNG DOANH THU HỆ THỐNG:", totalRevenue, labelStyle, currencyStyle);
        createRow(sheet, rowIdx++, "Tổng số đơn thành công:", totalSuccess, labelStyle, null);
        createRow(sheet, rowIdx++, "Tổng số đơn đã hủy:", totalCancelled, labelStyle, null);

        rowIdx += 2;
        sheet.createRow(rowIdx).createCell(0).setCellValue("Ngày xuất báo cáo:");
        sheet.getRow(rowIdx).createCell(1).setCellValue(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createDataSheet(Sheet sheet, Workbook workbook, List<UserStatsProjection> users) {
        String[] columns = {"Email", "Họ và tên", "SĐT", "Vai trò", "Trạng thái",
                "Tổng đơn", "Thành công", "Đã hủy", "Tổng chi tiêu (VNĐ)"};

        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle currencyStyle = workbook.createCellStyle();
        currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 1;
        for (UserStatsProjection user : users) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(user.getEmail());
            row.createCell(1).setCellValue(user.getFullName() != null ? user.getFullName() : "");
            row.createCell(2).setCellValue(user.getPhone() != null ? user.getPhone() : "");
            row.createCell(3).setCellValue(user.getRoleName());
            row.createCell(4).setCellValue(user.getIsEnabled() ? "Hoạt động" : "Khóa");
            row.createCell(5).setCellValue(user.getTotalOrders() != null ? user.getTotalOrders() : 0);
            row.createCell(6).setCellValue(user.getSuccessOrders() != null ? user.getSuccessOrders() : 0);
            row.createCell(7).setCellValue(user.getCancelledOrders() != null ? user.getCancelledOrders() : 0);

            Cell spentCell = row.createCell(8);
            spentCell.setCellValue(user.getTotalSpent() != null ? user.getTotalSpent() : 0);
            spentCell.setCellStyle(currencyStyle);
        }

        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    // Hàm phụ hỗ trợ tạo dòng Summary nhanh
    private void createRow(Sheet sheet, int idx, String label, Object value, CellStyle lblStyle, CellStyle valStyle) {
        Row row = sheet.createRow(idx);
        Cell cell0 = row.createCell(0);
        cell0.setCellValue(label);
        cell0.setCellStyle(lblStyle);

        Cell cell1 = row.createCell(1);
        if (value instanceof Number) {
            cell1.setCellValue(((Number) value).doubleValue());
        } else {
            cell1.setCellValue(value.toString());
        }
        if (valStyle != null) cell1.setCellStyle(valStyle);
    }
}