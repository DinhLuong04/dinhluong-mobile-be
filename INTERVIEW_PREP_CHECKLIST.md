# ✅ INTERVIEW PREP CHECKLIST

## 📋 CÓ GÌ ĐỀ ÔN TRONG DỰ ÁN?

### **TECHNOLOGIES USED**
- [x] Spring Boot 3.3.6
- [x] Java 21
- [x] MySQL Database
- [x] Redis Caching
- [x] JWT Authentication
- [x] OAuth2 (Google, Facebook)
- [x] Spring Security
- [x] Spring WebSocket + STOMP
- [x] MapStruct
- [x] JPA/Hibernate
- [x] Cloudinary (Image Storage)
- [x] VNPay (Payment Gateway)
- [x] Gemini API (AI Chatbot)
- [x] Gmail SMTP (Email)

### **CORE CONCEPTS**
- [x] Layered Architecture (3-tier)
- [x] REST API Design
- [x] Dependency Injection & IoC
- [x] Bean Lifecycle
- [x] Transaction Management
- [x] Exception Handling
- [x] Async Processing (@Async)
- [x] Request/Response Cycle
- [x] HTTP Methods (GET, POST, PUT, DELETE)
- [x] HTTP Status Codes
- [x] CORS & CSRF

### **DESIGN PATTERNS**
- [x] MVC Pattern
- [x] Repository Pattern
- [x] Service Layer Pattern
- [x] Mapper/Converter Pattern (DTO)
- [x] Adapter Pattern
- [x] Strategy Pattern
- [x] Observer Pattern
- [x] Decorator Pattern
- [x] Facade Pattern
- [x] Singleton Pattern
- [x] Template Method Pattern
- [x] Factory Pattern

### **DATABASE**
- [x] JPA/Hibernate ORM
- [x] Entity Relationships (@OneToMany, @ManyToOne, etc.)
- [x] JPQL Queries
- [x] Native SQL Queries
- [x] Query Methods (Naming Convention)
- [x] Pagination & Sorting
- [x] N+1 Query Problem
- [x] Lazy vs Eager Loading
- [x] Transaction Propagation
- [x] Optimistic Locking (@Version)
- [x] Cascade Types
- [x] Soft Delete (isDeleted flag)

### **SECURITY**
- [x] JWT Token (Structure, Generation, Validation)
- [x] Filter Chain
- [x] SecurityContext
- [x] Role-Based Access Control (RBAC)
- [x] Password Encoding (BCrypt)
- [x] OAuth2 Flow
- [x] Authorization vs Authentication
- [x] Token Expiration
- [x] Refresh Token Strategy
- [x] HTTPS
- [x] SQL Injection Prevention
- [x] XSS Prevention

### **REAL-TIME COMMUNICATION**
- [x] WebSocket vs HTTP
- [x] STOMP Protocol
- [x] Message Broker Pattern
- [x] @MessageMapping
- [x] @SendTo
- [x] @SendToUser
- [x] Subscription Topics
- [x] User Handshake
- [x] Connection Management

### **JAVA FUNDAMENTALS**
- [x] OOP (Encapsulation, Inheritance, Polymorphism, Abstraction)
- [x] Collections (List, Set, Map)
- [x] Stream API
- [x] Lambda Expressions
- [x] Method References
- [x] Generics
- [x] Exception Handling (try-catch-finally, custom exceptions)
- [x] String operations
- [x] Annotations & Reflection
- [x] Serialization

---

## 📌 CÁC BỘ PHẬN CHÍNH CẦN ÔN

### 1. **Authentication Module**
**Files:**
- AuthController, AuthService, AuthServiceImpl
- JwtAuthenticationFilter, JwtAuthenticationEntryPoint
- JwtTokenProvider, CustomUserDetailsService
- SecurityConfig

**Questions to answer:**
- [ ] JWT token structure là gì?
- [ ] Authentication flow hoạt động thế nào?
- [ ] OAuth2 login khác gì với normal login?
- [ ] Filter chain làm gì?
- [ ] Làm thế nào validate JWT token?

**Demo capability:**
- [ ] Giải thích JWT generation & validation
- [ ] Vẽ authentication flow diagram
- [ ] Explain OAuth2 flow step-by-step

---

### 2. **Product Management Module**
**Files:**
- Product, ProductImage, ProductVariant, ProductHighlightSpec entities
- ProductRepository, ProductController, ProductService
- ProductMapper

**Questions to answer:**
- [ ] Entity relationships là gì?
- [ ] N+1 problem trong search products?
- [ ] Cách filter/search efficiently?
- [ ] MapStruct dùng để làm gì?
- [ ] Pagination & Sorting implementation?

**Demo capability:**
- [ ] Write efficient product search query
- [ ] Optimize N+1 problem
- [ ] Explain DTO mapping strategy

---

### 3. **Order & Payment Module**
**Files:**
- Order, OrderItem, Payment entities
- OrderRepository, OrderController, OrderService
- PaymentService, VNPay integration

**Questions to answer:**
- [ ] Order creation flow?
- [ ] VNPay payment integration?
- [ ] Transaction management?
- [ ] Stock management & race conditions?
- [ ] Payment validation & security?

