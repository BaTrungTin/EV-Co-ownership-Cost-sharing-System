// Funds Management JavaScript

let funds = [];
let groups = [];
let currentUser = null;
let allFunds = []; // All funds from all groups
let currentFundId = null; // For transaction modal
let currentTransactionType = null; // 'DEPOSIT' or 'WITHDRAW'

// Load data on page load
document.addEventListener('DOMContentLoaded', async () => {
    if (checkAuth()) {
        await loadCurrentUserInfo();
        await loadGroups();
        await loadAllFunds();
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

// Load groups for filter and create fund
async function loadGroups() {
    try {
        const endpoint = currentUser && currentUser.roles && Array.isArray(currentUser.roles) && currentUser.roles.includes('ADMIN') 
            ? '/admin/groups' 
            : '/groups';
        const result = await apiCall(endpoint);
        if (result && result.response && result.response.ok) {
            groups = Array.isArray(result.data) ? result.data : [];
            populateGroupFilter();
            populateFundGroupSelect();
        }
    } catch (error) {
        console.error('Error loading groups:', error);
    }
}

// Load all funds from user's groups
async function loadAllFunds() {
    try {
        allFunds = [];
        
        // Load funds from each group
        for (const group of groups) {
            try {
                const result = await apiCall(`/funds/group/${group.id}`);
                if (result && result.response && result.response.ok) {
                    const groupFunds = Array.isArray(result.data) ? result.data : [];
                    allFunds = allFunds.concat(groupFunds);
                }
            } catch (error) {
                console.error(`Error loading funds for group ${group.id}:`, error);
            }
        }
        
        funds = allFunds;
        populateFundsTable(funds);
    } catch (error) {
        console.error('Error loading funds:', error);
        showAlert('Lỗi khi tải danh sách quỹ', 'error');
    }
}

// Populate group filter dropdown
function populateGroupFilter() {
    const select = document.getElementById('groupFilter');
    if (!select) return;
    
    select.innerHTML = '<option value="">Tất cả nhóm</option>';
    groups.forEach(group => {
        const option = document.createElement('option');
        option.value = group.id;
        option.textContent = group.name || `Nhóm ${group.id}`;
        select.appendChild(option);
    });
}

// Populate group select in create fund modal
function populateFundGroupSelect() {
    const select = document.getElementById('fundGroupId');
    if (!select) return;
    
    select.innerHTML = '<option value="">Chọn nhóm</option>';
    groups.forEach(group => {
        const option = document.createElement('option');
        option.value = group.id;
        option.textContent = group.name || `Nhóm ${group.id}`;
        select.appendChild(option);
    });
}

// Filter funds by group
function filterFunds() {
    const groupId = document.getElementById('groupFilter').value;
    if (!groupId) {
        funds = allFunds;
    } else {
        funds = allFunds.filter(fund => fund.group && fund.group.id == groupId);
    }
    populateFundsTable(funds);
}

// Populate funds table
function populateFundsTable(funds) {
    const tbody = document.getElementById('fundsTableBody');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    if (!funds || funds.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center py-4">Chưa có quỹ nào</td></tr>';
        return;
    }
    
    funds.forEach(fund => {
        const tr = document.createElement('tr');
        const fundTypeText = getFundTypeText(fund.fundType);
        const balance = formatCurrency(fund.balance || 0);
        const groupName = fund.group ? (fund.group.name || `Nhóm ${fund.group.id}`) : '-';
        
        tr.innerHTML = `
            <td>
              <div class="d-flex px-2 py-1">
                <div class="d-flex flex-column justify-content-center">
                  <h6 class="mb-0 text-sm">${fund.id || '-'}</h6>
                </div>
              </div>
            </td>
            <td>
              <p class="text-xs font-weight-bold mb-0">${escapeHtml(fundTypeText)}</p>
            </td>
            <td class="align-middle text-center">
              <span class="text-secondary text-xs font-weight-bold">${escapeHtml(groupName)}</span>
            </td>
            <td class="align-middle text-center">
              <span class="text-success text-xs font-weight-bold">${balance}</span>
            </td>
            <td class="align-middle text-center">
              <span class="text-secondary text-xs">${escapeHtml(fund.description ? fund.description.substring(0, 50) + '...' : '-')}</span>
            </td>
            <td class="align-middle text-center">
              <button class="btn btn-sm btn-primary me-1" onclick="viewFund(${fund.id})">Xem</button>
              <button class="btn btn-sm btn-success me-1" onclick="showTransactionModal(${fund.id}, 'DEPOSIT')">Nộp</button>
              <button class="btn btn-sm btn-warning" onclick="showTransactionModal(${fund.id}, 'WITHDRAW')">Rút</button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

// Get fund type text in Vietnamese
function getFundTypeText(type) {
    const typeMap = {
        'MAINTENANCE_RESERVE': 'Quỹ bảo dưỡng',
        'EMERGENCY': 'Phí dự phòng',
        'OTHER': 'Khác'
    };
    return typeMap[type] || type;
}

// Show create fund modal
window.showCreateFundModal = function() {
    const modal = new bootstrap.Modal(document.getElementById('createFundModal'));
    modal.show();
};

// Handle create fund
window.handleCreateFund = async function() {
    const groupId = document.getElementById('fundGroupId').value;
    const fundType = document.getElementById('fundType').value;
    const description = document.getElementById('fundDescription').value;
    
    if (!groupId || !fundType) {
        showAlert('Vui lòng điền đầy đủ thông tin', 'error');
        return;
    }
    
    try {
        const result = await apiCall('/funds', {
            method: 'POST',
            body: JSON.stringify({
                groupId: parseInt(groupId),
                fundType: fundType,
                description: description || ''
            })
        });
        
        if (result && result.response && result.response.ok) {
            showAlert('Tạo quỹ thành công!', 'success');
            const modal = bootstrap.Modal.getInstance(document.getElementById('createFundModal'));
            if (modal) modal.hide();
            document.getElementById('createFundForm').reset();
            await loadAllFunds();
        } else {
            const errorMsg = result?.data?.message || 'Không thể tạo quỹ';
            showAlert(errorMsg, 'error');
        }
    } catch (error) {
        console.error('Error creating fund:', error);
        showAlert('Lỗi khi tạo quỹ: ' + error.message, 'error');
    }
};

// View fund details
window.viewFund = async function(fundId) {
    try {
        // Find fund in allFunds
        const fund = allFunds.find(f => f.id === fundId);
        if (!fund) {
            showAlert('Không tìm thấy quỹ', 'error');
            return;
        }
        
        // Load transactions
        const transactionsResult = await apiCall(`/funds/${fundId}/transactions`);
        const transactions = transactionsResult && transactionsResult.response && transactionsResult.response.ok
            ? (Array.isArray(transactionsResult.data) ? transactionsResult.data : [])
            : [];
        
        displayFundDetails(fund, transactions);
    } catch (error) {
        console.error('Error loading fund:', error);
        showAlert('Lỗi khi tải chi tiết quỹ', 'error');
    }
};

// Display fund details in modal
function displayFundDetails(fund, transactions) {
    document.getElementById('viewFundTitle').textContent = `Quỹ #${fund.id} - ${getFundTypeText(fund.fundType)}`;
    
    const body = document.getElementById('viewFundBody');
    body.innerHTML = `
        <div class="mb-3">
          <strong>Nhóm:</strong> ${fund.group ? (fund.group.name || `Nhóm ${fund.group.id}`) : '-'}
        </div>
        <div class="mb-3">
          <strong>Loại quỹ:</strong> ${getFundTypeText(fund.fundType)}
        </div>
        <div class="mb-3">
          <strong>Số dư hiện tại:</strong> <span class="text-success font-weight-bold">${formatCurrency(fund.balance || 0)}</span>
        </div>
        <div class="mb-3">
          <strong>Mô tả:</strong>
          <p>${escapeHtml(fund.description || '-')}</p>
        </div>
        <div class="mb-3">
          <strong>Ngày tạo:</strong> ${formatDate(fund.createdAt)}
        </div>
        <hr>
        <div class="mb-3">
          <h6>Lịch sử giao dịch</h6>
          <div class="table-responsive" style="max-height: 400px; overflow-y: auto;">
            <table class="table table-sm">
              <thead class="table-light sticky-top">
                <tr>
                  <th>Ngày</th>
                  <th>Loại</th>
                  <th>Số tiền</th>
                  <th>Người thực hiện</th>
                  <th>Mô tả</th>
                </tr>
              </thead>
              <tbody>
                ${transactions.length > 0 
                  ? transactions.map(t => `
                    <tr>
                      <td>${formatDateTime(t.transactionDate)}</td>
                      <td><span class="badge bg-${t.type === 'DEPOSIT' ? 'success' : 'warning'}">${t.type === 'DEPOSIT' ? 'Nộp' : 'Rút'}</span></td>
                      <td>${formatCurrency(t.amount || 0)}</td>
                      <td>${t.createdBy ? (t.createdBy.fullName || t.createdBy.email) : '-'}</td>
                      <td>${escapeHtml(t.description || '-')}</td>
                    </tr>
                  `).join('')
                  : '<tr><td colspan="5" class="text-center">Chưa có giao dịch nào</td></tr>'}
              </tbody>
            </table>
          </div>
        </div>
    `;
    
    const modal = new bootstrap.Modal(document.getElementById('viewFundModal'));
    modal.show();
}

// Show transaction modal
window.showTransactionModal = function(fundId, type) {
    currentFundId = fundId;
    currentTransactionType = type;
    
    const modal = new bootstrap.Modal(document.getElementById('transactionModal'));
    const title = document.getElementById('transactionModalTitle');
    const submitBtn = document.getElementById('transactionSubmitBtn');
    const referenceDiv = document.getElementById('transactionReferenceDiv');
    
    if (type === 'DEPOSIT') {
        title.textContent = 'Nộp tiền vào quỹ';
        submitBtn.textContent = 'Nộp tiền';
        submitBtn.className = 'btn btn-success';
        referenceDiv.style.display = 'none';
    } else {
        title.textContent = 'Rút tiền từ quỹ';
        submitBtn.textContent = 'Rút tiền';
        submitBtn.className = 'btn btn-warning';
        referenceDiv.style.display = 'block';
    }
    
    document.getElementById('transactionForm').reset();
    modal.show();
};

// Handle transaction
window.handleTransaction = async function() {
    const amount = parseFloat(document.getElementById('transactionAmount').value);
    const description = document.getElementById('transactionDescription').value;
    const reference = document.getElementById('transactionReference').value;
    
    if (!amount || amount <= 0) {
        showAlert('Vui lòng nhập số tiền hợp lệ', 'error');
        return;
    }
    
    try {
        let result;
        if (currentTransactionType === 'DEPOSIT') {
            result = await apiCall(`/funds/${currentFundId}/deposit?amount=${amount}${description ? '&description=' + encodeURIComponent(description) : ''}`, {
                method: 'POST'
            });
        } else {
            result = await apiCall(`/funds/${currentFundId}/withdraw?amount=${amount}${description ? '&description=' + encodeURIComponent(description) : ''}${reference ? '&reference=' + encodeURIComponent(reference) : ''}`, {
                method: 'POST'
            });
        }
        
        if (result && result.response && result.response.ok) {
            showAlert(`${currentTransactionType === 'DEPOSIT' ? 'Nộp' : 'Rút'} tiền thành công!`, 'success');
            const modal = bootstrap.Modal.getInstance(document.getElementById('transactionModal'));
            if (modal) modal.hide();
            document.getElementById('transactionForm').reset();
            await loadAllFunds();
        } else {
            const errorMsg = result?.data?.message || `Không thể ${currentTransactionType === 'DEPOSIT' ? 'nộp' : 'rút'} tiền`;
            showAlert(errorMsg, 'error');
        }
    } catch (error) {
        console.error('Error processing transaction:', error);
        showAlert(`Lỗi khi ${currentTransactionType === 'DEPOSIT' ? 'nộp' : 'rút'} tiền: ` + error.message, 'error');
    }
};

// Helper functions
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function formatCurrency(amount) {
    if (amount === null || amount === undefined) return '0 ₫';
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
}

function formatDate(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN');
}

function formatDateTime(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleString('vi-VN');
}
