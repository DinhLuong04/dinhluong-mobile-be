# 🔧 ACTIONABLE REFACTORING CHECKLIST

**Based on:** ARCHITECTURE_ANALYSIS.md  
**Priority Level:** Critical → Important → Enhancement  
**Timeline:** 4 weeks (1 week per phase)

---

## ⚠️ PHASE 1: CRITICAL (Start Here - 1 Week)

### Task 1.1: Fix Dependency Injection Mixing ⭐⭐⭐
**Impact:** HIGH | Testability, Maintainability  
**Effort:** 4-5 hours

**Files to Fix:**
- `config/DataInitializer.java` - Convert 3 fields to constructor
- `controller/ProductController.java` - Convert 2 fields to constructor  
- `service/imp/AuthServiceImpl.java` - Convert 4 fields to constructor
- `service/GoogleService.java` - Convert 3 fields to constructor
- `service/FacebookService.java` - Convert 3 fields to constructor
- `service/CartService.java` - Convert 4 fields to constructor

**Pattern:**
```java
// BEFORE (Field Injection)
@Service
public class AuthServiceImpl {
    @Autowired
    private UserRepository userRepository;
}

// AFTER (Constructor Injection)
@Service
@RequiredArgsConstructor  // Lombok generates constructor
public class AuthServiceImpl {
    private final UserRepository userRepository;
}
```

**Benefits:**
- ✅ Unit tests can inject mocks without reflection
- ✅ Dependencies visible at glance
- ✅ Immutability (final fields)
- ✅ Null pointer safety

---

### Task 1.2: Replace Generic RuntimeException with Custom Exceptions ⭐⭐⭐
**Impact:** HIGH | Error Handling, Debugging  
**Effort:** 3-4 hours

**Current Issues:**
```
Found 50+ instances of:
throw new RuntimeException("message");
```

**Custom Exceptions Already Exist:**
- ✅ AppException
- ✅ ValidationException  
- ✅ ResourceNotFoundException
- ✅ AuthenticationException
- ✅ AuthorizationException

**Replacements Needed:**

| Current | Replace With | Where |
|---------|--------------|-------|
| `RuntimeException("Email đã tồn tại")` | `ValidationException("Email already exists")` | AuthServiceImpl:26 |
| `RuntimeException("Sai mật khẩu")` | `AuthenticationException("Invalid password")` | AuthServiceImpl:78 |
| `RuntimeException("Sản phẩm không tồn tại")` | `ResourceNotFoundException("Product not found")` | CartService:35 |
| `RuntimeException("Tài khoản đã bị khóa")` | `AuthorizationException("Account locked")` | GoogleService:62 |

**Detailed Task List:**
- [ ] Search: `throw new RuntimeException` across all services
- [ ] Create domain-specific exception classes:
  - `EmailAlreadyExistsException extends AppException`
  - `InvalidPasswordException extends AppException`
  - `InsufficientStockException extends AppException`
  - `OrderCreationException extends AppException`
- [ ] Update GlobalExceptionHandler to handle new exceptions
- [ ] Update unit tests

---

### Task 1.3: Implement @Cacheable for Frequently Accessed Data ⭐⭐
**Impact:** MEDIUM | Performance, Scalability  
**Effort:** 2-3 hours

**Redis Already Configured:** ✅ RedisConfig.java exists

**Caching Opportunities:**

1. **Product Service** - Most frequently read
```java
@Cacheable(value = "products", key = "#slug")
@Transactional(readOnly = true)
public Product getBySlug(String slug) {
    return productRepository.findBySlug(slug).orElse(null);
}

@Cacheable(value = "featuredProducts", key = "#limit")
@Transactional(readOnly = true)
public List<Product> getFeaturedProducts(int limit) {
    return productRepository.findFeaturedProducts(limit);
}

@CacheEvict(value = "products", allEntries = true)
public void updateProduct(Product product) {
    productRepository.save(product);
}
```

2. **Category Service**
```java
@Cacheable(value = "categories", key = "#slug")
public Category getCategoryBySlug(String slug) {
    return categoryRepository.findBySlug(slug).orElse(null);
}
```

3. **User Service**
```java
@Cacheable(value = "users", key = "#email")
public User findByEmail(String email) {
    return userRepository.findByEmail(email).orElse(null);
}
```

**Checklist:**
- [ ] Add `@EnableCaching` to main application class
- [ ] Add `@Cacheable` to read-heavy methods (ProductService, CategoryService)
- [ ] Add `@CacheEvict` to write operations
- [ ] Test cache invalidation
- [ ] Configure cache TTL in application.properties

---

## 🟡 PHASE 2: IMPORTANT (2-3 Weeks)

