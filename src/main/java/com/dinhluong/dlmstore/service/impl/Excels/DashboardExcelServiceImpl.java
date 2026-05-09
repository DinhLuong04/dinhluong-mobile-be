package com.dinhluong.dlmstore.service.impl.Excels;

import com.dinhluong.dlmstore.dto.responses.DashboardResponse;
import com.dinhluong.dlmstore.service.ExcelExportService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service("dashboardExcelService")
public class DashboardExcelServiceImpl implements ExcelExportService<DashboardResponse> {

    @Override
    public ByteArrayInputStream exportToExcel(DashboardResponse data, String timeFilter) {

        try (
                Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream out = new ByteArrayOutputStream()
        ) {

            // =====================================================
            // STYLE
            // =====================================================

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);

            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Currency Style
            CellStyle currencyStyle = workbook.createCellStyle();

            DataFormat format = workbook.createDataFormat();
            currencyStyle.setDataFormat(format.getFormat("#,##0 ₫"));

            // Title Style
            CellStyle titleStyle = workbook.createCellStyle();

            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);

            titleStyle.setFont(titleFont);

            // =====================================================
            // SHEET 1 - TỔNG QUAN
            // =====================================================

            Sheet overviewSheet = workbook.createSheet("Tong_Quan");

            int rowIndex = 0;

            Row titleRow = overviewSheet.createRow(rowIndex++);
            Cell titleCell = titleRow.createCell(0);

            titleCell.setCellValue("BÁO CÁO DASHBOARD - " + timeFilter.toUpperCase());
            titleCell.setCellStyle(titleStyle);

            rowIndex++;

            createOverviewRow(
                    overviewSheet,
                    rowIndex++,
                    "Doanh thu hợp lệ",
                    data.getOverview().getTotalRevenue().doubleValue(),
                    currencyStyle
            );

            createOverviewRow(
                    overviewSheet,
                    rowIndex++,
                    "Đơn hàng hoàn tất",
                    data.getOverview().getCompletedOrders(),
                    null
            );

            createOverviewRow(
                    overviewSheet,
                    rowIndex++,
                    "Khách hàng mới",
                    data.getOverview().getNewUsers(),
                    null
            );

            createOverviewRow(
                    overviewSheet,
                    rowIndex++,
                    "Công việc chờ xử lý",
                    data.getOverview().getPendingTasks(),
                    null
            );

            overviewSheet.autoSizeColumn(0);
            overviewSheet.autoSizeColumn(1);

            // =====================================================
            // SHEET 2 - DOANH THU
            // =====================================================

            Sheet revenueSheet = workbook.createSheet("Doanh_Thu");

            Row revenueHeader = revenueSheet.createRow(0);

            String[] revenueColumns = {
                    "Ngày",
                    "Doanh thu",
                    "Số đơn"
            };

            for (int i = 0; i < revenueColumns.length; i++) {
                Cell cell = revenueHeader.createCell(i);
                cell.setCellValue(revenueColumns[i]);
                cell.setCellStyle(headerStyle);
            }

            int revenueRowIndex = 1;

            for (DashboardResponse.RevenueTrend r : data.getRevenueTrends()) {

                Row row = revenueSheet.createRow(revenueRowIndex++);

                row.createCell(0).setCellValue(r.getDate());

                Cell revenueCell = row.createCell(1);
                revenueCell.setCellValue(r.getRevenue().doubleValue());
                revenueCell.setCellStyle(currencyStyle);

                row.createCell(2).setCellValue(r.getOrders());
            }

            revenueSheet.createFreezePane(0, 1);
            revenueSheet.setAutoFilter(new CellRangeAddress(0, 0, 0, 2));

            autoSizeColumns(revenueSheet, 3);

            // =====================================================
            // SHEET 3 - TOP SẢN PHẨM
            // =====================================================

            Sheet productSheet = workbook.createSheet("Top_San_Pham");

            Row productHeader = productSheet.createRow(0);

            String[] productColumns = {
                    "STT",
                    "Tên sản phẩm",
                    "Biến thể",
                    "Đã bán",
                    "Doanh thu"
            };

            for (int i = 0; i < productColumns.length; i++) {

                Cell cell = productHeader.createCell(i);

                cell.setCellValue(productColumns[i]);
                cell.setCellStyle(headerStyle);
            }

            int productRowIndex = 1;
            int stt = 1;

            for (DashboardResponse.TopProduct p : data.getTopProducts()) {

                Row row = productSheet.createRow(productRowIndex++);

                row.createCell(0).setCellValue(stt++);
                row.createCell(1).setCellValue(p.getName());
                row.createCell(2).setCellValue(p.getVariant());
                row.createCell(3).setCellValue(p.getSold());

                Cell revenueCell = row.createCell(4);
                revenueCell.setCellValue(p.getRevenue().doubleValue());
                revenueCell.setCellStyle(currencyStyle);
            }

            productSheet.createFreezePane(0, 1);
            productSheet.setAutoFilter(new CellRangeAddress(0, 0, 0, 4));

            autoSizeColumns(productSheet, 5);

            // =====================================================
            // SHEET 4 - TỒN KHO THẤP
            // =====================================================

            Sheet stockSheet = workbook.createSheet("Ton_Kho_Thap");

            Row stockHeader = stockSheet.createRow(0);

            String[] stockColumns = {
                    "SKU",
                    "Tên sản phẩm",
                    "Biến thể",
                    "Tồn kho"
            };

