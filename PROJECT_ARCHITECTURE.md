# 🏗️ KIẾN TRÚC & THÀNH PHẦN DỰ ÁN CHI TIẾT

## 📊 DỰ ÁN OVERVIEW

**Tên**: Ecomerce Shop - Backend  
**Công nghệ**: Spring Boot 3.3.6 + Java 21  
**Kiến trúc**: Layered Architecture (3-tier + security layer)  
**Database**: MySQL + Redis  
**Authentication**: JWT + OAuth2 (Google, Facebook)  

---

## 🗂️ KIẾN TRÚC TỔNG QUAN

```
┌─────────────────────────────────────────────┐
│         REST API Clients (Frontend)         │
└────────────┬────────────────────────────────┘
             │ HTTP/HTTPS
┌────────────▼────────────────────────────────┐
│         Controller Layer (@RestController)  │
│  - Handle HTTP requests                     │
│  - Input validation (@Valid)                │
│  - Response formatting (ApiResponse<T>)     │
└────────────┬────────────────────────────────┘
             │ Dependency Injection
┌────────────▼────────────────────────────────┐
│         Service Layer (@Service)            │
│  - Business logic                           │
│  - Transaction management (@Transactional)  │
│  - Data mapping (MapStruct @Mapper)         │
│  - Integration with external APIs           │
└────────────┬────────────────────────────────┘
             │ Query/Commands
┌────────────▼────────────────────────────────┐
│       Repository Layer (@Repository)        │
│  - JpaRepository & JpaSpecificationExecutor │
│  - Native queries & JPQL                    │
│  - Pagination & Sorting                     │
└────────────┬────────────────────────────────┘
             │ SQL
┌────────────▼────────────────────────────────┐
│        Data Access Layer                    │
│  - MySQL (Primary Database)                 │
│  - Redis (Caching & Sessions)               │
└─────────────────────────────────────────────┘

Security Layer (Cross-cutting):
├─ Spring Security Configuration
├─ JWT Authentication Filter
├─ Global Exception Handler
└─ CORS & CSRF Protection
```

---

## 🎯 CÁC MODULES CHÍNH

### **1. AUTHENTICATION & AUTHORIZATION**

**Files chính:**
- `AuthController.java` - REST endpoints
- `AuthService.java` - Interface
- `AuthService impl` - Business logic
- `SecurityConfig.java` - Spring Security setup
- `JwtAuthenticationFilter.java` - JWT validation filter
- `JwtAuthenticationEntryPoint.java` - Exception handling

**Flow - Normal Login:**
```
1. POST /api/auth/login
   ↓
2. AuthController.login(LoginRequest)
   ↓
3. AuthService.login(email, password)
   ↓
4. UserRepository.findByEmail(email)
   ↓
5. PasswordEncoder.matches(password, hashed)
   ↓
6. JwtTokenProvider.generateToken(user)
   ↓
7. Return LoginResponse {token, user}
```

**Flow - Google OAuth2 Login:**
```
1. Frontend gửi Google token
   ↓
2. POST /api/auth/oauth2/google
   ↓
3. GoogleService.validateAndGetUser(token)
   ↓
4. Check user trong DB
   ↓
5. Nếu chưa có → Create new user với authProvider="GOOGLE"
   ↓
6. JwtTokenProvider.generateToken(user)
   ↓
7. Return JWT token
```

**Key Points:**
- JWT token stored in Authorization header: `Bearer <token>`
- Token format: `Header.Payload.Signature`
- Payload chứa: userId, email, roles, exp
- Validate mỗi request qua JwtAuthenticationFilter
- Role-based access: PUBLIC, USER, ADMIN

---

### **2. PRODUCT MANAGEMENT**