### Task 2.1: Create Service Facades - Extract from Controllers ⭐⭐⭐
**Impact:** HIGH | Maintainability, SOLID principles  
**Effort:** 6-8 hours

**Problem:** Business logic scattered across controllers

**Solution:** Create Facade services

**Step 1: Create OrderFacadeService**
```java
@Service
@Transactional
@RequiredArgsConstructor
public class OrderFacadeService {
    
    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final ProductService productService;
    private final PaymentService paymentService;
    private final NotificationService notificationService;
    
    public OrderResponse placeOrder(PlaceOrderRequest request, Long userId) {
        // 1. Fetch cart
        Cart cart = cartService.getCartByUserId(userId);
        
        // 2. Validate products & stock
        for (CartItem item : cart.getItems()) {
            Product product = productService.getBySlug(item.getProduct().getSlug());
            if (product.getStock() < item.getQuantity()) {
                throw new InsufficientStockException("Product out of stock");
            }
        }
        
        // 3. Create order
        Order order = Order.builder()
            .userId(userId)
            .items(cart.getItems())
            .totalAmount(calculateTotal(cart))
            .build();
        orderRepository.save(order);
        
        // 4. Handle payment
        if ("COD".equals(request.getPaymentMethod())) {
            paymentService.createCODPayment(order);
        } else if ("VNPAY".equals(request.getPaymentMethod())) {
            paymentService.createVNPayPayment(order);
        }
        
        // 5. Notify user
        notificationService.notifyOrderCreated(userId, order);
        
        return mapToResponse(order);
    }
}
```

**Step 2: Create PaymentFacadeService**
```java
@Service
@Transactional
@RequiredArgsConstructor
public class PaymentFacadeService {
    
    private final PaymentStrategyFactory strategyFactory;
    private final PaymentRepository paymentRepository;
    
    public String getPaymentUrl(Order order, String method, HttpServletRequest request) {
        PaymentStrategy strategy = strategyFactory.getStrategy(method);
        return strategy.getPaymentUrl(order, request);
    }
    
    public PaymentResponse handlePaymentCallback(HttpServletRequest request, String method) {
        PaymentStrategy strategy = strategyFactory.getStrategy(method);
        return strategy.handleCallback(request);
    }
}
```

**Step 3: Refactor OrderController**
```java
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderFacadeService orderFacade;
    private final PaymentFacadeService paymentFacade;
    private final CustomUserPrincipal userPrincipal;
    
    @PostMapping("/place")
    public ApiResponse<OrderResponse> placeOrder(@RequestBody PlaceOrderRequest request) {
        Long userId = userPrincipal.getId();
        OrderResponse response = orderFacade.placeOrder(request, userId);
        return ApiResponse.success("Order placed successfully", response);
    }
    
    @GetMapping("/vnpay-return")
    public ApiResponse<PaymentResponse> vnpayReturn(HttpServletRequest request) {
        PaymentResponse response = paymentFacade.handlePaymentCallback(request, "vnpay");
        return ApiResponse.success("Payment processed", response);
    }
}
```

**Files to Create:**
- [ ] `service/OrderFacadeService.java`
- [ ] `service/PaymentFacadeService.java`
- [ ] Refactor `controller/OrderController.java`

---

### Task 2.2: Implement Strategy Pattern with Interfaces ⭐⭐
**Impact:** HIGH | SOLID principles, Extensibility  
**Effort:** 5-6 hours

**Current Problem:** No unified interfaces for OAuth services

**Step 1: Create Strategy Interfaces**
```java
// service/strategy/AuthenticationStrategy.java
public interface AuthenticationStrategy {
    LoginResponse authenticate(AuthenticationCredentials credentials);
}

public class AuthenticationCredentials {
    private String type;  // "LOCAL", "GOOGLE", "FACEBOOK"
    private String credential1;  // email or accessToken
    private String credential2;  // password (if local)
}

// service/strategy/PaymentStrategy.java
public interface PaymentStrategy {
    String getPaymentUrl(Order order, HttpServletRequest request);
    PaymentResponse handleCallback(HttpServletRequest request);
}
```

