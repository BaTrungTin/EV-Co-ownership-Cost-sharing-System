# 📋 KẾ HOẠCH CÔNG VIỆC THEO NGÀY - CO-OWNER PROJECT

## 🎯 Lưu ý quan trọng
**Đây là training plan dựa trên code mẫu đã có sẵn.**
- Code reference đã hoàn chỉnh trong source code
- Team sẽ rebuild lại từng phần theo đúng module của mình
- Mỗi người giữ nguyên module từ Day 1 → Day 3 để đảm bảo tính nhất quán

---

## 🎯 Tổng quan Flow 3 ngày

**Ngày 1 - Foundation (Xây dựng nền tảng):**
- Xây dựng các module cơ bản: User, Group, Vehicle, Booking
- Tạo Model → Repository → Service → Controller
- **Chưa có bảo mật** - tất cả endpoints đều public

**Ngày 2 - Refinement (Hoàn thiện):**
- Hoàn thiện User module: validation, pagination
- Thêm GlobalExceptionHandler (xử lý lỗi chung)
- Thêm Swagger docs
- **Vẫn chưa có bảo mật** - nhưng đã chuẩn bị infrastructure

**Ngày 3 - Security (Bảo mật):**
- Thêm JWT Authentication để protect tất cả endpoints
- Mỗi người update module của mình để dùng current user từ JWT token
- Hoàn thiện các module với security integration

**👉 Nguyên tắc:** Mỗi người tiếp tục làm module của mình từ Day 1 → Day 3

---

# Kế hoạch công việc Ngày 1 (4 thành viên)

## Nhánh chung
- main: ổn định
- dev: tích hợp
- Mỗi người làm trên nhánh `feature/<tên>`

---

## Tín – Auth & User (feature/tin)

**Files cần tạo (xem reference trong source code):**
- `model/User.java` - Entity với fields: id, email, fullName, password, roles (ManyToMany với Role)
- `model/Role.java` - Entity với fields: id, name
- `repository/UserRepository.java` - JpaRepository với method `findByEmail()`
- `repository/RoleRepository.java` - JpaRepository với method `findByName()`
- `dto/CreateUserRequest.java` - DTO với: email, fullName, password (validation)
- `dto/UserDto.java` - DTO trả về: id, email, fullName
- `service/UserService.java` - Business logic cho register và list
- `controller/UserController.java` - REST endpoints

**Endpoints cần tạo:**
- `POST /api/users/register`: Đăng ký user mới
  - Validate: email hợp lệ/unique, fullName, password ≥ 6 ký tự
  - Gán role mặc định "CO_OWNER" (tạo nếu chưa có)
  - Hash password bằng BCrypt
  - Trả về `UserDto`
- `GET /api/users`: Lấy danh sách users (chưa có pagination, sẽ làm Day 2)
  - Trả về `List<UserDto>`

**Validation:**
- Email: @Email, unique
- Password: tối thiểu 6 ký tự
- FullName: không null

---

## Trinh – Group & Ownership (feature/trinh)

**Files cần tạo (xem reference trong source code):**
- `model/Group.java` - Entity với fields: id, name (unique)
- `model/OwnershipShare.java` - Entity với: id, user (ManyToOne), group (ManyToOne), percentage (0-1)
- `repository/GroupRepository.java` - JpaRepository với method `findByName()`
- `repository/OwnershipShareRepository.java` - JpaRepository với methods: `findByGroupId()`, `existsByGroupIdAndUserId()`
- `dto/CreateGroupRequest.java` - DTO với: name
- `dto/AddMemberRequest.java` - DTO với: userId, percentage
- `service/GroupService.java` - Business logic
- `controller/GroupController.java` - REST endpoints

**Endpoints cần tạo:**
- `POST /api/groups`: Tạo nhóm mới
  - Validate: name unique
  - Trả về `Group`
- `POST /api/groups/{id}/members`: Thêm thành viên vào nhóm
  - Validate: 
    - User không trùng trong group
    - Tổng percentage ≤ 1.0 (tổng hiện tại + mới ≤ 1.0)
  - Trả về `OwnershipShare`
- `GET /api/groups/{id}`: Chi tiết nhóm
  - Trả về `Group` (bao gồm danh sách OwnershipShare)

---

