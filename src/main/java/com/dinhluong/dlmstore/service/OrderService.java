package com.dinhluong.dlmstore.service;

import com.dinhluong.dlmstore.dto.requests.PlaceOrderRequest;
import com.dinhluong.dlmstore.dto.responses.ComboItemDetail;
import com.dinhluong.dlmstore.dto.responses.OrderItemResponse;
import com.dinhluong.dlmstore.dto.responses.OrderResponse;
import com.dinhluong.dlmstore.dto.requests.PlaceOrderItemRequest;
import com.dinhluong.dlmstore.entity.Order;
import com.dinhluong.dlmstore.entity.OrderItem;
import com.dinhluong.dlmstore.entity.Payment;
import com.dinhluong.dlmstore.entity.Product;
import com.dinhluong.dlmstore.entity.ProductVariant;
import com.dinhluong.dlmstore.entity.UserVoucher;
import com.dinhluong.dlmstore.entity.Voucher;
import com.dinhluong.dlmstore.repository.OrderItemRepository;
import com.dinhluong.dlmstore.repository.OrderRepository;

import com.dinhluong.dlmstore.repository.PaymentRepository;
import com.dinhluong.dlmstore.repository.ProductRepository;

import com.dinhluong.dlmstore.repository.ProductVariantRepository;

import com.dinhluong.dlmstore.repository.UserVoucherRepository;

import com.dinhluong.dlmstore.repository.VoucherRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import java.time.LocalDateTime;

import java.util.ArrayList;

