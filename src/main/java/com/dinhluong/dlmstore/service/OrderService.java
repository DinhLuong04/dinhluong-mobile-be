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
                    if (comboId == null) continue;

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

            // 3.2. Kiểm tra xem User đã thu thập voucher này vào ví chưa và còn dùng được không
            UserVoucher userVoucher = userVoucherRepository.findByUserIdAndVoucherId(request.getUserId(), voucher.getId())
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

            // 3.5. Đánh dấu User đã sử dụng Voucher này (Xóa khỏi danh sách khả dụng của họ)
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

    // 1. Lấy danh sách đơn hàng (Có filter status)
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

    // 2. Lấy đơn hàng gần đây
    public List<OrderResponse> getRecentOrders(Long userId) {
        List<Order> orders = orderRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId);
        return orders.stream().map(this::mapToOrderResponse).collect(Collectors.toList());
    }

    public OrderResponse getOrderDetail(Long orderId, Long userId) {
        // Tìm đơn hàng
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        // Kiểm tra quyền sở hữu
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền xem đơn hàng này");
        }

        // Lấy danh sách items
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());

        // Map items sang DTO (Bổ sung query ProductVariant để lấy Tên và Ảnh)
        List<OrderItemResponse> itemResponses = orderItems.stream().map(item -> {

            // Tìm thông tin biến thể sản phẩm để lấy Tên và Ảnh
            ProductVariant variant = productVariantRepository.findById(item.getProductVariantId())
                    .orElse(null);

            String productName = (variant != null && variant.getProduct() != null)
                    ? variant.getProduct().getName()
                    : "Sản phẩm không xác định";
            String imageUrl = (variant != null && variant.getProduct() != null)
                    ? variant.getProduct().getThumbnailUrl()
                    : "";

            return OrderItemResponse.builder()
                    .id(item.getId())
                    .productVariantId(item.getProductVariantId())
                    .productName(productName) // 🔥 Đã thêm
                    .imageUrl(imageUrl) // 🔥 Đã thêm
                    // .variantName(variant.getName()) // Nếu bảng ProductVariant của bạn có cột lưu
                    // tên phân loại ("Đỏ", "Xanh") thì mở comment dòng này
                    .quantity(item.getQuantity())
                    .priceAtPurchase(item.getPriceAtPurchase())
                    .comboItems(item.getComboItems())
                    .build();
        }).toList();

        // Lắp ráp kết quả cuối cùng (Bổ sung thông tin người nhận)
        return OrderResponse.builder()
                .id(order.getId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .receiverName(order.getReceiverName()) // 🔥 Đã thêm
                .receiverPhone(order.getReceiverPhone()) // 🔥 Đã thêm
                .receiverAddress(order.getReceiverAddress()) // 🔥 Đã thêm
                .items(itemResponses) // Lưu ý: Nên đổi "Items" thành "items" (chữ i thường) trong OrderResponse DTO
                .build();
    }

    // Hàm helper map Entity sang DTO (Dùng cho API lấy danh sách list order)
    private OrderResponse mapToOrderResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .receiverName(order.getReceiverName()) // 🔥 Đã thêm
                .receiverPhone(order.getReceiverPhone()) // 🔥 Đã thêm
                .receiverAddress(order.getReceiverAddress()) // 🔥 Đã thêm
                .build();
    }

}