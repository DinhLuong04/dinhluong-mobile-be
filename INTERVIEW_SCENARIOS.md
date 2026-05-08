# 🎤 SỬ DỤNG DỰ ÁN ĐỂ TRẢ LỜI PHỎNG VẤN

## 📌 CÁC CÂU HỎI THỰC TẾ & CÁCH TRẢ LỜI

---

## 1️⃣ GENERAL QUESTIONS

### Q: "Giới thiệu về dự án của bạn?"

**Cách trả lời:**
```
"Dự án của tôi là một E-commerce Backend được xây dựng bằng Spring Boot 3.3.6 
với Java 21. Đây là một RESTful API hoàn chỉnh cho một trang web bán hàng trực tuyến.

Kiến trúc: Tôi sử dụng layered architecture với 3 tiers:
- Controller Layer: Xử lý HTTP requests
- Service Layer: Business logic
- Repository Layer: Data access

Database: MySQL làm database chính, Redis làm caching layer.

Tính năng chính:
- Authentication: JWT + OAuth2 (Google, Facebook)
- Product management: CRUD, search, filtering
- Shopping cart: Add, remove, update quantity
- Order & Payment: Tích hợp VNPay payment gateway
- Real-time chat: WebSocket + STOMP
- Review & Rating: Product reviews with images
- Notification: Email + in-app notifications
- AI Chatbot: Tích hợp Gemini API

Tôi cũng sử dụng:
- MapStruct cho entity ↔ DTO mapping
- Spring Security cho authorization
- Cloudinary cho cloud image storage
"
```

---

### Q: "Tại sao bạn chọn Spring Boot?"

**Cách trả lời:**
```
"Spring Boot là framework phù hợp nhất vì:

1. Convention over Configuration
   - Giảm boilerplate code
   - Auto-configuration giúp khởi động nhanh

2. Comprehensive Ecosystem
   - Spring Data JPA cho database
   - Spring Security cho authentication
   - Spring WebSocket cho real-time communication
   - Tất cả tích hợp sẵn

3. Production Ready
   - Built-in monitoring
   - Health checks
   - Metrics collection

4. Large Community
   - Dễ tìm documentation
   - Nhiều libraries built on top

Trong dự án, nó giúp tôi:
- Tập trung vào business logic thay vì setup
- Dễ scaling & deployment
- Maintainable code structure
"
```

---

## 2️⃣ ARCHITECTURE & DESIGN PATTERNS

### Q: "Mô tả kiến trúc của dự án?"

**Cách trả lời:**
```
"Tôi sử dụng Layered (N-tier) Architecture:

┌────────────────────────┐
│   Presentation Layer   │  (Controllers)
│   - HTTP handling      │
│   - Input validation   │
│   - Response formatting│
└───────────┬────────────┘
            │ (DTOs)
┌───────────▼────────────┐
│   Business Logic Layer │  (Services)
│   - Core logic         │
│   - Transactions       │
│   - Data mapping       │
└───────────┬────────────┘
            │ (JPA Repositories)
┌───────────▼────────────┐
│   Persistence Layer    │  (Repositories)
│   - Data access        │
│   - Query generation   │
│   - CRUD operations    │
└───────────┬────────────┘
            │ (SQL)
┌───────────▼────────────┐
│   Database Layer       │
│   - MySQL              │
│   - Redis              │
└────────────────────────┘

Ưu điểm:
- Separation of concerns
- Easy to test (mock dependencies)
- Scalable
- Maintainable

Ví dụ thực tế:
Khi tạo order:
1. OrderController nhận POST request
2. Gọi OrderService.createOrder()
3. OrderService gọi ProductRepository để check stock
4. Tạo Order entity, lưu database
5. Return OrderResponse DTO

Mỗi layer có responsibility riêng.
"
```

---

### Q: "Bạn sử dụng design patterns nào?"