**Entities:**
```
Product
├─ id (Long, PK)
├─ name (String)
├─ slug (String, unique) - for SEO-friendly URLs
├─ description (Text)
├─ category (ManyToOne → Category)
├─ brand (ManyToOne → Brand)
├─ originalPrice (BigDecimal)
├─ displayPrice (BigDecimal)
├─ stock (Integer)
├─ highlightSpecs (OneToMany → ProductHighlightSpec)
├─ images (OneToMany → ProductImage)
├─ variants (OneToMany → ProductVariant)
├─ specifications (OneToMany → ProductSpecValue)
├─ comments (OneToMany → ProductComment)
├─ status (Enum: ACTIVE, INACTIVE, DELETED)
├─ productType (Enum: MAIN, COMBO, BUNDLE)
├─ isDeleted (Boolean, soft delete)
└─ version (Long, optimistic locking)
```

**Repository Methods:**
```java
// Search by keyword
Page<Product> searchByKeyword(String keyword, Pageable pageable);

// Find by slug
Optional<Product> findBySlug(String slug);

// Complex filtering with specifications
Page<Product> findWithFilters(
    ProductType productType,
    String keyword,
    ProductStatus status,
    Long brandId,
    Long categoryId,
    Pageable pageable);
```

**Mapper - ProductMapper:**
```java
// Entity → DTO mapping
ProductCardResponse toCardResponse(Product product);
ProductDetailResponse toDetailResponse(Product product);

// Custom mapping logic
@AfterMapping
protected void calculateCardFields(Product product, 
                                   @MappingTarget ProductCardResponse response) {
    // Calculate discount percentage
    // Format prices
    // Build colors list
    // Build variants list
}
```

---

### **3. SHOPPING CART**

**Entities:**
```
Cart (1 user = 1 cart)
├─ id
├─ user (OneToOne → Users)
└─ items (OneToMany → CartItem)

CartItem
├─ id
├─ cart (ManyToOne → Cart)
├─ product (ManyToOne → Product)
├─ productVariant (optional, ManyToOne → ProductVariant)
├─ quantity
├─ addedAt
└─ updatedAt
```

**Operations:**
```
1. Add to cart
   - Find or create cart for user
   - Check if product variant exists
   - Add new CartItem or update quantity
   - Check stock availability

2. Update quantity
   - Find CartItem
   - Validate new quantity vs available stock
   - Update quantity

3. Remove from cart
   - Delete CartItem

4. Get cart
   - Fetch cart with all items
   - Calculate total price
   - Apply discounts/vouchers
```

---

### **4. ORDER & PAYMENT**

**Entities:**
```
Order
├─ id
├─ user (ManyToOne → Users)
├─ orderCode (unique)
├─ items (OneToMany → OrderItem)
├─ totalAmount
├─ discountAmount (from voucher)
├─ shippingAddress (ManyToOne → Address)
├─ billingAddress (ManyToOne → Address)
├─ status (PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED)
├─ payment (OneToOne → Payment)
├─ createdAt
└─ updatedAt

OrderItem
├─ id
├─ order
├─ product
├─ productVariant
├─ quantity
├─ unitPrice
├─ totalPrice

Payment
├─ id
├─ order
├─ amount
├─ method (CREDIT_CARD, E_WALLET, BANK_TRANSFER, COD)
├─ transactionId (from gateway)
├─ status (PENDING, COMPLETED, FAILED, REFUNDED)
└─ createdAt
```

**Order Flow:**
```
1. CREATE ORDER
   POST /api/orders
   ├─ Get user cart
   ├─ Validate all items in stock
   ├─ Validate voucher (if provided)
   ├─ Calculate total price
   ├─ Create Order record
   ├─ Create OrderItems from CartItems
   ├─ Clear user cart
   └─ Return Order with payment link

2. PROCESS PAYMENT (VNPay)
   POST /api/payments/vnpay
   ├─ Generate VNPay payment URL
   ├─ Store payment info in DB
   └─ Redirect to VNPay gateway

3. PAYMENT CALLBACK (VNPay → Server)
   GET /api/payments/vnpay/callback
   ├─ Validate signature
   ├─ Update Payment status
   ├─ Update Order status
   ├─ Reduce product stock
   ├─ Send email confirmation
   └─ Send notification

4. GET ORDER STATUS
   GET /api/orders/{orderId}
   └─ Return Order with payment details
```