**Demo capability:**
- [ ] Explain order flow diagram
- [ ] Handle optimistic locking issues
- [ ] VNPay signature validation

---

### 4. **Real-time Chat Module**
**Files:**
- ChatController, ChatService
- WebSocketConfig, UserHandshakeHandler, JwtHandshakeInterceptor
- ChatMessage, Conversation entities

**Questions to answer:**
- [ ] WebSocket vs HTTP?
- [ ] STOMP protocol là gì?
- [ ] Message broker pattern?
- [ ] @SendTo vs @SendToUser?
- [ ] Authentication in WebSocket?

**Demo capability:**
- [ ] Draw WebSocket architecture
- [ ] Explain STOMP frame structure
- [ ] Message routing logic

---

### 5. **Security & Exception Handling**
**Files:**
- SecurityConfig, JwtAuthenticationFilter
- GlobalExceptionHandler, CustomExceptions
- JwtAuthenticationEntryPoint, CustomAccessDeniedHandler

**Questions to answer:**
- [ ] Exception handling strategy?
- [ ] Global exception handler pattern?
- [ ] CORS configuration?
- [ ] CSRF protection needed?
- [ ] Security best practices?

**Demo capability:**
- [ ] Explain exception flow
- [ ] Design error response format
- [ ] Handle various exceptions gracefully

---

## 🧠 MAIN CONCEPTS TO MASTER

### **Spring Boot Concepts**
- [ ] Application Context & Beans
- [ ] Dependency Injection (constructor, field, setter)
- [ ] Auto-configuration
- [ ] Spring Boot Starters
- [ ] Actuator
- [ ] Properties & YAML Configuration
- [ ] Profiles (dev, staging, prod)

### **Spring Security**
- [ ] Security Filter Chain
- [ ] Authentication Manager
- [ ] Authorization
- [ ] Role-based Access
- [ ] JWT vs Sessions
- [ ] OAuth2 Flow

### **Spring Data JPA**
- [ ] EntityManager & Persistence Context
- [ ] Query Methods (naming convention)
- [ ] JPQL vs Native Queries
- [ ] Lazy vs Eager Loading
- [ ] Transaction Propagation
- [ ] Cascade Types

### **Microservices Patterns** (Future learning)
- [ ] Circuit Breaker
- [ ] Retry Pattern
- [ ] Timeout Pattern
- [ ] Load Balancing
- [ ] Service Discovery

---

## 🎯 PROBLEM SOLVING PRACTICE

### **Scenarios to Practice:**

1. **Race Condition in Stock Update**
   - [ ] Problem: Two users buy last item simultaneously
   - [ ] Solution: Optimistic locking with @Version
   - [ ] Test case implementation

2. **N+1 Query Problem in Product List**
   - [ ] Problem: Extra queries in loop
   - [ ] Solutions: JOIN FETCH, Projection, Pagination
   - [ ] Performance comparison

3. **JWT Token Expiration Handling**
   - [ ] Problem: Token expires mid-session
   - [ ] Solution: Refresh token strategy
   - [ ] Client-side implementation

4. **Handling Large File Upload**
   - [ ] Problem: Memory issues with large files
   - [ ] Solution: Streaming upload, progress tracking
   - [ ] Configuration

5. **Optimizing Search Performance**
   - [ ] Problem: Slow search with 1M+ products
   - [ ] Solutions: Indexing, Full-text search, Caching
   - [ ] Query optimization

6. **Concurrent Payment Processing**
   - [ ] Problem: Duplicate payments
   - [ ] Solution: Idempotent keys, Transaction locking
   - [ ] Retry logic

7. **WebSocket Connection Management**
   - [ ] Problem: Stale connections
   - [ ] Solution: Heartbeat, Connection pooling
   - [ ] Cleanup strategy

8. **Email Delivery Guarantee**
   - [ ] Problem: Email may not be sent
   - [ ] Solution: Async processing, Retry queue
   - [ ] Fallback mechanism

---

## 📚 QUICK REFERENCE GUIDE

### **Key Annotations**
```java
// Spring Core
@SpringBootApplication, @Component, @Service, @Repository, 
@Controller, @RestController, @Autowired, @Bean, @Configuration,
@ConfigurationProperties

// Spring Web
@RestMapping, @PostMapping, @GetMapping, @PutMapping, @DeleteMapping,
@RequestBody, @PathVariable, @RequestParam, @RequestHeader,
@ResponseEntity, @ResponseStatus, @CookieValue, @SessionAttribute

// Spring Data JPA
@Entity, @Table, @Id, @GeneratedValue, @Column, 
@OneToMany, @ManyToOne, @ManyToMany, @OneToOne,
@JoinColumn, @JoinTable, @Query, @Transactional,
@Version, @CreationTimestamp, @UpdateTimestamp

// Spring Security
@Secured, @PreAuthorize, @PostAuthorize, @EnableGlobalMethodSecurity,
@EnableWebSecurity, @EnableWebSocketMessageBroker

// Validation
@Valid, @Validated, @NotNull, @NotBlank, @Email, @Size,
@Min, @Max, @Pattern, @AssertTrue, @CustomValidator

// MapStruct
@Mapper, @Mapping, @MappingTarget, @AfterMapping, @BeforeMapping,
@InheritConfiguration, @InheritInverseConfiguration

// Lombok
@Getter, @Setter, @NoArgsConstructor, @AllArgsConstructor,
@Data, @Builder, @Slf4j, @RequiredArgsConstructor

// Other
@Async, @Scheduled, @Cacheable, @CacheEvict, @CachePut,
@Transactional, @MessageMapping, @SendTo, @SendToUser,
@ControllerAdvice, @ExceptionHandler
```

