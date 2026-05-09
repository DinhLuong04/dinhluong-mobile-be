package com.dinhluong.dlmstore.service.impl.Excels;

import com.dinhluong.dlmstore.dto.responses.OrderItemResponse;
import com.dinhluong.dlmstore.dto.responses.OrderResponse;
import com.dinhluong.dlmstore.service.ExcelExportService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class OrderExcelExportService implements ExcelExportService<List<OrderResponse>> {

    @Override
    public ByteArrayInputStream exportToExcel(List<OrderResponse> orders, String extraParam) {

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Orders");

            // =========================
            // HEADER STYLE
            // =========================
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);

            // =========================
            // HEADER
            // =========================
            Row header = sheet.createRow(0);

            // 🔥 ĐÃ BỔ SUNG THÊM 4 CỘT MỚI
            String[] columns = {
                    "Mã đơn",
                    "Ngày đặt",
                    "Ngày giao",      // MỚI
                    "Khách hàng",
                    "SĐT",
                    "Địa chỉ",
                    "Ghi chú khách",  // MỚI
                    "Trạng thái",
                    "Thanh toán",
                    "TT Thanh toán",
                    "Tổng tiền",
                    "Tiền giảm giá",  // MỚI
                    "Sản phẩm",
                    "Phân loại",
                    "Số lượng",
                    "Đơn giá",
                    "Thành tiền",
                    "Lý do",
                    "Người hủy"       // MỚI
            };

            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // =========================
            // DATA
            // =========================
            int rowIdx = 1;

            for (OrderResponse order : orders) {
                for (OrderItemResponse item : order.getItems()) {

                    Row row = sheet.createRow(rowIdx++);

                    row.createCell(0).setCellValue(order.getId());

                    row.createCell(1).setCellValue(
                            order.getCreatedAt() != null ? order.getCreatedAt().toString() : ""
                    );

                    // 🔥 MỚI: Ngày giao hàng
                    row.createCell(2).setCellValue(
                            order.getDeliveredAt() != null ? order.getDeliveredAt().toString() : ""
                    );

                    row.createCell(3).setCellValue(order.getReceiverName());
                    row.createCell(4).setCellValue(order.getReceiverPhone());
                    row.createCell(5).setCellValue(order.getReceiverAddress());

                    // 🔥 MỚI: Ghi chú của khách
                    row.createCell(6).setCellValue(
                            order.getUserNote() != null ? order.getUserNote() : ""
                    );

                    row.createCell(7).setCellValue(
                            order.getStatus() != null ? order.getStatus().name() : "" // Sửa .name() vì DTO status thường đã là String
                    );

                    row.createCell(8).setCellValue(order.getPaymentMethod());
                    row.createCell(9).setCellValue(order.getPaymentStatus());

                    row.createCell(10).setCellValue(
                            order.getTotalAmount() != null ? order.getTotalAmount().doubleValue() : 0
                    );

                    // 🔥 MỚI: Tiền giảm giá
                    row.createCell(11).setCellValue(
                            order.getDiscountAmount() != null ? order.getDiscountAmount().doubleValue() : 0
                    );

                    row.createCell(12).setCellValue(item.getProductName());

                    row.createCell(13).setCellValue(
                            item.getVariantName() != null ? item.getVariantName() : ""
                    );

                    row.createCell(14).setCellValue(item.getQuantity());

                    row.createCell(15).setCellValue(
                            item.getPriceAtPurchase() != null ? item.getPriceAtPurchase().doubleValue() : 0
                    );

                    row.createCell(16).setCellValue(
                            item.getPriceAtPurchase() != null 
                            ? item.getPriceAtPurchase().multiply(java.math.BigDecimal.valueOf(item.getQuantity())).doubleValue() 
                            : 0
                    );

                    row.createCell(17).setCellValue(
                            order.getReason() != null ? order.getReason() : ""
                    );

                    // 🔥 MỚI: Người hủy
                    row.createCell(18).setCellValue(
                            order.getCancelledBy() != null ? order.getCancelledBy() : ""
                    );
                }
            }

            // Auto size
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);

            return new ByteArrayInputStream(out.toByteArray());

        } catch (Exception e) {
            throw new RuntimeException("Lỗi xuất Excel: " + e.getMessage());
        }
    }
}