## Lâm – Vehicle & Contract (feature/lam)

**Files cần tạo (xem reference trong source code):**
- `model/Vehicle.java` - Entity với: id, vin (unique), plate (unique), model, group (ManyToOne)
- `model/EContract.java` - Entity với: id, groupId, contractNo, startDate, endDate (stub - để sau)
- `repository/VehicleRepository.java` - JpaRepository với methods: `findByVin()`, `findByPlate()`, `findByGroupId()`
- `repository/EContractRepository.java` - JpaRepository (stub)
- `dto/CreateVehicleRequest.java` - DTO với: vin, plate, model, groupId
- `service/VehicleService.java` - Business logic
- `controller/VehicleController.java` - REST endpoints

**Endpoints cần tạo:**
- `POST /api/vehicles`: Tạo xe mới
  - Validate: vin unique, plate unique, group tồn tại
  - Trả về `Vehicle`
- `GET /api/vehicles?groupId={id}`: Liệt kê xe theo group
  - Trả về `List<Vehicle>`

**Note:** EContract để stub (chưa implement), chỉ cần model và repository cơ bản.

---

## Thắng – Booking (feature/thang)

**Files cần tạo (xem reference trong source code):**
- `model/Booking.java` - Entity với: id, group (ManyToOne), vehicle (ManyToOne), user (ManyToOne), startTime, endTime, status (String: PENDING/CONFIRMED/CANCELLED)
- `repository/BookingRepository.java` - JpaRepository với methods: `findByVehicleId()`, `findByUserId()`, `findByUserIdAndStatus()`
- `dto/CreateBookingRequest.java` - DTO với: vehicleId, startTime, endTime (không có userId)
- `service/BookingService.java` - Business logic
- `controller/BookingController.java` - REST endpoints

**Endpoints cần tạo:**
- `POST /api/bookings`: Tạo booking mới
  - Validate: 
    - startTime < endTime
    - User thuộc group của vehicle
    - Không trùng slot với booking khác (CONFIRMED hoặc PENDING) - chỉ check overlap với status này
  - Nhận `userId` từ request (sẽ bỏ Day 3, dùng current user)
  - Status mặc định: "CONFIRMED"
  - Trả về `Booking`
- `GET /api/bookings?vehicleId={id}`: Danh sách booking theo xe
  - Trả về `List<Booking>`

---

## Mục tiêu chốt ngày 1
- Mỗi nhánh có 2–3 endpoint chạy được, validate cơ bản
- Build OK, DB tạo bảng tự động (`ddl-auto: update`)
- PR từ `feature/<tên>` -> `dev` để review/merge
- Test bằng Postman/curl: tạo user, group, vehicle, booking

---

# Kế hoạch công việc Ngày 2 (4 thành viên)

## 🔗 Mối liên kết với Ngày 1

**Dựa trên code Day 1 đã có:**
- Đã có các module cơ bản: User, Group, Vehicle, Booking
- Cần hoàn thiện: validation (Tín), pagination cho User endpoint (Trinh), exception handling (Lâm), Swagger (Thắng)

**Lưu ý:** Trinh làm pagination cho User endpoint (không phải Group) để học pattern pagination, sau đó có thể áp dụng cho Group ở các ngày sau.

---

## Mục tiêu chung
- Hoàn thiện User module: validation (Tín), pagination (Trinh)
- Thêm GlobalExceptionHandler (xử lý lỗi chung) - Lâm
- Thêm Swagger docs - Thắng
- Chuẩn bị infrastructure cho Day 3

---

## Tín – Hoàn thiện User (feature/tin)

**Files cần sửa (xem reference trong source code):**
- `service/UserService.java`:
  - Hoàn thiện `register()`: Check trùng email, chuẩn hóa lỗi
  - Thêm method `listUsers(Pageable)` để hỗ trợ pagination (Day 2)
  
**Validation:**
- Email: @Email annotation, check unique trong DB
- Password: @Size(min = 6)
- FullName: @NotBlank

---

## Trinh – Listing + Pagination (feature/trinh)

**Files cần sửa (xem reference trong source code):**
- `controller/UserController.java`:
  - Update `GET /api/users`: Thêm `Pageable` parameter
  - Sử dụng `@PageableDefault(size = 10)`
  - Trả về `Page<UserDto>` (Spring Data Page)
  - Hỗ trợ query params: `page`, `size`, `sort` (ví dụ: `?page=0&size=10&sort=email,asc`)
