package com.dinhluong.dlmstore.service.impl.Excels;

import com.dinhluong.dlmstore.dto.responses.ImportReportResponse;
import com.dinhluong.dlmstore.dto.responses.ImportValidationError;
import com.dinhluong.dlmstore.entity.*;
import com.dinhluong.dlmstore.entity.Enums.OsType;
import com.dinhluong.dlmstore.entity.Enums.ProductStatus;
import com.dinhluong.dlmstore.entity.Enums.ProductType;
import com.dinhluong.dlmstore.repository.*;
import com.dinhluong.dlmstore.service.ExcelImportService;
import com.dinhluong.dlmstore.service.tools.ProductDataEnricher;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductExcelImportServiceImpl
        implements ExcelImportService<ImportReportResponse> {

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final SpecAttributeRepository specAttributeRepository;
    private final ProductVariantRepository productVariantRepository; // Thêm để check SKU duplicate DB
    private final ProductDataEnricher productDataEnricher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportReportResponse importFromExcel(MultipartFile file) {

        validateFile(file);

        ImportReportResponse report = new ImportReportResponse();
        Set<String> slugSet = new HashSet<>();
        Set<String> skuSet = new HashSet<>();

        try (
                InputStream is = file.getInputStream();
                Workbook workbook = new XSSFWorkbook(is)
        ) {
            // Yêu cầu 10: Validate sheet bắt buộc
            validateRequiredSheets(workbook);

            Map<Long, Product> productMap = new HashMap<>();

            // =====================================================
            // 1. PRODUCTS (Bắt buộc)
            // =====================================================
            Sheet productSheet = workbook.getSheet("Products");
            // Đã được validate khác null ở validateRequiredSheets()
            parseProductSheet(productSheet, productMap, slugSet, report);

            // =====================================================
            // 2. VARIANTS (Optional)
            // =====================================================
            Sheet variantSheet = workbook.getSheet("Product_Variants");
            if (variantSheet != null) {
                parseVariantSheet(variantSheet, productMap, skuSet, report);
            }

            // =====================================================
            // 3. IMAGES (Optional)
            // =====================================================
            Sheet imageSheet = workbook.getSheet("Product_Images");
            if (imageSheet != null) {
                parseImageSheet(imageSheet, productMap, report);
            }

            // =====================================================
            // 4. HIGHLIGHTS (Optional)
            // =====================================================
            Sheet highlightSheet = workbook.getSheet("Product_Highlight_Specs");
            if (highlightSheet != null) {
                parseHighlightSheet(highlightSheet, productMap, report);
            }

            // =====================================================
            // 5. SPEC VALUES (Optional)
            // =====================================================
            Sheet specValueSheet = workbook.getSheet("Product_Spec_Values");
            if (specValueSheet != null) {
                parseSpecValueSheet(specValueSheet, productMap, report);
            }

            // =====================================================
            // Yêu cầu 14: BLOCKED NẾU CÓ LỖI VALIDATE
            // =====================================================
            if (!report.getErrors().isEmpty()) {
                report.setMessage(
                        "Import thất bại. Có "
                                + report.getErrors().size()
                                + " lỗi validate."
                );
                return report;
            }

            // =====================================================
            // ENRICH & SAVE
            // =====================================================
            for (Product product : productMap.values()) {
                productDataEnricher.enrichProductBeforeSave(product, null);
            }
            System.out.println("SAVE SIZE = " + productMap.size());

            for (Product p : productMap.values()) {
                System.out.println(
                        "PRODUCT: "
                                + p.getId()
                                + " | "
                                + p.getName()
                                + " | "
                                + p.getDisplayPrice()
                );
            }
            productRepository.saveAll(productMap.values());

            // =====================================================
            // REPORT SUCESS
            // =====================================================
            report.setTotalProcessed(
                    report.getTotalAdded()
                            + report.getTotalUpdated()
                            + report.getTotalSkipped()
            );

            report.setMessage(
                    "IMPORT THÀNH CÔNG\n\n"
                            + "===== PRODUCT =====\n"
                            + "➕ Thêm mới: " + report.getTotalAdded() + "\n"
                            + "🔄 Cập nhật: " + report.getTotalUpdated() + "\n"
                            + "⏭️ Bỏ qua: " + report.getTotalSkipped() + "\n\n"
                            + "===== VARIANT =====\n"
                            + "➕ Variant mới: " + report.getVariantAdded() + "\n"
                            + "🔄 Variant cập nhật: " + report.getVariantUpdated() + "\n\n"
                            + "📦 Tổng xử lý: " + report.getTotalProcessed()
            );

            return report;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi xử lý file Excel: " + e.getMessage());
        }
    }

    // =====================================================
    // PRODUCT SHEET
    // =====================================================

    private void parseProductSheet(
            Sheet sheet,
            Map<Long, Product> productMap,
            Set<String> slugSet,
            ImportReportResponse report
    ) {
        int addCount = 0;
        int updateCount = 0;
        int skippedCount = 0;

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            long excelId = (long) getNumericValue(row.getCell(0));
            String name = getStringValue(row.getCell(1));
            String slug = getStringValue(row.getCell(2));
            BigDecimal displayPrice = BigDecimal.valueOf(getNumericValue(row.getCell(3)));
            BigDecimal originalPrice = BigDecimal.valueOf(getNumericValue(row.getCell(4)));

            // Yêu cầu 12: Validate dữ liệu tối thiểu
            boolean hasError = false;

            if (name.isEmpty()) {
                addError(report, "Products", i, "name", "Tên sản phẩm không được để trống");
                hasError = true;
            }

            if (slug.isEmpty() || !slug.matches("^[a-z0-9-]+$")) {
                addError(report, "Products", i, "slug", "Slug trống hoặc sai định dạng");
                hasError = true;
            }

            if (displayPrice.compareTo(BigDecimal.ZERO) < 0) {
                addError(report, "Products", i, "displayPrice", "Giá bán không được âm");
                hasError = true;
            }

            if (originalPrice.compareTo(displayPrice) < 0) {
                addError(report, "Products", i, "originalPrice", "Giá gốc phải lớn hơn hoặc bằng giá bán");
                hasError = true;
            }

            if (hasError) continue; // Skip dòng này nếu có lỗi validate

            Product product = null;
            boolean isNew = false;
            boolean changed = false;

            // Tìm kiếm Product hiện có
            if (excelId > 0) {
                product = productRepository.findById(excelId).orElse(null);
            }
            if (product == null && !slug.isEmpty()) {
                product = productRepository.findBySlug(slug).orElse(null);
            }
            if (product == null && !name.isEmpty()) {
                product = productRepository.findByName(name).orElse(null);
            }

            // Khởi tạo mới
            if (product == null) {
                product = new Product();
                isNew = true;
            }

            // Yêu cầu 11: Tránh NullPointerException cho các list
            if (product.getVariants() == null) product.setVariants(new ArrayList<>());
            if (product.getImages() == null) product.setImages(new ArrayList<>());
            if (product.getHighlightSpecs() == null) product.setHighlightSpecs(new ArrayList<>());
            if (product.getSpecValues() == null) product.setSpecValues(new ArrayList<>());

            // Set dữ liệu cơ bản
            if (isDifferent(product.getName(), name)) {
                product.setName(name);
                changed = true;
            }

            // Yêu cầu 8: Fix duplicate slug
            if (isDifferent(product.getSlug(), slug)) {
                product.setSlug(makeUniqueSlug(slug, slugSet));
                changed = true;
            } else {
                slugSet.add(product.getSlug()); // Đánh dấu đã dùng trong file
            }

            if (isDifferent(product.getDisplayPrice(), displayPrice)) {
                product.setDisplayPrice(displayPrice);
                changed = true;
            }

            if (isDifferent(product.getOriginalPrice(), originalPrice)) {
                product.setOriginalPrice(originalPrice);
                changed = true;
            }

            // Các cột khác (Status, ProductType, Categogy, Brand...) giữ nguyên logic
            try {
                ProductStatus status = ProductStatus.valueOf(getStringValue(row.getCell(5)));
                if (isDifferent(product.getStatus(), status)) { product.setStatus(status); changed = true; }
            } catch (Exception ignored) {}

            try {
                ProductType type = ProductType.valueOf(getStringValue(row.getCell(6)));
                if (isDifferent(product.getProductType(), type)) { product.setProductType(type); changed = true; }
            } catch (Exception ignored) {}

            Long categoryId = (long) getNumericValue(row.getCell(7));
            if (categoryId > 0) {
                Category category = categoryRepository.findById(categoryId).orElse(null);
                if (category != null && (product.getCategory() == null || !product.getCategory().getId().equals(categoryId))) {
                    product.setCategory(category);
                    changed = true;
                }
            }

            Long brandId = (long) getNumericValue(row.getCell(8));
            if (brandId > 0) {
                Brand brand = brandRepository.findById(brandId).orElse(null);
                if (brand != null && (product.getBrand() == null || !product.getBrand().getId().equals(brandId))) {
                    product.setBrand(brand);
                    changed = true;
                }
            }

            // =====================================================
            // OS TYPE
            // =====================================================

            try {

                OsType osType =
                        OsType.valueOf(
                                getStringValue(row.getCell(9))
                        );

                if (isDifferent(product.getOsType(), osType)) {
                    product.setOsType(osType);
                    changed = true;
                }

            } catch (Exception ignored) {}

            // =====================================================
            // SCREEN SIZE
            // =====================================================

            Double screenSize =
                    getNumericValue(row.getCell(10));

            if (isDifferent(product.getScreenSize(), screenSize)) {
                product.setScreenSize(screenSize);
                changed = true;
            }

            // =====================================================
            // RESOLUTION
            // =====================================================

            String resolution =
                    getStringValue(row.getCell(11));

            if (isDifferent(
                    product.getScreenResolutionType(),
                    resolution
            )) {

                product.setScreenResolutionType(resolution);

                changed = true;
            }

            // =====================================================
            // REFRESH RATE
            // =====================================================

            Integer refreshRate =
                    (int) getNumericValue(row.getCell(12));

            if (isDifferent(
                    product.getRefreshRate(),
                    refreshRate
            )) {

                product.setRefreshRate(refreshRate);

                changed = true;
            }

            // =====================================================
            // BATTERY
            // =====================================================

            Integer battery =
                    (int) getNumericValue(row.getCell(13));

            if (isDifferent(
                    product.getBatteryCapacity(),
                    battery
            )) {

                product.setBatteryCapacity(battery);

                changed = true;
            }

            // =====================================================
            // SUPPORT 5G
            // =====================================================

            Boolean support5g =
                    getNumericValue(row.getCell(14)) == 1;

            if (isDifferent(
                    product.getSupport5g(),
                    support5g
            )) {

                product.setSupport5g(support5g);

                changed = true;
            }

            // =====================================================
            // THUMBNAIL
            // =====================================================

            String thumbnail =
                    getStringValue(row.getCell(15));

            if (isDifferent(
                    product.getThumbnailUrl(),
                    thumbnail
            )) {

                product.setThumbnailUrl(thumbnail);

                changed = true;
            }

            // =====================================================
            // INSTALLMENT
            // =====================================================

            String installment =
                    getStringValue(row.getCell(16));

            if (isDifferent(
                    product.getInstallmentText(),
                    installment
            )) {

                product.setInstallmentText(installment);

                changed = true;
            }

            // =====================================================
            // SPECIAL FEATURES
            // =====================================================

            String special =
                    getStringValue(row.getCell(17));

            if (isDifferent(
                    product.getSpecialFeatures(),
                    special
            )) {

                product.setSpecialFeatures(special);

                changed = true;
            }

            // =====================================================
            // HIGHLIGHT FEATURES
            // =====================================================

            String highlight =
                    getStringValue(row.getCell(18));

            if (isDifferent(
                    product.getHighlightFeatures(),
                    highlight
            )) {

                product.setHighlightFeatures(highlight);

                changed = true;
            }

            // =====================================================
            // DESCRIPTION
            // =====================================================

            String description =
                    getStringValue(row.getCell(19));

            if (isDifferent(
                    product.getDescription(),
                    description
            )) {

                product.setDescription(description);

                changed = true;
            }

            // Cập nhật Report
            if (isNew) addCount++;
            else if (changed) updateCount++;
            else skippedCount++;

            // Yêu cầu 7: Fix mapping Product <-> Variant bằng Temporary Negative Key
            long mapKey = excelId > 0 ? excelId : -(i + 1);
            productMap.put(mapKey, product);
        }

        report.setTotalAdded(addCount);
        report.setTotalUpdated(updateCount);
        report.setTotalSkipped(skippedCount);
    }

    // =====================================================
    // VARIANT
    // =====================================================

    private void parseVariantSheet(
            Sheet sheet,
            Map<Long, Product> productMap,
            Set<String> skuSet,
            ImportReportResponse report
    ) {
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            long productExcelId = (long) getNumericValue(row.getCell(1));
            Product parentProduct = productMap.get(productExcelId);

            // Bỏ qua nếu mapping không tìm thấy Product cha hợp lệ
            if (parentProduct == null) continue;

            String sku = getStringValue(row.getCell(2));
            BigDecimal price = BigDecimal.valueOf(getNumericValue(row.getCell(7)));
            int stock = (int) getNumericValue(row.getCell(8));

            // Yêu cầu 12, 13: Validate Variant
            boolean hasError = false;

            if (sku.isEmpty()) {
                addError(report, "Product_Variants", i, "sku", "SKU không được để trống");
                hasError = true;
            } else if (!skuSet.add(sku)) {
                addError(report, "Product_Variants", i, "sku", "SKU bị trùng lặp trong file Excel");
                hasError = true;
            }

            if (stock < 0) {
                addError(report, "Product_Variants", i, "stockQuantity", "Tồn kho không được âm");
                hasError = true;
            }

            if (price != null && price.compareTo(BigDecimal.ZERO) < 0) {
                addError(report, "Product_Variants", i, "price", "Giá variant không được âm");
                hasError = true;
            }

            // Kiểm tra trùng SKU trong hệ thống cho variant mới
            ProductVariant variant = parentProduct.getVariants().stream()
                    .filter(v -> sku.equals(v.getSku()))
                    .findFirst()
                    .orElse(null);

            if (variant == null && productVariantRepository.existsBySku(sku)) {
                addError(report, "Product_Variants", i, "sku", "SKU đã tồn tại trên hệ thống cho một sản phẩm khác");
                hasError = true;
            }

            if (hasError) continue; // Skip variant lỗi, không rollback toàn bộ

            boolean isNew = false;
            if (variant == null) {
                variant = new ProductVariant();
                variant.setSku(sku);
                variant.setProduct(parentProduct);
                parentProduct.getVariants().add(variant);
                isNew = true;
            }

            variant.setRam(getStringValue(row.getCell(3)));
            variant.setRom(getStringValue(row.getCell(4)));
            variant.setColorName(getStringValue(row.getCell(5)));
            variant.setColorHex(getStringValue(row.getCell(6)));
            variant.setPrice(price);
            variant.setStockQuantity(stock);
            variant.setImageUrl(getStringValue(row.getCell(9)));
            variant.setActive(getNumericValue(row.getCell(10)) == 1);

            if (isNew) {
                report.setVariantAdded(report.getVariantAdded() + 1);
            } else {
                report.setVariantUpdated(report.getVariantUpdated() + 1);
            }
        }
    }

    // =====================================================
    // IMAGE, HIGHLIGHT, SPEC VALUE giữ nguyên logic nhưng
    // đảm bảo map thông qua ID âm/dương
    // =====================================================

    private void parseImageSheet(Sheet sheet, Map<Long, Product> productMap, ImportReportResponse report) {
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            long productExcelId = (long) getNumericValue(row.getCell(1));
            Product parentProduct = productMap.get(productExcelId);
            if (parentProduct == null) continue;

            String imageUrl = getStringValue(row.getCell(2));
            if (imageUrl.isEmpty()) continue;

            boolean exists = parentProduct.getImages().stream().anyMatch(img -> imageUrl.equals(img.getImageUrl()));
            if (exists) continue;

            ProductImage img = new ProductImage();
            img.setImageUrl(imageUrl);
            img.setSortOrder((int) getNumericValue(row.getCell(3)));
            img.setProduct(parentProduct);
            parentProduct.getImages().add(img);

            report.setImageAdded(report.getImageAdded() + 1);
        }
    }

    private void parseHighlightSheet(Sheet sheet, Map<Long, Product> productMap, ImportReportResponse report) {
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            long productExcelId = (long) getNumericValue(row.getCell(1));
            Product parentProduct = productMap.get(productExcelId);
            if (parentProduct == null) continue;

            String label = getStringValue(row.getCell(2));
            if (label.isEmpty()) continue;

            ProductHighlightSpec spec = parentProduct.getHighlightSpecs().stream()
                    .filter(s -> label.equalsIgnoreCase(s.getLabel())).findFirst().orElse(null);

            boolean isNew = false;
            if (spec == null) {
                spec = new ProductHighlightSpec();
                spec.setProduct(parentProduct);
                parentProduct.getHighlightSpecs().add(spec);
                isNew = true;
            }

            spec.setLabel(label);
            spec.setValue(getStringValue(row.getCell(3)));
            spec.setIconUrl(getStringValue(row.getCell(4)));

            if (isNew) report.setHighlightAdded(report.getHighlightAdded() + 1);
            else report.setHighlightUpdated(report.getHighlightUpdated() + 1);
        }
    }

    private void parseSpecValueSheet(Sheet sheet, Map<Long, Product> productMap, ImportReportResponse report) {
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            long productExcelId = (long) getNumericValue(row.getCell(1));
            Product parentProduct = productMap.get(productExcelId);
            if (parentProduct == null) continue;

            long attributeId = (long) getNumericValue(row.getCell(2));
            String value = getStringValue(row.getCell(3));
            if (attributeId <= 0) continue;

            SpecAttribute attribute = specAttributeRepository.findById(attributeId).orElse(null);
            if (attribute == null) continue;

            ProductSpecValue specValue = parentProduct.getSpecValues().stream()
                    .filter(v -> v.getAttribute() != null && v.getAttribute().getId().equals(attributeId))
                    .findFirst().orElse(null);

            boolean isNew = false;
            if (specValue == null) {
                specValue = new ProductSpecValue();
                specValue.setProduct(parentProduct);
                specValue.setAttribute(attribute);
                parentProduct.getSpecValues().add(specValue);
                isNew = true;
            }

            specValue.setValue(value);

            if (isNew) report.setSpecValueAdded(report.getSpecValueAdded() + 1);
            else report.setSpecValueUpdated(report.getSpecValueUpdated() + 1);
        }
    }

    // =====================================================
    // HELPER METHODS
    // =====================================================

    private void validateRequiredSheets(Workbook workbook) {
        if (workbook.getSheet("Products") == null) {
            throw new RuntimeException("Thiếu sheet bắt buộc: Products");
        }
    }

    private String makeUniqueSlug(String slug, Set<String> slugSet) {
        String baseSlug = slug;
        int counter = 1;

        while (slugSet.contains(slug) || productRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }
        slugSet.add(slug);
        return slug;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File Excel không được để trống");
        }
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.endsWith(".xlsx")) {
            throw new RuntimeException("Chỉ hỗ trợ file .xlsx");
        }
        if (file.getSize() > 20 * 1024 * 1024) {
            throw new RuntimeException("File vượt quá 20MB");
        }
    }

    private void addError(ImportReportResponse report, String sheet, int row, String field, String message) {
        report.getErrors().add(new ImportValidationError(sheet, row + 1, field, message));
    }

    private String getStringValue(Cell cell) {
        if (cell == null) return "";
        try {
            if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue().trim();
            if (cell.getCellType() == CellType.NUMERIC) return String.valueOf((long) cell.getNumericCellValue());
            if (cell.getCellType() == CellType.BOOLEAN) return String.valueOf(cell.getBooleanCellValue());
        } catch (Exception ignored) {}
        return "";
    }

    private double getNumericValue(Cell cell) {
        if (cell == null) return 0;
        try {
            if (cell.getCellType() == CellType.NUMERIC) return cell.getNumericCellValue();
            if (cell.getCellType() == CellType.STRING) return Double.parseDouble(cell.getStringCellValue());
        } catch (Exception ignored) {}
        return 0;
    }

    private boolean isDifferent(Object oldValue, Object newValue) {
        if (oldValue == null && newValue == null) return false;
        if (oldValue == null || newValue == null) return true;
        return !oldValue.toString().trim().equals(newValue.toString().trim());
    }
}