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