- `service/UserService.java`:
  - Thêm method `listUsers(Pageable pageable)`: Trả về `Page<UserDto>`
  - Sử dụng `userRepository.findAll(pageable).map(this::toDto)`
  - Đảm bảo query hiệu quả với pagination

**Kết quả:**
- `GET /api/users?page=0&size=10&sort=email,asc` hoạt động
- Response format: Spring Data `Page<UserDto>` với các fields: `content`, `totalElements`, `totalPages`, `number`, `size`, `sort`
- Có thể sort theo: email, fullName, id
- Cân nhắc index cho các trường sort (email, fullName) trong database

---

## Lâm – GlobalExceptionHandler (feature/lam)

**Files cần tạo (xem reference trong source code):**
- `exception/GlobalExceptionHandler.java`:
  - `@RestControllerAdvice` class
  - Handle `MethodArgumentNotValidException` → 400 Bad Request
    - Format: `{timestamp, code: "VALIDATION_ERROR", message, details: {field: error}}`
  - Handle `IllegalArgumentException` → 409 Conflict
    - Format: `{timestamp, code: "INVALID_ARGUMENT", message}`
  - Handle `DataIntegrityViolationException` → 409 Conflict
    - Format: `{timestamp, code: "DATA_INTEGRITY", message}`
  - Handle `Exception` (generic) → 500 Internal Server Error

**Files cần sửa:**
- `dto/CreateUserRequest.java`: Thêm validation annotations
  - `@Email` cho email
  - `@NotBlank` cho fullName
  - `@Size(min = 6)` cho password

---

## Thắng – Swagger Documentation (feature/thang)

**Files cần tạo/sửa (xem reference trong source code):**
- `config/OpenApiConfig.java`:
  - Tạo `@Configuration` class
  - Bean `OpenAPI` với info: title, version, description
  - (SecurityScheme sẽ thêm Day 3)
- `pom.xml`: Thêm dependency `springdoc-openapi-ui` (nếu chưa có)
- `controller/UserController.java`: Thêm `@Tag(name = "user-controller")`
- `controller/AuthController.java`: Thêm `@Tag(name = "auth-controller")` (nếu có)

**Kết quả:**
- Truy cập `/swagger-ui.html` để xem API docs
- Các endpoint hiển thị với mô tả request/response

---

## Mốc thời gian đề xuất
- 09:00–10:00: Kickoff, chốt format lỗi, pagination structure
- 10:00–12:00: Mỗi người triển khai phần được giao
- 13:00–14:30: Tích hợp, fix conflict, test
- 14:30–16:00: Hoàn thiện Swagger, test pagination
- 16:00–17:00: Review chéo, demo endpoints, chốt DONE

## Tiêu chí hoàn thành Ngày 2
- `POST /api/users/register` validate đầy đủ, lỗi chuẩn hóa
- `GET /api/users?page=0&size=10` hoạt động với pagination
- GlobalExceptionHandler xử lý tất cả lỗi trong app
- Swagger hiển thị đủ endpoints, mô tả rõ
- README cập nhật (nếu cần)

---

# Kế hoạch công việc Ngày 3 (4 thành viên)

## 🔗 Mối liên kết với Ngày 2

**Dựa trên code Day 2 đã có:**
- GlobalExceptionHandler (Lâm) → dùng để handle JWT errors
- Swagger (Thắng) → cập nhật thêm SecurityScheme
- User module đã hoàn thiện (Tín, Trinh)

**Ngày 3 sẽ:**
- Thêm JWT Authentication
- Mỗi người update module của mình để dùng current user từ JWT

---

## Mục tiêu chung
- Triển khai JWT Authentication để protect tất cả endpoints (trừ `/api/auth/**`)
- Mỗi người update module của mình để dùng current user từ JWT token
- Sử dụng GlobalExceptionHandler để handle JWT errors
- Cập nhật Swagger với SecurityScheme

---

## Tín – JWT Authentication (feature/tin)

