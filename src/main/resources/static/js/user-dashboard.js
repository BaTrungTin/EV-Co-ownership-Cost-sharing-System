// User Dashboard JavaScript
// Dashboard cho user thường (không phải admin)

// Elements
const loadingState = document.getElementById('loadingState');
const errorState = document.getElementById('errorState');
const dashboardContent = document.getElementById('dashboardContent');
const userNameNav = document.getElementById('userNameNav');
const userNameText = document.getElementById('userNameText');

// Global variables
let currentUser = null;

// Check authentication on page load
window.addEventListener('DOMContentLoaded', async () => {
    console.log('User dashboard: DOMContentLoaded');
    
    if (!checkAuth()) {
        console.log('User dashboard: Not authenticated, redirecting...');
        return; // checkAuth đã redirect về login
    }
    
    // Load current user info
    await loadCurrentUser();
    
    // Load dashboard data
    await loadDashboardData();
});

// Load current user info
async function loadCurrentUser() {
    try {
        const result = await apiCall('/auth/me');
        if (!result || !result.response || !result.response.ok) {
            if (result && result.response && result.response.status === 401) {
                // Unauthorized - redirect to login
                logout();
                return;
            }
            throw new Error('Failed to load user info');
        }
        
        currentUser = result.data;
        
        // Hiển thị thông tin user
        if (userNameText) {
            userNameText.textContent = currentUser.fullName || currentUser.email;
        }
        
        // Hiển thị ID của user trong navbar
        const userIdText = document.getElementById('userIdText');
        if (userIdText && currentUser.id) {
            userIdText.textContent = `(ID: ${currentUser.id})`;
        }
        
        // Hiển thị thông tin user trong info card
        if (currentUser.id) {
            const userInfoId = document.getElementById('userInfoId');
            const userInfoEmail = document.getElementById('userInfoEmail');
            const userInfoName = document.getElementById('userInfoName');
            if (userInfoId) userInfoId.textContent = currentUser.id;
            if (userInfoEmail) userInfoEmail.textContent = currentUser.email || '-';
            if (userInfoName) userInfoName.textContent = currentUser.fullName || '-';
        }
        
        // Check if user is ADMIN - redirect to admin dashboard
        const isAdmin = currentUser.roles && Array.isArray(currentUser.roles) && currentUser.roles.includes('ADMIN');
        if (isAdmin) {
            window.location.href = 'admin-dashboard.html';
            return;
        }
        
        // Show dashboard content immediately after user info is loaded
        if (dashboardContent) {
            dashboardContent.style.display = 'block';
            console.log('Dashboard content shown after user info loaded');
        }
        
    } catch (error) {
        console.error('Error loading user info:', error);
        showError('Không thể tải thông tin người dùng: ' + error.message);
        // Still show dashboard content even on error
        if (dashboardContent) {
            dashboardContent.style.display = 'block';
        }
    }
}

