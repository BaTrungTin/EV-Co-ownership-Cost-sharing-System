# Kế hoạch công việc Ngày 1 (4 thành viên)

## Nhánh chung
- main: ổn định
- dev: tích hợp
- Mỗi người làm trên nhánh `feature/<tên>`

---

## Tín – Auth & User (feature/tin)
- Model: `User`, `Role`.
- Repository: `UserRepository`, `RoleRepository`.
- DTO: `CreateUserRequest`, `UserDto`.
- Endpoints:
  - POST `/api/users/register`: đăng ký user (role mặc định CO_OWNER).
  - GET `/api/users`: lấy danh sách người dùng (UserDto).
- Validate: email hợp lệ/unique, fullName độ dài hợp lý, password tối thiểu 6 ký tự.

---

## Trinh – Group & Ownership (feature/trinh)
- Model: `Group`, `OwnershipShare (user, group, percentage 0–1)`.
- Repository: `GroupRepository`, `OwnershipShareRepository`.
- DTO: `CreateGroupRequest (name)`, `AddMemberRequest (userId, percentage)`.
- Endpoints:
  - POST `/api/groups`: tạo nhóm.
  - POST `/api/groups/{id}/members`: thêm thành viên + tỉ lệ sở hữu.
  - GET `/api/groups/{id}`: chi tiết nhóm + danh sách thành viên.
- Validate: tổng percentage ≤ 1.0; user không trùng trong group; `name` unique.

---

## Lâm – Vehicle & Contract (stub) (feature/lam)
- Model: `Vehicle (vin, plate, model, group)`, `EContract (groupId, contractNo, start/end)`.
- Repository: `VehicleRepository`, `EContractRepository`.
- DTO: `CreateVehicleRequest (vin, plate, model, groupId)`.
- Endpoints:
  - POST `/api/vehicles`: tạo xe và gán vào group.
  - GET `/api/vehicles?groupId=`: liệt kê xe theo group.
- Validate: `vin`/`plate` unique; group tồn tại. (Contract để stub ngày 1)

---

## Thắng – Booking (stub) (feature/thang)
- Model: `Booking (groupId, vehicleId, userId, startTime, endTime, status)`.
- Repository: `BookingRepository`.
- DTO: `CreateBookingRequest (vehicleId, startTime, endTime)`.
- Endpoints:
  - POST `/api/bookings`: tạo booking.
  - GET `/api/bookings?vehicleId=`: danh sách booking theo xe (có thể lọc ngày).
- Validate: không trùng slot cùng `vehicle`; `startTime < endTime`; `user` thuộc group của `vehicle`.

---

## Mục tiêu chốt ngày 1
- Mỗi nhánh có 2–3 endpoint chạy được, validate cơ bản.
- Build OK, DB tạo bảng tự động (`ddl-auto: update`).
- PR từ `feature/<tên>` -> `dev` để review/merge.

---

# Kế hoạch công việc Ngày 2 (4 thành viên)

## Mục tiêu chung
- Hoàn thiện module User: validate, lỗi chung, phân trang/sort, role mặc định, API docs.
- Thêm bảo mật cơ bản (chuẩn bị skeleton JWT cho ngày sau).
- Viết test cơ bản và dữ liệu mẫu để demo.

## Tín – Auth & User (feature/tin)
- Hoàn thiện `POST /api/users/register`: validate đủ, check trùng email/username, gán `CO_OWNER` mặc định.
- Chuẩn hóa response lỗi 400/409; thống nhất `UserDto` trả về.
- Thêm seed user mẫu (runner hoặc `data.sql`).

## Trinh – Listing + Pagination (feature/trinh)
- Thêm phân trang/sort cho `GET /api/users` với `page`, `size`, `sort`.
- Trả cấu trúc wrapper: `{items, total, page, size, sort}` (hoặc `Page<UserDto>` nếu thống nhất).
- Đảm bảo query hiệu quả; cân nhắc index cho các trường sort (username/email).

## Lâm – Validation + Exception + Response format (feature/lam)
- Tạo `@ControllerAdvice` global handle: `MethodArgumentNotValidException`, `DataIntegrityViolationException`, 404, 500.
- Chuẩn hóa format lỗi: `{timestamp, path, message, details, code}`.
- Ràng buộc `CreateUserRequest`: email hợp lệ, password ≥ 6, username rule rõ ràng.