**Cách trả lời:**
```
"Tôi sử dụng nhiều design patterns:

1. MVC Pattern
   - Model: JPA Entities
   - View: JSON responses (DTOs)
   - Controller: REST endpoints

2. Repository Pattern
   - Abstraction layer cho data access
   - JpaRepository<T, ID>
   - Dễ switch database implementation

3. Service Layer / Facade Pattern
   - OrderService điều phối CartService, PaymentService, NotificationService
   - Simplify complex operations

4. Adapter Pattern
   - GoogleService wraps Google API
   - FacebookService wraps Facebook API
   - CloudinaryService wraps Cloudinary

5. Strategy Pattern
   - Authenticate: email/password, Google, Facebook
   - PaymentMethod: CREDIT_CARD, E_WALLET, BANK_TRANSFER

6. Observer Pattern
   - WebSocket cho chat: server push notifications
   - Observers: subscribed clients

7. Decorator Pattern
   - @Transactional: adds transaction management
   - @Async: adds asynchronous execution
   - @Validated: adds input validation

8. Mapper/Converter Pattern
   - MapStruct @Mapper: Entity ↔ DTO
   - @AfterMapping: custom mapping logic
   - ProductMapper.toCardResponse()

9. Singleton Pattern
   - Spring Beans (single instance per container)
   - @Configuration classes

10. Template Method Pattern
    - JpaRepository: template for common CRUD operations
    - JpaSpecificationExecutor: template for complex queries

Tất cả patterns này đều serve một mục đích:
- Maintainability
- Flexibility
- Reusability
- Testability
"
```

---

## 3️⃣ DATABASE & ORM

### Q: "Giải thích N+1 query problem và cách bạn giải quyết?"

**Cách trả lời:**
```
"N+1 Query Problem:
- Xảy ra khi fetch master record (1 query)
- Rồi fetch related records của mỗi master (N queries)
- Total: 1 + N queries (không hiệu quả)

Ví dụ trong dự án:
❌ PROBLEM:
    List<Product> products = productRepository.findAll();
    for (Product p : products) {
        // LazyInitializationException hoặc extra query
        System.out.println(p.getCategory().getName());
    }

✅ SOLUTION 1: JOIN FETCH in JPQL
    @Query(\"\"\"
        SELECT DISTINCT p FROM Product p
        LEFT JOIN FETCH p.category
        LEFT JOIN FETCH p.brand
        WHERE p.status = 'ACTIVE'
    \"\"\")
    List<Product> findAllWithDetails();
    
    Benefit: Single query with JOINs

✅ SOLUTION 2: Use EAGER loading (careful!)
    @OneToMany(fetch = FetchType.EAGER)
    private List<ProductImage> images;
    
    Warning: Mỗi khi fetch Product, lại fetch images
    Có thể tạo performance issue

✅ SOLUTION 3: Projection / DTO Query
    @Query(\"\"\"
        SELECT new com.dinhluong.dlmstore.dto.ProductDTO(
            p.id, p.name, p.price, c.name
        )
        FROM Product p
        LEFT JOIN p.category c
    \"\"\")
    List<ProductDTO> findAllProjection();
    
    Benefit: Fetch chỉ cần thiết columns, không fetch lazy fields

✅ SOLUTION 4: Pagination
    Page<Product> products = productRepository.findAll(
        PageRequest.of(0, 20)
    );
    
    Benefit: Giới hạn records, giảm memory usage
    
Trong dự án tôi sử dụng:
- JOIN FETCH cho queries cần many relationships
- Pagination cho list endpoints
- Projections cho simple reads
"
```

---

### Q: "Làm thế nào bạn handle optimistic locking?"

