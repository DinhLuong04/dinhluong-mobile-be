package com.dinhluong.dlmstore.controller;

import com.dinhluong.dlmstore.dto.ApiResponse;
import com.dinhluong.dlmstore.dto.requests.PlaceOrderRequest;
import com.dinhluong.dlmstore.dto.responses.OrderResponse;
import com.dinhluong.dlmstore.entity.Order;
import com.dinhluong.dlmstore.entity.Payment;
import com.dinhluong.dlmstore.repository.PaymentRepository;
import com.dinhluong.dlmstore.security.CustomUserPrincipal;
import com.dinhluong.dlmstore.service.OrderService;
import com.dinhluong.dlmstore.utils.VNPayConfig;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final PaymentRepository paymentRepository;

    @Value("${vnpay.tmnCode}")
    private String vnp_TmnCode;

    @Value("${vnpay.hashSecret}")
    private String secretKey;

    @Value("${vnpay.payUrl}")
    private String vnp_PayUrl;

    @Value("${vnpay.returnUrl}")
    private String vnp_ReturnUrl;

    @PostMapping("/place")
    public ResponseEntity<?> placeOrder(
            @RequestBody PlaceOrderRequest request,
            HttpServletRequest httpRequest) {

        try {
            // 1. Lưu đơn hàng và khởi tạo Payment (PENDING) trong Service
            Order savedOrder = orderService.createOrder(request);

            Map<String, Object> response = new HashMap<>();

            // 2. COD
            if ("cod".equalsIgnoreCase(request.getPaymentMethod())) {
                response.put("status", "success");
                response.put("message", "Đặt hàng COD thành công");
                response.put("paymentUrl", null);
                return ResponseEntity.ok(response);
            }

            // 3. VNPay
            if ("vnpay".equalsIgnoreCase(request.getPaymentMethod())) {
                String paymentUrl = createVNPayUrl(savedOrder, httpRequest);

                response.put("status", "success");
                response.put("message", "Vui lòng thanh toán qua VNPay");
                response.put("paymentUrl", paymentUrl);

                return ResponseEntity.ok(response);
            }

            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Phương thức thanh toán không hợp lệ!"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // nhận kết quả trả về từ VNPay
    @GetMapping("/vnpay-return")
    public ResponseEntity<?> vnpayReturn(HttpServletRequest request) {
        try {
            Map<String, String> fields = new HashMap<>();
            for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
                String fieldName = params.nextElement();
                String fieldValue = request.getParameter(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    fields.put(fieldName, fieldValue);
                }
            }

            String vnp_SecureHash = request.getParameter("vnp_SecureHash");
            if (fields.containsKey("vnp_SecureHashType")) {
                fields.remove("vnp_SecureHashType");
            }
            if (fields.containsKey("vnp_SecureHash")) {
                fields.remove("vnp_SecureHash");
            }

            // Hash lại data để kiểm tra chữ ký
            String signValue = VNPayConfig.hmacSHA512(secretKey, VNPayConfig.buildHashData(fields));

            if (signValue.equals(vnp_SecureHash)) {
                Long orderId = Long.parseLong(request.getParameter("vnp_TxnRef"));
                String transactionNo = request.getParameter("vnp_TransactionNo");
                String responseCode = request.getParameter("vnp_ResponseCode");
                Payment payment = paymentRepository.findByOrderId(orderId)
                        .orElseThrow(
                                () -> new RuntimeException("Không tìm thấy thông tin thanh toán cho đơn hàng này"));

                if ("00".equals(responseCode)) {
                    // Thanh toán thành công
                    payment.setStatus(Payment.PaymentStatus.PAID);
                    payment.setTransactionId(transactionNo);
                    payment.setPaidAt(LocalDateTime.now());
                    paymentRepository.save(payment);
                    return ResponseEntity.ok(Map.of(
                            "status", "success",
                            "message", "Thanh toán thành công!",
                            "orderId", orderId));
                } else {
                    // Thanh toán thất bại hoặc bị hủy
                    payment.setStatus(Payment.PaymentStatus.FAILED);
                    paymentRepository.save(payment);

                    return ResponseEntity.badRequest().body(Map.of(
                            "status", "failed",
                            "message", "Thanh toán thất bại hoặc đã bị hủy. Mã lỗi: " + responseCode));
                }
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "Chữ ký không hợp lệ (Invalid signature)!"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Có lỗi xảy ra trong quá trình xử lý: " + e.getMessage()));
        }
    }

    // VNPay
    private String createVNPayUrl(Order order, HttpServletRequest request) throws Exception {

        long amount = order.getTotalAmount().longValue() * 100;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");

        vnp_Params.put("vnp_TxnRef", String.valueOf(order.getId()));

        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang #" + order.getId());
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", VNPayConfig.getIpAddress(request));

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");

        vnp_Params.put("vnp_CreateDate", formatter.format(calendar.getTime()));

        calendar.add(Calendar.MINUTE, 15);
        vnp_Params.put("vnp_ExpireDate", formatter.format(calendar.getTime()));

        // Build hash data
        String hashData = VNPayConfig.buildHashData(vnp_Params);
        String vnp_SecureHash = VNPayConfig.hmacSHA512(secretKey, hashData);

        // Build query
        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, String> entry : vnp_Params.entrySet()) {
            query.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            query.append('=');
            query.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            query.append('&');
        }

        query.append("vnp_SecureHash=").append(vnp_SecureHash);

        return vnp_PayUrl + "?" + query.toString();
    }

    // 1. Lấy danh sách đơn hàng của user đang đăng nhập
    @GetMapping("/my-orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal CustomUserPrincipal currentUser) {

        try {
            List<OrderResponse> responseData = orderService.getMyOrders(currentUser.getId(), status);
            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đơn hàng thành công", responseData));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Trạng thái đơn hàng không hợp lệ"));
        }
    }

    // Lấy 5 đơn hàng gần đây (Cho màn Overview)
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getRecentOrders(
            @AuthenticationPrincipal CustomUserPrincipal currentUser) {

        List<OrderResponse> responseData = orderService.getRecentOrders(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Lấy đơn hàng gần đây thành công", responseData));
    }

    // Lấy chi tiết đơn hàng
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderDetail(
            @PathVariable Long orderId,
            @AuthenticationPrincipal CustomUserPrincipal currentUser) {

        try {
            OrderResponse responseData = orderService.getOrderDetail(orderId, currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết đơn hàng thành công", responseData));

        } catch (RuntimeException e) {
            int errorCode = e.getMessage().contains("quyền") ? 403 : 404;
            return ResponseEntity.status(errorCode)
                    .body(ApiResponse.error(errorCode, e.getMessage()));
        }
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelMyOrder(
            @PathVariable Long orderId,
            @RequestBody(required = false) Map<String, String> request, // 🔥 BƯỚC 1: THÊM REQUEST BODY NÀY
            @AuthenticationPrincipal CustomUserPrincipal currentUser) {

        try {

            String reason = (request != null && request.containsKey("reason")) ? request.get("reason") : "";
            orderService.getOrderDetail(orderId, currentUser.getId());

            OrderResponse updatedOrder = orderService.updateOrderStatus(orderId, "CANCELLED", reason, "USER");

            return ResponseEntity.ok(ApiResponse.success("Hủy đơn hàng thành công", updatedOrder));

        } catch (RuntimeException e) {

            int errorCode = e.getMessage().contains("quyền") ? 403 : 400;
            return ResponseEntity.status(errorCode)
                    .body(ApiResponse.error(errorCode, e.getMessage()));
        }
    }

}