// Load dashboard data
async function loadDashboardData() {
    console.log('User dashboard: loadDashboardData started');
    try {
        // Show loading
        if (loadingState) {
            loadingState.style.display = 'block';
            console.log('Loading state shown');
        }
        if (errorState) errorState.style.display = 'none';
        if (dashboardContent) {
            dashboardContent.style.display = 'none';
            console.log('Dashboard content hidden');
        }
        
        // Load data in parallel
        const [groupsResult, bookingsResult, expensesResult, paymentsResult, usageHistoryResult] = await Promise.allSettled([
            apiCall('/groups'),
            apiCall('/bookings'),
            apiCall('/expenses/my-shares'),
            apiCall('/payments/my-payments'),
            apiCall('/usage-history/my-history')
        ]);
        
        // Process groups
        let groups = [];
        if (groupsResult.status === 'fulfilled' && groupsResult.value?.response?.ok) {
            groups = Array.isArray(groupsResult.value.data) ? groupsResult.value.data : [];
        } else {
            console.warn('Failed to load groups:', groupsResult);
        }
        
        // Process bookings
        let bookings = [];
        if (bookingsResult.status === 'fulfilled' && bookingsResult.value?.response?.ok) {
            bookings = Array.isArray(bookingsResult.value.data) ? bookingsResult.value.data : [];
        } else {
            console.warn('Failed to load bookings:', bookingsResult);
        }
        
        // Process expenses
        let expenses = [];
        if (expensesResult.status === 'fulfilled' && expensesResult.value?.response?.ok) {
            expenses = Array.isArray(expensesResult.value.data) ? expensesResult.value.data : [];
        } else {
            console.warn('Failed to load expenses:', expensesResult);
        }
        
        // Process payments
        let payments = [];
        if (paymentsResult.status === 'fulfilled' && paymentsResult.value?.response?.ok) {
            payments = Array.isArray(paymentsResult.value.data) ? paymentsResult.value.data : [];
        } else {
            console.warn('Failed to load payments:', paymentsResult);
        }
        
        // Process usage history
        let usageHistory = [];
        if (usageHistoryResult.status === 'fulfilled' && usageHistoryResult.value?.response?.ok) {
            usageHistory = Array.isArray(usageHistoryResult.value.data) ? usageHistoryResult.value.data : [];
        } else {
            console.warn('Failed to load usage history:', usageHistoryResult);
        }
        
        // Hide loading, show content first
        if (loadingState) loadingState.style.display = 'none';
        if (dashboardContent) {
            dashboardContent.style.display = 'block';
        } else {
            console.error('dashboardContent element not found!');
        }
        
        // Update stats - ensure elements exist and dashboardContent is visible
        const totalGroupsEl = document.getElementById('totalGroups');
        const totalBookingsEl = document.getElementById('totalBookings');
        const totalExpensesEl = document.getElementById('totalExpenses');
        const totalPaymentsEl = document.getElementById('totalPayments');
        
        console.log('Updating stats:', {
            groups: groups.length,
            bookings: bookings.length,
            expenses: expenses.length,
            payments: payments.length,
            totalGroupsEl: !!totalGroupsEl,
            totalBookingsEl: !!totalBookingsEl,
            totalExpensesEl: !!totalExpensesEl,
            totalPaymentsEl: !!totalPaymentsEl
        });
        
        if (totalGroupsEl) {
            totalGroupsEl.textContent = groups.length;
            console.log('Updated totalGroups to:', groups.length);
        } else {
            console.error('totalGroups element not found');
        }
        
        if (totalBookingsEl) {
            totalBookingsEl.textContent = bookings.length;
            console.log('Updated totalBookings to:', bookings.length);
        } else {
            console.error('totalBookings element not found');
        }
        
        if (totalExpensesEl) {
            totalExpensesEl.textContent = expenses.length;
            console.log('Updated totalExpenses to:', expenses.length);
        } else {
            console.error('totalExpenses element not found');
        }
        
        if (totalPaymentsEl) {
            totalPaymentsEl.textContent = payments.length;
            console.log('Updated totalPayments to:', payments.length);
        } else {
            console.error('totalPayments element not found');
        }
        
        // Populate tables
        populateGroupsTable(groups);
        populateBookingsTable(bookings.slice(0, 5)); // Show only 5 recent bookings
        populateUsageHistoryTable(usageHistory.slice(0, 10)); // Show only 10 recent history entries
        
    } catch (error) {
        console.error('Error loading dashboard data:', error);
        showError('Không thể tải dữ liệu dashboard: ' + error.message);
        if (loadingState) loadingState.style.display = 'none';
        if (errorState) errorState.style.display = 'block';
        // Still show dashboard content even on error
        if (dashboardContent) {
            dashboardContent.style.display = 'block';
        }
        // Ensure stats are set to 0 if there was an error
        const totalGroupsEl = document.getElementById('totalGroups');
        const totalBookingsEl = document.getElementById('totalBookings');
        const totalExpensesEl = document.getElementById('totalExpenses');
        const totalPaymentsEl = document.getElementById('totalPayments');
        if (totalGroupsEl) totalGroupsEl.textContent = '0';
        if (totalBookingsEl) totalBookingsEl.textContent = '0';
        if (totalExpensesEl) totalExpensesEl.textContent = '0';
        if (totalPaymentsEl) totalPaymentsEl.textContent = '0';
    }
}

