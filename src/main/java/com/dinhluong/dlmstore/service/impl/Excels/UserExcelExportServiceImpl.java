package com.dinhluong.dlmstore.service.impl.Excels;

import com.dinhluong.dlmstore.entity.Users;
import com.dinhluong.dlmstore.repository.UserRepository;
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
public class UserExcelExportServiceImpl implements ExcelExportService<List<Users>> {
    private final UserRepository userRepository;
    public ByteArrayInputStream generateExportData(String keyword, Boolean isEnabled) {
        // 1. Service tự gọi DB để lấy dữ liệu (Controller không cần quan tâm)
        List<Users> users = userRepository.searchAdminUsers(keyword, isEnabled);
        
        // 2. Gọi hàm thực thi build Excel bên dưới
        return this.exportToExcel(users, null);
    }
    @Override
    public ByteArrayInputStream exportToExcel(List<Users> users, String extraParam) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // --- 1. SHEET TỔNG QUAN ---
            Sheet summarySheet = workbook.createSheet("Tổng quan");
            createSummarySheet(summarySheet, workbook, users);

            // --- 2. SHEET DANH SÁCH CHI TIẾT ---
            Sheet dataSheet = workbook.createSheet("Danh sách chi tiết");
            createDataSheet(dataSheet, workbook, users);

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (Exception e) {
            throw new RuntimeException("Lỗi xuất file Excel khách hàng: " + e.getMessage());
        }
    }

    private void createSummarySheet(Sheet sheet, Workbook workbook, List<Users> users) {
        long total = users.size();
        long active = users.stream().filter(u -> u.getIsEnabled() != null && u.getIsEnabled()).count();
        long locked = total - active;

        // Style cho tiêu đề
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        headerStyle.setFont(font);

        Row titleRow = sheet.createRow(0);
        titleRow.createCell(0).setCellValue("BÁO CÁO TỔNG QUAN KHÁCH HÀNG");
        titleRow.getCell(0).setCellStyle(headerStyle);

        sheet.createRow(2).createCell(0).setCellValue("Tổng số khách hàng:");
        sheet.getRow(2).createCell(1).setCellValue(total);

        sheet.createRow(3).createCell(0).setCellValue("Đang hoạt động:");
        sheet.getRow(3).createCell(1).setCellValue(active);

        sheet.createRow(4).createCell(0).setCellValue("Tài khoản bị khóa:");
        sheet.getRow(4).createCell(1).setCellValue(locked);

        sheet.createRow(6).createCell(0).setCellValue("Ngày xuất báo cáo:");
        sheet.getRow(6).createCell(1).setCellValue(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

   private void createDataSheet(Sheet sheet, Workbook workbook, List<Users> users) {
        // Đã đổi lại các cột cho KHỚP HOÀN TOÀN với chuẩn Import
        String[] columns = {"Email", "Họ và tên", "Số điện thoại", "Mật khẩu", "Vai trò", "Trạng thái"};
        
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 1;
        for (Users user : users) {
            Row row = sheet.createRow(rowIdx++);
            
            // Cột 0: Email
            row.createCell(0).setCellValue(user.getEmail());
            
            // Cột 1: Họ tên
            row.createCell(1).setCellValue(user.getFullName() != null ? user.getFullName() : "");
            
            // Cột 2: SĐT
            row.createCell(2).setCellValue(user.getPhone() != null ? user.getPhone() : "");
            
            // Cột 3: Mật khẩu (Khi xuất ra luôn để trống để bảo mật, khi Import Admin sẽ tự gõ pass mới vào)
            row.createCell(3).setCellValue(""); 
            
            // Cột 4: Vai trò (Lấy tên Role ra, VD: "USER" hoặc "ADMIN")
            String roleName = user.getRole() != null ? user.getRole().getName() : "USER";
            row.createCell(4).setCellValue(roleName);
            
            // Cột 5: Trạng thái
            String status = (user.getIsEnabled() != null && user.getIsEnabled()) ? "Hoạt động" : "Khóa";
            row.createCell(5).setCellValue(status);
        }

        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}