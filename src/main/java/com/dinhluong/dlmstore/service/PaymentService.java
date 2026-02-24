package com.dinhluong.dlmstore.service;

import com.dinhluong.dlmstore.dto.responses.PaymentResponse;
import com.dinhluong.dlmstore.entity.Order;
import com.dinhluong.dlmstore.entity.Payment;
import com.dinhluong.dlmstore.repository.OrderRepository;
import com.dinhluong.dlmstore.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository; // Inject thêm để lấy Tên KH

    public List<PaymentResponse> getAdminPayments(String methodStr, String statusStr, String keyword) {
        
        // 1. Ép kiểu Enum Phương thức thanh toán
        Payment.PaymentMethod method = null;
        if (methodStr != null && !methodStr.equalsIgnoreCase("ALL")) {
            try { method = Payment.PaymentMethod.valueOf(methodStr.toUpperCase()); } 
            catch (IllegalArgumentException ignored) {}
        }

        // 2. Ép kiểu Enum Trạng thái
        Payment.PaymentStatus status = null;
        if (statusStr != null && !statusStr.equalsIgnoreCase("ALL")) {
            try { status = Payment.PaymentStatus.valueOf(statusStr.toUpperCase()); } 
            catch (IllegalArgumentException ignored) {}
        }

        // 3. Truy vấn Database
        List<Payment> payments = paymentRepository.searchAdminPayments(method, status, keyword);

        // 4. Map dữ liệu sang DTO
        return payments.stream().map(payment -> {
            String customerName = "Khách hàng ẩn danh";
            
            // Tìm Order để lấy Tên người nhận
            Order order = orderRepository.findById(payment.getOrderId()).orElse(null);
            if (order != null) {
                customerName = order.getReceiverName();
            }

            return PaymentResponse.builder()
                    .id(payment.getId())
                    .orderId(payment.getOrderId())
                    .customerName(customerName) // Ghép tên KH vào đây
                    .method(payment.getMethod().name())
                    .amount(payment.getAmount())
                    .status(payment.getStatus().name())
                    .transactionId(payment.getTransactionId())
                    .paidAt(payment.getPaidAt())
                    .build();
        }).collect(Collectors.toList());
    }
}