**Cách trả lời:**
```
"Optimistic locking dùng để giải quyết race condition khi update records.

Ví dụ: Hai users cùng lúc mua last product in stock.

Cách implement:
1. Thêm @Version field trong Entity:

    @Entity
    @Table(name = \"products\")
    public class Product {
        @Version
        private Long version;
        
        private Integer stock;
    }

2. Khi update:
    - Hibernate automatically increments version
    - Nếu version mismatch → Exception
    - Người sau sẽ fail, retry từ đầu

3. Exception handling:
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Object>> handleOptimisticLocking(
        ObjectOptimisticLockingFailureException ex) {
        
        return ApiResponse.error(409, 
            \"Sản phẩm bạn chọn vừa có người khác nhanh tay mua mất. \" +
            \"Vui lòng tải lại giỏ hàng!\");
    }

Flow:
User1: SELECT stock=1, version=1
User2: SELECT stock=1, version=1

User1: UPDATE stock=0, version=2 ✅ Success
User2: UPDATE stock=0 WHERE version=1 ❌ Fail (version now 2)

User2 retries:
User2: SELECT stock=0, version=2 → Stock hết, show error

Ưu điểm:
- Không block reads
- High concurrency
- Low overhead

Nhược điểm:
- Client phải retry
- Busy waiting nếu conflicts cao

Trong dự án:
- Apply cho Order creation (stock update)
- Apply cho Voucher usage (usedCount update)
- Handle exception với user-friendly message
"
```

---

## 4️⃣ SECURITY & AUTHENTICATION

### Q: "Mô tả JWT authentication flow trong dự án?"

**Cách trả lời:**
```
"JWT (JSON Web Token) là stateless authentication method.

Structure: Header.Payload.Signature

Ví dụ token:
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.
eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.
SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c

Header: {\"alg\": \"HS256\", \"typ\": \"JWT\"}
Payload: {\"userId\": 123, \"email\": \"user@example.com\", \"exp\": 1699999999}
Signature: HMACSHA256(base64(header) + \".\" + base64(payload), secret)

Flow trong dự án:

1. LOGIN REQUEST:
   POST /api/auth/login
   {
     \"email\": \"user@example.com\",
     \"password\": \"password123\"
   }

2. SERVER PROCESSING:
   AuthController.login(LoginRequest request)
   ├─ AuthService.login(email, password)
   ├─ UserRepository.findByEmail(email)
   ├─ PasswordEncoder.matches(request.password, user.hashedPassword)
   ├─ JwtTokenProvider.generateToken(user)
   │  └─ Create token with:
   │     - userId
   │     - email
   │     - roles
   │     - exp = now + 3600 (1 hour)
   │     - signature with secret key
   └─ Return LoginResponse { token, user }

3. CLIENT STORAGE:
   localStorage.setItem('token', response.token)

4. SUBSEQUENT REQUESTS:
   GET /api/products
   Headers: Authorization: Bearer <token>

5. SERVER VALIDATION (JwtAuthenticationFilter):
   Filter nhận request
   ├─ Extract token từ Authorization header
   ├─ Validate signature bằng secret key
   ├─ Check expiration
   ├─ Extract email từ payload
   ├─ Load UserDetails từ database
   ├─ Create UsernamePasswordAuthenticationToken
   ├─ Set vào SecurityContext
   └─ Continue filter chain

6. AUTHORIZATION:
   Spring Security check request path vs user roles
   ├─ PUBLIC_APIS: permitAll()
   ├─ USER_APIS: hasAnyRole(\"USER\", \"ADMIN\")
   ├─ ADMIN_APIS: hasRole(\"ADMIN\")

7. TOKEN EXPIRATION:
   Khi token hết hạn:
   ├─ JwtTokenProvider.validateToken() trả false
   ├─ Authentication fails
   ├─ Return 401 Unauthorized
   ├─ Client redirect to login

Ưu điểm:
- Stateless: server không cần lưu session
- Scalable: dùng cho microservices
- Mobile-friendly: dễ dùng cho mobile apps
- CORS-friendly

Nhược điểm:
- Token size lớn hơn session id
- Không thể revoke token trước hạn
- Secret key phải bảo vệ kỹ

Best practices:
- Token TTL: 1 hour (short-lived)
- Refresh token: 7 days (long-lived)
- HTTPS only
- Secure secret key (dùng environment variables)
"
```

---

### Q: "Bạn tích hợp Google OAuth2 thế nào?"

