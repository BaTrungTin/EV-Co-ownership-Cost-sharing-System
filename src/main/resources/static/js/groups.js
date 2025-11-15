// Groups Management JavaScript

let groups = [];
let currentUser = null;
let allUsers = []; // Store all users for member selection
let isAdmin = false; // Will be set after loading user info

// Load groups on page load
document.addEventListener('DOMContentLoaded', async () => {
    if (checkAuth()) {
        // Load current user info
        await loadCurrentUserInfo();
        
        // Set isAdmin flag
        isAdmin = currentUser && currentUser.roles && Array.isArray(currentUser.roles) && currentUser.roles.includes('ADMIN');
        
        // Hide loading, show content
        const loadingState = document.getElementById('loadingState');
        const mainContent = document.getElementById('mainContent');
        if (loadingState) loadingState.style.display = 'none';
        if (mainContent) mainContent.style.display = 'block';
        
        await loadGroups();
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

// Load groups from API
async function loadGroups() {
    try {
        // Check if user is admin - if yes, use admin endpoint to see all groups
        let endpoint = '/groups';
        if (currentUser && currentUser.roles && Array.isArray(currentUser.roles) && currentUser.roles.includes('ADMIN')) {
            endpoint = '/admin/groups';
        }
        
        const result = await apiCall(endpoint);
        if (result && result.response && result.response.ok) {
            groups = Array.isArray(result.data) ? result.data : [];
            populateGroupsTable(groups);
        } else {
            const errorMsg = result?.data?.message || 'Không thể tải danh sách nhóm';
            console.error('Error loading groups:', errorMsg, result);
            showAlert(errorMsg, 'error');
            populateGroupsTable([]);
        }
    } catch (error) {
        console.error('Error loading groups:', error);
        showAlert('Lỗi khi tải danh sách nhóm: ' + error.message, 'error');
        populateGroupsTable([]);
    }
}

// Populate groups table
function populateGroupsTable(groups) {
    const tbody = document.getElementById('groupsTableBody');
    if (!tbody) {
        console.error('groupsTableBody not found');
        return;
    }
    
    tbody.innerHTML = '';

    if (!groups || groups.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" class="text-center py-4">Chưa có nhóm nào</td></tr>';
        return;
    }

    groups.forEach(group => {
        const tr = document.createElement('tr');
        // ownershipShares có thể là undefined hoặc null nếu chưa được load
        const ownershipShares = group.ownershipShares || [];
        const memberCount = Array.isArray(ownershipShares) ? ownershipShares.length : 0;
        const groupName = group.name || 'N/A';
        const groupId = group.id || 0;
        
        // Check if current user is admin
        const isAdmin = currentUser && currentUser.roles && Array.isArray(currentUser.roles) && currentUser.roles.includes('ADMIN');
        
        // Check if current user is the group owner
        let isGroupOwner = false;
        if (currentUser && group.createdBy) {
            // Handle both cases: createdBy as object with id, or just id
            if (typeof group.createdBy === 'object' && group.createdBy.id) {
                isGroupOwner = group.createdBy.id === currentUser.id;
            } else if (typeof group.createdBy === 'number') {
                isGroupOwner = group.createdBy === currentUser.id;
            }
        }
        
        // Admin có thể xem tất cả, nhưng chỉ group owner mới có quyền thêm/xóa
        // Nếu là admin nhưng không phải owner, chỉ hiển thị nút Xem
        const actionButtons = (isGroupOwner || isAdmin) 
            ? `<button class="btn btn-sm btn-primary me-1" onclick="viewGroup(${groupId})">Xem</button>
               ${isGroupOwner ? `<button class="btn btn-sm btn-success me-1" onclick="addMember(${groupId})">Thêm</button>
               <button class="btn btn-sm btn-danger" onclick="deleteGroup(${groupId}, '${escapeHtml(groupName)}')">Xóa</button>` : ''}`
            : `<button class="btn btn-sm btn-primary" onclick="viewGroup(${groupId})">Xem</button>`;
        
        tr.innerHTML = `
            <td>
              <div class="d-flex px-2 py-1">
                <div class="d-flex flex-column justify-content-center">
                  <h6 class="mb-0 text-sm">${groupId}</h6>
                </div>
              </div>
            </td>
            <td>
              <p class="text-xs font-weight-bold mb-0">${escapeHtml(groupName)}</p>
            </td>
            <td class="align-middle text-center text-sm">
              <span class="text-secondary text-xs font-weight-bold">${memberCount}</span>
            </td>
            <td class="align-middle text-center">
              ${actionButtons}
            </td>
        `;
        tbody.appendChild(tr);
    });
}

// Escape HTML to prevent XSS
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Show create group modal - sử dụng Bootstrap modal
function showCreateGroupModal() {
    const modalElement = document.getElementById('createGroupModal');
    if (modalElement) {
        const modal = new bootstrap.Modal(modalElement);
        modal.show();
        document.getElementById('createGroupForm').reset();
    }
}

// Create group - wait for DOM to be ready
document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('createGroupForm');
    if (form) {
        form.addEventListener('submit', handleCreateGroup);
    }
});

async function handleCreateGroup(e) {
    e.preventDefault();
    
    const formData = {
        name: document.getElementById('groupName').value.trim()
    };

    try {
        const result = await apiCall('/groups', {
            method: 'POST',
            body: JSON.stringify(formData)
        });

        if (result && result.response && result.response.ok) {
            // Hide Bootstrap modal
            const modalElement = document.getElementById('createGroupModal');
            if (modalElement) {
                const modal = bootstrap.Modal.getInstance(modalElement);
                if (modal) modal.hide();
            }
            document.getElementById('createGroupForm').reset();
            showAlert('Tạo nhóm thành công!', 'success');
            await loadGroups();
        } else {
            const errorMsg = result?.data?.message || result?.data?.error || 'Không thể tạo nhóm';
            console.error('Error creating group:', result);
            showAlert(errorMsg, 'error');
        }
    } catch (error) {
        console.error('Error creating group:', error);
        showAlert('Lỗi khi tạo nhóm', 'error');
    }
}

// View group details
window.viewGroup = async function viewGroup(groupId) {
    try {
        const result = await apiCall(`/groups/${groupId}`);
        if (result && result.response && result.response.ok) {
            const group = result.data;
            
            // Load members
            const membersResult = await apiCall(`/groups/${groupId}/members`);
            const members = membersResult && membersResult.response && membersResult.response.ok
                ? (Array.isArray(membersResult.data) ? membersResult.data : [])
                : [];
            
            displayGroupDetails(group, members);
        } else {
            showAlert('Không thể tải chi tiết nhóm', 'error');
        }
    } catch (error) {
        console.error('Error loading group:', error);
        showAlert('Lỗi khi tải chi tiết nhóm: ' + error.message, 'error');
    }
}

// Display group details in modal
function displayGroupDetails(group, members) {
    document.getElementById('viewGroupTitle').textContent = `Chi tiết nhóm: ${escapeHtml(group.name || 'N/A')}`;
    
    // Check if current user is the group owner
    const isGroupOwner = currentUser && group.createdBy && 
        (typeof group.createdBy === 'object' && group.createdBy.id 
            ? group.createdBy.id === currentUser.id
            : group.createdBy === currentUser.id);
    
    const ownerInfo = group.createdBy 
        ? (typeof group.createdBy === 'object' 
            ? (group.createdBy.fullName || group.createdBy.email || `ID: ${group.createdBy.id}`)
            : `ID: ${group.createdBy}`)
        : 'N/A';
    
    const body = document.getElementById('viewGroupBody');
    body.innerHTML = `
        <div class="mb-3">
          <strong>ID nhóm:</strong> ${group.id || '-'}
        </div>
        <div class="mb-3">
          <strong>Tên nhóm:</strong> ${escapeHtml(group.name || '-')}
        </div>
        <div class="mb-3">
          <strong>Trưởng nhóm:</strong> ${escapeHtml(ownerInfo)}
          ${isGroupOwner ? '<span class="badge bg-primary ms-2">Bạn</span>' : ''}
        </div>
        <hr>
        <div class="mb-3">
          <h6>Danh sách thành viên (${members.length})</h6>
          <div class="table-responsive" style="max-height: 300px; overflow-y: auto;">
            <table class="table table-sm">
              <thead class="table-light sticky-top">
                <tr>
                  <th>ID</th>
                  <th>Email</th>
                  <th>Họ tên</th>
                  <th>Tỉ lệ sở hữu</th>
                </tr>
              </thead>
              <tbody>
                ${members.length > 0 
                  ? members.map(m => {
                      const user = m.user || {};
                      const isOwner = group.createdBy && 
                          (typeof group.createdBy === 'object' && group.createdBy.id
                              ? group.createdBy.id === user.id
                              : group.createdBy === user.id);
                      return `
                        <tr ${isOwner ? 'class="table-warning"' : ''}>
                          <td>${user.id || '-'}</td>
                          <td>${escapeHtml(user.email || '-')}</td>
                          <td>${escapeHtml(user.fullName || '-')} ${isOwner ? '<span class="badge bg-primary">Trưởng nhóm</span>' : ''}</td>
                          <td>${(m.percentage * 100).toFixed(2)}%</td>
                        </tr>
                      `;
                    }).join('')
                  : '<tr><td colspan="4" class="text-center">Chưa có thành viên nào</td></tr>'}
              </tbody>
            </table>
          </div>
        </div>
        ${isGroupOwner 
          ? `<div class="mb-3">
              <button class="btn btn-warning btn-sm" onclick="showTransferOwnershipModal(${group.id})">
                Chuyển quyền trưởng nhóm
              </button>
            </div>`
          : ''}
    `;
    
    const modal = new bootstrap.Modal(document.getElementById('viewGroupModal'));
    modal.show();
}

// Show transfer ownership modal
window.showTransferOwnershipModal = async function(groupId) {
    window.currentTransferGroupId = groupId;
    
    try {
        // Load members of the group
        const membersResult = await apiCall(`/groups/${groupId}/members`);
        const members = membersResult && membersResult.response && membersResult.response.ok
            ? (Array.isArray(membersResult.data) ? membersResult.data : [])
            : [];
        
        // Populate select with members (excluding current owner)
        const select = document.getElementById('newOwnerSelect');
        select.innerHTML = '<option value="">Chọn thành viên...</option>';
        
        members.forEach(member => {
            const user = member.user || {};
            // Don't show current user as option
            if (user.id && user.id !== currentUser?.id) {
                const option = document.createElement('option');
                option.value = user.id;
                option.textContent = `${user.id} - ${user.email || ''} (${user.fullName || 'N/A'})`;
                select.appendChild(option);
            }
        });
        
        if (select.options.length === 1) {
            select.innerHTML = '<option value="">Không có thành viên nào khác</option>';
            select.disabled = true;
        } else {
            select.disabled = false;
        }
        
        const modal = new bootstrap.Modal(document.getElementById('transferOwnershipModal'));
        modal.show();
    } catch (error) {
        console.error('Error loading members:', error);
        showAlert('Lỗi khi tải danh sách thành viên', 'error');
    }
};

// Handle transfer ownership
window.handleTransferOwnership = async function() {
    const groupId = window.currentTransferGroupId;
    const newOwnerId = document.getElementById('newOwnerSelect').value;
    
    if (!groupId || !newOwnerId) {
        showAlert('Vui lòng chọn thành viên', 'error');
        return;
    }
    
    if (!confirm('Bạn có chắc muốn chuyển quyền trưởng nhóm? Sau khi chuyển, bạn sẽ mất quyền quản lý nhóm này.')) {
        return;
    }
    
    try {
        const result = await apiCall(`/groups/${groupId}/transfer-ownership`, {
            method: 'PUT',
            body: JSON.stringify({
                newOwnerId: parseInt(newOwnerId)
            })
        });
        
        if (result && result.response && result.response.ok) {
            showAlert('Chuyển quyền trưởng nhóm thành công!', 'success');
            const modal = bootstrap.Modal.getInstance(document.getElementById('transferOwnershipModal'));
            if (modal) modal.hide();
            const viewModal = bootstrap.Modal.getInstance(document.getElementById('viewGroupModal'));
            if (viewModal) viewModal.hide();
            await loadGroups();
        } else {
            const errorMsg = result?.data?.message || 'Không thể chuyển quyền trưởng nhóm';
            showAlert(errorMsg, 'error');
        }
    } catch (error) {
        console.error('Error transferring ownership:', error);
        showAlert('Lỗi khi chuyển quyền trưởng nhóm: ' + error.message, 'error');
    }
};

// Add member to group
async function addMember(groupId) {
    // Store groupId for later use
    window.currentAddMemberGroupId = groupId;
    
    // Show modal
    const modalElement = document.getElementById('addMemberModal');
    if (modalElement) {
        const modal = new bootstrap.Modal(modalElement);
        modal.show();
        
        // Reset form
        document.getElementById('memberUserId').value = '';
        document.getElementById('memberUserIdManual').value = '';
        document.getElementById('memberPercentage').value = '';
        
        // Load users list
        await loadUsersForMember();
    }
}

// Load users for member selection
async function loadUsersForMember() {
    try {
        // Check if admin to use admin endpoint
        const endpoint = isAdmin ? '/admin/users' : '/users';
        const result = await apiCall(endpoint);
        
        if (result && result.response && result.response.ok) {
            allUsers = Array.isArray(result.data) ? result.data : [];
            populateUsersListForMember();
            populateUserSelect();
        } else {
            console.error('Error loading users:', result);
            document.getElementById('usersListForMember').innerHTML = 
                '<tr><td colspan="3" class="text-center text-danger">Không thể tải danh sách người dùng</td></tr>';
        }
    } catch (error) {
        console.error('Error loading users:', error);
        document.getElementById('usersListForMember').innerHTML = 
            '<tr><td colspan="3" class="text-center text-danger">Lỗi khi tải danh sách người dùng</td></tr>';
    }
}

// Populate users list in modal
function populateUsersListForMember() {
    const tbody = document.getElementById('usersListForMember');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    if (!allUsers || allUsers.length === 0) {
        tbody.innerHTML = '<tr><td colspan="3" class="text-center">Không có người dùng nào</td></tr>';
        return;
    }
    
    allUsers.forEach(user => {
        const tr = document.createElement('tr');
        tr.style.cursor = 'pointer';
        tr.onclick = () => {
            document.getElementById('memberUserId').value = user.id;
            document.getElementById('memberUserIdManual').value = user.id;
        };
        tr.innerHTML = `
            <td><strong>${user.id || '-'}</strong></td>
            <td>${escapeHtml(user.email || '-')}</td>
            <td>${escapeHtml(user.fullName || '-')}</td>
        `;
        tbody.appendChild(tr);
    });
}

// Populate user select dropdown
function populateUserSelect() {
    const select = document.getElementById('memberUserId');
    if (!select) return;
    
    select.innerHTML = '<option value="">Chọn người dùng...</option>';
    
    allUsers.forEach(user => {
        const option = document.createElement('option');
        option.value = user.id;
        option.textContent = `ID: ${user.id} - ${user.email} (${user.fullName || 'N/A'})`;
        select.appendChild(option);
    });
    
    // Update manual input when select changes
    select.addEventListener('change', (e) => {
        document.getElementById('memberUserIdManual').value = e.target.value || '';
    });
}

// Handle add member from modal
function handleAddMember() {
    const groupId = window.currentAddMemberGroupId;
    if (!groupId) {
        showAlert('Lỗi: Không tìm thấy ID nhóm', 'error');
        return;
    }
    
    // Get userId from select or manual input
    const userIdSelect = document.getElementById('memberUserId').value;
    const userIdManual = document.getElementById('memberUserIdManual').value;
    const userId = userIdSelect || userIdManual;
    
    const percentage = parseFloat(document.getElementById('memberPercentage').value);
    
    if (!userId) {
        showAlert('Vui lòng chọn hoặc nhập ID người dùng', 'error');
        return;
    }
    
    if (!percentage || isNaN(percentage) || percentage <= 0 || percentage > 1) {
        showAlert('Vui lòng nhập tỉ lệ sở hữu hợp lệ (0-1)', 'error');
        return;
    }
    
    // Hide modal
    const modalElement = document.getElementById('addMemberModal');
    if (modalElement) {
        const modal = bootstrap.Modal.getInstance(modalElement);
        if (modal) modal.hide();
    }
    
    addMemberToGroup(groupId, userId, percentage);
}


// Add member to group API call
async function addMemberToGroup(groupId, userId, percentage) {
    try {
        const result = await apiCall(`/groups/${groupId}/members`, {
            method: 'POST',
            body: JSON.stringify({
                userId: parseInt(userId),
                percentage: percentage
            })
        });

        if (result && result.response && result.response.ok) {
            showAlert('Thêm thành viên thành công!', 'success');
            
            // Force reload groups list to get updated ownershipShares
            // Clear current groups array first
            groups = [];
            await loadGroups();
            
            // Also update the specific group in the array if it exists
            try {
                const groupResult = await apiCall(`/groups/${groupId}`);
                if (groupResult && groupResult.response && groupResult.response.ok) {
                    const updatedGroup = groupResult.data;
                    const index = groups.findIndex(g => g.id === groupId);
                    if (index !== -1) {
                        groups[index] = updatedGroup;
                        // Trigger load ownershipShares if needed
                        if (updatedGroup.ownershipShares) {
                            updatedGroup.ownershipShares.size; // Trigger load
                        }
                    }
                    // Repopulate table with updated data
                    populateGroupsTable(groups);
                }
            } catch (error) {
                console.error('Error reloading group:', error);
            }
            
            // If view group modal is open, reload it
            const viewGroupModal = document.getElementById('viewGroupModal');
            if (viewGroupModal && bootstrap.Modal.getInstance(viewGroupModal)) {
                // Reload group details
                await viewGroup(groupId);
            }
        } else {
            const errorMsg = result?.data?.message || 'Không thể thêm thành viên';
            showAlert(errorMsg, 'error');
        }
    } catch (error) {
        console.error('Error adding member:', error);
        showAlert('Lỗi khi thêm thành viên: ' + error.message, 'error');
    }
}

// Delete group
function deleteGroup(groupId, groupName) {
    if (!confirm(`Bạn có chắc chắn muốn xóa nhóm "${groupName}"?\n\nLưu ý: Chỉ có thể xóa nhóm khi không có xe, chi phí, booking, vote, hoặc quỹ chung.`)) {
        return;
    }

    deleteGroupAPI(groupId);
}

// Delete group API call
async function deleteGroupAPI(groupId) {
    try {
        const result = await apiCall(`/groups/${groupId}`, {
            method: 'DELETE'
        });

        if (result && result.response && (result.response.ok || result.response.status === 204)) {
            showAlert('Xóa nhóm thành công!', 'success');
            await loadGroups();
        } else {
            const errorMsg = result?.data?.message || result?.data?.error || 'Không thể xóa nhóm';
            console.error('Error deleting group:', result);
            showAlert(errorMsg, 'error');
        }
    } catch (error) {
        console.error('Error deleting group:', error);
        showAlert('Lỗi khi xóa nhóm: ' + error.message, 'error');
    }
}

