// Bookings Management JavaScript

let bookings = [];
let vehicles = [];
let currentUser = null;
let isAdmin = false;

// Load data on page load
document.addEventListener('DOMContentLoaded', async () => {
    if (checkAuth()) {
        // Load current user info to check if admin
        await loadCurrentUserInfo();
        
        await loadVehicles();
        await loadBookings();
    }
});

// Load current user info
async function loadCurrentUserInfo() {
    try {
        const result = await apiCall('/auth/me');
        if (result && result.response && result.response.ok) {
            currentUser = result.data;
            isAdmin = currentUser && currentUser.roles && Array.isArray(currentUser.roles) && currentUser.roles.includes('ADMIN');
        }
    } catch (error) {
        console.error('Error loading current user:', error);
    }
}

// Load vehicles
async function loadVehicles() {
    try {
        // Admin uses admin endpoint to see all vehicles
        const endpoint = isAdmin ? '/admin/vehicles' : '/vehicles';
        const result = await apiCall(endpoint);
        if (result && result.response.ok) {
            vehicles = Array.isArray(result.data) ? result.data : [];
            populateVehicleSelect();
        }
    } catch (error) {
        console.error('Error loading vehicles:', error);
    }
}

// Populate vehicle select
function populateVehicleSelect() {
    const select = document.getElementById('bookingVehicleId');
    select.innerHTML = '<option value="">Chọn xe</option>';
    
    vehicles.forEach(vehicle => {
        const option = document.createElement('option');
        option.value = vehicle.id;
        option.textContent = `${vehicle.plate} - ${vehicle.model}`;
        select.appendChild(option);
    });
}

// Load bookings
async function loadBookings() {
    try {
        // Admin uses admin endpoint to see all bookings
        const endpoint = isAdmin ? '/admin/bookings' : '/bookings';
        const result = await apiCall(endpoint);
        if (result && result.response.ok) {
            bookings = Array.isArray(result.data) ? result.data : [];
            populateBookingsTable(bookings);
        } else {
            showAlert('Không thể tải danh sách booking', 'error');
            populateBookingsTable([]);
        }
    } catch (error) {
        console.error('Error loading bookings:', error);
        showAlert('Lỗi khi tải danh sách booking', 'error');
        populateBookingsTable([]);
    }
}

// Populate bookings table
function populateBookingsTable(bookings) {
    const tbody = document.getElementById('bookingsTableBody');
    if (!tbody) {
        console.error('bookingsTableBody not found');
        return;
    }
    tbody.innerHTML = '';

    if (!bookings || bookings.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center py-4">Chưa có booking nào</td></tr>';
        return;
    }

    bookings.forEach(booking => {
        const tr = document.createElement('tr');
        const startTime = formatDateTime(booking.startTime);
        const endTime = formatDateTime(booking.endTime);
        const status = booking.status || '-';
        const statusBadge = status === 'PENDING' ? 'warning' : 
                           status === 'CONFIRMED' ? 'success' : 
                           status === 'CANCELLED' ? 'danger' : 'secondary';
        
        // For admin, show user info; for regular users, show vehicle info
        const userInfo = isAdmin && booking.user 
            ? (booking.user.fullName || booking.user.email || '-')
            : (booking.vehicle?.plate || '-');
        
        // Determine if user can cancel (only if they created the booking or are admin)
        const canCancel = isAdmin || (booking.user && booking.user.id === currentUser?.id);
        
        tr.innerHTML = `
            <td>
              <div class="d-flex px-2 py-1">
                <div class="d-flex flex-column justify-content-center">
                  <h6 class="mb-0 text-sm">${booking.id || '-'}</h6>
                </div>
              </div>
            </td>
            <td>
              <p class="text-xs font-weight-bold mb-0">${escapeHtml(userInfo)}</p>
            </td>
            <td class="align-middle text-center text-sm">
              <span class="text-secondary text-xs font-weight-bold">${startTime}</span>
            </td>
            <td class="align-middle text-center text-sm">
              <span class="text-secondary text-xs font-weight-bold">${endTime}</span>
            </td>
            <td class="align-middle text-center">
              <span class="badge badge-sm bg-${statusBadge}">${status}</span>
            </td>
            <td class="align-middle text-center">
              ${(booking.status === 'PENDING' || booking.status === 'CONFIRMED') && canCancel
                  ? `<button class="btn btn-sm btn-danger" onclick="cancelBooking(${booking.id})">Hủy</button>` 
                  : '<span class="text-secondary text-xs">-</span>'}
            </td>
        `;
        tbody.appendChild(tr);
    });
}

// Format datetime helper
function formatDateTime(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleString('vi-VN');
}

// Escape HTML to prevent XSS
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Show create booking modal - sử dụng Bootstrap modal
function showCreateBookingModal() {
    const modalElement = document.getElementById('createBookingModal');
    if (modalElement) {
        const modal = new bootstrap.Modal(modalElement);
        modal.show();
        const form = document.getElementById('createBookingForm');
        if (form) form.reset();
    }
}

// Create booking - handled in HTML inline script to avoid duplicate listeners

// Cancel booking - expose to window for onclick handlers
window.cancelBooking = async function cancelBooking(bookingId) {
    if (!bookingId) {
        console.error('cancelBooking called without bookingId');
        showAlert('Lỗi: Không tìm thấy ID booking', 'error');
        return;
    }

    if (!confirm('Bạn có chắc muốn hủy booking này?')) {
        return;
    }

    console.log('Attempting to cancel booking:', bookingId);

    try {
        const result = await apiCall(`/bookings/${bookingId}/cancel`, {
            method: 'PUT'
        });

        console.log('Cancel booking result:', result);

        if (result && result.response && result.response.ok) {
            showAlert('Hủy booking thành công!', 'success');
            await loadBookings();
        } else {
            // Better error handling
            let errorMsg = 'Không thể hủy booking';
            if (result) {
                if (result.data) {
                    if (typeof result.data === 'string') {
                        errorMsg = result.data;
                    } else if (result.data.message) {
                        errorMsg = result.data.message;
                    } else if (result.data.error) {
                        errorMsg = result.data.error;
                    } else if (result.data.details) {
                        // Handle validation errors
                        const details = result.data.details;
                        if (typeof details === 'object') {
                            const errorMessages = Object.values(details).join(', ');
                            errorMsg = errorMessages || errorMsg;
                        }
                    }
                }
                // Check response status for more info
                if (result.response) {
                    if (result.response.status === 403) {
                        errorMsg = 'Bạn không có quyền hủy booking này';
                    } else if (result.response.status === 400) {
                        errorMsg = errorMsg || 'Dữ liệu không hợp lệ';
                    } else if (result.response.status === 404) {
                        errorMsg = 'Không tìm thấy booking';
                    } else if (result.response.status === 500) {
                        errorMsg = 'Lỗi server. Vui lòng thử lại sau.';
                    }
                }
            }
            console.error('Cancel booking error:', result);
            showAlert(errorMsg, 'error');
        }
    } catch (error) {
        console.error('Error canceling booking:', error);
        const errorMsg = error.message || 'Lỗi khi hủy booking. Vui lòng thử lại.';
        showAlert(errorMsg, 'error');
    }
};