**Cách trả lời:**
```
"OAuth2 là authorization protocol cho phép user login bằng account của bên thứ 3.

Flow:

1. FRONTEND - User clicks \"Login with Google\":
   ├─ Redirect to Google OAuth consent screen
   ├─ Google shows permissions needed
   └─ User authorizes

2. GOOGLE → CLIENT:
   └─ Return authorization code

3. CLIENT → SERVER:
   POST /api/auth/oauth2/google
   { \"code\": \"...\", \"idToken\": \"...\" }

4. SERVER PROCESSES:
   GoogleService.validateAndGetUser(idToken)
   ├─ Validate token signature
   ├─ Verify token with Google API
   ├─ Extract user info:
   │  - email
   │  - name
   │  - picture
   │  - sub (unique Google ID)
   ├─ Check user in database:
   │  - If exists: return user
   │  - If not exists:
   │    ├─ Create new User
   │    ├─ Set authProvider = \"GOOGLE\"
   │    └─ Set googleId
   ├─ JwtTokenProvider.generateToken(user)
   └─ Return LoginResponse { token, user }

5. CLIENT STORES TOKEN:
   localStorage.setItem('token', response.token)

6. NEXT REQUESTS:
   Same as normal JWT flow

Benefits:
- No password management
- User trust Google
- Easy signup & login

Configuration:
    spring.security.oauth2.client.registration.google.client-id=...
    spring.security.oauth2.client.registration.google.client-secret=...
    spring.security.oauth2.client.registration.google.scope=openid,profile,email

Security:
- Validate token signature
- Verify token expiration
- Use HTTPS
- Store secret securely

Trong dự án:
- Support Google + Facebook oauth
- Check authProvider khi login
- Handle user creation vs update
"
```

---

## 5️⃣ REAL-TIME COMMUNICATION

### Q: "Làm thế nào bạn implement real-time chat?"

**Cách trả lời:**
```
"Real-time chat sử dụng WebSocket + STOMP (Simple Text Oriented Messaging Protocol).

Khác biệt HTTP vs WebSocket:

HTTP (Traditional):
├─ Request-Response
├─ Server không thể initiate communication
├─ Polling: client liên tục hỏi \"có message không?\"
└─ Inefficient

WebSocket:
├─ Bidirectional persistent connection
├─ Server có thể push data to client
├─ Full-duplex communication
└─ Low latency, high efficiency

STOMP:
├─ Protocol over WebSocket
├─ Provides publish/subscribe messaging
├─ Frame-based format: CONNECT, SUBSCRIBE, SEND, MESSAGE

Architecture:

                    ┌─ /topic/room1 (public)
Browser 1 ─ WS ─┤├─ /user/user1/queue/direct (private)
                └─ /app/sendMessage (endpoint)

Browser 2 ─ WS ─┤├─ /topic/room1
                └─ /user/user2/queue/direct

Server (Message Broker):
├─ Routes /topic messages to all subscribers
├─ Routes /queue messages to specific user
└─ Maintains connections

Configuration:

    @Configuration
    @EnableWebSocketMessageBroker
    public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
        
        @Override
        public void configureMessageBroker(MessageBrokerRegistry registry) {
            // Enable simple broker
            registry.enableSimpleBroker(\"/topic\", \"/queue\");
            
            // Prefix for client -> server
            registry.setApplicationDestinationPrefixes(\"/app\");
            
            // Prefix for direct messages
            registry.setUserDestinationPrefix(\"/user\");
        }
        
        @Override
        public void registerStompEndpoints(StompEndpointRegistry registry) {
            registry.addEndpoint(\"/ws\")
                    .setAllowedOriginPatterns(\"*\")
                    .setHandshakeHandler(new UserHandshakeHandler())
                    .addInterceptors(jwtHandshakeInterceptor);
        }
    }

Chat Flow:

1. CONNECT:
   Client: CONNECT ws://localhost:8080/ws
   Server: Authenticate via JWT token
           Extract user info from token

2. SUBSCRIBE:
   Client: SUBSCRIBE /topic/conversation/123
           (Subscribe to room 123)
   
   Client: SUBSCRIBE /user/{userId}/queue/notification
           (Listen for direct messages)

3. SEND MESSAGE:
   Client: SEND /app/sendMessage/123
           { \"content\": \"Hello\", \"conversationId\": 123 }

4. SERVER RECEIVES:
   @MessageMapping(\"/sendMessage/{conversationId}\")
   public void sendMessage(
       @DestinationVariable Long conversationId,
       ChatMessageDTO message) {
       
       // Save to database
       chatMessageRepository.save(message);
       
       // Broadcast to subscribers
   }

5. BROADCAST:
   @SendTo(\"/topic/conversation/{conversationId}\")
   ChatMessageDTO sendMessage(...) {
       return message;  // Automatically sent to all subscribers
   }

6. CLIENT RECEIVES:
   Client 1 & 2 receive message in real-time
   Update UI immediately

Direct Message:
   @SendToUser(\"/queue/notification\")
   NotificationDTO sendNotification(String userId, NotificationDTO notif) {
       return notif;  // Sent only to that specific user
   }

Security:
   - Authenticate via JWT at handshake
   - Validate user can access conversation
   - Encrypt messages if sensitive

Performance:
   - Message queuing for high throughput
   - Memory optimization for long connections
   - Connection pooling

Trong dự án:
- ChatController handles messaging
- ChatService persists messages
- Notification real-time via WebSocket
- Order status updates via WebSocket
"
```