// Populate groups table
function populateGroupsTable(groups) {
    const tbody = document.getElementById('groupsTableBody');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    if (!groups || groups.length === 0) {
        tbody.innerHTML = '<tr><td colspan="3" class="text-center py-4">Bạn chưa tham gia nhóm nào</td></tr>';
        return;
    }
    
    groups.forEach(group => {
        const tr = document.createElement('tr');
        const memberCount = group.ownershipShares ? group.ownershipShares.length : 0;
        
        tr.innerHTML = `
            <td>
              <div class="d-flex px-2 py-1">
                <div class="d-flex flex-column justify-content-center">
                  <h6 class="mb-0 text-sm">${group.id || '-'}</h6>
                </div>
              </div>
            </td>
            <td>
              <p class="text-xs font-weight-bold mb-0">${escapeHtml(group.name || '-')}</p>
            </td>
            <td class="align-middle text-center">
              <span class="text-secondary text-xs font-weight-bold">${memberCount}</span>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

// Populate bookings table
function populateBookingsTable(bookings) {
    const tbody = document.getElementById('bookingsTableBody');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    if (!bookings || bookings.length === 0) {
        tbody.innerHTML = '<tr><td colspan="3" class="text-center py-4">Chưa có booking nào</td></tr>';
        return;
    }
    
    bookings.forEach(booking => {
        const tr = document.createElement('tr');
        const startTime = formatDateTime(booking.startTime);
        const status = booking.status || '-';
        const statusBadge = status === 'PENDING' ? 'warning' : 
                           status === 'CONFIRMED' ? 'success' : 
                           status === 'CANCELLED' ? 'danger' : 'secondary';
        
        tr.innerHTML = `
            <td>
              <p class="text-xs font-weight-bold mb-0">${escapeHtml(booking.vehicle?.plate || '-')}</p>
            </td>
            <td>
              <p class="text-xs font-weight-bold mb-0">${startTime}</p>
            </td>
            <td class="align-middle text-center">
              <span class="badge badge-sm bg-${statusBadge}">${status}</span>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

// Populate usage history table
function populateUsageHistoryTable(usageHistory) {
    const tbody = document.getElementById('usageHistoryTableBody');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    if (!usageHistory || usageHistory.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center py-4">Chưa có lịch sử sử dụng nào</td></tr>';
        return;
    }
    
    usageHistory.forEach(history => {
        const tr = document.createElement('tr');
        const vehiclePlate = history.vehicle ? (history.vehicle.plate || `ID: ${history.vehicle.id}`) : '-';
        const startTime = formatDateTime(history.startTime);
        const endTime = formatDateTime(history.endTime);
        const distance = history.distance !== null && history.distance !== undefined 
            ? `${history.distance} km` 
            : (history.startOdometer !== null && history.endOdometer !== null
                ? `${history.endOdometer - history.startOdometer} km`
                : '-');
        const notes = history.notes ? (history.notes.length > 50 ? history.notes.substring(0, 50) + '...' : history.notes) : '-';
        
        tr.innerHTML = `
            <td>
              <div class="d-flex px-2 py-1">
                <div class="d-flex flex-column justify-content-center">
                  <h6 class="mb-0 text-sm">${history.id || '-'}</h6>
                </div>
              </div>
            </td>
            <td>
              <p class="text-xs font-weight-bold mb-0">${escapeHtml(vehiclePlate)}</p>
            </td>
            <td>
              <p class="text-xs font-weight-bold mb-0">${startTime}</p>
            </td>
            <td>
              <p class="text-xs font-weight-bold mb-0">${endTime}</p>
            </td>
            <td class="align-middle text-center">
              <span class="text-secondary text-xs font-weight-bold">${distance}</span>
            </td>
            <td class="align-middle text-center">
              <span class="text-secondary text-xs">${escapeHtml(notes)}</span>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

// Helper functions
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function formatDateTime(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleString('vi-VN');
}

function showError(message) {
    const errorMsg = document.getElementById('errorMessage');
    if (errorMsg) {
        errorMsg.textContent = message;
    }
    if (errorState) {
        errorState.style.display = 'block';
    }
}

