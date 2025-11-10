package com.evcoownership.coowner.controller;

import com.evcoownership.coowner.dto.CreateBookingRequest;
import com.evcoownership.coowner.model.Booking;
import com.evcoownership.coowner.model.User;
import com.evcoownership.coowner.service.BookingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus; 
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; 
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Booking Controller", description = "Endpoints để quản lý Đặt chỗ (Booking)") 
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Operation(
            summary = "Tạo một đặt chỗ mới",
            description = "Tạo một bản ghi đặt chỗ mới cho người dùng đã đăng nhập."
    ) 
    @ApiResponses(value = { 
            @ApiResponse(responseCode = "201", description = "Tạo đặt chỗ thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ (ví dụ: trùng slot, sai thời gian)"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền (sai group)")
    })
    @PostMapping
    public ResponseEntity<Booking> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            @Parameter(hidden = true) 
            @AuthenticationPrincipal User currentUser
    ) {
        
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Truyền currentUser xuống service
        Booking newBooking = bookingService.createNewBooking(request, currentUser.getId());
        
        // Trả về 201 Created
        return ResponseEntity.status(HttpStatus.CREATED).body(newBooking);
    }

    @Operation(
            summary = "Lấy danh sách đặt chỗ theo xe",
            description = "Lấy tất cả các đặt chỗ cho một xe cụ thể, với tùy chọn lọc theo khoảng thời gian."
    ) 
    @ApiResponses(value = { 
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "404", description = "Xe không tồn tại")
    })
    @GetMapping
    public ResponseEntity<List<Booking>> getBookingsByVehicle(
            @Parameter(description = "ID của xe cần tìm", required = true) 
            @RequestParam Long vehicleId,
            
            @Parameter(description = "Thời gian bắt đầu lọc (tùy chọn)", example = "2025-01-01T10:00:00") 
            @RequestParam(required = false) LocalDateTime startTime,
            
            @Parameter(description = "Thời gian kết thúc lọc (tùt chọn)", example = "2025-01-30T17:00:00") 
            @RequestParam(required = false) LocalDateTime endTime
    ) {
        List<Booking> bookings = bookingService.findBookings(vehicleId, startTime, endTime);
        return ResponseEntity.ok(bookings);
    }
    
}