**Transaction Safety:**
- Use `@Transactional` on order creation
- Optimistic locking with `@Version` for stock updates
- Handle `ObjectOptimisticLockingFailureException`

---

### **5. NOTIFICATION SYSTEM**

**Entities:**
```
Notification
├─ id
├─ user (ManyToOne → Users)
├─ title
├─ message
├─ type (ORDER, PAYMENT, PROMOTION, SYSTEM)
├─ relatedEntityId (orderId, productId, etc)
├─ isRead
└─ createdAt
```

**Push Methods:**
```
1. Email notifications
   - Order confirmation
   - Order status updates
   - Promotion notifications

2. In-app notifications
   - WebSocket real-time push
   - Store in Notification table
   - Mark as read on view

3. Webhook notifications
   - Payment status from VNPay
   - External system events
```

---

### **6. REAL-TIME CHAT**

**WebSocket Architecture:**
```
Client ← WebSocket → STOMP Broker ← [Endpoints]
         (ws://localhost:8080/ws)

Message Flow:
1. Client connects → ws://localhost:8080/ws
2. Authenticate via JWT
3. Subscribe to topics:
   - /user/{userId}/queue/notification (direct messages)
   - /topic/conversation/{conversationId} (group chat)

4. Send message:
   POST /app/sendMessage/{conversationId}
   └─ Broadcast to /topic/conversation/{conversationId}

5. Server sends:
   - Chat message to subscribers
   - Notification to user's queue
```

**Entities:**
```
Conversation
├─ id
├─ participants (List<User>)
├─ createdAt
└─ lastMessageAt

ChatMessage
├─ id
├─ conversation
├─ sender (User)
├─ content
├─ isRead
└─ createdAt
```

**Configuration:**
```java
@EnableWebSocketMessageBroker
registry.enableSimpleBroker("/topic", "/queue");
registry.setApplicationDestinationPrefixes("/app");
registry.setUserDestinationPrefix("/user");
```

---

### **7. VOUCHER & PROMOTION**

**Entities:**
```
Voucher
├─ id
├─ code (unique)
├─ description
├─ discountType (PERCENTAGE, FIXED_AMOUNT)
├─ discountValue
├─ minOrderAmount
├─ maxUsage
├─ usedCount
├─ validFrom
├─ validTo
└─ isActive

UserVoucher (Join table)
├─ userId
├─ voucherId
├─ usedAt
└─ validUntil
```

**Validation:**
```
1. Check voucher exists & active
2. Check code matches
3. Check not expired
4. Check user hasn't used quota
5. Check minimum order amount
6. Apply discount to order total
```

---

### **8. REVIEW & RATING**

**Entities:**
```
ProductComment (Review)
├─ id
├─ product
├─ user
├─ rating (1-5)
├─ title
├─ content
├─ helpfulCount
├─ images (OneToMany → ProductCommentImage)
├─ replies (OneToMany → ProductComment, self-referential)
└─ createdAt

ProductCommentImage
├─ id
├─ comment
└─ imageUrl
```

**Aggregation:**
```
- Calculate average rating
- Sort by helpful count & recency
- Filter by rating (1-5)
- Support review images (via Cloudinary)
```

---

### **9. IMAGE MANAGEMENT**

**Services:**
```
CloudinaryService
├─ Upload image → Cloudinary
├─ Delete image
├─ Resize & transform
└─ Return public URL

ProductImage (Entity)
├─ product
├─ cloudinaryUrl (public URL)
└─ displayOrder
```

