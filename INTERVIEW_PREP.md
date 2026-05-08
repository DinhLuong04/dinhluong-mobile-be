# 📚 ÔN TẬP PHỎNG VẤN INTERN JAVA - ECOMERCE SHOP PROJECT

## 📋 MỤC LỤC
1. [Core Java Concepts](#core-java-concepts)
2. [Technologies Used](#technologies-used)
3. [Design Patterns](#design-patterns)
4. [Spring Boot Architecture](#spring-boot-architecture)
5. [Database & ORM](#database--orm)
6. [Security & Authentication](#security--authentication)
7. [Real-time Communication](#real-time-communication)
8. [Common Challenges & Solutions](#common-challenges--solutions)
9. [Questions to Practice](#questions-to-practice)

---

## Core Java Concepts

### 1. **Object-Oriented Programming (OOP)**
- **Encapsulation**: Sử dụng private/public trong các entity như `Users.java`, `Product.java`
- **Inheritance**: Sử dụng `extends` trong service implementations
- **Polymorphism**: Override methods trong mapper, service interfaces
- **Abstraction**: Interface `AuthService`, `CartService` định nghĩa hợp đồng

**Ôn tập**: 
- Khác biệt giữa abstract class và interface
- Khi nào sử dụng inheritance vs composition
- Access modifiers và visibility

### 2. **Collections Framework**
- `List`, `Set`, `Map` được sử dụng trong:
  - `ProductMapper`: `.stream().collect(Collectors.toList())`
  - Xử lý danh sách sản phẩm, combo items
  - Caching trong Redis

**Ôn tập**:
- List vs Set vs Map - khi nào dùng cái nào
- Stream API - filter, map, collect, findFirst, forEach
- HashMap vs TreeMap vs ConcurrentHashMap
- Collections.nCopies, Collections.unmodifiableList

### 3. **Exception Handling**
Dự án sử dụng custom exceptions:
- `AppException.java` - base exception
- `ResourceNotFoundException.java`
- `InsufficientStockException.java`
- `AuthenticationException.java`
- `AuthorizationException.java`
- `ValidationException.java`

**Ôn tập**:
- Checked vs Unchecked exceptions
- Try-catch-finally, try-with-resources
- Custom exception best practices
- Exception chaining
- GlobalExceptionHandler pattern (@ControllerAdvice)

### 4. **Generics**
- Repository generics: `JpaRepository<Product, Long>`
- Response wrappers: `ApiResponse<T>`
- DTO templates

**Ôn tập**:
- Type erasure
- Bounded type parameters: `<T extends Entity>`
- Wildcard: `<? extends Number>`, `<? super Number>`
- Generic methods

### 5. **Annotations & Reflection**
Sử dụng rộng rãi:
- `@Autowired` - dependency injection
- `@RestController`, `@Service`, `@Repository`
- `@RequestMapping`, `@PostMapping`, `@GetMapping`
- `@Valid`, `@Validated`
- `@Transactional`
- `@Mapper` (MapStruct)

**Ôn tập**:
- Built-in vs Custom annotations
- Meta-annotations (@Target, @Retention)
- Reflection API - getClass(), getMethods(), invoke()
- Proxy patterns

### 6. **Lambda & Functional Programming**
```java
// Trong ProductMapper
var productMap = products.stream()
    .collect(Collectors.toMap(Product::getId, Function.identity()));

// Filter unique specs
.filter(distinctByKey(spec -> spec.getId()))
.collect(Collectors.toList());
```

**Ôn tập**:
- Functional interfaces
- Method references (::)
- Function, Predicate, Consumer, Supplier
- Stream operations (terminal vs intermediate)

---

## Technologies Used

### **Backend Framework**
| Công nghệ | Phiên bản | Mục đích |
|-----------|----------|---------|
| **Spring Boot** | 3.3.6 | Framework chính |
| **Java** | 21 | JDK version |
| **Spring Security** | 3.3.6 | Authentication & Authorization |
| **Spring Data JPA** | 3.3.6 | ORM, database access |
| **Spring WebSocket** | 3.3.6 | Real-time chat |
| **Spring Mail** | 3.3.6 | Email sending |
| **OAuth2 Client** | 3.3.6 | Google/Facebook login |

### **Database & Caching**
| Công nghệ | Sử dụng |
|-----------|--------|
| **MySQL** | Relational database chính |
| **Redis** | Caching, session storage |
| **JPA/Hibernate** | ORM mapping |

### **APIs & Integration**
| Service | Mục đích |
|---------|---------|
| **Google API** | OAuth2 login, Gemini AI |
| **Cloudinary** | Cloud image storage |
| **VN Pay** | Payment gateway |
| **Gmail SMTP** | Email service |
| **Gemini 2.5 Flash** | AI chatbot |
| **ImgBB** | Image upload backup |

### **Libraries**
| Library | Version | Mục đích |
|---------|---------|---------|
| **MapStruct** | 1.5.5 | DTO mapping |
| **JWT (JJWT)** | 0.11.5 | Token authentication |
| **Jackson** | Latest | JSON processing |
| **Lombok** | Latest | Boilerplate reduction |
| **Validation** | Latest | Bean validation |
| **Swagger/SpringDoc** | 2.2.0 | API documentation |

---

## Design Patterns

### **1. MVC Pattern (Model-View-Controller)**
```
Controller Layer (REST endpoints)
    ↓
Service Layer (Business logic)
    ↓
Repository Layer (Data access)
    ↓
Database/Redis
```

**Ví dụ trong dự án:**
- **Controllers**: `ProductController`, `OrderController`, `AuthController`
- **Services**: `ProductService`, `OrderService`, `AuthService`
- **Repositories**: `ProductRepository`, `OrderRepository`

**Ôn tập**: MVC flow, request-response cycle, separation of concerns

### **2. Repository Pattern**
```java
public interface ProductRepository extends JpaRepository<Product, Long>, 
                                          JpaSpecificationExecutor<Product> {
    Optional<Product> findBySlug(String slug);
    Page<Product> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
```

**Ôn tập**: 
- Data access abstraction
- CRUD operations
- JPA Specifications for complex queries
- Query methods vs @Query annotations

### **3. Service Layer Pattern**
```java
public interface AuthService {
    void verifyEmail(String verificationCode);
    Users login(String email, String password);
    void register(RegisterRequest request);
}
```

**Ôn tập**: Business logic encapsulation, transaction management

### **4. Mapper/Converter Pattern (DTO Pattern)**
```java
@Mapper(componentModel = "spring")
public abstract class ProductMapper {
    
    @Mapping(source = "id", target = "id")
    @Mapping(source = "thumbnailUrl", target = "image")
    public abstract ProductCardResponse toCardResponse(Product product);
    
    @AfterMapping
    protected void calculateCardFields(Product product, 
                                       @MappingTarget ProductCardResponse response) {
        // Custom mapping logic
    }
}
```

**Ôn tập**: 
- MapStruct annotations
- DTO vs Entity
- @Mapping, @AfterMapping, @BeforeMapping
- Custom mappers

### **5. Strategy Pattern**
```java
// Different authentication strategies
- Normal login (email/password)
- Google OAuth2
- Facebook OAuth2
```

**Ôn tập**: Strategy selection, multiple implementations

### **6. Adapter Pattern**
```java
// Converting different APIs to unified interface
CloudinaryService - wraps Cloudinary API
GoogleService - wraps Google API
FacebookService - wraps Facebook API
```

### **7. Observer Pattern**
```java
// WebSocket implementation
- Server sends notifications to multiple connected clients
- ChatController broadcasts messages
- NotificationService pushes updates
```

### **8. Decorator Pattern**
```java
@Transactional  // Decorator adds transaction management
@Validated      // Decorator adds validation
@Async          // Decorator adds asynchronous execution
public void sendEmail() { }
```

### **9. Singleton Pattern**
```java
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) { }
    // Spring manages single instance
}
```

### **10. Template Method Pattern**
```java
// JpaSpecificationExecutor - template for query building
public interface ProductRepository extends JpaRepository<Product, Long>, 
                                          JpaSpecificationExecutor<Product>
```

### **11. Facade Pattern**
```java
// OrderService acts as facade
- Coordinates between CartService, PaymentService, NotificationService
- Simplifies complex operations
```

### **12. Factory Pattern**
```java
// Spring Bean Factory
@Bean
public AuthenticationManager authenticationManager(
    AuthenticationConfiguration config) { }
```

---

## Spring Boot Architecture

### **1. Dependency Injection (DI)**
```java
// Constructor injection (recommended)
@RestController
public class ProductController {
    private final ProductService productService;
    
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
}

// Or field injection
@Autowired
private ProductService productService;

// Or setter injection
@Autowired
public void setProductService(ProductService service) { }
```

**Ôn tập**: 
- IoC (Inversion of Control) container
- Constructor vs field vs setter injection
- @Component, @Service, @Repository, @Controller
- Bean lifecycle

### **2. Configuration & Properties**
```properties
# application.properties
spring.application.name=dlmstore
spring.datasource.url=jdbc:mysql://localhost:3306/dlmstore
spring.jpa.hibernate.naming.physical-strategy=...
jwt.secret=...
```

**Ôn tập**: @ConfigurationProperties, @Value, property sources

### **3. Auto-Configuration**
```java
@SpringBootApplication
@EnableAsync  // Enable async operations
public class DlmstoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(DlmstoreApplication.class, args);
    }
}
```

**Ôn tập**: @SpringBootApplication components, auto-configuration conditions

### **4. Aspect-Oriented Programming (AOP)**
```java
// Implicit AOP usage
@Transactional  // Creates proxy for transaction management
@Async          // Async execution proxy
@Validated      // Method parameter validation
```

**Ôn tập**: 
- Pointcuts & Advices
- Before, After, Around advice
- Cross-cutting concerns
- Proxy patterns in Spring

### **5. Exception Handling - GlobalExceptionHandler**
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Object>> handleAppException(AppException ex) {
        // Centralized exception handling
    }
    
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Object>> handleOptimisticLocking(
        ObjectOptimisticLockingFailureException ex) { }
}
```

**Ôn tập**: @ControllerAdvice, @ExceptionHandler, consistent error responses

---

## Database & ORM

### **1. JPA/Hibernate Mapping**
```java
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String slug;
    
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductImage> images;
    
    @Version  // Optimistic locking
    private Long version;
}
```

**Ôn tập**: 
- Entity annotations
- @Id, @GeneratedValue, @Column
- @OneToMany, @ManyToOne, @ManyToMany, @OneToOne
- @JoinColumn, @JoinTable
- Cascade types
- FetchType: LAZY vs EAGER
- N+1 problem và solutions

### **2. Repository & Query Methods**
```java
// Method naming convention
Optional<Product> findBySlug(String slug);
List<Product> findByBrandId(Long brandId);
Page<Product> findByCategory(Category category, Pageable pageable);

// JPQL Query
@Query("""
    SELECT p FROM Product p
    WHERE p.status = 'ACTIVE' 
    AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
""")
Page<Product> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
```

**Ôn tập**: 
- Method naming query generation
- JPQL vs HQL
- Native SQL queries
- Pagination & Sorting
- Specifications for dynamic queries

### **3. Pagination**
```java
// Interface
public interface Pageable { }

// Usage
Page<Product> products = productRepository.findAll(PageRequest.of(0, 20));
products.getContent();      // List<Product>
products.getTotalPages();
products.getTotalElements();
products.hasNext();
```

**Ôn tập**: Pagination best practices, offset vs cursor pagination

### **4. Relationships**
- **One-to-Many**: Category → Products
- **Many-to-One**: Product → Category
- **Many-to-Many**: Users ↔ Vouchers (UserVoucher)
- **One-to-One**: User → Address

### **5. Optimistic Locking**
```java
@Version
private Long version;

// Handled by GlobalExceptionHandler
@ExceptionHandler(ObjectOptimisticLockingFailureException.class)
```

**Ôn tập**: Concurrency control, race conditions

### **6. Transaction Management**
```java
@Service
public class OrderService {
    @Transactional  // Default: PROPAGATION_REQUIRED
    public Order createOrder(OrderRequest request) {
        // Multiple operations in single transaction
    }
}
```

**Ôn tập**: 
- @Transactional annotation
- Transaction propagation
- Isolation levels
- Rollback rules

---

## Security & Authentication

### **1. JWT (JSON Web Token) Authentication**
```java
// Token creation
String token = tokenProvider.generateToken(user);

// Token validation
boolean isValid = tokenProvider.validateToken(token);

// Token extraction
String email = tokenProvider.getEmailFromJWT(token);
```

**Dependencies:**
```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
```

**Ôn tập**: 
- JWT structure (Header.Payload.Signature)
- Claims & Token expiration
- Token refresh strategies
- Security best practices for token storage

### **2. JWT Filter Chain**
```java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        // 1. Extract token from Authorization header
        String token = getJwtFromRequest(request);
        
        // 2. Validate token
        if (token != null && tokenProvider.validateToken(token)) {
            
            // 3. Get user info from token
            String email = tokenProvider.getEmailFromJWT(token);
            
            // 4. Load user details
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            
            // 5. Create authentication token
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            
            // 6. Set in SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        
        // 7. Continue filter chain
        filterChain.doFilter(request, response);
    }
}
```

**Ôn tập**: 
- Filter chain concept
- OncePerRequestFilter
- SecurityContext & SecurityContextHolder
- Authorization header format

### **3. Spring Security Configuration**
```java
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationEntryPoint unauthorizedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CORS & CSRF for stateless API
            .cors(cors -> cors.configure(http))
            .csrf(csrf -> csrf.disable())
            
            // Exception handling
            .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedHandler))
            
            // Stateless session
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(AppConstants.PUBLIC_APIS).permitAll()
                .requestMatchers(AppConstants.USER_APIS).hasAnyRole("USER", "ADMIN")
                .requestMatchers(AppConstants.ADMIN_APIS).hasRole("ADMIN")
                .anyRequest().authenticated());
        
        // Add JWT filter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

**Ôn tập**: 
- SecurityFilterChain builder
- CORS vs CSRF
- Role-based access control (RBAC)
- Path-based authorization
- Custom filters

### **4. Password Encoding**
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

**Ôn tập**: 
- BCrypt hashing
- Salt & rounds
- Password validation
- Avoid plain text passwords

### **5. OAuth2 Login**
```properties
spring.security.oauth2.client.registration.google.client-id=...
spring.security.oauth2.client.registration.google.client-secret=...
spring.security.oauth2.client.registration.google.scope=openid,profile,email
```

**Ôn tập**: 
- OAuth2 flow (Authorization Code, Implicit, Client Credentials, Resource Owner)
- Social login integration
- Token exchange
- User info endpoint

### **6. Role-Based Access Control (RBAC)**
```java
// Define roles
@RequestMapping("/api/admin")  // ADMIN only
@RequestMapping("/api/user")   // USER & ADMIN
@RequestMapping("/api/public") // Everyone
```

**Ôn tập**: 
- Role hierarchy
- Permission vs Role
- @PreAuthorize, @PostAuthorize annotations
- Method-level security

---

## Real-time Communication

### **1. WebSocket with STOMP**
```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enable simple broker
        registry.enableSimpleBroker("/topic", "/queue");
        
        // Prefix for client → server messages
        registry.setApplicationDestinationPrefixes("/app");
        
        // User destination prefix for direct messages
        registry.setUserDestinationPrefix("/user");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .setHandshakeHandler(new UserHandshakeHandler())
                .addInterceptors(jwtHandshakeInterceptor);
    }
}
```

**Ôn tập**: 
- WebSocket vs HTTP
- STOMP (Simple Text Oriented Messaging Protocol)
- Message broker patterns
- Client-server messaging

### **2. Chat Implementation**
```java
@RestController
@RequestMapping("/api/chat")
public class ChatController {
    
    @MessageMapping("/sendMessage/{conversationId}")  // /app/sendMessage/{conversationId}
    @SendTo("/topic/conversation/{conversationId}")   // Broadcast to all subscribers
    public ChatMessageDTO sendMessage(
        @DestinationVariable Long conversationId,
        ChatMessageDTO message) {
        // Handle message
        return message;
    }
}
```

**Ôn tập**: 
- @MessageMapping (server-side message handler)
- @SendTo (broadcast destination)
- @SendToUser (direct message to user)
- Message handlers

### **3. Async Processing**
```java
@SpringBootApplication
@EnableAsync
public class DlmstoreApplication { }

@Service
public class EmailService {
    @Async
    public void sendEmailAsync(String to, String subject, String content) {
        // Non-blocking email sending
    }
}
```

**Ôn tập**: 
- @Async annotation
- ExecutorService & ThreadPoolExecutor
- CompletableFuture
- Callback vs Promise vs Observable

---

## API Response Structure

### **Generic Response Wrapper**
```java
public class ApiResponse<T> {
    private String code;        // "success" or "error"
    private int status;         // HTTP status
    private String message;     // Human-readable message
    private Date timestamp;     // Response time
    private T data;             // Actual data
    
    public static <T> ApiResponse<T> success(T data) {
        // Create success response
    }
    
    public static <T> ApiResponse<T> error(int status, String message) {
        // Create error response
    }
}
```

**Ôn tập**: 
- REST API best practices
- Consistent response format
- HTTP status codes (200, 201, 400, 401, 403, 404, 500)
- Error handling standardization

---

## Caching with Redis

### **Redis Configuration**
```java
@Configuration
public class RedisConfig {
    // RedisTemplate configuration
    // StringRedisTemplate for string values
    // Jackson serialization
}
```

**Ôn tập**: 
- Key-value store concepts
- Redis data structures (String, List, Set, Hash, Sorted Set)
- TTL (Time To Live)
- Eviction policies
- Cache invalidation strategies

---

## Common Challenges & Solutions

### **1. N+1 Query Problem**
```java
// PROBLEM: Fetching product with categories
@OneToMany(fetch = FetchType.EAGER)  // ❌ Causes N+1 queries
private List<ProductImage> images;

// SOLUTION: Use LAZY loading + joins in queries
@OneToMany(fetch = FetchType.LAZY)
private List<ProductImage> images;

// Or use JOIN FETCH in JPQL
@Query("""
    SELECT DISTINCT p FROM Product p
    LEFT JOIN FETCH p.images
    LEFT JOIN FETCH p.category
    WHERE p.id IN :ids
""")
List<Product> findAllWithDetails(@Param("ids") List<Long> ids);
```

### **2. Concurrency & Race Conditions**
```java
// PROBLEM: Multiple users buying last item
// SOLUTION: Optimistic Locking with @Version
@Version
private Long version;

@ExceptionHandler(ObjectOptimisticLockingFailureException.class)
public ResponseEntity<ApiResponse<Object>> handleOptimisticLocking(...) { }
```

### **3. Memory Leaks in DTO Mapping**
```java
// SOLUTION: Use MapStruct for efficient mapping
@Mapper(componentModel = "spring")
public abstract class ProductMapper {
    @Mapping(source = "id", target = "id")
    public abstract ProductDTO toDTO(Product product);
}
```

### **4. Large File Upload**
```properties
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=100MB
```

**Ôn tập**: Streaming uploads, progress tracking, validation

### **5. Security Vulnerabilities**
- **CORS**: Allow specific origins only
- **CSRF**: Disable for stateless APIs
- **SQL Injection**: Use parameterized queries
- **XSS**: Validate & sanitize input
- **HTTPS**: Use in production

### **6. Email Verification & OTP**
- Store OTP with TTL in Redis
- Send via Gmail SMTP
- Validate before account activation

### **7. Payment Integration (VNPay)**
```properties
vnpay.tmnCode=...
vnpay.hashSecret=...
vnpay.payUrl=https://sandbox.vnpayment.vn/...
vnpay.returnUrl=...
```

**Ôn tập**: 
- Payment gateway flow
- Webhook handling
- Transaction status tracking
- Refund processing

---

## Questions to Practice

### **Basic Java Questions**
1. Giải thích SOLID principles và cách áp dụng vào dự án
2. Khác biệt giữa `==` và `.equals()` trong Java
3. String pool là gì? Tại sao String immutable?
4. Exception handling - checked vs unchecked
5. Generics - type erasure là gì?
6. Collections - ArrayList vs LinkedList, HashMap vs TreeMap
7. Stream API - terminal vs intermediate operations

### **Spring & Spring Boot**
1. Dependency Injection - IoC container hoạt động như thế nào?
2. Bean lifecycle - từ lúc tạo đến destroy
3. @Component vs @Service vs @Repository vs @Controller
4. Constructor injection vs field injection - nên dùng cái nào?
5. @Transactional - đặt ở đâu, có tác dụng gì?
6. @Async - hoạt động như thế nào? Thread pool?
7. AOP - Aspect-Oriented Programming ứng dụng thế nào?

### **Spring Security & Authentication**
1. JWT - cấu trúc gồm những phần nào? Ưu nhược điểm?
2. Filter chain hoạt động thế nào?
3. SecurityContext là gì?
4. CORS vs CSRF - khác biệt gì? Vì sao disable CSRF cho API?
5. OAuth2 flow - Google login hoạt động như thế nào?
6. Password encoding - tại sao dùng BCrypt?

### **Database & ORM**
1. JPA/Hibernate - Entity lifecycle (Transient, Managed, Detached, Removed)
2. N+1 problem - nguyên nhân gì? Cách khắc phục?
3. Lazy vs Eager loading - ưu nhược?
4. Transaction propagation - REQUIRED, REQUIRES_NEW, NESTED khác gì?
5. Optimistic locking - khi nào sử dụng?
6. Custom repository queries - @Query, method naming, Specifications

### **Real-time Communication**
1. WebSocket vs HTTP - khi nào dùng WebSocket?
2. STOMP là gì? Ưu điểm so với raw WebSocket?
3. Message broker pattern - /topic vs /queue
4. JWT + WebSocket - xác thực thế nào?

### **Project-Specific**
1. Trong dự án, làm thế nào xác thực người dùng?
2. Tính năng chat realtime được implement như thế nào?
3. PaymentService tích hợp VNPay - flow là gì?
4. ProductMapper dùng MapStruct - tại sao không dùng manual mapping?
5. Redis được dùng cho việc gì? Caching strategy?
6. Gemini AI được tích hợp cho chatbot - API call flow?
7. Cloudinary - tại sao upload ảnh lên cloud thay vì server?

### **Design Patterns**
1. MVC pattern - tại sao cần chia thành Controller, Service, Repository?
2. Adapter pattern - GoogleService, FacebookService implement thế nào?
3. Strategy pattern - OAuth2 vs normal login
4. Decorator pattern - @Transactional, @Async, @Validated
5. Singleton pattern - Spring beans
6. Facade pattern - OrderService

### **Performance & Optimization**
1. Pagination vs Lazy loading - khi nào dùng?
2. Caching strategy - Redis key naming, TTL, invalidation
3. Async processing - @Async vs @Transactional
4. Query optimization - JOIN FETCH, Projections, Specifications
5. Connection pooling - DataSource configuration

### **Testing & Debugging**
1. Làm thế nào viết unit test cho Service layer?
2. Mock dependencies - @Mock, @InjectMocks
3. Integration testing - @SpringBootTest
4. Debug JWT issues - token validation, claims

### **Best Practices**
1. Exception handling - custom exceptions vs generic exceptions
2. Logging - @Slf4j, logging levels (DEBUG, INFO, WARN, ERROR)
3. API versioning - /api/v1/products vs /api/v2/products
4. Documentation - Swagger/OpenAPI
5. Code review - commit message conventions, branch strategy

---

## 💡 ÔN TẬP NGOÀI DỰ ÁN

### **Java Fundamentals**
- [ ] String operations, StringBuilder vs StringBuffer
- [ ] Thread & Concurrency - synchronized, volatile, concurrent collections
- [ ] Serialization & Deserialization
- [ ] Reflection & Annotation processing
- [ ] ClassLoader & JAR files

### **Spring Ecosystem**
- [ ] Spring MVC - Interceptors, HandlerMapping
- [ ] Spring Cloud - Microservices, Service Registry
- [ ] Spring Data - Query DSL, Specifications
- [ ] Spring Batch - Batch processing
- [ ] Spring Integration - Message-driven apps

### **System Design**
- [ ] Database design - Normalization, Indexes, Sharding
- [ ] Caching strategies - LRU, LFU, Write-through, Write-back
- [ ] Load balancing & horizontal scaling
- [ ] Message queues - RabbitMQ, Kafka
- [ ] API design - RESTful, GraphQL, gRPC

### **DevOps & Deployment**
- [ ] Docker - Containerization
- [ ] Kubernetes - Orchestration
- [ ] CI/CD - Jenkins, GitLab CI, GitHub Actions
- [ ] Environment management - dev, staging, production
- [ ] Monitoring & Logging - ELK stack, Prometheus

---

## 📝 ĐIỀU CẦN LƯU Ý TRONG PHỎNG VẤN

1. **Giải thích rõ ràng** - Không chỉ biết, mà phải giải thích được tại sao
2. **Dẫn dắt từ dự án** - Khi được hỏi, hãy kể ví dụ từ dự án của mình
3. **Trade-offs** - Mọi quyết định thiết kế đều có pros & cons
4. **Best practices** - Tại sao chọn cách này thay vì cách kia
5. **Curiosity** - Hỏi lại interviewer về expectations, tech stack, team

---

## 🎯 TIMELINE ÔN TẬP

- **Tuần 1**: Core Java concepts, OOP, Collections, Exceptions
- **Tuần 2**: Spring Boot, Dependency Injection, Configuration
- **Tuần 3**: Spring Security, JWT, Authentication
- **Tuần 4**: JPA/Hibernate, Transactions, N+1 problems
- **Tuần 5**: REST APIs, Response handling, Error handling
- **Tuần 6**: Design patterns, Real-time communication, WebSocket
- **Tuần 7**: Project deep-dive, edge cases, optimization
- **Tuần 8**: Mock interviews, practice questions

---

## 🔗 ĐỌC THÊM

- Spring Boot Documentation: https://spring.io/projects/spring-boot
- Spring Security Reference: https://spring.io/projects/spring-security
- JPA & Hibernate: https://hibernate.org/orm/documentation/
- JWT Best Practices: https://tools.ietf.org/html/rfc7519
- OWASP Security: https://owasp.org/

---

**Chúc bạn ôn tập hiệu quả! 🚀**
