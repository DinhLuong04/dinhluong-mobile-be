DinhLuong Mobile 

Backend của DinhLuong Mobile  — hệ thống thương mại điện tử cung cấp RESTful API và WebSocket realtime cho ứng dụng mua sắm trực tuyến.
Hệ thống được xây dựng bằng Java Spring Boot và sử dụng MySQL để lưu trữ dữ liệu.

🧩 Tổng quan

Ngôn ngữ: Java
Framework: Spring Boot
Build Tool: Maven (mvnw, mvnw.cmd)
Kiến trúc: RESTful API + WebSocket (Realtime Communication)
Database: MySQL (JPA / Hibernate)

Backend cung cấp các API phục vụ cho các chức năng chính của hệ thống thương mại điện tử như:

Xác thực và phân quyền người dùng

Quản lý sản phẩm và danh mục

Quản lý giỏ hàng và đơn hàng

Đánh giá sản phẩm

Chat realtime

Hệ thống thông báo

Quản lý voucher và khuyến mãi

🚀 Tính năng chính
🔐 Xác thực & phân quyền

Đăng ký / đăng nhập người dùng

Xác thực bằng JWT Token

Phân quyền User / Admin

👤 Quản lý người dùng

Cập nhật thông tin cá nhân

Quản lý địa chỉ giao hàng

Xem lịch sử đơn hàng

🛒 Quản lý sản phẩm & giỏ hàng

Danh sách sản phẩm

Tìm kiếm và lọc sản phẩm

Thêm / xóa / cập nhật sản phẩm trong giỏ hàng

📦 Đơn hàng & thanh toán

Tạo đơn hàng

Theo dõi trạng thái đơn hàng

Quản lý lịch sử mua hàng

⭐ Đánh giá sản phẩm

Đánh giá và bình luận sản phẩm

Hiển thị rating

💬 Chat realtime

Chat giữa người dùng

Sử dụng WebSocket + STOMP

🔔 Thông báo hệ thống

Thông báo đơn hàng

Thông báo hệ thống

🎫 Voucher & khuyến mãi

Áp dụng mã giảm giá

Quản lý chương trình khuyến mãi

🛠️ Quản trị viên (Admin)

Quản lý sản phẩm

Quản lý danh mục và thương hiệu

Quản lý đơn hàng

Quản lý người dùng

🛠️ Yêu cầu hệ thống

Trước khi chạy dự án, hãy đảm bảo hệ thống có:

Java: 11 trở lên
Maven: hoặc sử dụng Maven Wrapper (mvnw, mvnw.cmd)
Database: MySQL

Kiểm tra Java:

java -version

Kiểm tra Maven:

mvn -v
▶️ Cài đặt & chạy dự án (Windows)
1. Clone repository
git clone <repository-url>
cd dlmstore-backend
2. Cài đặt dependencies
.\mvnw.cmd clean install
3. Chạy ứng dụng

Chạy trực tiếp bằng Spring Boot:

.\mvnw.cmd spring-boot:run
4. Build và chạy file JAR

Build project:

.\mvnw.cmd clean package -DskipTests

Sau khi build thành công, chạy ứng dụng:

java -jar target/dlmstore-0.0.1-SNAPSHOT.jar
⚙️ Cấu hình Database

Trước khi chạy ứng dụng, hãy cấu hình MySQL trong file:

src/main/resources/application.properties

Ví dụ:

spring.datasource.url=jdbc:mysql://localhost:3306/dlmstore
spring.datasource.username=root
spring.datasource.password=yourpassword

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

📧 Liên hệ

Ban Dinh Luong

Email: bandinhluong220204@gmail.com

GitHub: https://github.com/DinhLuong04