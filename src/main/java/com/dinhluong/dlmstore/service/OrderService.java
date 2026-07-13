package com.dinhluong.dlmstore.service;

import com.dinhluong.dlmstore.dto.requests.PlaceOrderRequest;
import com.dinhluong.dlmstore.dto.responses.ComboItemDetail;
import com.dinhluong.dlmstore.dto.responses.OrderItemResponse;
import com.dinhluong.dlmstore.dto.responses.OrderResponse;
import com.dinhluong.dlmstore.dto.responses.OrderStatsResponse;
import com.dinhluong.dlmstore.dto.requests.PlaceOrderItemRequest;
import com.dinhluong.dlmstore.entity.Enums.ProductStatus;
import com.dinhluong.dlmstore.entity.Order;
import com.dinhluong.dlmstore.entity.Order.OrderStatus;
import com.dinhluong.dlmstore.entity.OrderItem;
import com.dinhluong.dlmstore.entity.Payment;
import com.dinhluong.dlmstore.entity.Product;
import com.dinhluong.dlmstore.entity.ProductVariant;
import com.dinhluong.dlmstore.entity.UserVoucher;
import com.dinhluong.dlmstore.entity.Voucher;
import com.dinhluong.dlmstore.exception.ValidationException;
import com.dinhluong.dlmstore.repository.CartItemRepository;
import com.dinhluong.dlmstore.repository.CartRepository;
import com.dinhluong.dlmstore.repository.OrderItemRepository;
import com.dinhluong.dlmstore.repository.OrderRepository;

import com.dinhluong.dlmstore.repository.PaymentRepository;
import com.dinhluong.dlmstore.repository.ProductRepository;

import com.dinhluong.dlmstore.repository.ProductVariantRepository;

import com.dinhluong.dlmstore.repository.UserVoucherRepository;

import com.dinhluong.dlmstore.repository.VoucherRepository;
import com.dinhluong.dlmstore.service.impl.Excels.OrderExcelExportService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;

import java.time.LocalDateTime;

import java.util.ArrayList;

import java.util.List;

