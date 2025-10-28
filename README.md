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
