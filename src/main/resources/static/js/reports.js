// Reports Management JavaScript

let groups = [];
let currentUser = null;
let isAdmin = false;

// Load data on page load
document.addEventListener('DOMContentLoaded', async () => {
    if (checkAuth()) {
        // Load current user info to check if admin
        await loadCurrentUserInfo();
        
        await loadGroups();
        // Set default dates
        const today = new Date();
        const lastMonth = new Date(today);
        lastMonth.setMonth(lastMonth.getMonth() - 1);
        const startDateInput = document.getElementById('startDate');
        const endDateInput = document.getElementById('endDate');
        if (startDateInput) startDateInput.valueAsDate = lastMonth;
        if (endDateInput) endDateInput.valueAsDate = today;
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

// Load groups
async function loadGroups() {
    try {
        // Admin uses admin endpoint to see all groups
        const endpoint = isAdmin ? '/admin/groups' : '/groups';
        const result = await apiCall(endpoint);
        if (result && result.response.ok) {
            groups = Array.isArray(result.data) ? result.data : [];
            populateGroupSelect();
        }
    } catch (error) {
        console.error('Error loading groups:', error);
    }
}

// Populate group select
function populateGroupSelect() {
    const select = document.getElementById('reportGroupId');
    select.innerHTML = '<option value="">Chọn nhóm</option>';
    
    groups.forEach(group => {
        const option = document.createElement('option');
        option.value = group.id;
        option.textContent = group.name;
        select.appendChild(option);
    });
}

// Generate financial report
async function generateFinancialReport() {
    const groupId = document.getElementById('reportGroupId').value;
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;

    if (!groupId || !startDate || !endDate) {
        showAlert('Vui lòng điền đầy đủ thông tin', 'error');
        return;
    }

    try {
        const result = await apiCall(`/reports/group/${groupId}?startDate=${startDate}&endDate=${endDate}`);
        if (result && result.response.ok) {
            displayReport(result.data);
        } else {
            showAlert('Không thể tạo báo cáo', 'error');
        }
    } catch (error) {
        console.error('Error generating report:', error);
        showAlert('Lỗi khi tạo báo cáo', 'error');
    }
}

// Display report
function displayReport(data) {
    const reportResult = document.getElementById('reportResult');
    reportResult.innerHTML = `
        <div class="table-section">
            <h3>Tổng quan</h3>
            <p><strong>Tổng chi phí:</strong> ${formatCurrency(data.totalExpenses || 0)}</p>
            <p><strong>Tổng đã trả:</strong> ${formatCurrency(data.totalPaid || 0)}</p>
            <p><strong>Còn lại:</strong> ${formatCurrency(data.totalRemaining || 0)}</p>
        </div>
    `;
}

