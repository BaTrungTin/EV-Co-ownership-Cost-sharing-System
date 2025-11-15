// Profile JavaScript
// Note: API_BASE_URL và apiCall() đã được định nghĩa trong common.js

const loadingState = document.getElementById('loadingState');
const errorState = document.getElementById('errorState');
const profileContent = document.getElementById('profileContent');
const userName = document.getElementById('userName');
const userRole = document.getElementById('userRole');
const logoutBtn = document.getElementById('logoutBtn');

// Global variables
let currentUser = null;

// Check authentication on page load
window.addEventListener('DOMContentLoaded', async () => {
    // Sử dụng checkAuth từ common.js
    if (!checkAuth()) {
        return; // checkAuth đã redirect về login
    }
    
    // Load profile data
    await loadProfile();
});

// Load profile data
async function loadProfile() {
    try {
        // Load current user info
        const userResult = await apiCall('/auth/me');
        if (!userResult || !userResult.response || !userResult.response.ok) {
            if (userResult && userResult.response && userResult.response.status === 401) {
                window.location.href = 'login.html';
                return;
            }
            throw new Error('Failed to load user info');
        }
        
        currentUser = userResult.data;
        
        // Hiển thị thông tin user
        userName.textContent = currentUser.fullName || currentUser.email;
        userRole.textContent = currentUser.roles ? currentUser.roles.join(', ') : 'USER';
        
        // Hiển thị thông tin profile
        document.getElementById('profileFullName').textContent = currentUser.fullName || '-';
        document.getElementById('profileEmail').textContent = currentUser.email || '-';
        document.getElementById('profileRoles').textContent = currentUser.roles ? currentUser.roles.join(', ') : 'USER';
        
        // Load user data (bookings, groups, expense shares)
        await loadUserData();
        
    } catch (error) {
        console.error('Error loading profile:', error);
        showError('Không thể tải thông tin profile: ' + error.message);
    }
}

// Load user data
async function loadUserData() {
    try {
        // Gọi các API để lấy dữ liệu của user
        const [bookingsResult, groupsResult, expenseSharesResult] = await Promise.allSettled([
            apiCall('/bookings'),
            apiCall('/groups'),
            apiCall('/expenses/my-shares')
        ]);
        
        // Parse responses
        const bookings = bookingsResult.status === 'fulfilled' && bookingsResult.value?.response?.ok 
            ? (Array.isArray(bookingsResult.value.data) ? bookingsResult.value.data : []) : [];
        const groups = groupsResult.status === 'fulfilled' && groupsResult.value?.response?.ok 
            ? (Array.isArray(groupsResult.value.data) ? groupsResult.value.data : []) : [];
        const expenseShares = expenseSharesResult.status === 'fulfilled' && expenseSharesResult.value?.response?.ok 
            ? (Array.isArray(expenseSharesResult.value.data) ? expenseSharesResult.value.data : []) : [];
        
        // Tính số lượng groups mà user là thành viên
        const myGroups = groups.filter(g => {
            if (!g.ownershipShares || g.ownershipShares.length === 0) return false;
            return g.ownershipShares.some(os => os.user && os.user.id === currentUser.id);
        });
        
        // Update stats
        document.getElementById('myBookingsCount').textContent = bookings.length;
        document.getElementById('myGroupsCount').textContent = myGroups.length;
        document.getElementById('myVehiclesCount').textContent = '-'; // TODO: Tính từ groups
        document.getElementById('myExpensesCount').textContent = expenseShares.length;
        
        // Populate tables
        populateMyBookingsTable(bookings);
        populateMyGroupsTable(myGroups);
        
        // Show profile content
        loadingState.style.display = 'none';
        profileContent.style.display = 'block';
        
    } catch (error) {
        console.error('Error loading user data:', error);
        showError('Không thể tải dữ liệu: ' + error.message);
    }
}

// Populate my bookings table
function populateMyBookingsTable(bookings) {
    const tbody = document.getElementById('myBookingsTableBody');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    if (!bookings || bookings.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5">Bạn chưa có booking nào</td></tr>';
        return;
    }
    
    bookings.forEach(booking => {
        const tr = document.createElement('tr');
        const startTime = booking.startTime ? new Date(booking.startTime).toLocaleString('vi-VN') : '-';
        const endTime = booking.endTime ? new Date(booking.endTime).toLocaleString('vi-VN') : '-';
        const status = booking.status || 'PENDING';
        tr.innerHTML = `
            <td>${booking.id || '-'}</td>
            <td>${booking.vehicle?.plate || booking.vehicle?.model || booking.vehicleId || '-'}</td>
            <td>${startTime}</td>
            <td>${endTime}</td>
            <td><span class="status-badge status-${status.toLowerCase()}">${status}</span></td>
        `;
        tbody.appendChild(tr);
    });
}

// Populate my groups table
function populateMyGroupsTable(groups) {
    const tbody = document.getElementById('myGroupsTableBody');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    if (!groups || groups.length === 0) {
        tbody.innerHTML = '<tr><td colspan="3">Bạn chưa tham gia nhóm nào</td></tr>';
        return;
    }
    
    groups.forEach(group => {
        // Tìm ownership share của current user
        const ownershipShare = group.ownershipShares?.find(os => 
            os.user && os.user.id === currentUser?.id
        );
        const percentage = ownershipShare ? (ownershipShare.percentage * 100).toFixed(1) + '%' : '-';
        
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${group.id || '-'}</td>
            <td>${group.name || '-'}</td>
            <td>${percentage}</td>
        `;
        tbody.appendChild(tr);
    });
}

// Show error state
function showError(message = 'Đã xảy ra lỗi. Vui lòng thử lại sau.') {
    loadingState.style.display = 'none';
    errorState.style.display = 'flex';
    profileContent.style.display = 'none';
    const errorMessage = document.getElementById('errorMessage');
    if (errorMessage) {
        errorMessage.textContent = message;
    }
}

// Logout
logoutBtn.addEventListener('click', () => {
    logout(); // Sử dụng logout từ common.js
});