**Step 2: Implement Strategies**
```java
@Service
public class LocalAuthStrategy implements AuthenticationStrategy {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public LoginResponse authenticate(AuthenticationCredentials credentials) {
        Users user = userRepository.findByEmail(credentials.getCredential1())
            .orElseThrow(() -> new AuthenticationException("Invalid email"));
        
        if (!passwordEncoder.matches(credentials.getCredential2(), user.getPassword())) {
            throw new AuthenticationException("Invalid password");
        }
        
        return new LoginResponse(...);
    }
}

@Service("google")
public class GoogleAuthStrategy implements AuthenticationStrategy {
    
    @Override
    public LoginResponse authenticate(AuthenticationCredentials credentials) {
        // credentials.credential1 = accessToken
        String accessToken = credentials.getCredential1();
        return googleLogin(accessToken);
    }
}

@Service("facebook")
public class FacebookAuthStrategy implements AuthenticationStrategy {
    
    @Override
    public LoginResponse authenticate(AuthenticationCredentials credentials) {
        // credentials.credential1 = accessToken
        String accessToken = credentials.getCredential1();
        return facebookLogin(accessToken);
    }
}
```

**Step 3: Create Strategy Factory**
```java
@Service
@RequiredArgsConstructor
public class AuthStrategyFactory {
    
    private final Map<String, AuthenticationStrategy> strategies;
    
    public LoginResponse authenticate(String strategyName, AuthenticationCredentials credentials) {
        AuthenticationStrategy strategy = strategies.get(strategyName.toLowerCase());
        if (strategy == null) {
            throw new ValidationException("Strategy not found: " + strategyName);
        }
        return strategy.authenticate(credentials);
    }
}
```

**Files to Create:**
- [ ] `service/strategy/AuthenticationStrategy.java`
- [ ] `service/strategy/AuthenticationCredentials.java`
- [ ] `service/strategy/PaymentStrategy.java`
- [ ] `service/strategy/AuthStrategyFactory.java`
- [ ] Refactor existing OAuth services to implement interface
- [ ] Update `AuthController` to use factory

---

### Task 2.3: Fix N+1 Query Problems ⭐⭐
**Impact:** MEDIUM | Performance  
**Effort:** 4-5 hours

**Critical Path #1: CartService.getCartByUserId**
```java
// BEFORE (N+1 problem)
public CartResponse getCartByUserId(Long userId) {
    List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
    
    List<Long> mainVariantIds = items.stream()
        .map(CartItem::getProductVariantId)
        .toList();
    
    Map<Long, ProductVariant> variantMap = productVariantRepository.findAllById(mainVariantIds)
        .stream()
        .map(v -> v.getProduct().getId())  // ❌ N queries for products
        .toList();
}

// AFTER (Eager loading)
@Query("""
    SELECT DISTINCT v FROM ProductVariant v
    JOIN FETCH v.product p
    WHERE v.id IN :ids
""")
List<ProductVariant> findAllByIdWithProduct(@Param("ids") List<Long> ids);

// In service:
List<ProductVariant> variants = productVariantRepository.findAllByIdWithProduct(mainVariantIds);
```

**Critical Path #2: ProductService.getAllProducts**
```java
// Add to ProductRepository
@Query("""
    SELECT DISTINCT p FROM Product p
    LEFT JOIN FETCH p.images
    LEFT JOIN FETCH p.variants
    WHERE p.category.slug = :slug
""")
Page<Product> findByCategorySlugWithRelations(@Param("slug") String slug, Pageable pageable);
```

**Checklist:**
- [ ] Profile current queries using DataSource logging
- [ ] Identify N+1 query locations
- [ ] Add JOIN FETCH queries
- [ ] Test with multiple records
- [ ] Verify query count in logs

---

## 🟢 PHASE 3: ENHANCEMENT (3-4 Weeks)

### Task 3.1: Event-Driven Architecture ⭐⭐
**Impact:** MEDIUM | Loose coupling, Scalability  
**Effort:** 6-8 hours

**Step 1: Create Domain Events**
```java
// event/OrderCreatedEvent.java
public class OrderCreatedEvent extends ApplicationEvent {
    private final Order order;
    
    public OrderCreatedEvent(Object source, Order order) {
        super(source);
        this.order = order;
    }
    
    public Order getOrder() {
        return order;
    }
}

// event/PaymentCompletedEvent.java
public class PaymentCompletedEvent extends ApplicationEvent {
    private final Payment payment;
    
    public PaymentCompletedEvent(Object source, Payment payment) {
        super(source);
        this.payment = payment;
    }
}
```

**Step 2: Create Event Publishers**
```java
@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final ApplicationEventPublisher eventPublisher;
    
    @Transactional
    public Order createOrder(PlaceOrderRequest request) {
        Order order = Order.builder()
            .userId(request.getUserId())
            // ...
            .build();
        
        orderRepository.save(order);
        
        // Publish event (listeners notified asynchronously)
        eventPublisher.publishEvent(new OrderCreatedEvent(this, order));
        
        return order;
    }
}
```