---

## 6️⃣ EXTERNAL APIs & INTEGRATION

### Q: "Làm thế nào bạn tích hợp VNPay payment gateway?"

**Cách trả lời:**
```
"VNPay là cổng thanh toán phổ biến ở Việt Nam.

Integration Flow:

1. CREATE ORDER:
   OrderController.createOrder(OrderRequest)
   ├─ Validate order
   ├─ Create Order record
   ├─ Create OrderItems
   └─ Return Order with order_id

2. PAYMENT INITIATION:
   POST /api/payments/vnpay
   {
     \"orderId\": 123,
     \"amount\": 1000000  // VND
   }

3. SERVER CREATES PAYMENT LINK:
   PaymentService.createVNPayLink(Order order)
   ├─ Prepare request to VNPay:
   │  {
   │    \"vnp_Command\": \"pay\",
   │    \"vnp_Amount\": 100000000 (x100 VND),
   │    \"vnp_CreateDate\": \"20231201120000\",
   │    \"vnp_TmnCode\": \"5YAK9J0P\",
   │    \"vnp_OrderInfo\": \"Order #123\",
   │    \"vnp_OrderType\": \"billpayment\",
   │    \"vnp_ReturnUrl\": \"https://yourdomain/payment/result\",
   │    \"vnp_IpAddr\": \"192.168.1.1\",
   │    \"vnp_Locale\": \"vn\"
   │  }
   │
   ├─ Generate signature:
   │  signature = HMACSHA512(sortedParams, hashSecret)
   │  vnp_SecureHash = signature
   │
   ├─ Build VNPay URL:
   │  https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Command=pay&...
   │
   └─ Return { paymentUrl: \"https://...\" }

4. CLIENT REDIRECT:
   Frontend: window.location.href = paymentUrl
   ├─ User redirected to VNPay gateway
   ├─ User enters card info
   └─ VNPay processes payment

5. VNPay processes payment (banks, cards, etc.)

6. PAYMENT CALLBACK:
   VNPay → Server
   GET /api/payments/vnpay/callback?vnp_Amount=...&vnp_TransactionNo=...&...

7. SERVER VALIDATES CALLBACK:
   PaymentService.handleVNPayCallback(params)
   ├─ Verify signature:
   │  savedSignature = HMACSHA512(params, hashSecret)
   │  if (savedSignature != vnp_SecureHash) reject
   │
   ├─ Check transaction status (vnp_ResponseCode)
   │  \"00\": Success
   │  \"01\": Chưa hoàn thành
   │  \"02\": Lỗi
   │
   ├─ Update Payment record:
   │  payment.transactionId = vnp_TransactionNo
   │  payment.status = COMPLETED
   │
   ├─ Update Order record:
   │  order.status = PAID
   │
   ├─ Reduce product stock:
   │  for each OrderItem:
   │    product.stock -= item.quantity
   │
   ├─ Send email confirmation
   ├─ Send notification to user
   │
   └─ Return { code: \"00\", message: \"Success\" }

8. USER REDIRECT:
   Server redirects to:
   https://frontend.com/payment/result?code=00&orderCode=...

9. FRONTEND:
   Show success/failure message
   Update order status
   Refresh cart

Configuration:
    vnpay.tmnCode=5YAK9J0P          # Merchant code
    vnpay.hashSecret=P3DYHVH6...    # Secret key
    vnpay.payUrl=https://sandbox... # Sandbox for testing
    vnpay.returnUrl=https://...     # Callback URL

Security:
- Validate signature mỗi request
- Use HTTPS
- Store tmnCode & hashSecret in environment variables
- Validate amount trên server
- Handle timeout gracefully
- Implement retry logic

Error Handling:
- User cancels payment
- Payment fails
- Timeout
- Duplicate transaction

Trong dự án:
- PaymentService handles VNPay integration
- Payment entity stores transaction info
- Order entity links to Payment
- AsyncEmailService sends confirmation
"
```

