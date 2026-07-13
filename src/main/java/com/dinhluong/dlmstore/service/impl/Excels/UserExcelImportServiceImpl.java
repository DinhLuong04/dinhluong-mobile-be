package com.dinhluong.dlmstore.service.impl.Excels;

import com.dinhluong.dlmstore.dto.responses.ImportUserReport;
import com.dinhluong.dlmstore.entity.Roles;
import com.dinhluong.dlmstore.entity.Users;
import com.dinhluong.dlmstore.repository.RoleRepository;
import com.dinhluong.dlmstore.repository.UserRepository;
import com.dinhluong.dlmstore.service.ExcelImportService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserExcelImportServiceImpl implements ExcelImportService<ImportUserReport> {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public ImportUserReport importFromExcel(MultipartFile file) {
        List<Users> usersToSave = new ArrayList<>();
        List<ImportUserReport.UserImportError> errorDetails = new ArrayList<>();

        // Dùng Set để phát hiện các Email bị copy trùng lặp ngay bên trong file Excel
        Set<String> emailsInFile = new HashSet<>();

        // Lấy sẵn Role USER để ép quyền (Tối ưu performance, không cần gọi DB trong vòng lặp)
        Roles userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Lỗi hệ thống: Chưa có Role USER trong DB"));

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheet("Danh sách chi tiết");
            if (sheet == null) {
                sheet = workbook.getSheetAt(0);
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;

                int excelRowNumber = i + 1;

                try {
                    String email = getCellValue(row.getCell(0));
                    String fullName = getCellValue(row.getCell(1));
                    String phone = getCellValue(row.getCell(2));
                    String rawPassword = getCellValue(row.getCell(3));
                    String statusStr = getCellValue(row.getCell(5));

                    // --- 1. VALIDATE DỮ LIỆU ---
                    if (email.isEmpty()) {
                        errorDetails.add(new ImportUserReport.UserImportError(excelRowNumber, "N/A", "Email không được để trống"));
                        continue;
                    }
                    if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                        errorDetails.add(new ImportUserReport.UserImportError(excelRowNumber, email, "Sai định dạng Email"));
                        continue;
                    }
                    if (!emailsInFile.add(email)) {
                        errorDetails.add(new ImportUserReport.UserImportError(excelRowNumber, email, "Email bị lặp lại nhiều lần trong file Excel này"));
                        continue;
                    }
                    if (userRepository.existsByEmail(email)) {
                        errorDetails.add(new ImportUserReport.UserImportError(excelRowNumber, email, "Khách hàng đã tồn tại (Hệ thống từ chối ghi đè)"));
                        continue;
                    }

                    // --- 2. XỬ LÝ MẶC ĐỊNH ---
                    // Nếu Admin không nhập pass, tự động gán pass mặc định là 123456
                    if (rawPassword.isEmpty()) {
                        rawPassword = "123456";
                    }
                    // Mặc định là Hoạt động trừ khi gõ chữ Khóa
                    boolean isEnabled = !statusStr.equalsIgnoreCase("Khóa") && !statusStr.equalsIgnoreCase("Bị khóa");

                    // --- 3. LẮP RÁP THỰC THỂ ---
                    Users newUser = Users.builder()
                            .email(email)
                            .fullName(fullName)
                            .phone(phone)
                            .password(passwordEncoder.encode(rawPassword))
                            .role(userRole) // Ép quyền bảo mật
                            .authProvider("LOCAL")
                            .isEnabled(isEnabled)
                            .createdAt(LocalDateTime.now())
                            .build();

                    usersToSave.add(newUser);

                } catch (Exception e) {
                    errorDetails.add(new ImportUserReport.UserImportError(excelRowNumber, "Lỗi dòng", "Dữ liệu không hợp lệ: " + e.getMessage()));
                }
            }

            // Lưu toàn bộ danh sách hợp lệ vào DB (Batch Insert giúp chạy cực nhanh)
            if (!usersToSave.isEmpty()) {
                userRepository.saveAll(usersToSave);
            }

            // Trả về báo cáo
            return ImportUserReport.builder()
                    .successCount(usersToSave.size())
                    .failCount(errorDetails.size())
                    .errors(errorDetails)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Không thể đọc file Excel: " + e.getMessage());
        }
    }

    private boolean isRowEmpty(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.NUMERIC) {
            // Chống lỗi SĐT biến thành số thập phân khoa học (VD: 9.8E+8)
            return String.format("%.0f", cell.getNumericCellValue());
        }
        return cell.getStringCellValue().trim();
    }
}