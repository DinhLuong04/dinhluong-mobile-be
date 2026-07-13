package com.dinhluong.dlmstore.service.impl.Excels;

import com.dinhluong.dlmstore.entity.*;
import com.dinhluong.dlmstore.service.ExcelExportService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ProductExcelExportServiceImpl implements ExcelExportService<List<Product>> {

    @Override
    public ByteArrayInputStream exportToExcel(List<Product> products, String extraParam) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            CellStyle headerStyle = createHeaderStyle(workbook);

            createProductSheet(workbook, headerStyle, products);
            createVariantSheet(workbook, headerStyle, products);
            createImageSheet(workbook, headerStyle, products);
            createHighlightSpecSheet(workbook, headerStyle, products);
            createSpecValueSheet(workbook, headerStyle, products);

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi xuất file Excel cho Product: " + e.getMessage());
        }
    }

    private void createProductSheet(Workbook workbook, CellStyle headerStyle, List<Product> products) {
        Sheet sheet = workbook.createSheet("Products");
        String[] headers = {
                "ID", "Name", "Slug", "Display Price", "Original Price", "Status", "Product Type",
                "Category ID", "Brand ID", "OS Type", "Screen Size (inch)", "Resolution Type", "Refresh Rate (Hz)",
                "Battery (mAh)", "Support 5G (1/0)", "Thumbnail URL", "Installment Text", "Special Features",
                "Highlight Features", "Description"
        };
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 1;
        for (Product p : products) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(p.getId());
            row.createCell(1).setCellValue(p.getName());
            row.createCell(2).setCellValue(p.getSlug());
            if (p.getDisplayPrice() != null) row.createCell(3).setCellValue(p.getDisplayPrice().doubleValue());
            if (p.getOriginalPrice() != null) row.createCell(4).setCellValue(p.getOriginalPrice().doubleValue());
            row.createCell(5).setCellValue(p.getStatus() != null ? p.getStatus().name() : "");
            row.createCell(6).setCellValue(p.getProductType() != null ? p.getProductType().name() : "");
            row.createCell(7).setCellValue(p.getCategory() != null ? p.getCategory().getId() : null);
            row.createCell(8).setCellValue(p.getBrand() != null ? p.getBrand().getId() : null);
            row.createCell(9).setCellValue(p.getOsType() != null ? p.getOsType().name() : "");
            if (p.getScreenSize() != null) row.createCell(10).setCellValue(p.getScreenSize().doubleValue());
            row.createCell(11).setCellValue(p.getScreenResolutionType() != null ? p.getScreenResolutionType() : "");
            if (p.getRefreshRate() != null) row.createCell(12).setCellValue(p.getRefreshRate());
            if (p.getBatteryCapacity() != null) row.createCell(13).setCellValue(p.getBatteryCapacity());
            row.createCell(14).setCellValue(p.getSupport5g() != null && p.getSupport5g() ? 1 : 0);
            row.createCell(15).setCellValue(p.getThumbnailUrl() != null ? p.getThumbnailUrl() : "");
            row.createCell(16).setCellValue(p.getInstallmentText() != null ? p.getInstallmentText() : "");
            row.createCell(17).setCellValue(p.getSpecialFeatures() != null ? p.getSpecialFeatures() : "");
            row.createCell(18).setCellValue(p.getHighlightFeatures() != null ? p.getHighlightFeatures() : "");
            row.createCell(19).setCellValue(p.getDescription() != null ? p.getDescription() : "");
        }
        autoSizeColumns(sheet, headers.length);
    }

    private void createVariantSheet(Workbook workbook, CellStyle headerStyle, List<Product> products) {
        Sheet sheet = workbook.createSheet("Product_Variants");
        String[] headers = {"Variant ID", "Product ID", "SKU", "RAM", "ROM", "Color Name", "Color Hex", "Price", "Stock Quantity", "Image URL", "Is Active (1/0)"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 1;
        for (Product p : products) {
            if (p.getVariants() != null) {
                for (ProductVariant v : p.getVariants()) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(v.getId());
                    row.createCell(1).setCellValue(p.getId());
                    row.createCell(2).setCellValue(v.getSku());
                    row.createCell(3).setCellValue(v.getRam());
                    row.createCell(4).setCellValue(v.getRom());
                    row.createCell(5).setCellValue(v.getColorName());
                    row.createCell(6).setCellValue(v.getColorHex());
                    if (v.getPrice() != null) row.createCell(7).setCellValue(v.getPrice().doubleValue());
                    row.createCell(8).setCellValue(v.getStockQuantity() != null ? v.getStockQuantity() : 0);
                    row.createCell(9).setCellValue(v.getImageUrl() != null ? v.getImageUrl() : "");
                    row.createCell(10).setCellValue(v.isActive() ? 1 : 0);
                }
            }
        }
        autoSizeColumns(sheet, headers.length);
    }

    private void createImageSheet(Workbook workbook, CellStyle headerStyle, List<Product> products) {
        Sheet sheet = workbook.createSheet("Product_Images");
        String[] headers = {"Image ID", "Product ID", "Image URL", "Sort Order"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 1;
        for (Product p : products) {
            if (p.getImages() != null) {
                for (ProductImage img : p.getImages()) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(img.getId());
                    row.createCell(1).setCellValue(p.getId());
                    row.createCell(2).setCellValue(img.getImageUrl());
                    row.createCell(3).setCellValue(img.getSortOrder() != null ? img.getSortOrder() : 0);
                }
            }
        }
        autoSizeColumns(sheet, headers.length);
    }

    private void createHighlightSpecSheet(Workbook workbook, CellStyle headerStyle, List<Product> products) {
        Sheet sheet = workbook.createSheet("Product_Highlight_Specs");
        String[] headers = {"Highlight ID", "Product ID", "Label", "Value", "Icon URL"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 1;
        for (Product p : products) {
            if (p.getHighlightSpecs() != null) {
                for (ProductHighlightSpec spec : p.getHighlightSpecs()) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(spec.getId());
                    row.createCell(1).setCellValue(p.getId());
                    row.createCell(2).setCellValue(spec.getLabel());
                    row.createCell(3).setCellValue(spec.getValue());
                    row.createCell(4).setCellValue(spec.getIconUrl());
                }
            }
        }
        autoSizeColumns(sheet, headers.length);
    }

    private void createSpecValueSheet(Workbook workbook, CellStyle headerStyle, List<Product> products) {
        Sheet sheet = workbook.createSheet("Product_Spec_Values");
        String[] headers = {"Spec Value ID", "Product ID", "Attribute ID", "Value"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 1;
        for (Product p : products) {
            if (p.getSpecValues() != null) {
                for (ProductSpecValue s : p.getSpecValues()) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(s.getId());
                    row.createCell(1).setCellValue(p.getId());
                    row.createCell(2).setCellValue(s.getAttribute() != null ? s.getAttribute().getId() : null);
                    row.createCell(3).setCellValue(s.getValue());
                }
            }
        }
        autoSizeColumns(sheet, headers.length);
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private void autoSizeColumns(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}