---

## 7️⃣ TESTING & QUALITY

### Q: "Làm thế nào bạn test code?"

**Cách trả lời:**
```
"Tôi sử dụng multiple layers of testing:

1. UNIT TESTS:
   Test individual methods in isolation
   
   @RunWith(SpringRunner.class)
   public class ProductServiceTest {
       @MockBean
       private ProductRepository productRepository;
       
       @InjectMocks
       private ProductService productService;
       
       @Test
       public void testSearchProducts() {
           // Arrange
           List<Product> mockProducts = Arrays.asList(
               new Product(1L, \"Product 1\", \"slug1\", 100.0)
           );
           when(productRepository.findAll()).thenReturn(mockProducts);
           
           // Act
           List<Product> result = productService.getAllProducts();
           
           // Assert
           assertEquals(1, result.size());
           assertEquals(\"Product 1\", result.get(0).getName());
           
           // Verify
           verify(productRepository).findAll();
       }
   }

2. INTEGRATION TESTS:
   Test multiple components together
   
   @SpringBootTest
   public class OrderControllerTest {
       @Autowired
       private MockMvc mockMvc;
       
       @Test
       public void testCreateOrder() throws Exception {
           String orderJson = \"{...}\";
           
           mockMvc.perform(post(\"/api/orders\")
               .contentType(MediaType.APPLICATION_JSON)
               .content(orderJson))
               .andExpect(status().isCreated())
               .andExpect(jsonPath(\"$.code\").value(\"success\"));
       }
   }

3. ACCEPTANCE TESTS:
   Full end-to-end testing via RestTemplate
   
   @SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
   public class AcceptanceTest {
       @Autowired
       private TestRestTemplate restTemplate;
       
       @Test
       public void testFullOrderFlow() {
           // Test login → add to cart → create order → payment
       }
   }

4. DATABASE TESTS:
   Test repository queries
   
   @DataJpaTest
   public class ProductRepositoryTest {
       @Autowired
       private ProductRepository productRepository;
       
       @Autowired
       private TestEntityManager entityManager;
       
       @Test
       public void testFindBySlug() {
           // Setup test data
           Product product = new Product(\"slug\", \"name\");
           entityManager.persistAndFlush(product);
           
           // Query
           Optional<Product> found = productRepository.findBySlug(\"slug\");
           
           // Assert
           assertTrue(found.isPresent());
       }
   }

Coverage:
- Aim for 70-80% code coverage
- Focus on critical business logic
- Don't test getters/setters

Mocking:
- @MockBean: Mock Spring beans
- @Mock: Mock objects
- when().thenReturn(): Set expectations
- verify(): Verify calls

Best Practices:
- Test behavior, not implementation
- Write tests before code (TDD optional)
- Keep tests independent
- Use descriptive test names
- Follow Arrange-Act-Assert pattern

Trong dự án:
- Service layer: Unit + Integration tests
- Controller layer: MockMvc tests
- Repository layer: DataJpaTest
- Util layer: Unit tests
"
```