**Files cần tạo (xem reference trong source code):**
- `security/JwtAuthenticationFilter.java`:
  - Extends `OncePerRequestFilter`
  - Extract token từ header `Authorization: Bearer <token>`
  - Validate token bằng `jwtService.validateToken(token)`
  - Extract email bằng `jwtService.extractEmail(token)`
  - Load User từ `userRepository.findByEmail(email)`
  - Set Authentication vào `SecurityContextHolder`
  - Nếu không có token hoặc invalid → continue filter chain (sẽ bị 401 nếu endpoint require auth)

**Files cần sửa:**
- `config/SecurityConfig.java`:
  - Thêm `JwtAuthenticationFilter` vào filter chain
  - `permitAll()` cho: `/api/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**`, `/api/users/register`
  - `authenticated()` cho tất cả endpoints khác
  - Disable CSRF, CORS config, SessionCreationPolicy.STATELESS

**Files cần tạo (nếu chưa có):**
- `controller/AuthController.java`:
  - `POST /api/auth/login`: Login endpoint
    - Nhận email + password
    - Verify password
    - Generate JWT token bằng `jwtService.generateToken(email)`
    - Trả về `{token: "..."}`

**Note:** `JwtService` đã có sẵn trong source code với methods: `generateToken()`, `validateToken()`, `extractEmail()`

---

## Trinh – SecurityUtils + Group Security (feature/trinh)

**Mục đích của SecurityUtils:**
- **Vấn đề:** Khi dùng JWT, mỗi controller cần lấy current user từ JWT token để biết user đang đăng nhập là ai
- **Giải pháp:** Tạo `SecurityUtils` - utility class để tái sử dụng code, tránh lặp lại logic lấy user trong mỗi controller
- **Cách hoạt động:** 
  - `JwtAuthenticationFilter` (Day 3) đã set Authentication vào `SecurityContextHolder`
  - `SecurityUtils` lấy email từ Authentication, rồi load User từ database
  - Tất cả controllers chỉ cần gọi `securityUtils.getCurrentUser()` là có User object

**Files cần tạo (xem reference trong source code):**
- `security/SecurityUtils.java`:
  - `@Component` class (để Spring inject vào các controller)
  - Inject `UserRepository`
  - Method `getCurrentUser()`: 
    - Lấy `Authentication` từ `SecurityContextHolder.getContext().getAuthentication()`
    - Lấy email từ `auth.getName()` (email được set bởi JwtAuthenticationFilter)
    - Load User từ `userRepository.findByEmail(email)`
    - Throw exception nếu không có user
  - Method `getCurrentUserEmail()`: Lấy email từ Authentication (dùng khi chỉ cần email, không cần load User)

**Ví dụ sử dụng trong Controller:**
```java
@RestController
public class BookingController {
    private final SecurityUtils securityUtils;
    
    @PostMapping("/bookings")
    public ResponseEntity<Booking> create(@RequestBody CreateBookingRequest req) {
        User currentUser = securityUtils.getCurrentUser(); // Lấy user đang đăng nhập
        return bookingService.create(req, currentUser.getId()); // Dùng userId
    }
}
```

**Files cần sửa:**
- `controller/GroupController.java`:
  - Inject `SecurityUtils`
  - Update `POST /api/groups`: 
    - Gán creator là current user (nếu Group model có field creator)
    - Hoặc chỉ cần đảm bảo endpoint này require authentication (tự động qua SecurityConfig)

**Note:** SecurityUtils là utility class dùng chung cho tất cả controllers (BookingController, GroupController, VehicleController, ...)

---

## Lâm – Exception Handler + Vehicle Security (feature/lam)

**Files cần sửa (xem reference trong source code):**
- `exception/GlobalExceptionHandler.java` (đã tạo Day 2):
  - Thêm handler `@ExceptionHandler(JwtException.class)`:
    - Return 401 Unauthorized
    - Format: `{timestamp, code: "UNAUTHORIZED", message: "Token không hợp lệ"}`
  - Thêm handler `@ExceptionHandler(AccessDeniedException.class)`:
    - Return 403 Forbidden
    - Format: `{timestamp, code: "FORBIDDEN", message: "Không có quyền truy cập"}`

**Files cần tạo:**
- `exception/ForbiddenException.java`:
  - Extends `RuntimeException`
  - Custom exception cho 403
  - Thêm handler trong GlobalExceptionHandler: `@ExceptionHandler(ForbiddenException.class)` → 403