## Thắng – Docs + Tests + Tooling (feature/thang)
- Thêm OpenAPI/Swagger (springdoc-openapi), expose `/swagger-ui` với mô tả request/response.
- Unit test `UserService`, integration test `UserController` cơ bản (H2 nếu cần).
- Tạo Postman collection/cURL và cập nhật README hướng dẫn chạy.

## Mốc thời gian đề xuất
- 09:00–10:00: Kickoff, chốt contract DTO/format lỗi/phân trang.
- 10:00–12:00: Mỗi người triển khai phần được giao.
- 13:00–14:30: Tích hợp, fix conflict, đồng bộ format.
- 14:30–16:00: Viết test, seed data, hoàn thiện Swagger.
- 16:00–17:00: Review chéo, demo endpoints, chốt DONE.

## Tiêu chí hoàn thành Ngày 2
- `POST /api/users/register` chạy ổn, validate đầy đủ, lỗi chuẩn hóa.
- `GET /api/users` hỗ trợ phân trang/sort, trả đúng cấu trúc.
- Global exception handler áp dụng cho toàn app.
- Swagger hiển thị đủ 2 endpoints, mô tả rõ request/response.
- Có tối thiểu 5–8 test (unit + integration) xanh, README cập nhật.

---

# Kế hoạch công việc Ngày 3 (4 thành viên)

## Mục tiêu chung
- **Triển khai JWT Authentication hoàn chỉnh**: JWT Filter, Security Context, protect tất cả endpoints trừ `/api/auth/**`.
- **Authorization dựa trên user context**: Lấy current user từ JWT token, validate ownership trong các operations.
- **Hoàn thiện Booking module**: Cancel booking, status management, get bookings by user/vehicle với filter.
- **Cải thiện business logic**: Validate permissions, ownership checks, booking conflicts nâng cao.

---

## Tín – JWT Filter + Security Context (feature/tin)

### Nhiệm vụ chính:
1. **Tạo JwtAuthenticationFilter**:
   - Extract token từ header `Authorization: Bearer <token>`
   - Validate token với `JwtService.validateToken()`
   - Load User từ email trong token
   - Set Authentication vào SecurityContext

2. **Cải thiện JwtService**:
   - Thêm method `validateToken(String token)`: verify signature, check expiration
   - Thêm method `extractEmail(String token)`: parse email từ token
   - Handle JwtException và throw custom exception

3. **Update SecurityConfig**:
   - Thêm JwtAuthenticationFilter vào filter chain (trước UsernamePasswordAuthenticationFilter)
   - Protect tất cả endpoints trừ `/api/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**`
   - Yêu cầu authenticated cho các endpoint khác

4. **Tạo UserPrincipal hoặc Custom UserDetailsService**:
   - Load user với roles từ database
   - Implement UserDetails interface

### Files cần tạo/sửa:
- `src/main/java/com/evcoownership/coowner/security/JwtAuthenticationFilter.java` (mới)
- `src/main/java/com/evcoownership/coowner/security/UserDetailsServiceImpl.java` (mới - optional)
- `src/main/java/com/evcoownership/coowner/config/SecurityConfig.java` (update)
- `src/main/java/com/evcoownership/coowner/security/JwtService.java` (update)

### Deliverables:
- JWT filter hoạt động, extract user từ token
- Tất cả endpoints (trừ auth) yêu cầu valid JWT token
- Response 401 Unauthorized khi token invalid/expired/missing

---

## Trinh – Current User Context + Authorization (feature/trinh)

### Nhiệm vụ chính:
1. **Tạo SecurityUtils/CurrentUser helper**:
   - Method `getCurrentUser()`: lấy User từ SecurityContext
   - Method `getCurrentUserEmail()`: lấy email từ Authentication
   - Throw exception nếu user chưa authenticated

2. **Update Controllers để dùng current user**:
   - `BookingController.create()`: lấy userId từ current user (bỏ `userId` trong request)
   - `GroupController.create()`: gán creator là current user
   - `VehicleController`: validate user thuộc group trước khi tạo/query vehicle

3. **Authorization checks trong Service layer**:
   - `BookingService.create()`: validate current user thuộc group của vehicle
   - `GroupService.addMember()`: chỉ owner/creator của group mới được thêm member
   - `VehicleService.create()`: chỉ member của group mới được tạo vehicle

