// Admin Dashboard JavaScript
// Chỉ hiển thị dữ liệu cho Admin

// Elements
const loadingState = document.getElementById('loadingState');
const errorState = document.getElementById('errorState');
const adminDashboard = document.getElementById('adminDashboard');
const userName = document.getElementById('userName');
const userRole = document.getElementById('userRole');
const dashboardTitle = document.getElementById('dashboardTitle');
const logoutBtn = document.getElementById('logoutBtn');

// Global variables
let currentUser = null;
let isAdmin = false;

// Check authentication on page load
window.addEventListener('DOMContentLoaded', async () => {
    // Sử dụng checkAuth từ common.js
    if (!checkAuth()) {
        return; // checkAuth đã redirect về login
    }
    
    // Load current user info
    await loadCurrentUser();
});

// Load current user info và check role
async function loadCurrentUser() {
    try {
        // Sử dụng apiCall từ common.js thay vì fetch trực tiếp
        const result = await apiCall('/auth/me');
        if (!result || !result.response || !result.response.ok) {
            if (result && result.response && result.response.status === 401) {
                // Token không hợp lệ, redirect về login
                window.location.href = 'login.html';
                return;
            }
            throw new Error('Failed to load user info');
        }
        
        currentUser = result.data;
        
        // Check if user is ADMIN
        isAdmin = currentUser.roles && Array.isArray(currentUser.roles) && currentUser.roles.includes('ADMIN');
        
        // ⚠️ IMPORTANT: Chỉ ADMIN mới được vào dashboard.html
        // User thường sẽ bị redirect
        if (!isAdmin) {
            console.warn('User không có quyền Admin. Redirect đến trang user...');
            window.location.href = 'user-dashboard.html';
            return;
        }

        // Hiển thị thông tin user
        userName.textContent = currentUser.fullName || currentUser.email;
        userRole.textContent = 'ADMIN';
        dashboardTitle.textContent = '🚗 Admin Dashboard';

        // Ẩn admin dashboard - sẽ hiển thị sau khi load data
        adminDashboard.style.display = 'none';

        // Load admin dashboard data
        await loadAdminDashboard();

    } catch (error) {
        console.error('Error loading user info:', error);
        showError('Không thể tải thông tin người dùng: ' + error.message);
    }
}