**Upload Flow:**
```
1. POST /api/products/{productId}/images
2. Validate file (type, size)
3. Upload to Cloudinary
4. Save URL in ProductImage
5. Return public URL
```

---

### **10. AI CHATBOT (Gemini Integration)**

**Services:**
```
ChatbotService
├─ Call Gemini API
├─ Process user query
├─ Generate AI response
└─ Store interaction history

Endpoint:
POST /api/chatbot
{
  "message": "What are top-selling products?",
  "conversationId": 123
}
```

**API Integration:**
```
Request:
curl -X POST "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent"
  -H "Content-Type: application/json"
  -d '{"contents": [{"parts": [{"text": "..."}]}]}'
  -H "x-goog-api-key: AIzaSyBm3S_..."

Response:
{
  "candidates": [{
    "content": {
      "parts": [{
        "text": "..."
      }]
    }
  }]
}
```

---

## 🔐 SECURITY LAYERS

### **1. CORS Configuration**
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000", "https://yourdomain.com")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
```

### **2. CSRF Protection**
```java
// Disabled for stateless API (using JWT)
.csrf(csrf -> csrf.disable())
```

### **3. Exception Handling**
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(AppException.class)
    // Handle custom exceptions
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    // Handle validation errors
    
    @ExceptionHandler(Exception.class)
    // Catch-all for unexpected errors
}
```

### **4. Input Validation**
```java
public class LoginRequest {
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;
    
    @NotBlank
    @Size(min = 6, message = "Mật khẩu ít nhất 6 ký tự")
    private String password;
}
```

---

## 🚀 DEPLOYMENT CONSIDERATIONS

### **Database**
- MySQL version: 5.7+
- Encoding: UTF-8MB4
- Connection pooling: HikariCP
- Prepared statements to prevent SQL injection

### **Caching**
- Redis for session storage
- TTL configuration for cache entries
- Key naming strategy: `prefix:entity:id`

### **External APIs**
- Rate limiting for Gemini API
- Retry logic for failed requests
- Timeout configuration

### **Security**
- API keys in environment variables (not in code)
- HTTPS only in production
- JWT token expiration (typically 1 hour)
- Refresh token strategy

---

## 📈 PERFORMANCE OPTIMIZATION

### **Database Queries**
```java
// ❌ N+1 Query Problem
List<Product> products = productRepository.findAll();
for (Product p : products) {
    p.getCategory().getName();  // Extra query per product
}

// ✅ Solution: JOIN FETCH
@Query("""
    SELECT DISTINCT p FROM Product p
    LEFT JOIN FETCH p.category
    LEFT JOIN FETCH p.brand
    WHERE p.status = 'ACTIVE'
""")
List<Product> findAllWithDetails();
```

### **Pagination**
```java
// Instead of loading all records
Page<Product> page = productRepository.findAll(
    PageRequest.of(0, 20, Sort.by("createdAt").descending())
);
```

### **Caching Strategy**
```java
// Cache popular categories
@Cacheable(value = "categories")
public List<Category> getAllCategories() { }

// Invalidate cache on update
@CacheEvict(value = "categories", allEntries = true)
public Category saveCategory(Category category) { }
```

---

## 🐛 COMMON ISSUES & SOLUTIONS

| Issue | Cause | Solution |
|-------|-------|----------|
| N+1 Query | Lazy loading in loop | Use JOIN FETCH, @Query |
| ConcurrentModificationException | Modifying list during iteration | Use Iterator or Stream |
| LazyInitializationException | Access lazy field outside session | Initialize in query or use EAGER |
| OOM (Out of Memory) | Large result set | Implement pagination |
| Deadlock | Transaction locks | Use READ_COMMITTED isolation |
| Token expired | JWT expiration | Implement refresh token |
| Stock race condition | Concurrent purchases | Use optimistic locking |

---

**Ghi chú:** Hãy sâu đi vào từng module này để thực sự hiểu cách dự án hoạt động! 🎯