4. **Custom Exception cho Authorization**:
   - Tạo `UnauthorizedException` hoặc `ForbiddenException`
   - Handle trong GlobalExceptionHandler → 403 Forbidden

### Files cần tạo/sửa:
- `src/main/java/com/evcoownership/coowner/security/SecurityUtils.java` (mới)
- `src/main/java/com/evcoownership/coowner/controller/BookingController.java` (update)
- `src/main/java/com/evcoownership/coowner/controller/GroupController.java` (update)
- `src/main/java/com/evcoownership/coowner/service/BookingService.java` (update)
- `src/main/java/com/evcoownership/coowner/service/GroupService.java` (update)
- `src/main/java/com/evcoownership/coowner/exception/GlobalExceptionHandler.java` (update)

### Deliverables:
- Controllers tự động lấy current user từ JWT
- Business logic validate permissions (user thuộc group, có quyền thực hiện action)
- Error 403 khi không đủ quyền

---

## Lâm – Booking Module Hoàn thiện (feature/lam)

### Nhiệm vụ chính:
1. **Thêm endpoints Booking**:
   - `PUT /api/bookings/{id}/cancel`: Cancel booking (chỉ owner booking mới cancel được)
   - `GET /api/bookings/my-bookings`: Lấy bookings của current user (có filter: status, vehicleId, dateRange)
   - `GET /api/bookings/{id}`: Chi tiết booking
   - `PUT /api/bookings/{id}/status`: Update status (PENDING → CONFIRMED/CANCELLED) - chỉ admin/owner group

2. **Cải thiện BookingService**:
   - Method `cancel(Long bookingId)`: Validate owner, set status = "CANCELLED"
   - Method `getMyBookings(String status, Long vehicleId, LocalDate startDate, LocalDate endDate)`: Filter phức tạp
   - Method `getBooking(Long id)`: Chi tiết với relations (vehicle, group, user)
   - Method `updateStatus(Long id, String status)`: Với authorization check

3. **Validation nâng cao**:
   - Booking conflict: check overlap với status = "CONFIRMED" hoặc "PENDING" (bỏ qua "CANCELLED")
   - Không cho cancel booking đã qua (endTime < now)
   - Validate date range cho filter

4. **DTO mới**:
   - `BookingDto`: Response với thông tin đầy đủ (vehicle model, user name, group name)
   - `UpdateBookingStatusRequest`: DTO cho update status

### Files cần tạo/sửa:
- `src/main/java/com/evcoownership/coowner/dto/BookingDto.java` (mới)
- `src/main/java/com/evcoownership/coowner/dto/UpdateBookingStatusRequest.java` (mới)
- `src/main/java/com/evcoownership/coowner/controller/BookingController.java` (update)
- `src/main/java/com/evcoownership/coowner/service/BookingService.java` (update)
- `src/main/java/com/evcoownership/coowner/repository/BookingRepository.java` (update - thêm query methods)

### Deliverables:
- Booking CRUD đầy đủ với authorization
- Filter bookings theo user, vehicle, status, date range
- Cancel booking với validation
- Response BookingDto chuẩn hóa

---

## Thắng – Enhanced Exception + Testing Auth (feature/thang)

### Nhiệm vụ chính:
1. **Cải thiện GlobalExceptionHandler**:
   - Handle `JwtException` → 401 Unauthorized với message rõ ràng
   - Handle `AccessDeniedException` / `ForbiddenException` → 403 Forbidden
   - Handle `EntityNotFoundException` → 404 Not Found (tạo custom exception nếu chưa có)
   - Format lỗi authentication: `{timestamp, code: "UNAUTHORIZED", message: "...", path}`

2. **Tạo custom exceptions**:
   - `ResourceNotFoundException`: Cho 404 (group/vehicle/user không tồn tại)
   - `ForbiddenException`: Cho 403 (không đủ quyền)
   - `UnauthorizedException`: Cho 401 (chưa login hoặc token invalid)

3. **Integration Tests cho Authentication**:
   - Test login endpoint: success, invalid email, wrong password
   - Test protected endpoint: với token, không có token, token expired, token invalid
   - Test authorization: user A không thể cancel booking của user B
   - Test current user context: booking tạo với đúng user từ token

4. **Cập nhật Swagger/OpenAPI**:
   - Thêm SecurityScheme cho Bearer JWT
   - Tag các endpoints cần authentication
   - Example request với Authorization header