---

## 8️⃣ PERFORMANCE & OPTIMIZATION

### Q: "Bạn optimize performance như thế nào?"

**Cách trả lời:**
```
"Performance optimization ở nhiều layers:

1. DATABASE LEVEL:

   Indexing:
   - Index frequently queried columns: name, slug, status
   - Composite index: (category_id, status, created_at)
   
   Query Optimization:
   - Use JOIN FETCH để avoid N+1
   - Use Pagination để limit result size
   - Use Projections để fetch chỉ needed columns
   
   Example:
   @Query(\"\"\"
       SELECT new com.dinhluong.ProductCardDTO(
           p.id, p.name, p.price, c.name
       )
       FROM Product p
       LEFT JOIN p.category c
       WHERE p.status = 'ACTIVE'
   \"\"\")
   Page<ProductCardDTO> findCardProducts(Pageable pageable);

2. APPLICATION LEVEL:

   Caching:
   - @Cacheable: Cache method result
   - @CacheEvict: Clear cache on update
   
   @Cacheable(\"categories\")
   public List<Category> getAllCategories() {
       return categoryRepository.findAll();
   }
   
   @CacheEvict(value = \"categories\", allEntries = true)
   public Category updateCategory(Category category) {
       return categoryRepository.save(category);
   }
   
   Connection Pooling:
   - HikariCP (default in Spring Boot)
   - Pool size = cores * 2 + spare connections
   
   Async Processing:
   @Async
   public void sendEmailAsync(String to, String subject) {
       // Non-blocking email sending
   }

3. DATA TRANSFER LEVEL:

   DTOs instead of Entities:
   - Avoid lazy loading issues
   - Select only needed fields
   - Reduce JSON size
   
   Compression:
   - GZIP response compression
   - spring.compression.enabled=true
   
   Pagination:
   - Default page size: 20
   - Max page size: 100
   
   Filtering:
   - Server-side filtering reduces data transfer
   - Client receives only needed records

4. MONITORING & PROFILING:

   Logging:
   - Use appropriate log levels
   - Avoid logging in loops
   
   Metrics:
   - Actuator endpoints: /metrics
   - Track response times, error rates
   
   Profiling:
   - Use JProfiler or YourKit
   - Identify bottlenecks
   - Monitor memory usage

5. INFRASTRUCTURE:

   Load Balancing:
   - Nginx / HAProxy
   - Distribute traffic
   
   CDN:
   - Serve static content from CDN
   - Cloudinary cho images
   
   Database Replication:
   - Read replicas cho high read traffic
   - Master-slave setup

Specific Optimizations in Project:

- Product search: Indexed columns, pagination
- Chat messages: Archive old messages, pagination
- Order history: Pagination, cached calculations
- Payment status: Queue + async processing
- Image storage: Cloudinary (avoid server storage)

Benchmarking:
- Response time: target < 200ms
- Database query time: target < 50ms
- Memory footprint: monitor heap usage
- CPU usage: target < 70%
"
```

---

## 9️⃣ DEPLOYMENT & DEVOPS

### Q: "Bạn deploy application thế nào?"

