// Contracts Management JavaScript

let contracts = [];
let groups = [];
let currentUser = null;
let isAdmin = false;

// Load data on page load
document.addEventListener('DOMContentLoaded', async () => {
    if (checkAuth()) {
        await loadCurrentUserInfo();
        await loadGroups();
        await loadContracts();
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
        const endpoint = isAdmin ? '/admin/groups' : '/groups';
        const result = await apiCall(endpoint);
        if (result && result.response && result.response.ok) {
            groups = Array.isArray(result.data) ? result.data : [];
            populateGroupSelect();
        }
    } catch (error) {
        console.error('Error loading groups:', error);
    }
}

// Populate group select
function populateGroupSelect() {
    const select = document.getElementById('contractGroupId');
    if (!select) return;
    
    select.innerHTML = '<option value="">Chọn nhóm</option>';
    groups.forEach(group => {
        const option = document.createElement('option');
        option.value = group.id;
        option.textContent = group.name || `Nhóm ${group.id}`;
        select.appendChild(option);
    });
}

// Load contracts
async function loadContracts() {
    try {
        contracts = [];
        
        // Load contracts from all groups
        for (const group of groups) {
            try {
                const result = await apiCall(`/contracts/group/${group.id}`);
                if (result && result.response && result.response.ok) {
                    const groupContracts = Array.isArray(result.data) ? result.data : [];
                    contracts = contracts.concat(groupContracts);
                } else if (result && result.response) {
                    // Handle specific error cases
                    if (result.response.status === 500) {
                        console.error(`Server error loading contracts for group ${group.id}:`, result.data);
                    } else if (result.response.status === 404) {
                        console.warn(`Group ${group.id} not found`);
                    } else {
                        console.error(`Error loading contracts for group ${group.id}:`, result.data);
                    }
                }
            } catch (error) {
                console.error(`Error loading contracts for group ${group.id}:`, error);
                // Continue with other groups even if one fails
            }
        }
        
        populateContractsTable(contracts);
    } catch (error) {
        console.error('Error loading contracts:', error);
        showAlert('Lỗi khi tải danh sách hợp đồng: ' + (error.message || 'Unknown error'), 'error');
    }
}

// Populate contracts table
function populateContractsTable(contracts) {
    const tbody = document.getElementById('contractsTableBody');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    if (!contracts || contracts.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center py-4">Chưa có hợp đồng nào</td></tr>';
        return;
    }
    
    contracts.forEach(contract => {
        const tr = document.createElement('tr');
        const status = contract.status || 'DRAFT';
        const statusBadge = status === 'DRAFT' ? 'secondary' :
                           status === 'PENDING' ? 'warning' :
                           status === 'SIGNED' ? 'success' :
                           status === 'EXPIRED' ? 'danger' :
                           status === 'CANCELLED' ? 'dark' : 'secondary';
        
        const groupName = contract.group ? (contract.group.name || `ID: ${contract.group.id}`) : 'N/A';
        
        tr.innerHTML = `
            <td>
              <div class="d-flex px-2 py-1">
                <div class="d-flex flex-column justify-content-center">
                  <h6 class="mb-0 text-sm">${contract.id || '-'}</h6>
                </div>
              </div>
            </td>
            <td>
              <p class="text-xs font-weight-bold mb-0">${escapeHtml(contract.contractNo || '-')}</p>
            </td>
            <td>
              <p class="text-xs font-weight-bold mb-0">${escapeHtml(groupName)}</p>
            </td>
            <td class="align-middle text-center text-sm">
              <span class="text-secondary text-xs font-weight-bold">${formatDate(contract.startDate)}</span>
            </td>
            <td class="align-middle text-center text-sm">
              <span class="text-secondary text-xs font-weight-bold">${formatDate(contract.endDate)}</span>
            </td>
            <td class="align-middle text-center">
              <span class="badge badge-sm bg-${statusBadge}">${status}</span>
            </td>
            <td class="align-middle text-center">
              <button class="btn btn-sm btn-primary me-1" onclick="viewContract(${contract.id})">Xem</button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

// View contract details
window.viewContract = async function(contractId) {
    try {
        const result = await apiCall(`/contracts/${contractId}`);
        if (result && result.response && result.response.ok) {
            const contract = result.data;
            displayContractDetails(contract);
        } else {
            showAlert('Không thể tải chi tiết hợp đồng', 'error');
        }
    } catch (error) {
        console.error('Error loading contract:', error);
        showAlert('Lỗi khi tải chi tiết hợp đồng: ' + error.message, 'error');
    }
};

// Display contract details in modal
function displayContractDetails(contract) {
    document.getElementById('viewContractTitle').textContent = `Chi tiết hợp đồng: ${escapeHtml(contract.contractNo || 'N/A')}`;
    
    const groupName = contract.group ? (contract.group.name || `ID: ${contract.group.id}`) : 'N/A';
    const createdBy = contract.createdBy ? (contract.createdBy.fullName || contract.createdBy.email || `ID: ${contract.createdBy.id}`) : 'N/A';
    const status = contract.status || 'DRAFT';
    const statusBadge = status === 'DRAFT' ? 'secondary' :
                        status === 'PENDING' ? 'warning' :
                        status === 'SIGNED' ? 'success' :
                        status === 'EXPIRED' ? 'danger' :
                        status === 'CANCELLED' ? 'dark' : 'secondary';
    
    const body = document.getElementById('viewContractBody');
    body.innerHTML = `
        <div class="mb-3">
          <strong>ID hợp đồng:</strong> ${contract.id || '-'}
        </div>
        <div class="mb-3">
          <strong>Số hợp đồng:</strong> ${escapeHtml(contract.contractNo || '-')}
        </div>
        <div class="mb-3">
          <strong>Nhóm:</strong> ${escapeHtml(groupName)}
        </div>
        <div class="mb-3">
          <strong>Ngày bắt đầu:</strong> ${formatDate(contract.startDate)}
        </div>
        <div class="mb-3">
          <strong>Ngày kết thúc:</strong> ${formatDate(contract.endDate)}
        </div>
        <div class="mb-3">
          <strong>Trạng thái:</strong> <span class="badge bg-${statusBadge}">${status}</span>
        </div>
        <div class="mb-3">
          <strong>Người tạo:</strong> ${escapeHtml(createdBy)}
        </div>
        <div class="mb-3">
          <strong>Ngày tạo:</strong> ${formatDate(contract.createdAt)}
        </div>
        ${contract.signedAt ? `<div class="mb-3"><strong>Ngày ký:</strong> ${formatDate(contract.signedAt)}</div>` : ''}
        ${contract.terms ? `<div class="mb-3"><strong>Điều khoản:</strong><br><div class="p-3 bg-light rounded">${escapeHtml(contract.terms)}</div></div>` : ''}
        ${contract.documentUrl ? `<div class="mb-3"><strong>Tài liệu:</strong> <a href="${escapeHtml(contract.documentUrl)}" target="_blank">Xem PDF</a></div>` : ''}
    `;
    
    // Show sign button only if contract is not signed and user is member of group
    const signBtn = document.getElementById('signContractBtn');
    if (signBtn) {
        if (status === 'PENDING' && !isAdmin) {
            signBtn.style.display = 'block';
            signBtn.setAttribute('data-contract-id', contract.id);
        } else {
            signBtn.style.display = 'none';
        }
    }
    
    const modal = new bootstrap.Modal(document.getElementById('viewContractModal'));
    modal.show();
}

// Handle sign contract
window.handleSignContract = async function() {
    const signBtn = document.getElementById('signContractBtn');
    const contractId = signBtn ? signBtn.getAttribute('data-contract-id') : null;
    
    if (!contractId) {
        showAlert('Lỗi: Không tìm thấy ID hợp đồng', 'error');
        return;
    }
    
    if (!confirm('Bạn có chắc muốn ký hợp đồng này?')) {
        return;
    }
    
    try {
        const result = await apiCall(`/contracts/${contractId}/sign`, {
            method: 'PUT'
        });
        
        if (result && result.response && result.response.ok) {
            showAlert('Ký hợp đồng thành công!', 'success');
            const modal = bootstrap.Modal.getInstance(document.getElementById('viewContractModal'));
            if (modal) modal.hide();
            await loadContracts();
        } else {
            const errorMsg = result?.data?.message || 'Không thể ký hợp đồng';
            showAlert(errorMsg, 'error');
        }
    } catch (error) {
        console.error('Error signing contract:', error);
        showAlert('Lỗi khi ký hợp đồng: ' + error.message, 'error');
    }
};

// Handle create contract form
document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('createContractForm');
    if (form) {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            
            const groupId = document.getElementById('contractGroupId').value;
            const contractNo = document.getElementById('contractNo').value;
            const startDate = document.getElementById('contractStartDate').value;
            const endDate = document.getElementById('contractEndDate').value;
            const terms = document.getElementById('contractTerms').value;
            const documentUrl = document.getElementById('contractDocumentUrl').value;
            
            if (!groupId || !contractNo || !startDate || !endDate) {
                showAlert('Vui lòng điền đầy đủ thông tin bắt buộc', 'error');
                return;
            }
            
            const start = new Date(startDate);
            const end = new Date(endDate);
            if (end <= start) {
                showAlert('Ngày kết thúc phải sau ngày bắt đầu', 'error');
                return;
            }
            
            try {
                const result = await apiCall('/contracts', {
                    method: 'POST',
                    body: JSON.stringify({
                        groupId: parseInt(groupId),
                        contractNo: contractNo,
                        startDate: startDate,
                        endDate: endDate,
                        terms: terms || null,
                        documentUrl: documentUrl || null
                    })
                });
                
                if (result && result.response && result.response.ok) {
                    const modal = bootstrap.Modal.getInstance(document.getElementById('createContractModal'));
                    if (modal) modal.hide();
                    form.reset();
                    showAlert('Tạo hợp đồng thành công!', 'success');
                    await loadContracts();
                } else {
                    let errorMsg = 'Không thể tạo hợp đồng';
                    if (result && result.data) {
                        if (result.data.message) {
                            errorMsg = result.data.message;
                        } else if (result.data.details) {
                            const details = result.data.details;
                            if (typeof details === 'object') {
                                const errorMessages = Object.values(details).join(', ');
                                errorMsg = errorMessages || errorMsg;
                            }
                        }
                    }
                    // Check for 409 Conflict (contract number already exists)
                    if (result && result.response && result.response.status === 409) {
                        errorMsg = 'Số hợp đồng đã tồn tại. Vui lòng sử dụng số hợp đồng khác.';
                    }
                    showAlert(errorMsg, 'error');
                }
            } catch (error) {
                console.error('Error creating contract:', error);
                showAlert('Lỗi khi tạo hợp đồng: ' + error.message, 'error');
            }
        });
    }
});

// Helper functions
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function formatDate(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN');
}