import com.dinhluong.dlmstore.entity.Cart;
import com.dinhluong.dlmstore.entity.CartItem;
import com.dinhluong.dlmstore.entity.Notification;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderExcelExportService orderExcelExportService;
    private final OrderRepository orderRepository;

    private final OrderItemRepository orderItemRepository;

    private final ProductVariantRepository productVariantRepository;

    private final VoucherRepository voucherRepository;

    private final UserVoucherRepository userVoucherRepository;

    private final PaymentRepository paymentRepository;

    private final ProductRepository productRepository;
    private final NotificationService notificationService;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    @Transactional

    public Order createOrder(PlaceOrderRequest request) {

        // 1. Tạo mới đơn hàng với totalAmount tạm thời là 0

        Order order = Order.builder()

                .userId(request.getUserId())

                .receiverName(request.getReceiverName())

                .receiverPhone(request.getReceiverPhone())

                .receiverAddress(request.getReceiverAddress())
                .userNote(request.getNote()) // 🔥 THÊM GHI CHÚ CỦA USER
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

            // 1. Tìm Variant
            ProductVariant mainVariant = productVariantRepository.findById(itemDTO.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại hoặc vừa mới ngừng kinh doanh!"));

            // 2. 🔥 KIỂM TRA SẢN PHẨM CHA (PRODUCT) TRƯỚC KHI TRUY CẬP
            Product parentProduct = mainVariant.getProduct();

            if (parentProduct == null) {
                // Không được gọi parentProduct.getName() ở đây vì nó đang null
                throw new RuntimeException(
                        "Một sản phẩm trong đơn hàng của bạn vừa ngừng kinh doanh."
                );
            }

            // 3. (Tùy chọn) Kiểm tra trạng thái ẩn/hiện
            if (parentProduct.getStatus() != ProductStatus.ACTIVE) {
                throw new ValidationException("Sản phẩm [" + parentProduct.getName() + "] hiện đang tạm ngưng bán!");
            }
            
            // 🔥 1. CHỐT CHẶN: TRỪ KHO SẢN PHẨM CHÍNH NGAY LÚC TẠO ĐƠN (Sẽ trigger Optimistic Locking nếu có tranh chấp)
            decreaseStock(mainVariant.getId(), itemDTO.getQuantity(), "Sản phẩm " + mainVariant.getProduct().getName());

            totalAmount = totalAmount.add(mainVariant.getPrice().multiply(new BigDecimal(itemDTO.getQuantity())));

            List<ComboItemDetail> comboDetails = new ArrayList<>();

            if (itemDTO.getComboIds() != null && !itemDTO.getComboIds().isEmpty()) {
                for (Long comboId : itemDTO.getComboIds()) {
                    if (comboId == null) continue;

                    Product comboVariant = productRepository.findById(comboId)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm combo có ID: " + comboId));

                    // 🔥 2. TRỪ KHO LUÔN CHO COMBO/PHỤ KIỆN
                    String accessorySku = "PK-" + comboVariant.getId();
                    ProductVariant accessoryRealVariant = productVariantRepository.findBySku(accessorySku)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy phụ kiện với SKU: " + accessorySku));
                    
                    decreaseStock(accessoryRealVariant.getId(), itemDTO.getQuantity(), "Phụ kiện: " + comboVariant.getName());

                    ComboItemDetail detail = new ComboItemDetail();
                    detail.setVariantId(comboVariant.getId());
                    detail.setPrice(comboVariant.getDisplayPrice());
                    detail.setName(comboVariant.getName());
                    detail.setImageUrl(comboVariant.getThumbnailUrl());
                    comboDetails.add(detail);
                    totalAmount = totalAmount.add(comboVariant.getDisplayPrice().multiply(new BigDecimal(itemDTO.getQuantity())));
                }
            }

            OrderItem mainItem = OrderItem.builder()
                    .orderId(order.getId())
                    .productVariantId(mainVariant.getId())
                    .quantity(itemDTO.getQuantity())
                    .priceAtPurchase(mainVariant.getPrice())
                    .slug(mainVariant.getProduct().getSlug())
                    .comboItems(comboDetails)
                    .productName(mainVariant.getProduct().getName()) // Chụp tên
                    .productImage(mainVariant.getProduct().getThumbnailUrl()) // Chụp ảnh
                    .variantName(String.format("%s - %s - %s", mainVariant.getRom(), mainVariant.getRam(), mainVariant.getColorName())
                            .replace("null - ", "").replace(" - null", ""))
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
            order.setDiscountAmount(discountValue);
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

        // 🔥 BƯỚC 6: XÓA CÁC SẢN PHẨM ĐÃ ĐẶT KHỎI GIỎ HÀNG
        Cart cart = cartRepository.findByUserId(request.getUserId()).orElse(null);
        if (cart != null) {
            for (PlaceOrderItemRequest itemDTO : request.getItems()) {
                // Tìm cartItem chính
                CartItem cartItem = cartItemRepository
                        .findByCartIdAndProductVariantIdAndParentIdIsNull(cart.getId(), itemDTO.getVariantId())
                        .orElse(null);
                
                if (cartItem != null) {
                    // Xóa các phụ kiện/combo đi kèm trong giỏ hàng (parentId = id của cartItem chính)
                    List<CartItem> combosInCart = cartItemRepository.findByParentId(cartItem.getId());
                    cartItemRepository.deleteAll(combosInCart);
                    
                    // Xóa item chính
                    cartItemRepository.delete(cartItem);
                }
            }
        }

        // 🔥 THÊM ĐOẠN NÀY ĐỂ BẮN THÔNG BÁO NGAY KHI ĐẶT HÀNG THÀNH CÔNG
        String message = "Đặt hàng thành công! Đơn hàng #" + savedOrder.getId()
                + " của bạn đang chờ hệ thống xác nhận.";
        notificationService.createAndSendNotification(
                savedOrder.getUserId(),
                Notification.NotificationType.ORDER_STATUS,
                message);
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
            // Kiểm tra xem sản phẩm còn kinh doanh không (Soft Delete sẽ trả về false)
            boolean available = productVariantRepository.existsAndActive(item.getProductVariantId());

            return OrderItemResponse.builder()
                    .id(item.getId())
                    .productVariantId(item.getProductVariantId())
                    .productName(item.getProductName() != null ? item.getProductName() : "Sản phẩm không xác định")
                    .imageUrl(item.getProductImage() != null ? item.getProductImage() : "")
                    .variantName(item.getVariantName() != null ? item.getVariantName() : "")
                    .quantity(item.getQuantity())
                    .priceAtPurchase(item.getPriceAtPurchase())
                    .slug(item.getSlug()) // 🔥 TRẢ LẠI SLUG THẬT, KHÔNG ÉP RỖNG NỮA
                    .isAvailable(available) // 🔥 THÊM TRƯỜNG NÀY (Nhớ cập nhật DTO)
                    .comboItems(item.getComboItems())
                    .build();
        }).toList();

        // 🔥 Lấy thông tin thanh toán
        Payment payment = paymentRepository.findByOrderId(order.getId()).orElse(null);
        String pMethod = payment != null ? payment.getMethod().name() : "COD";
        String pStatus = payment != null ? payment.getStatus().name() : "PENDING";

        return OrderResponse.builder().id(order.getId()).totalAmount(order.getTotalAmount()).status(order.getStatus())
                .createdAt(order.getCreatedAt()).receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone()).receiverAddress(order.getReceiverAddress())
                .paymentMethod(pMethod) // 🔥
                  .userNote(order.getUserNote())             // THIẾU DÒNG NÀY SẼ BỊ NULL
        .discountAmount(order.getDiscountAmount()) // THIẾU DÒNG NÀY SẼ BỊ NULL
        .cancelledBy(order.getCancelledBy())                         // THÊM
                .paymentStatus(pStatus) // 🔥 THÊM
                .reason(order.getReason()) // 🔥 THÊM
                .items(itemResponses).build();
    }

    // Hàm helper map Entity sang DTO (Dùng cho API lấy danh sách my-orders)
    private OrderResponse mapToOrderResponse(Order order) {
        Payment payment = paymentRepository.findByOrderId(order.getId()).orElse(null);
        String pMethod = payment != null ? payment.getMethod().name() : "COD";
        String pStatus = payment != null ? payment.getStatus().name() : "PENDING";

        // 🔥 LẤY DANH SÁCH SẢN PHẨM (ĐỂ FRONTEND CÓ SLUG ĐIỀU HƯỚNG)
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
        List<OrderItemResponse> itemResponses = orderItems.stream().map(item -> {
            boolean available = productVariantRepository.existsAndActive(item.getProductVariantId());
            return OrderItemResponse.builder()
                    .id(item.getId())
                    .productName(item.getProductName() != null ? item.getProductName() : "Sản phẩm không xác định")
                    .slug(item.getSlug()) // 🔥 TRẢ LẠI SLUG THẬT
                    .isAvailable(available)
                    .build();
        }).toList();

        return OrderResponse.builder()
                .id(order.getId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .receiverAddress(order.getReceiverAddress())
                .paymentMethod(pMethod)
                .paymentStatus(pStatus)
                .reason(order.getReason())
                .userNote(order.getUserNote())
                .deliveredAt(order.getDeliveredAt())
                .discountAmount(order.getDiscountAmount())
                .cancelledBy(order.getCancelledBy()) // 🔥 THÊM NGƯỜI HỦY
                .items(itemResponses) // 🔥 NHÉT DANH SÁCH ITEM VÀO RESPONSE
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
            List<OrderItemResponse> itemResponses = orderItems.stream().map(item -> OrderItemResponse.builder()
                    .id(item.getId())
                    .productVariantId(item.getProductVariantId())
                    .productName(item.getProductName() != null ? item.getProductName() : "Sản phẩm không xác định")
                    .variantName(item.getVariantName() != null ? item.getVariantName() : "")
                    .imageUrl(item.getProductImage() != null ? item.getProductImage() : "")
                    .quantity(item.getQuantity())
                    .priceAtPurchase(item.getPriceAtPurchase())
                    .comboItems(item.getComboItems())
                    .build()).toList();

            return OrderResponse.builder().id(order.getId()).totalAmount(order.getTotalAmount())
                    .status(order.getStatus()).createdAt(order.getCreatedAt()).receiverName(order.getReceiverName())
                    .receiverPhone(order.getReceiverPhone()).receiverAddress(order.getReceiverAddress())
                    .paymentMethod(pMethod) // 🔥
                     .reason(order.getReason())
                     .userNote(order.getUserNote())
                     .deliveredAt(order.getDeliveredAt())    
                     .discountAmount(order.getDiscountAmount())   
                     .cancelledBy(order.getCancelledBy())                // THÊM
                    .paymentStatus(pStatus) // 🔥 THÊM
                    .items(itemResponses).build();
        }).collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, String newStatusStr, String reason, String actionBy) {
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


        String vnStatus = translateStatus(newStatus);
        // Bước 1: Khởi tạo tin nhắn cơ bản từ hàm helper
        String message = generateNotificationMessage(newStatus, orderId, vnStatus, actionBy, reason);
        // ==============================================================
        // KỊCH BẢN B: HỦY ĐƠN HOẶC HOÀN HÀNG -> CỘNG LẠI KHO
        // ==============================================================
        if (newStatus == Order.OrderStatus.CANCELLED || newStatus == Order.OrderStatus.RETURNED) {
           if (currentStatus == Order.OrderStatus.PENDING || 
                currentStatus == Order.OrderStatus.PROCESSING || 
                currentStatus == Order.OrderStatus.SHIPPED) {
                for (OrderItem item : items) {
                    increaseStock(item.getProductVariantId(), item.getQuantity());

                    if (item.getComboItems() != null && !item.getComboItems().isEmpty()) {
                        for (ComboItemDetail combo : item.getComboItems()) {
                            String accessorySku = "PK-" + combo.getVariantId();
                            ProductVariant accessoryVariant = productVariantRepository.findBySku(accessorySku).orElse(null);
                            if (accessoryVariant != null) {
                                increaseStock(accessoryVariant.getId(), item.getQuantity());
                            }
                        }
                    }
                }
               // --- Kiểm tra tiền đã thu chưa để báo khách ---
               Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);
               String refundGuide = "";

               if (payment != null && "PAID".equals(payment.getStatus().name())) {
                   // Chuyển sang REFUND_PENDING để Admin dễ lọc danh sách nợ tiền
                   payment.setStatus(Payment.PaymentStatus.REFUND_PENDING);
                   paymentRepository.save(payment);

                   message += " Vì đơn đã thanh toán, anh/chị vui lòng liên hệ Chat/Hotline để nhận lại tiền hoàn ạ.";
               }

               // 3. Ghi lý do và người hủy
               order.setReason(reason);
               order.setCancelledBy(actionBy);



            }
        }

        // ==============================================================
        // 🔥 KỊCH BẢN C: GIAO HÀNG THÀNH CÔNG -> MỚI BẮT ĐẦU CỘNG LƯỢT BÁN
        // ==============================================================
        if (newStatus == Order.OrderStatus.DELIVERED && currentStatus != Order.OrderStatus.DELIVERED) {
            order.setDeliveredAt(LocalDateTime.now());
            for (OrderItem item : items) {
                // Tăng sold cho sản phẩm chính
                increaseSoldQuantity(item.getProductVariantId(), item.getQuantity());

                // Tăng sold cho các phụ kiện mua kèm (nếu có)
                if (item.getComboItems() != null && !item.getComboItems().isEmpty()) {
                    for (ComboItemDetail combo : item.getComboItems()) {
                        String accessorySku = "PK-" + combo.getVariantId();
                        ProductVariant accessoryVariant = productVariantRepository.findBySku(accessorySku).orElse(null);
                        if (accessoryVariant != null) {
                            increaseSoldQuantity(accessoryVariant.getId(), item.getQuantity());
                        }
                    }
                }
            }
        }

        // 3. Cập nhật trạng thái mới
        order.setStatus(newStatus);
        
        // Gán lý do nếu có
        if (reason != null && !reason.trim().isEmpty()) {
            order.setReason(reason);
        }

        // Gán người hủy nếu trạng thái là HỦY hoặc HOÀN
        if (newStatus == Order.OrderStatus.CANCELLED || newStatus == Order.OrderStatus.RETURNED) {
            order.setCancelledBy(actionBy);
        }

        // 🔥 GÁN XONG XUÔI HẾT RỒI MỚI LƯU (SAVE)
        Order updatedOrder = orderRepository.save(order);
        // 4. 🔥 TẠO VÀ GỬI THÔNG BÁO CHO USER (Đã FIX lỗi truyền tham số)

        // Nếu có message thì mới gửi
        if (message != null && !message.isEmpty()) {
            notificationService.createAndSendNotification(
                    updatedOrder.getUserId(),
                    Notification.NotificationType.ORDER_STATUS,
                    message);
        }

        return mapToOrderResponse(updatedOrder);
    }

    // Các hàm trợ giúp giữ nguyên
    private String translateStatus(Order.OrderStatus status) {
        switch (status) {
            case PENDING:
                return "Chờ xác nhận";
            case PROCESSING:
                return "Đang xử lý (Chờ lấy hàng)";
            case SHIPPED:
                return "Đang giao hàng";
            case DELIVERED:
                return "Đã giao thành công";
            case RETURNED:
                return "Chuyển hoàn (Trả hàng/Bom)";
            case CANCELLED:
                return "Đã hủy";
            default:
                return "Không xác định";
        }
    }

    // 🔥 CẬP NHẬT: Thêm tham số reason
    private String generateNotificationMessage(Order.OrderStatus status, Long orderId, String vnStatus,
            String actionBy, String reason) {
        
        String reasonText = (reason != null && !reason.trim().isEmpty()) ? " Lý do: " + reason : "";
        
        switch (status) {
            case PROCESSING:
                return "Đơn hàng #" + orderId + " đã được xác nhận và đang được chuẩn bị đóng gói.";
            case SHIPPED:
                return "Đơn hàng #" + orderId + " đã được giao cho shipper. Vui lòng chú ý điện thoại nhé!";
            case DELIVERED:
                return "Giao hàng thành công! Cảm ơn bạn đã mua sắm tại DinhLuongMobile.";
            case CANCELLED:
                if ("USER".equalsIgnoreCase(actionBy)) {
                    return "Đơn hàng #" + orderId + " đã được hủy thành công theo yêu cầu của bạn." + reasonText;
                } else {
                    return "Rất tiếc! Đơn hàng #" + orderId + " đã bị hủy bởi DinhLuongMobile." + reasonText;
                }
            case RETURNED:
                return "Giao hàng không thành công. Đơn hàng #" + orderId
                        + " đang được chuyển hoàn về kho." + reasonText;
            default:
                return "Đơn hàng #" + orderId + " đã chuyển sang trạng thái: " + vnStatus;
        }
    }

    /**
     * Hàm hỗ trợ trừ kho (KHÔNG CỘNG SOLD QUANTITY Ở ĐÂY NỮA)
     */
    private void decreaseStock(Long variantId, int quantity, String errorContext) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể: " + variantId));

        if (variant.getStockQuantity() < quantity) {
            throw new RuntimeException(errorContext + " không đủ số lượng trong kho!");
        }

        // Chỉ trừ tồn kho
        variant.setStockQuantity(variant.getStockQuantity() - quantity);
        productVariantRepository.save(variant);
        updateProductTotalStock(variant.getProduct()); // Cập nhật tổng tồn kho hiển thị
    }

    /**
     * Hàm hỗ trợ cộng lại kho (KHÔNG TRỪ SOLD QUANTITY Ở ĐÂY NỮA)
     */
    private void increaseStock(Long variantId, int quantity) {
        ProductVariant variant = productVariantRepository.findById(variantId).orElse(null);
        if (variant != null) {
            // Chỉ cộng lại tồn kho
            variant.setStockQuantity(variant.getStockQuantity() + quantity);
            productVariantRepository.save(variant);
            updateProductTotalStock(variant.getProduct()); // Cập nhật tổng tồn kho hiển thị
        }
    }
   
    
