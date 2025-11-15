// Admin Dashboard JavaScript
// Tích hợp với API backend

// Elements
const loadingState = document.getElementById('loadingState');
const errorState = document.getElementById('errorState');
const dashboardContent = document.getElementById('dashboardContent');
const userNameNav = document.getElementById('userNameNav');
const userNameText = document.getElementById('userNameText');
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
        // Sử dụng apiCall từ common.js
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
        
        // Hiển thị thông tin user (với ID)
        displayUserInfoInNavbar(currentUser);
        
        // Check if user is ADMIN
        isAdmin = currentUser.roles && Array.isArray(currentUser.roles) && currentUser.roles.includes('ADMIN');
        
        if (!isAdmin) {
            // User không phải admin - redirect đến profile
            showError('Bạn không có quyền Admin để truy cập trang này.');
            setTimeout(() => {
                window.location.href = 'profile.html';
            }, 2000);
            return;
        }
        
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
        if (dashboardContent) dashboardContent.style.display = 'none';
        
        // Gọi các API admin endpoints sử dụng apiCall từ common.js
        const [usersResult, groupsResult, vehiclesResult, bookingsResult, expensesResult] = await Promise.allSettled([
            apiCall('/admin/users'),
            apiCall('/admin/groups'),
            apiCall('/admin/vehicles'),
            apiCall('/admin/bookings'),
            apiCall('/admin/expenses')
        ]);
        
        // Check if any request failed with 403 (Forbidden) or 500 (Server Error)
        const errors = [];
        [usersResult, groupsResult, vehiclesResult, bookingsResult, expensesResult].forEach((res, index) => {
            const endpointNames = ['users', 'groups', 'vehicles', 'bookings', 'expenses'];
            if (res.status === 'rejected') {
                errors.push(`${endpointNames[index]}: ${res.reason?.message || 'Rejected'}`);
            } else if (res.status === 'fulfilled' && res.value) {
                const response = res.value.response;
                if (response && (response.status === 403 || response.status === 500)) {
                    const errorData = res.value.data;
                    const errorMsg = errorData?.message || errorData?.error || `HTTP ${response.status}`;
                    errors.push(`${endpointNames[index]}: ${errorMsg}`);
                }
            }
        });
        
        if (errors.length > 0) {
            const errorMsg = errors.includes('users: 403') || errors.some(e => e.includes('403'))
                ? 'Bạn không có quyền Admin để truy cập trang này. Vui lòng đăng nhập bằng tài khoản Admin.'
                : `Lỗi khi tải dữ liệu admin: ${errors.join(', ')}`;
            showError(errorMsg);
            console.error('Admin dashboard error:', { 
                errors,
                usersResult, 
                groupsResult, 
                vehiclesResult, 
                bookingsResult, 
                expensesResult 
            });
            
            // Still try to show partial data if some requests succeeded
            const hasAnySuccess = [usersResult, groupsResult, vehiclesResult, bookingsResult, expensesResult].some(res => {
                return res.status === 'fulfilled' && res.value?.response?.ok;
            });
            
            if (!hasAnySuccess) {
                return; // Don't continue if all requests failed
            }
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
        
        // Populate tables
        populateUsersTable(users);
        populateGroupsTable(groups);
        populateVehiclesTable(vehicles);
        
        // Hide loading and show content
        if (loadingState) loadingState.style.display = 'none';
        if (dashboardContent) dashboardContent.style.display = 'block';
        
    } catch (error) {
        console.error('Error loading admin dashboard:', error);
        showError('Không thể tải dữ liệu admin: ' + error.message);
    }
}

// Populate users table
function populateUsersTable(users) {
    const tbody = document.getElementById('usersTableBody');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    if (!users || users.length === 0) {
        tbody.innerHTML = '<tr><td colspan="3" class="text-center">Không có dữ liệu</td></tr>';
        return;
    }
    
    users.forEach(user => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>
              <div class="d-flex px-2 py-1">
                <div class="d-flex flex-column justify-content-center">
                  <h6 class="mb-0 text-sm">${user.id || '-'}</h6>
                </div>
              </div>
            </td>
            <td>
              <p class="text-xs font-weight-bold mb-0">${user.email || '-'}</p>
            </td>
            <td class="align-middle text-center text-sm">
              <span class="text-secondary text-xs font-weight-bold">${user.fullName || '-'}</span>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

// Populate groups table
function populateGroupsTable(groups) {
    const tbody = document.getElementById('groupsTableBody');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    if (!groups || groups.length === 0) {
        tbody.innerHTML = '<tr><td colspan="2" class="text-center">Không có dữ liệu</td></tr>';
        return;
    }
    
    groups.forEach(group => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>
              <div class="d-flex px-2 py-1">
                <div class="d-flex flex-column justify-content-center">
                  <h6 class="mb-0 text-sm">${group.id || '-'}</h6>
                </div>
              </div>
            </td>
            <td>
              <p class="text-xs font-weight-bold mb-0">${group.name || '-'}</p>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

// Populate vehicles table
function populateVehiclesTable(vehicles) {
    const tbody = document.getElementById('vehiclesTableBody');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    if (!vehicles || vehicles.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" class="text-center">Không có dữ liệu</td></tr>';
        return;
    }
    
    vehicles.forEach(vehicle => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>
              <div class="d-flex px-2 py-1">
                <div class="d-flex flex-column justify-content-center">
                  <h6 class="mb-0 text-sm">${vehicle.id || '-'}</h6>
                </div>
              </div>
            </td>
            <td>
              <p class="text-xs font-weight-bold mb-0">${vehicle.vin || '-'}</p>
            </td>
            <td class="align-middle text-center text-sm">
              <span class="text-secondary text-xs font-weight-bold">${vehicle.plate || '-'}</span>
            </td>
            <td class="align-middle text-center">
              <span class="text-secondary text-xs font-weight-bold">${vehicle.model || '-'}</span>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

// Show error state
function showError(message = 'Đã xảy ra lỗi. Vui lòng thử lại sau.') {
    if (loadingState) loadingState.style.display = 'none';
    if (errorState) {
        errorState.style.display = 'block';
        const errorMessage = document.getElementById('errorMessage');
        if (errorMessage) {
            errorMessage.textContent = message;
        }
    }
    if (dashboardContent) dashboardContent.style.display = 'none';
}

// Logout
if (logoutBtn) {
    logoutBtn.addEventListener('click', () => {
        logout(); // Sử dụng logout từ common.js
    });
}

// Initialize sidenav toggle
if (document.getElementById('iconNavbarSidenav')) {
    document.getElementById('iconNavbarSidenav').addEventListener('click', function() {
        const sidenav = document.getElementById('sidenav-main');
        if (sidenav) {
            sidenav.classList.toggle('g-sidenav-pinned');
        }
    });
}

if (document.getElementById('iconSidenav')) {
    document.getElementById('iconSidenav').addEventListener('click', function() {
        const sidenav = document.getElementById('sidenav-main');
        if (sidenav) {
            sidenav.classList.toggle('g-sidenav-pinned');
        }
    });
}

