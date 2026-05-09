package com.dinhluong.dlmstore.service.impl.Excels;

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
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserExcelImportServiceImpl implements ExcelImportService<String> {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public String importFromExcel(MultipartFile file) {
        List<Users> usersToSave = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            // THÔNG MINH: Tự tìm Sheet có tên "Danh sách chi tiết", nếu người dùng tạo file mới tinh thì lấy Sheet đầu tiên (index 0)
            Sheet sheet = workbook.getSheet("Danh sách chi tiết");
            if (sheet == null) {
                sheet = workbook.getSheetAt(0);
            }

            // Duyệt từ dòng 1 (bỏ qua Header)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    // Đọc chuẩn 6 cột theo thứ tự file Export: Email(0), Tên(1), SĐT(2), Pass(3), Role(4), Trạng thái(5)
                    String email = getCellValue(row.getCell(0));
                    String fullName = getCellValue(row.getCell(1));
                    String phone = getCellValue(row.getCell(2));
                    String rawPassword = getCellValue(row.getCell(3));
                    String roleStr = getCellValue(row.getCell(4));
                    String statusStr = getCellValue(row.getCell(5));

                    // 1. Nếu Email rỗng hoặc Mật khẩu rỗng -> Bỏ qua dòng này
                    if (email.isEmpty() || rawPassword.isEmpty()) {
                        failCount++;
                        continue; 
                    }

                    // 2. Nếu Email đã tồn tại trong DB -> Bỏ qua (Tránh lỗi SQL)
                    if (userRepository.existsByEmail(email)) {
                        failCount++;
                        continue; 
                    }

                    // 3. Xử lý Role: Ép tất cả về USER, chỉ khi gõ chính xác ADMIN mới cấp quyền
                    String finalRoleName = roleStr.equalsIgnoreCase("ADMIN") ? "ADMIN" : "USER";
                    Roles role = roleRepository.findByName(finalRoleName)
                            .orElseThrow(() -> new RuntimeException("Role không tồn tại trong DB"));

                    // 4. Xử lý Trạng thái: Mặc định là Hoạt động, trừ khi gõ chữ "Khóa"
                    boolean isEnabled = !statusStr.equalsIgnoreCase("Bị khóa") && !statusStr.equalsIgnoreCase("Khóa");

                    // 5. Lắp ráp Entity
                    Users newUser = Users.builder()
                            .email(email)
                            .fullName(fullName)
                            .phone(phone)
                            .password(passwordEncoder.encode(rawPassword)) // Mã hóa mật khẩu an toàn
                            .role(role)
                            .authProvider("LOCAL") // Default tạo từ Excel là Local
                            .isEnabled(isEnabled)
                            .build();

                    usersToSave.add(newUser);
                    successCount++;
                } catch (Exception e) {
                    failCount++; 
                    System.out.println("Lỗi tại dòng " + i + ": " + e.getMessage());// Dòng nào lỗi data thì tự bỏ qua, không làm sập cả file
                }
            }

            // Lưu 1 cục vào DB cho nhanh
            if (!usersToSave.isEmpty()) {
                userRepository.saveAll(usersToSave);
            }

            return String.format("Nhập dữ liệu thành công: %d tài khoản mới. Bỏ qua/Lỗi: %d dòng.", successCount, failCount);

        } catch (Exception e) {
            throw new RuntimeException("Lỗi không thể đọc file Excel: " + e.getMessage());
        }
    }

    // Hàm phụ: Ép kiểu mọi Cell về String để không bị lỗi số điện thoại (ví dụ: 098... bị thành 9.8E+8)
    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }
}