5. **Update README/Postman Collection**:
   - Hướng dẫn login lấy token
   - Gửi token trong header `Authorization: Bearer <token>`
   - Update Postman collection với auth flow

### Files cần tạo/sửa:
- `src/main/java/com/evcoownership/coowner/exception/ResourceNotFoundException.java` (mới)
- `src/main/java/com/evcoownership/coowner/exception/ForbiddenException.java` (mới)
- `src/main/java/com/evcoownership/coowner/exception/UnauthorizedException.java` (mới - optional)
- `src/main/java/com/evcoownership/coowner/exception/GlobalExceptionHandler.java` (update)
- `src/test/java/.../controller/AuthControllerTest.java` (mới)
- `src/test/java/.../controller/BookingControllerSecurityTest.java` (mới)
- Swagger config (update)
- README.md (update)

### Deliverables:
- Exception handling đầy đủ cho auth errors (401/403/404)
- 8–10 integration tests cho authentication flows (xanh)
- Swagger có security scheme, có thể test với Bearer token
- README hướng dẫn authentication flow rõ ràng

---

## Mốc thời gian đề xuất Ngày 3
- **09:00–09:30**: Kickoff, chốt JWT flow, authorization rules, exception hierarchy.
- **09:30–12:00**: Mỗi người triển khai phần được giao:
  - Tín: JWT Filter + SecurityConfig
  - Trinh: SecurityUtils + Current User Context
  - Lâm: Booking endpoints + Business logic
  - Thắng: Exception handling + Tests setup
- **13:00–14:00**: Tích hợp JWT Filter với Current User Context, fix conflicts.
- **14:00–15:30**: Hoàn thiện Booking module, authorization checks, exception handling.
- **15:30–16:30**: Viết integration tests, cập nhật Swagger, test end-to-end flow.
- **16:30–17:00**: Review chéo, demo authentication flow, chốt DONE.

---

## Tiêu chí hoàn thành Ngày 3

### Must Have (100%):
- ✅ Tất cả endpoints (trừ `/api/auth/**`) yêu cầu JWT token
- ✅ Request không có token → 401 Unauthorized
- ✅ Controllers tự động lấy current user từ JWT token
- ✅ Booking tạo với đúng user từ token (không cần truyền userId)
- ✅ Authorization: user chỉ thao tác resources của mình/group mình
- ✅ Cancel booking endpoint hoạt động với validation
- ✅ Exception handler xử lý JwtException, AccessDeniedException → 401/403

### Should Have (80%):
- ✅ `GET /api/bookings/my-bookings` với filter (status, vehicleId, date)
- ✅ Swagger có Bearer JWT security scheme
- ✅ 8+ integration tests cho auth flows (xanh)
- ✅ README có hướng dẫn authentication flow

### Nice to Have (nếu còn time):
- ✅ Role-based authorization (ADMIN có thể cancel mọi booking)
- ✅ Refresh token endpoint
- ✅ Logging authentication attempts

---

## Lưu ý kỹ thuật

### JWT Filter Flow:
```
Request → JwtAuthenticationFilter → Extract token → Validate → Load User → Set SecurityContext → Continue
```

### SecurityContext Usage:
```java
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
String email = auth.getName(); // email từ token
User user = userRepository.findByEmail(email).orElseThrow(...);
```

### Authorization Pattern:
```java
// Trong Service
User currentUser = securityUtils.getCurrentUser();
if (!booking.getUser().getId().equals(currentUser.getId())) {
    throw new ForbiddenException("Không có quyền thực hiện action này");
}
```

### Testing với JWT:
```java
// Tạo token test
String token = jwtService.generateToken("test@example.com");
// Gửi trong header
mockMvc.perform(post("/api/bookings")
    .header("Authorization", "Bearer " + token)
    .contentType(MediaType.APPLICATION_JSON)
    .content(requestJson))
```

---

## Checklist trước khi merge PR:
- [ ] JWT filter hoạt động, test với Postman/Swagger
- [ ] Current user context được sử dụng trong ít nhất 3 endpoints
- [ ] Authorization check trong BookingService, GroupService
- [ ] Exception handler xử lý 401/403/404
- [ ] Integration tests xanh (mvn test)
- [ ] Swagger có security scheme
- [ ] README cập nhật authentication flow
- [ ] Code review chéo giữa các thành viên