### **Common Exceptions**
```java
// Spring
DataIntegrityViolationException - Constraint violation
EntityNotFoundException - Entity not found
TransactionException - Transaction issues
ObjectOptimisticLockingFailureException - Version mismatch
LazyInitializationException - Lazy field access outside session

// Jakarta Validation
MethodArgumentNotValidException - Validation failed
ConstraintViolationException - Constraint violated

// Security
BadCredentialsException - Wrong password
UsernameNotFoundException - User not found
AccessDeniedException - No permission
InsufficientAuthenticationException - Not authenticated
```

### **HTTP Status Codes**
```
2xx Success
  200 OK - Successful GET, PUT, DELETE
  201 Created - Successful POST (resource created)
  204 No Content - Successful DELETE (no response body)

4xx Client Error
  400 Bad Request - Invalid request
  401 Unauthorized - Authentication failed
  403 Forbidden - Authentication OK but not authorized
  404 Not Found - Resource not found
  409 Conflict - Optimistic lock failure

5xx Server Error
  500 Internal Server Error - Unexpected error
  502 Bad Gateway - Server unavailable
  503 Service Unavailable - Maintenance
```

---

## 🚀 FINAL PREPARATION

### **1 Week Before Interview**
- [ ] Review INTERVIEW_PREP.md thoroughly
- [ ] Go through PROJECT_ARCHITECTURE.md
- [ ] Practice INTERVIEW_SCENARIOS.md answers
- [ ] Write code snippets by hand (no IDE)
- [ ] Draw diagrams (architecture, flows)

### **3 Days Before Interview**
- [ ] Review top 20 technologies used
- [ ] Practice explaining JWT flow
- [ ] Practice explaining OAuth2 flow
- [ ] Prepare 2-3 minute project summary
- [ ] Write down potential questions & answers

### **1 Day Before Interview**
- [ ] Get good sleep
- [ ] Review all core concepts (30 min)
- [ ] Prepare your environment
- [ ] Have project code ready to screen share
- [ ] Prepare questions to ask them

### **During Interview**
- [ ] Listen carefully to questions
- [ ] Ask for clarification if unsure
- [ ] Take time to think before answering
- [ ] Use concrete examples from your project
- [ ] Explain your thinking process
- [ ] Show enthusiasm for the technology
- [ ] Be honest about what you don't know

### **Interview Red Flags to Avoid**
- [ ] Don't say \"I don't know anything about that\"
- [ ] Don't memorize answers word-for-word
- [ ] Don't skip security considerations
- [ ] Don't ignore error handling
- [ ] Don't forget about performance
- [ ] Don't be defensive about code decisions
- [ ] Don't answer too quickly (seems like memorized)
- [ ] Don't forget to ask questions back

---

## 📞 QUESTIONS TO ASK THEM

1. **Tech Stack**
   - What technologies do you use?
   - Any microservices or monolith?
   - Database choices?

2. **Team & Process**
   - Team size?
   - Scrum or Kanban?
   - Code review process?
   - CI/CD pipeline?

3. **Challenges**
   - What are current technical challenges?
   - Performance issues?
   - Scalability concerns?

4. **Growth**
   - Career progression path?
   - Learning opportunities?
   - Mentorship program?
   - Conference attendance?

5. **Culture**
   - Work-life balance?
   - Remote work options?
   - Team collaboration style?
   - Company values?

---

## ✨ FINAL TIPS

1. **Confidence** - You've built a real project. Be proud of it!
2. **Honesty** - It's okay to not know everything
3. **Curiosity** - Ask questions and show interest
4. **Communication** - Explain clearly, not just technically
5. **Passion** - Show genuine interest in the role & company
6. **Preparation** - This checklist is your guide
7. **Practice** - Rehearse your answers out loud
8. **Relax** - They want you to succeed too!

---

**You've got this! 💪 Good luck! 🍀**

---

## 📁 FILES CREATED FOR YOU

1. **INTERVIEW_PREP.md** - Comprehensive guide covering all concepts
2. **PROJECT_ARCHITECTURE.md** - Deep dive into project structure & modules
3. **INTERVIEW_SCENARIOS.md** - Real interview questions with detailed answers
4. **INTERVIEW_PREP_CHECKLIST.md** (this file) - Quick reference & preparation timeline

**How to use these files:**
- Read them in order: Architecture → Prep → Scenarios → Checklist
- Use as reference during preparation
- Practice explaining concepts in your own words
- Share with friends/mentors for feedback