            for (int i = 0; i < stockColumns.length; i++) {

                Cell cell = stockHeader.createCell(i);

                cell.setCellValue(stockColumns[i]);
                cell.setCellStyle(headerStyle);
            }

            int stockRowIndex = 1;

            for (DashboardResponse.LowStockVariant s : data.getLowStockVariants()) {

                Row row = stockSheet.createRow(stockRowIndex++);

                row.createCell(0).setCellValue(s.getSku());
                row.createCell(1).setCellValue(s.getName());
                row.createCell(2).setCellValue(s.getVariant());
                row.createCell(3).setCellValue(s.getStock());
            }

            stockSheet.createFreezePane(0, 1);
            stockSheet.setAutoFilter(new CellRangeAddress(0, 0, 0, 3));

            autoSizeColumns(stockSheet, 4);

            // =====================================================
            // SHEET 5 - PHƯƠNG THỨC THANH TOÁN
            // =====================================================

            Sheet paymentSheet = workbook.createSheet("Thanh_Toan");

            Row paymentHeader = paymentSheet.createRow(0);

            String[] paymentColumns = {
                    "Phương thức",
                    "Tỷ lệ (%)"
            };

            for (int i = 0; i < paymentColumns.length; i++) {

                Cell cell = paymentHeader.createCell(i);

                cell.setCellValue(paymentColumns[i]);
                cell.setCellStyle(headerStyle);
            }

            int paymentRowIndex = 1;

            for (DashboardResponse.PaymentMethodStat p : data.getPaymentMethods()) {

                Row row = paymentSheet.createRow(paymentRowIndex++);

                row.createCell(0).setCellValue(p.getName());
                row.createCell(1).setCellValue(p.getValue());
            }

            autoSizeColumns(paymentSheet, 2);

            // =====================================================
            // SHEET 6 - THƯƠNG HIỆU
            // =====================================================

            Sheet brandSheet = workbook.createSheet("Thuong_Hieu");

            Row brandHeader = brandSheet.createRow(0);

            String[] brandColumns = {
                    "Thương hiệu",
                    "Doanh thu"
            };

            for (int i = 0; i < brandColumns.length; i++) {

                Cell cell = brandHeader.createCell(i);

                cell.setCellValue(brandColumns[i]);
                cell.setCellStyle(headerStyle);
            }

            int brandRowIndex = 1;

            for (DashboardResponse.TopBrand b : data.getTopBrands()) {

                Row row = brandSheet.createRow(brandRowIndex++);

                row.createCell(0).setCellValue(b.getName());

                Cell revenueCell = row.createCell(1);
                revenueCell.setCellValue(b.getRevenue().doubleValue());
                revenueCell.setCellStyle(currencyStyle);
            }

            autoSizeColumns(brandSheet, 2);

            // =====================================================
            // SHEET 7 - VOUCHER
            // =====================================================

            Sheet voucherSheet = workbook.createSheet("Voucher");

            Row voucherHeader = voucherSheet.createRow(0);

            String[] voucherColumns = {
                    "Mã Voucher",
                    "Đã dùng",
                    "Giới hạn",
                    "Hạn sử dụng"
            };

            for (int i = 0; i < voucherColumns.length; i++) {

                Cell cell = voucherHeader.createCell(i);

                cell.setCellValue(voucherColumns[i]);
                cell.setCellStyle(headerStyle);
            }

            int voucherRowIndex = 1;

            for (DashboardResponse.ActiveVoucher v : data.getActiveVouchers()) {

                Row row = voucherSheet.createRow(voucherRowIndex++);

                row.createCell(0).setCellValue(v.getCode());
                row.createCell(1).setCellValue(v.getUsed());
                row.createCell(2).setCellValue(v.getLimit());
                row.createCell(3).setCellValue(v.getExpiry().toString());
            }

            autoSizeColumns(voucherSheet, 4);

            // =====================================================
            // SHEET 8 - CSKH
            // =====================================================

            Sheet supportSheet = workbook.createSheet("Cham_Soc_Khach_Hang");

            Row supportHeader = supportSheet.createRow(0);

            String[] supportColumns = {
                    "AI xử lý",
                    "Nhân viên xử lý",
                    "Đánh giá trung bình"
            };

            for (int i = 0; i < supportColumns.length; i++) {

                Cell cell = supportHeader.createCell(i);

                cell.setCellValue(supportColumns[i]);
                cell.setCellStyle(headerStyle);
            }

            Row supportRow = supportSheet.createRow(1);

            supportRow.createCell(0)
                    .setCellValue(data.getSupportStats().getChatbotHandled());

            supportRow.createCell(1)
                    .setCellValue(data.getSupportStats().getHumanHandled());

            supportRow.createCell(2)
                    .setCellValue(data.getSupportStats().getAvgRating());

            autoSizeColumns(supportSheet, 3);

            // =====================================================
            // WRITE FILE
            // =====================================================

            workbook.write(out);

            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {

            throw new RuntimeException(
                    "Lỗi export Excel Dashboard: " + e.getMessage()
            );
        }
    }

    // =====================================================
    // HELPER METHODS
    // =====================================================

    private void createOverviewRow(
            Sheet sheet,
            int rowIndex,
            String label,
            double value,
            CellStyle style
    ) {

        Row row = sheet.createRow(rowIndex);

        row.createCell(0).setCellValue(label);

        Cell valueCell = row.createCell(1);

        valueCell.setCellValue(value);

        if (style != null) {
            valueCell.setCellStyle(style);
        }
    }

    private void autoSizeColumns(Sheet sheet, int totalColumns) {

        for (int i = 0; i < totalColumns; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}