import java.util.List;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;

    private final OrderItemRepository orderItemRepository;

    private final ProductVariantRepository productVariantRepository;

    private final VoucherRepository voucherRepository;

    private final UserVoucherRepository userVoucherRepository;

    private final PaymentRepository paymentRepository;

    private final ProductRepository productRepository;

    @Transactional

    public Order createOrder(PlaceOrderRequest request) {

        // 1. Tạo mới đơn hàng với totalAmount tạm thời là 0

        Order order = Order.builder()

                .userId(request.getUserId())

                .receiverName(request.getReceiverName())

                .receiverPhone(request.getReceiverPhone())

                .receiverAddress(request.getReceiverAddress())

                .status(Order.OrderStatus.PENDING)

                .totalAmount(BigDecimal.ZERO)

                .build();

        order = orderRepository.save(order);

        BigDecimal totalAmount = BigDecimal.ZERO;

        // 2. Xử lý từng Item

        for (PlaceOrderItemRequest itemDTO : request.getItems()) {

            if (itemDTO.getVariantId() == null) {

                throw new RuntimeException("Lỗi: variantId không được để trống!");

            }

            ProductVariant mainVariant = productVariantRepository.findById(itemDTO.getVariantId())

                    .orElseThrow(() -> new RuntimeException(

                            "Không tìm thấy sản phẩm variant có ID: " + itemDTO.getVariantId()));

            totalAmount = totalAmount.add(

                    mainVariant.getPrice().multiply(new BigDecimal(itemDTO.getQuantity())));

            List<ComboItemDetail> comboDetails = new ArrayList<>();

            if (itemDTO.getComboIds() != null && !itemDTO.getComboIds().isEmpty()) {

                for (Long comboId : itemDTO.getComboIds()) {

                    if (comboId == null)
                        continue;

                    Product comboVariant = productRepository.findById(comboId)

                            .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm combo có ID: " + comboId));

                    ComboItemDetail detail = new ComboItemDetail();

                    detail.setVariantId(comboVariant.getId());

                    detail.setPrice(comboVariant.getDisplayPrice());

                    detail.setName(comboVariant.getName());

                    detail.setImageUrl(comboVariant.getThumbnailUrl());

                    comboDetails.add(detail);

                    totalAmount = totalAmount.add(

                            comboVariant.getDisplayPrice().multiply(new BigDecimal(itemDTO.getQuantity())));

                }

            }

            OrderItem mainItem = OrderItem.builder()

                    .orderId(order.getId())

                    .productVariantId(mainVariant.getId())

                    .quantity(itemDTO.getQuantity())

                    .priceAtPurchase(mainVariant.getPrice())

                    .comboItems(comboDetails)

                    .build();

            orderItemRepository.save(mainItem);

        }

        // 3. Xử lý Voucher (🔥 Đã cập nhật)

        BigDecimal finalPrice = totalAmount;

        if (request.getVoucherCode() != null && !request.getVoucherCode().trim().isEmpty()) {

            // 3.1. Tìm Voucher gốc

            Voucher voucher = voucherRepository.findByCode(request.getVoucherCode());

            if (voucher == null) {

                throw new RuntimeException("Mã giảm giá không tồn tại!");

            }

            // 3.2. Kiểm tra xem User đã thu thập voucher này vào ví chưa và còn dùng được
            // không

            UserVoucher userVoucher = userVoucherRepository
                    .findByUserIdAndVoucherId(request.getUserId(), voucher.getId())

                    .orElseThrow(() -> new RuntimeException("Bạn chưa thu thập mã giảm giá này!"));

            if (userVoucher.getIsUsed() != null && userVoucher.getIsUsed()) {

                throw new RuntimeException("Mã giảm giá này đã được sử dụng!");

            }

            // 3.3. Kiểm tra điều kiện (HSD, Giá trị tối thiểu)

            if (!voucher.isApplicable(totalAmount)) {

                throw new RuntimeException("Đơn hàng không đủ điều kiện áp dụng mã giảm giá này!");

            }

            // 3.4. Tính toán tiền giảm

            BigDecimal discountValue = BigDecimal.ZERO;

            if (voucher.getDiscountType() == Voucher.DiscountType.FIXED) {

                discountValue = voucher.getDiscount();

            } else if (voucher.getDiscountType() == Voucher.DiscountType.PERCENT) {

                discountValue = totalAmount.multiply(voucher.getDiscount()).divide(new BigDecimal("100"));

            }

            if (discountValue.compareTo(totalAmount) > 0) {

                discountValue = totalAmount;

            }

            finalPrice = totalAmount.subtract(discountValue);

            // 3.5. Đánh dấu User đã sử dụng Voucher này (Xóa khỏi danh sách khả dụng của
            // họ)

            userVoucher.setIsUsed(true);

            userVoucher.setUsedAt(LocalDateTime.now());

            userVoucherRepository.save(userVoucher);

            // 3.6. Tăng lượt sử dụng chung của Voucher

            voucher.setUsedCount(voucher.getUsedCount() + 1);

            voucherRepository.save(voucher);

            // 3.7. Lưu Voucher ID vào đơn hàng

            order.setVoucherId(voucher.getId());

        }

        // 4. Cập nhật lại giá cuối cùng

        order.setTotalAmount(finalPrice);

        Order savedOrder = orderRepository.save(order);

        // 5. Tạo và lưu bản ghi Payment

        Payment.PaymentMethod method;

        try {

            method = Payment.PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase());

        } catch (IllegalArgumentException | NullPointerException e) {

            method = Payment.PaymentMethod.COD;

        }

        Payment payment = Payment.builder()

                .orderId(savedOrder.getId())

                .method(method)

                .amount(savedOrder.getTotalAmount())

                .status(Payment.PaymentStatus.PENDING)

                .build();

        paymentRepository.save(payment);

        return savedOrder;

    }

    public List<OrderResponse> getMyOrders(Long userId, String status) {
        List<Order> orders;

        if (status == null || status.equalsIgnoreCase("ALL")) {
            orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        } else {
            Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
            orders = orderRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, orderStatus);
        }

        return orders.stream().map(this::mapToOrderResponse).collect(Collectors.toList());
    }

    // 2. Lấy đơn hàng gần đây của User
    public List<OrderResponse> getRecentOrders(Long userId) {
        List<Order> orders = orderRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId);
        return orders.stream().map(this::mapToOrderResponse).collect(Collectors.toList());
    }

    // 3. Lấy chi tiết đơn hàng (Dùng chung cho cả User và Admin nếu muốn)
    public OrderResponse getOrderDetail(Long orderId, Long userId) {
        // Tìm đơn hàng
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        // Kiểm tra quyền sở hữu (Nếu là Admin thì bỏ qua logic này, ở đây giả định dùng
        // cho User)
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền xem đơn hàng này");
        }

        // Lấy danh sách items
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());

        // Map items sang DTO
        List<OrderItemResponse> itemResponses = orderItems.stream().map(item -> {
            ProductVariant variant = productVariantRepository.findById(item.getProductVariantId()).orElse(null);

            String productName = (variant != null && variant.getProduct() != null) ? variant.getProduct().getName()
                    : "Sản phẩm không xác định";
            String imageUrl = (variant != null && variant.getProduct() != null) ? variant.getProduct().getThumbnailUrl()
                    : "";
            String variantName = (variant != null)
                    ? String.format("%s - %s - %s", variant.getRom(), variant.getRam(), variant.getColorName())
                    : "";

            return OrderItemResponse.builder().id(item.getId()).productVariantId(item.getProductVariantId())
                    .productName(productName).imageUrl(imageUrl)
                    .variantName(variantName.replace("null - ", "").replace(" - null", "")).quantity(item.getQuantity())
                    .priceAtPurchase(item.getPriceAtPurchase()).comboItems(item.getComboItems()).build();
        }).toList();

        // 🔥 Lấy thông tin thanh toán
        Payment payment = paymentRepository.findByOrderId(order.getId()).orElse(null);
        String pMethod = payment != null ? payment.getMethod().name() : "COD";
        String pStatus = payment != null ? payment.getStatus().name() : "PENDING";

        return OrderResponse.builder().id(order.getId()).totalAmount(order.getTotalAmount()).status(order.getStatus())
                .createdAt(order.getCreatedAt()).receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone()).receiverAddress(order.getReceiverAddress())
                .paymentMethod(pMethod) // 🔥
                                        // THÊM
                .paymentStatus(pStatus) // 🔥 THÊM
                .items(itemResponses).build();
    }

    // Hàm helper map Entity sang DTO (Dùng cho API lấy danh sách)
    private OrderResponse mapToOrderResponse(Order order) {
        // 🔥 Lấy thông tin thanh toán
        Payment payment = paymentRepository.findByOrderId(order.getId()).orElse(null);
        String pMethod = payment != null ? payment.getMethod().name() : "COD";
        String pStatus = payment != null ? payment.getStatus().name() : "PENDING";

        return OrderResponse.builder().id(order.getId()).totalAmount(order.getTotalAmount()).status(order.getStatus())
                .createdAt(order.getCreatedAt()).receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone()).receiverAddress(order.getReceiverAddress())
                .paymentMethod(pMethod) // 🔥
                                        // THÊM
                .paymentStatus(pStatus) // 🔥 THÊM
                .build();
    }

    // ==========================================
    // ============= ADMIN APIs =================
    // ==========================================

    // Lấy danh sách đơn hàng cho Admin (Kèm chi tiết sản phẩm và thanh toán)
    public List<OrderResponse> getAdminOrders(String statusStr, String keyword) {
        Order.OrderStatus status = null;
        if (statusStr != null && !statusStr.equalsIgnoreCase("ALL")) {
            try {
                status = Order.OrderStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Bỏ qua
            }
        }

        List<Order> orders = orderRepository.searchAdminOrders(status, keyword);

        return orders.stream().map(order -> {

            // 🔥 Lấy thông tin thanh toán (ĐỂ BÊN TRONG VÒNG LẶP MỚI ĐÚNG)
            Payment payment = paymentRepository.findByOrderId(order.getId()).orElse(null);
            String pMethod = payment != null ? payment.getMethod().name() : "COD";
            String pStatus = payment != null ? payment.getStatus().name() : "PENDING";

            // Lấy chi tiết Item
            List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
            List<OrderItemResponse> itemResponses = orderItems.stream().map(item -> {
                ProductVariant variant = productVariantRepository.findById(item.getProductVariantId()).orElse(null);
                String productName = (variant != null && variant.getProduct() != null) ? variant.getProduct().getName()
                        : "Sản phẩm không xác định";
                String imageUrl = (variant != null && variant.getProduct() != null)
                        ? variant.getProduct().getThumbnailUrl()
                        : "";
                String variantName = (variant != null)
                        ? String.format("%s - %s - %s", variant.getRom(), variant.getRam(), variant.getColorName())
                        : "";

                return OrderItemResponse.builder().id(item.getId()).productVariantId(item.getProductVariantId())
                        .productName(productName).variantName(variantName.replace("null - ", "").replace(" - null", ""))
                        .imageUrl(imageUrl).quantity(item.getQuantity()).priceAtPurchase(item.getPriceAtPurchase())
                        .comboItems(item.getComboItems()).build();
            }).toList();

            return OrderResponse.builder().id(order.getId()).totalAmount(order.getTotalAmount())
                    .status(order.getStatus()).createdAt(order.getCreatedAt()).receiverName(order.getReceiverName())
                    .receiverPhone(order.getReceiverPhone()).receiverAddress(order.getReceiverAddress())
                    .paymentMethod(pMethod) // 🔥
                                            // THÊM
                    .paymentStatus(pStatus) // 🔥 THÊM
                    .items(itemResponses).build();
        }).collect(Collectors.toList());
    }

   @Transactional
    public OrderResponse updateOrderStatus(Long orderId, String newStatusStr) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        Order.OrderStatus currentStatus = order.getStatus();
        Order.OrderStatus newStatus = Order.OrderStatus.valueOf(newStatusStr.toUpperCase());

        // 1. Ngăn chặn cập nhật trạng thái đơn đã đóng
        if (currentStatus == Order.OrderStatus.DELIVERED ||
                currentStatus == Order.OrderStatus.CANCELLED ||
                currentStatus == Order.OrderStatus.RETURNED) {
            throw new RuntimeException("Không thể thay đổi trạng thái của đơn hàng đã đóng!");
        }

        // 2. Lấy danh sách sản phẩm trong đơn hàng
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);

        // ==============================================================
        // KỊCH BẢN A: Chuyển sang PROCESSING -> TRỪ KHO (Cả Chính & Phụ kiện)
        // ==============================================================
        if (currentStatus == Order.OrderStatus.PENDING && newStatus == Order.OrderStatus.PROCESSING) {
            for (OrderItem item : items) {
                // Trừ kho sản phẩm chính
                decreaseStock(item.getProductVariantId(), item.getQuantity(), "Sản phẩm chính");

                // Trừ kho phụ kiện (nếu có)
                if (item.getComboItems() != null && !item.getComboItems().isEmpty()) {
                    System.out.println("DEBUG: Tìm thấy " + item.getComboItems().size() + " phụ kiện");
                    for (ComboItemDetail combo : item.getComboItems()) {
                        String accessorySku = "PK-" + combo.getVariantId();
                        
                        // Dùng orElseThrow để bắt lỗi ngay lập tức nếu dữ liệu SKU phụ kiện sai
                        ProductVariant accessoryVariant = productVariantRepository.findBySku(accessorySku)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy phụ kiện với SKU: " + accessorySku));
                        
                        System.out.println("DEBUG: Đang trừ kho cho Variant ID (Phụ kiện): " + accessoryVariant.getId());
                        decreaseStock(accessoryVariant.getId(), item.getQuantity(), "Phụ kiện: " + combo.getName());
                    }
                } else {
                    System.out.println("DEBUG: comboItems đang bị NULL hoặc rỗng cho OrderItem ID: " + item.getId());
                }
            }
        }

        // ==============================================================
        // KỊCH BẢN B: HỦY ĐƠN HOẶC HOÀN HÀNG -> CỘNG LẠI KHO
        // ==============================================================
        else if (newStatus == Order.OrderStatus.CANCELLED || newStatus == Order.OrderStatus.RETURNED) {
            // Chỉ hoàn kho nếu trạng thái trước đó đã trừ kho (PROCESSING hoặc SHIPPED)
            if (currentStatus == Order.OrderStatus.PROCESSING || currentStatus == Order.OrderStatus.SHIPPED) {
                for (OrderItem item : items) {
                    // Hoàn kho sản phẩm chính
                    increaseStock(item.getProductVariantId(), item.getQuantity());

                    // Hoàn kho phụ kiện (nếu có)
                    if (item.getComboItems() != null && !item.getComboItems().isEmpty()) {
                        for (ComboItemDetail combo : item.getComboItems()) {
                            String accessorySku = "PK-" + combo.getVariantId();
                            
                            // Chỉ cộng lại kho nếu thực sự tìm thấy phụ kiện đó trong database
                            ProductVariant accessoryVariant = productVariantRepository.findBySku(accessorySku).orElse(null);
                            if (accessoryVariant != null) {
                                System.out.println("DEBUG: Đang hoàn kho cho Variant ID (Phụ kiện): " + accessoryVariant.getId());
                                increaseStock(accessoryVariant.getId(), item.getQuantity()); // ĐÃ FIX: dùng increaseStock
                            } else {
                                System.out.println("WARN: Không tìm thấy phụ kiện hoàn kho với SKU: " + accessorySku);
                            }
                        }
                    }
                }
            }
        }

        // 3. Cập nhật trạng thái mới
        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        return mapToOrderResponse(updatedOrder);
    }

    /**
     * Hàm hỗ trợ trừ kho và tăng soldQuantity
     */
    private void decreaseStock(Long variantId, int quantity, String errorContext) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể: " + variantId));

        if (variant.getStockQuantity() < quantity) {
            throw new RuntimeException(errorContext + " không đủ số lượng trong kho!");
        }

        // Trừ tồn kho
        variant.setStockQuantity(variant.getStockQuantity() - quantity);

        // Tăng số lượng đã bán cho sản phẩm cha
        Product product = variant.getProduct();
        product.setSoldQuantity((product.getSoldQuantity() != null ? product.getSoldQuantity() : 0) + quantity);

        productVariantRepository.save(variant);
        updateProductTotalStock(product);
    }

    /**
     * Hàm hỗ trợ cộng lại kho và giảm soldQuantity
     */
    private void increaseStock(Long variantId, int quantity) {
        ProductVariant variant = productVariantRepository.findById(variantId).orElse(null);
        if (variant != null) {
            // Cộng lại tồn kho
            variant.setStockQuantity(variant.getStockQuantity() + quantity);

            // Giảm lượt bán của sản phẩm cha
            Product product = variant.getProduct();
            int currentSold = product.getSoldQuantity() != null ? product.getSoldQuantity() : 0;
            product.setSoldQuantity(Math.max(0, currentSold - quantity));

            productVariantRepository.save(variant);
            updateProductTotalStock(product);
        }
    }

    /**
     * Cập nhật tổng tồn kho hiển thị của sản phẩm (Sum of all variants)
     */
    private void updateProductTotalStock(Product product) {
        if (product.getVariants() != null) {
            int newTotalStock = product.getVariants().stream()
                    .mapToInt(v -> v.getStockQuantity() != null ? v.getStockQuantity() : 0)
                    .sum();
            product.setTotalStock(newTotalStock);
            productRepository.save(product);
        }
    }
}