**Cách trả lời:**
```
"Deployment process:

1. LOCAL DEVELOPMENT:
   - Gradle/Maven build locally
   - Test on localhost:8080
   - Debug issues

2. PACKAGING:
   mvn clean package
   ├─ Compile code
   ├─ Run tests
   ├─ Create JAR file
   │  (with embedded Tomcat)
   └─ Output: dlmstore-0.0.1-SNAPSHOT.jar

3. CONTAINERIZATION (Optional):
   Dockerfile:
   FROM openjdk:21-slim
   COPY dlmstore-0.0.1-SNAPSHOT.jar app.jar
   ENTRYPOINT [\"java\", \"-jar\", \"app.jar\"]
   
   Build & push to registry:
   docker build -t dlmstore:latest .
   docker push registry.com/dlmstore:latest

4. DEPLOYMENT ENVIRONMENT:

   Development:
   - application-dev.properties
   - H2 database (in-memory)
   - Debug enabled
   
   Staging:
   - application-staging.properties
   - MySQL staging instance
   - Same config as production
   
   Production:
   - application-prod.properties
   - MySQL production instance
   - RDS or managed database
   - Redis managed cluster

5. CONFIGURATION MANAGEMENT:
   
   Environment Variables:
   export SPRING_PROFILES_ACTIVE=prod
   export DB_URL=jdbc:mysql://prod-db:3306/dlmstore
   export DB_PASSWORD=secure_password
   export JWT_SECRET=very_long_secret_key
   
   Or use .env files:
   SPRING_DATASOURCE_URL=...
   SPRING_DATASOURCE_PASSWORD=...

6. RUNNING APPLICATION:

   Local:
   java -jar dlmstore.jar
   
   Docker:
   docker run -p 8080:8080 \\
     -e SPRING_PROFILES_ACTIVE=prod \\
     -e DB_URL=jdbc:mysql://db:3306/dlmstore \\
     dlmstore:latest
   
   Kubernetes:
   kubectl apply -f deployment.yaml
   
   deployment.yaml:
   apiVersion: apps/v1
   kind: Deployment
   metadata:
     name: dlmstore
   spec:
     replicas: 3
     template:
       spec:
         containers:
         - name: dlmstore
           image: dlmstore:latest
           ports:
           - containerPort: 8080
           env:
           - name: SPRING_PROFILES_ACTIVE
             value: prod
           - name: DB_URL
             valueFrom:
               secretKeyRef:
                 name: db-secret
                 key: url

7. HEALTH CHECKS & MONITORING:

   Actuator Endpoints:
   /actuator/health - Is service running?
   /actuator/metrics - Performance metrics
   /actuator/info - Application info
   
   Logging:
   - Console logs
   - File logs
   - Centralized logging (ELK stack)
   
   Monitoring:
   - Prometheus for metrics
   - Grafana for dashboards
   - Alert manager for alerts

8. DATABASE MIGRATION:

   Flyway / Liquibase:
   - Version control for schema changes
   - Auto migration on app startup
   
   Or use Hibernate:
   spring.jpa.hibernate.ddl-auto=update

9. ZERO-DOWNTIME DEPLOYMENT:

   Blue-Green Deployment:
   - Blue: Current production
   - Green: New version
   - Switch router: traffic → green
   - If issue: rollback to blue
   
   Rolling Deployment:
   - Kill 1 instance → deploy new → repeat
   - Gradual traffic shift
   - No downtime

10. ROLLBACK STRATEGY:

    - Keep previous JAR versions
    - Database backward compatibility
    - Feature flags for gradual rollout
    - Monitor error rates post-deployment
    - Automatic rollback if error spike
"
```

---

## 🎓 TIPS TRƯỚC PHỎNG VẤN

1. **Practice explaining your code**
   - Trên whiteboard hoặc paper
   - Record yourself
   - Get feedback from friends

2. **Prepare examples from your project**
   - Have concrete examples ready
   - Don't just memorize theory
   - Show you've actually built something

3. **Understand trade-offs**
   - Why this technology vs other?
   - When to use, when not to use
   - Every decision has pros & cons

4. **Deep dive on 2-3 features**
   - Know one feature very well
   - Be able to discuss edge cases
   - Explain performance considerations

5. **Ask clarifying questions**
   - Don't assume
   - Show you think deeply
   - \"Could you explain more about...?\"

6. **Admit when you don't know**
   - \"I'm not sure, but I would...\"
   - Show problem-solving approach
   - No one knows everything

7. **Talk about improvements**
   - What would you do differently?
   - What would you add?
   - Show you think about growth

8. **Ask them questions too**
   - Tech stack they use
   - Team size & structure
   - Challenges they face
   - Growth opportunities

---

**Good luck with your interview! You've built a solid project. 🚀**