**Files cần sửa:**
- `controller/VehicleController.java`:
  - Đảm bảo các endpoints require authentication (tự động qua SecurityConfig)
  - Nếu có logic liên quan đến user → dùng `SecurityUtils.getCurrentUser()`

---

## Thắng – Booking Security + Swagger Update (feature/thang)

**Files cần sửa (xem reference trong source code):**
- `controller/BookingController.java`:
  - Inject `SecurityUtils`
  - Update `POST /api/bookings`:
    - Bỏ `userId` từ request
    - Lấy current user: `User currentUser = securityUtils.getCurrentUser()`
    - Pass `currentUser.getId()` vào service
  - Update `GET /api/bookings`:
    - Lấy current user để filter bookings của user đó
    - Sử dụng `bookingService.getMyBookings(userId, status, vehicleId)`
  - Update `PUT /api/bookings/{id}/cancel`:
    - Lấy current user
    - Pass `currentUser.getId()` vào service để check authorization
- `service/BookingService.java`:
  - `cancel()` method: Check `booking.getUser().getId().equals(userId)` → throw `ForbiddenException` nếu không match
  - Conflict check trong `create()`: Chỉ check overlap với status = "CONFIRMED" hoặc "PENDING" (bỏ qua "CANCELLED")

**Files cần sửa:**
- `config/OpenApiConfig.java`:
  - Thêm SecurityScheme vào Components:
    ```java
    .components(new Components()
        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .in(SecurityScheme.In.HEADER)
            .name("Authorization")))
    ```

**Files cần sửa:**
- `README.md`:
  - Thêm section "Authentication"
  - Hướng dẫn: Login để lấy token, gửi token trong header `Authorization: Bearer <token>`

---

## Mốc thời gian đề xuất
- 09:00–09:30: Kickoff, chốt JWT flow, cách dùng SecurityUtils
- 09:30–12:00: Mỗi người triển khai phần được giao
  - Tín: JWT Filter + SecurityConfig + AuthController
  - Trinh: SecurityUtils + GroupController
  - Lâm: GlobalExceptionHandler update + ForbiddenException + VehicleController
  - Thắng: BookingController + Swagger + README
- 13:00–14:00: Tích hợp, test JWT flow, fix conflicts
- 14:00–15:30: Test authorization, hoàn thiện
- 15:30–16:30: Cập nhật Swagger, README, review code
- 16:30–17:00: Review chéo, demo authentication flow, chốt DONE

## Tiêu chí hoàn thành Ngày 3
- Tất cả endpoints (trừ `/api/auth/**`, `/swagger-ui/**`) yêu cầu JWT token
- Request không có token → 401 Unauthorized (handle bởi GlobalExceptionHandler)
- Controllers tự động lấy current user từ JWT token (không cần truyền userId)
- `SecurityUtils` được tạo và sử dụng trong các controllers
- Swagger hiển thị SecurityScheme Bearer JWT
- GlobalExceptionHandler xử lý `JwtException` → 401, `AccessDeniedException` → 403
- `ForbiddenException` được tạo và handle
- README cập nhật hướng dẫn authentication
- Test flow: Login → lấy token → gọi protected endpoint với token

---

## 📝 Checklist tổng hợp

### Day 1 Checklist:
- [ ] Tín: User + Role models, repositories, DTOs, service, controller
- [ ] Trinh: Group + OwnershipShare models, repositories, DTOs, service, controller
- [ ] Lâm: Vehicle model, repository, DTOs, service, controller
- [ ] Thắng: Booking model, repository, DTOs, service, controller

### Day 2 Checklist:
- [ ] Tín: Hoàn thiện UserService validation
- [ ] Trinh: Pagination cho GET /api/users
- [ ] Lâm: GlobalExceptionHandler + validation annotations
- [ ] Thắng: Swagger config + OpenAPI

### Day 3 Checklist:
- [ ] Tín: JwtAuthenticationFilter + SecurityConfig + AuthController
- [ ] Trinh: SecurityUtils + GroupController update
- [ ] Lâm: GlobalExceptionHandler (JWT errors) + ForbiddenException + VehicleController
- [ ] Thắng: BookingController update + Swagger SecurityScheme + README