// BỔ SUNG: Hàm mới chuyên dùng để tăng số lượng đã bán
    private void increaseSoldQuantity(Long variantId, int quantity) {
        ProductVariant variant = productVariantRepository.findById(variantId).orElse(null);
        if (variant != null && variant.getProduct() != null) {
            Product product = variant.getProduct();
            int currentSold = product.getSoldQuantity() != null ? product.getSoldQuantity() : 0;
            product.setSoldQuantity(currentSold + quantity);
            productRepository.save(product);
        }
    }
    
    private void updateProductTotalStock(Product product) {
        if (product.getVariants() != null) {
            int newTotalStock = product.getVariants().stream()
                    .mapToInt(v -> v.getStockQuantity() != null ? v.getStockQuantity() : 0)
                    .sum();
            product.setTotalStock(newTotalStock);
            productRepository.save(product);
        }
    }


    public OrderStatsResponse getOrderStatistics() {
    List<Object[]> results = orderRepository.countOrdersByStatus();
    
    long pending = 0, processing = 0, shipped = 0, delivered = 0, cancelledOrReturned = 0, total = 0;

    for (Object[] row : results) {
        Order.OrderStatus status = (Order.OrderStatus) row[0];
        long count = (Long) row[1];
        total += count;

        switch (status) {
            case PENDING -> pending = count;
            case PROCESSING -> processing = count;
            case SHIPPED -> shipped = count;
            case DELIVERED -> delivered = count;
            case CANCELLED, RETURNED -> cancelledOrReturned += count;
        }
    }

    return OrderStatsResponse.builder()
            .pending(pending).processing(processing).shipped(shipped)
            .delivered(delivered).cancelledOrReturned(cancelledOrReturned)
            .total(total).build();
}

/**
 * Cập nhật trạng thái cho nhiều đơn hàng cùng lúc
 */
@Transactional
public void updateStatusBatch(List<Long> ids, OrderStatus newStatusStr, String reason, String actionBy) {
    if (ids == null || ids.isEmpty()) return;
    
    for (Long id : ids) {
        try {
            // Tận dụng lại 100% logic hoàn kho, cộng lượt bán, gửi thông báo đã viết
            updateOrderStatus(id, newStatusStr.name(), reason, actionBy);
        } catch (Exception e) {
            // Nếu có 1 đơn bị lỗi (vd: đơn đã Giao không thể Hủy), 
            // thì log ra và bỏ qua, tiếp tục chạy các đơn khác trong danh sách
            System.err.println("Không thể cập nhật đơn hàng #" + id + ": " + e.getMessage());
        }
    }
}

public ByteArrayInputStream exportOrdersExcel(String status, String keyword) {

    List<OrderResponse> orders = getAdminOrders(status, keyword);

    return orderExcelExportService.exportToExcel(
            orders,
            null
    );
}

}