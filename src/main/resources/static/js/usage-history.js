// Usage History JavaScript

let usageHistory = [];
let currentUser = null;

// Load data on page load
document.addEventListener('DOMContentLoaded', async () => {
    if (checkAuth()) {
        await loadCurrentUserInfo();
        await loadUsageHistory();
    }
});

// Load current user info
async function loadCurrentUserInfo() {
    try {
        const result = await apiCall('/auth/me');
        if (result && result.response && result.response.ok) {
            currentUser = result.data;
        }
    } catch (error) {
        console.error('Error loading current user:', error);
    }
}

// Load usage history
async function loadUsageHistory() {
    try {
        const result = await apiCall('/usage-history/my-history');
        if (result && result.response && result.response.ok) {
            usageHistory = Array.isArray(result.data) ? result.data : [];
            populateUsageHistoryTable(usageHistory);
        } else {
            showAlert('Không thể tải lịch sử sử dụng', 'error');
            populateUsageHistoryTable([]);
        }
    } catch (error) {
        console.error('Error loading usage history:', error);
        showAlert('Lỗi khi tải lịch sử sử dụng: ' + error.message, 'error');
        populateUsageHistoryTable([]);
    }
}

// Populate usage history table
function populateUsageHistoryTable(history) {
    const tbody = document.getElementById('usageHistoryTableBody');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    if (!history || history.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center py-4">Chưa có lịch sử sử dụng nào</td></tr>';
        return;
    }
    
    // Sort by start time (newest first)
    const sortedHistory = [...history].sort((a, b) => {
        const dateA = a.startTime ? new Date(a.startTime) : new Date(0);
        const dateB = b.startTime ? new Date(b.startTime) : new Date(0);
        return dateB - dateA;
    });
    
    sortedHistory.forEach(item => {
        const tr = document.createElement('tr');
        const vehiclePlate = item.vehicle ? (item.vehicle.plate || `ID: ${item.vehicle.id}`) : '-';
        const startTime = formatDateTime(item.startTime);
        const endTime = formatDateTime(item.endTime);
        const distance = item.distance !== null && item.distance !== undefined 
            ? `${item.distance} km` 
            : (item.startOdometer !== null && item.endOdometer !== null
                ? `${item.endOdometer - item.startOdometer} km`
                : '-');
        const notes = item.notes ? (item.notes.length > 50 ? item.notes.substring(0, 50) + '...' : item.notes) : '-';
        
        tr.innerHTML = `
            <td>
              <div class="d-flex px-2 py-1">
                <div class="d-flex flex-column justify-content-center">
                  <h6 class="mb-0 text-sm">${item.id || '-'}</h6>
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
    return date.toLocaleString('vi-VN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