**Step 3: Create Event Listeners**
```java
@Component
public class OrderNotificationListener {
    
    @Autowired
    private NotificationService notificationService;
    
    @EventListener
    @Async
    public void handleOrderCreated(OrderCreatedEvent event) {
        Order order = event.getOrder();
        notificationService.notifyOrderCreated(order.getUserId(), order);
    }
}

@Component
public class OrderEmailListener {
    
    @Autowired
    private EmailService emailService;
    
    @EventListener
    @Async
    public void handleOrderCreated(OrderCreatedEvent event) {
        Order order = event.getOrder();
        emailService.sendOrderConfirmationEmail(order.getUser().getEmail(), order);
    }
}

@Component
public class OrderInventoryListener {
    
    @Autowired
    private InventoryService inventoryService;
    
    @EventListener
    @Async
    public void handleOrderCreated(OrderCreatedEvent event) {
        Order order = event.getOrder();
        inventoryService.updateInventory(order);
    }
}
```

**Files to Create:**
- [ ] `event/OrderCreatedEvent.java`
- [ ] `event/PaymentCompletedEvent.java`
- [ ] `listener/OrderNotificationListener.java`
- [ ] `listener/OrderEmailListener.java`
- [ ] Refactor service to publish events

---

### Task 3.2: Create Repository Interfaces for Better Abstraction ⭐
**Impact:** MEDIUM | SOLID principles, Testability  
**Effort:** 4-5 hours

**Create Repository Interfaces:**
```java
// repository/contract/IProductRepository.java
public interface IProductRepository {
    Product findBySlug(String slug);
    Page<Product> searchProducts(String keyword, Pageable pageable);
    List<Product> findFeaturedProducts(int limit);
}

// repository/ProductRepository.java (implementation)
@Repository
public class ProductRepositoryImpl implements IProductRepository, JpaRepository<Product, Long> {
    // Implement interface methods
}

// service/ProductService.java (use interface)
@Service
public class ProductService {
    
    private final IProductRepository productRepository;  // Depend on interface
    
    public Product getBySlug(String slug) {
        return productRepository.findBySlug(slug);
    }
}
```

**Files to Create:**
- [ ] `repository/contract/IProductRepository.java`
- [ ] `repository/contract/IOrderRepository.java`
- [ ] `repository/contract/IUserRepository.java`
- [ ] Implement interfaces in concrete repositories
- [ ] Update services to use interfaces

---

## 📊 PHASE 4: OPTIMIZATION (4+ Weeks)

### Task 4.1: Performance Tuning
- [ ] Add database indexes for frequently searched columns
- [ ] Implement query result pagination everywhere
- [ ] Add request/response caching headers
- [ ] Implement database connection pooling optimization

### Task 4.2: Monitoring & Logging
- [ ] Add SLF4J logging with appropriate levels
- [ ] Implement request tracing (MDC)
- [ ] Add metrics collection (Micrometer)
- [ ] Implement health checks endpoint

### Task 4.3: Documentation
- [ ] Create architecture decision records (ADRs)
- [ ] Document API contracts
- [ ] Create deployment guide
- [ ] Document configuration options

---

## 📈 PROGRESS TRACKING

### Week 1 (Phase 1)
```
[ ] 1.1 Fix Dependency Injection (4-5h)
[ ] 1.2 Replace RuntimeException (3-4h)
[ ] 1.3 Add @Cacheable (2-3h)
---
    Total: 9-12 hours
```

### Weeks 2-3 (Phase 2)
```
[ ] 2.1 Create Facades (6-8h)
[ ] 2.2 Strategy Pattern (5-6h)
[ ] 2.3 Fix N+1 Queries (4-5h)
---
    Total: 15-19 hours
```

### Weeks 3-4 (Phase 3)
```
[ ] 3.1 Event-Driven (6-8h)
[ ] 3.2 Repository Interfaces (4-5h)
---
    Total: 10-13 hours
```

**Total Time Investment:** ~35-45 hours (distributed over 4 weeks)

---

## 🎯 SUCCESS METRICS

After refactoring:
- ✅ 100% constructor injection (no @Autowired fields)
- ✅ 0 RuntimeException in domain code
- ✅ All read queries using @Cacheable
- ✅ Query count reduced by 40%+
- ✅ Testability score improved from 6/10 to 9/10
- ✅ SOLID compliance score improved from 6.5/10 to 9/10

---

## 📝 HOW TO USE THIS CHECKLIST

1. **Start with Phase 1** - These fixes have highest impact per effort ratio
2. **Complete each task** - Use the provided code examples
3. **Test thoroughly** - Unit tests for each refactoring
4. **Review & document** - Get team feedback
5. **Deploy incrementally** - Don't do everything at once

**Estimated Completion:** 4 weeks with 2 developers

Good luck! 💪