// Load admin dashboard data
async function loadAdminDashboard() {
    try {
        // Show loading
        if (loadingState) loadingState.style.display = 'block';
        if (errorState) errorState.style.display = 'none';
        if (adminDashboard) adminDashboard.style.display = 'none';

        // Gọi các API admin endpoints sử dụng apiCall từ common.js
        const [usersResult, groupsResult, vehiclesResult, bookingsResult, expensesResult] = await Promise.allSettled([
            apiCall('/admin/users'),
            apiCall('/admin/groups'),
            apiCall('/admin/vehicles'),
            apiCall('/admin/bookings'),
            apiCall('/admin/expenses')
        ]);
        
        // Check if any request failed with 403 (Forbidden) or 500 (Server Error)
        const hasError = [usersResult, groupsResult, vehiclesResult, bookingsResult, expensesResult].some(res => {
            if (res.status === 'rejected') return true;
            if (res.status === 'fulfilled' && res.value) {
                const response = res.value.response;
                return response && (response.status === 403 || response.status === 500);
            }
            return false;
        });
        
        if (hasError) {
            const errorMsg = usersResult.status === 'fulfilled' && usersResult.value?.response?.status === 403
                ? 'Bạn không có quyền Admin để truy cập trang này. Vui lòng đăng nhập bằng tài khoản Admin.'
                : 'Lỗi khi tải dữ liệu admin. Vui lòng thử lại sau.';
            showError(errorMsg);
            console.error('Admin dashboard error:', { usersResult, groupsResult, vehiclesResult, bookingsResult, expensesResult });
            return;
        }
        
        // Parse responses
        const users = usersResult.status === 'fulfilled' && usersResult.value?.response?.ok 
            ? (Array.isArray(usersResult.value.data) ? usersResult.value.data : []) : [];
        const groups = groupsResult.status === 'fulfilled' && groupsResult.value?.response?.ok 
            ? (Array.isArray(groupsResult.value.data) ? groupsResult.value.data : []) : [];
        const vehicles = vehiclesResult.status === 'fulfilled' && vehiclesResult.value?.response?.ok 
            ? (Array.isArray(vehiclesResult.value.data) ? vehiclesResult.value.data : []) : [];
        const bookings = bookingsResult.status === 'fulfilled' && bookingsResult.value?.response?.ok 
            ? (Array.isArray(bookingsResult.value.data) ? bookingsResult.value.data : []) : [];
        const expenses = expensesResult.status === 'fulfilled' && expensesResult.value?.response?.ok 
            ? (Array.isArray(expensesResult.value.data) ? expensesResult.value.data : []) : [];
        
        // Update stats
        document.getElementById('totalUsers').textContent = users.length;
        document.getElementById('totalGroups').textContent = groups.length;
        document.getElementById('totalVehicles').textContent = vehicles.length;
        document.getElementById('totalBookings').textContent = bookings.length;
        document.getElementById('totalExpenses').textContent = expenses.length;
        
        // Populate tables
        populateUsersTable(users);
        populateGroupsTable(groups);
        populateVehiclesTable(vehicles);
        
        // Show admin dashboard
        if (loadingState) loadingState.style.display = 'none';
        if (adminDashboard) adminDashboard.style.display = 'block';

    } catch (error) {
        console.error('Error loading admin dashboard:', error);
        showError('Không thể tải dữ liệu admin: ' + error.message);
    }
}

// Populate users table (Admin only)
function populateUsersTable(users) {
    const tbody = document.getElementById('usersTableBody');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    if (!users || users.length === 0) {
        tbody.innerHTML = '<tr><td colspan="3">Không có dữ liệu</td></tr>';
        return;
    }
    
    users.forEach(user => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${user.id || '-'}</td>
            <td>${user.email || '-'}</td>
            <td>${user.fullName || '-'}</td>
        `;
        tbody.appendChild(tr);
    });
}

// Populate groups table (Admin only)
function populateGroupsTable(groups) {
    const tbody = document.getElementById('groupsTableBody');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    if (!groups || groups.length === 0) {
        tbody.innerHTML = '<tr><td colspan="2">Không có dữ liệu</td></tr>';
        return;
    }
    
    groups.forEach(group => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${group.id || '-'}</td>
            <td>${group.name || '-'}</td>
        `;
        tbody.appendChild(tr);
    });
}

// Populate vehicles table (Admin only)
function populateVehiclesTable(vehicles) {
    const tbody = document.getElementById('vehiclesTableBody');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    if (!vehicles || vehicles.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4">Không có dữ liệu</td></tr>';
        return;
    }
    
    vehicles.forEach(vehicle => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${vehicle.id || '-'}</td>
            <td>${vehicle.vin || '-'}</td>
            <td>${vehicle.licensePlate || vehicle.plate || '-'}</td>
            <td>${vehicle.model || '-'}</td>
        `;
        tbody.appendChild(tr);
    });
}

// Show error state
function showError(message = 'Đã xảy ra lỗi. Vui lòng thử lại sau.') {
    if (loadingState) loadingState.style.display = 'none';
    if (errorState) errorState.style.display = 'flex';
    if (adminDashboard) adminDashboard.style.display = 'none';
    const errorMessage = document.getElementById('errorMessage');
    if (errorMessage) {
        errorMessage.textContent = message;
    }
}

// Logout
if (logoutBtn) {
    logoutBtn.addEventListener('click', () => {
        logout(); // Sử dụng logout từ common.js
    });
}
