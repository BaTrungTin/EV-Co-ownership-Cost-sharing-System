// Votes Management JavaScript

let votes = [];
let groups = [];
let currentUser = null;
let allVotes = []; // All votes from all groups

// Load data on page load
document.addEventListener('DOMContentLoaded', async () => {
    if (checkAuth()) {
        await loadCurrentUserInfo();
        await loadGroups();
        await loadAllVotes();
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

// Load groups for filter and create vote
async function loadGroups() {
    try {
        const endpoint = currentUser && currentUser.roles && Array.isArray(currentUser.roles) && currentUser.roles.includes('ADMIN') 
            ? '/admin/groups' 
            : '/groups';
        const result = await apiCall(endpoint);
        if (result && result.response && result.response.ok) {
            groups = Array.isArray(result.data) ? result.data : [];
            populateGroupFilter();
            populateVoteGroupSelect();
        }
    } catch (error) {
        console.error('Error loading groups:', error);
    }
}

// Load all votes from user's groups
async function loadAllVotes() {
    try {
        allVotes = [];
        
        // Check if groups are loaded
        if (!groups || groups.length === 0) {
            console.warn('No groups available, loading groups first...');
            await loadGroups();
        }
        
        // Load votes from each group
        for (const group of groups) {
            try {
                const result = await apiCall(`/votes/group/${group.id}`);
                if (result && result.response && result.response.ok) {
                    const groupVotes = Array.isArray(result.data) ? result.data : [];
                    allVotes = allVotes.concat(groupVotes);
                } else {
                    console.warn(`Failed to load votes for group ${group.id}:`, result);
                }
            } catch (error) {
                console.error(`Error loading votes for group ${group.id}:`, error);
            }
        }
        
        // Remove duplicates based on vote ID
        const uniqueVotes = [];
        const voteIds = new Set();
        for (const vote of allVotes) {
            if (vote.id && !voteIds.has(vote.id)) {
                voteIds.add(vote.id);
                uniqueVotes.push(vote);
            }
        }
        
        votes = uniqueVotes;
        allVotes = uniqueVotes;
        populateVotesTable(votes);
    } catch (error) {
        console.error('Error loading votes:', error);
        showAlert('Lỗi khi tải danh sách phiếu bầu', 'error');
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

// Populate group select in create vote modal
function populateVoteGroupSelect() {
    const select = document.getElementById('voteGroupId');
    if (!select) return;
    
    select.innerHTML = '<option value="">Chọn nhóm</option>';
    groups.forEach(group => {
        const option = document.createElement('option');
        option.value = group.id;
        option.textContent = group.name || `Nhóm ${group.id}`;
        select.appendChild(option);
    });
}

// Filter votes by group
function filterVotes() {
    const groupId = document.getElementById('groupFilter').value;
    if (!groupId) {
        votes = [...allVotes]; // Create a copy
    } else {
        votes = allVotes.filter(vote => {
            // Handle both cases: group as object with id, or just id
            if (vote.group) {
                if (typeof vote.group === 'object' && vote.group.id) {
                    return vote.group.id == groupId;
                } else if (typeof vote.group === 'number') {
                    return vote.group == groupId;
                }
            }
            return false;
        });
    }
    populateVotesTable(votes);
}

// Populate votes table
function populateVotesTable(votes) {
    const tbody = document.getElementById('votesTableBody');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    if (!votes || votes.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center py-4">Chưa có phiếu bầu nào</td></tr>';
        return;
    }
    
    votes.forEach(vote => {
        const tr = document.createElement('tr');
        const status = vote.status || 'OPEN';
        const statusBadge = status === 'OPEN' ? 'success' : 
                          status === 'CLOSED' ? 'secondary' : 
                          status === 'APPROVED' ? 'primary' : 
                          status === 'REJECTED' ? 'danger' : 'secondary';
        
        const topicText = getTopicText(vote.topic);
        const deadline = formatDateTime(vote.deadline);
        const groupName = vote.group ? (vote.group.name || `Nhóm ${vote.group.id}`) : '-';
        
        tr.innerHTML = `
            <td>
              <div class="d-flex px-2 py-1">
                <div class="d-flex flex-column justify-content-center">
                  <h6 class="mb-0 text-sm">${vote.id || '-'}</h6>
                </div>
              </div>
            </td>
            <td>
              <p class="text-xs font-weight-bold mb-0">${escapeHtml(topicText)}</p>
              <p class="text-xs text-secondary mb-0">${escapeHtml(vote.description ? vote.description.substring(0, 50) + '...' : '-')}</p>
            </td>
            <td class="align-middle text-center">
              <span class="text-secondary text-xs font-weight-bold">${escapeHtml(groupName)}</span>
            </td>
            <td class="align-middle text-center">
              <span class="badge badge-sm bg-${statusBadge}">${getStatusText(status)}</span>
            </td>
            <td class="align-middle text-center">
              <span class="text-secondary text-xs font-weight-bold">${deadline}</span>
            </td>
            <td class="align-middle text-center">
              <button class="btn btn-sm btn-primary me-1" onclick="viewVote(${vote.id})">Xem</button>
              ${status === 'OPEN' && vote.createdBy && vote.createdBy.id === currentUser?.id 
                ? `<button class="btn btn-sm btn-warning me-1" onclick="closeVote(${vote.id})">Đóng</button>`
                : ''}
            </td>
        `;
        tbody.appendChild(tr);
    });
}

// Get topic text in Vietnamese
function getTopicText(topic) {
    const topicMap = {
        'UPGRADE_BATTERY': 'Nâng cấp pin',
        'INSURANCE': 'Bảo hiểm',
        'SELL_VEHICLE': 'Bán xe',
        'OTHER': 'Khác'
    };
    return topicMap[topic] || topic;
}

// Get status text in Vietnamese
function getStatusText(status) {
    const statusMap = {
        'OPEN': 'Đang mở',
        'CLOSED': 'Đã đóng',
        'APPROVED': 'Đã chấp thuận',
        'REJECTED': 'Đã từ chối'
    };
    return statusMap[status] || status;
}

// Show create vote modal
window.showCreateVoteModal = function() {
    const modal = new bootstrap.Modal(document.getElementById('createVoteModal'));
    modal.show();
    
    // Set default deadline to 7 days from now
    const deadline = new Date();
    deadline.setDate(deadline.getDate() + 7);
    const deadlineStr = deadline.toISOString().slice(0, 16);
    document.getElementById('voteDeadline').value = deadlineStr;
};

// Handle create vote
window.handleCreateVote = async function() {
    const groupId = document.getElementById('voteGroupId').value;
    const topic = document.getElementById('voteTopic').value;
    const description = document.getElementById('voteDescription').value;
    const method = document.getElementById('voteMethod').value;
    const deadline = document.getElementById('voteDeadline').value;
    
    if (!groupId || !topic || !description || !deadline) {
        showAlert('Vui lòng điền đầy đủ thông tin', 'error');
        return;
    }
    
    try {
        const result = await apiCall('/votes', {
            method: 'POST',
            body: JSON.stringify({
                groupId: parseInt(groupId),
                topic: topic,
                description: description,
                votingMethod: method,
                deadline: deadline
            })
        });
        
        if (result && result.response && result.response.ok) {
            showAlert('Tạo phiếu bầu thành công!', 'success');
            const modal = bootstrap.Modal.getInstance(document.getElementById('createVoteModal'));
            if (modal) modal.hide();
            document.getElementById('createVoteForm').reset();
            
            // Get the newly created vote ID
            const newVote = result.data;
            const newVoteId = newVote ? newVote.id : null;
            
            // Reload groups first to ensure we have the latest groups
            await loadGroups();
            
            // Reload the specific vote from API to get full details including group
            if (newVoteId) {
                try {
                    const voteResult = await apiCall(`/votes/${newVoteId}`);
                    if (voteResult && voteResult.response && voteResult.response.ok) {
                        const fullVote = voteResult.data;
                        // Add to allVotes if not already there
                        const existingIndex = allVotes.findIndex(v => v.id === fullVote.id);
                        if (existingIndex === -1) {
                            allVotes.push(fullVote);
                        } else {
                            // Update existing vote
                            allVotes[existingIndex] = fullVote;
                        }
                    }
                } catch (error) {
                    console.error('Error loading new vote:', error);
                }
            }
            
            // Then reload all votes to ensure consistency
            await loadAllVotes();
        } else {
            const errorMsg = result?.data?.message || 'Không thể tạo phiếu bầu';
            showAlert(errorMsg, 'error');
        }
    } catch (error) {
        console.error('Error creating vote:', error);
        showAlert('Lỗi khi tạo phiếu bầu: ' + error.message, 'error');
    }
};

// View vote details
window.viewVote = async function(voteId) {
    try {
        const result = await apiCall(`/votes/${voteId}`);
        if (result && result.response && result.response.ok) {
            const vote = result.data;
            displayVoteDetails(vote);
        } else {
            showAlert('Không thể tải chi tiết phiếu bầu', 'error');
        }
    } catch (error) {
        console.error('Error loading vote:', error);
        showAlert('Lỗi khi tải chi tiết phiếu bầu', 'error');
    }
};

// Display vote details in modal
async function displayVoteDetails(vote) {
    document.getElementById('viewVoteTitle').textContent = `Phiếu bầu #${vote.id} - ${getTopicText(vote.topic)}`;
    
    // Load vote options and check if user already voted
    let voteOptions = [];
    let myVote = null;
    
    try {
        // Get vote options (YES, NO counts)
        const voteResult = await apiCall(`/votes/${vote.id}/options`);
        if (voteResult && voteResult.response && voteResult.response.ok) {
            voteOptions = Array.isArray(voteResult.data) ? voteResult.data : [];
        }
        
        // Check if current user has already voted
        try {
            const myVoteResult = await apiCall(`/votes/${vote.id}/my-vote`);
            if (myVoteResult && myVoteResult.response && myVoteResult.response.ok) {
                myVote = myVoteResult.data;
            }
        } catch (error) {
            // User hasn't voted yet or endpoint doesn't exist - that's okay
            console.log('User has not voted yet or endpoint not available');
        }
    } catch (error) {
        console.error('Error loading vote details:', error);
    }
    
    // Allow creator to vote - only check if vote is open, deadline not passed, and user hasn't voted yet
    const canVote = vote.status === 'OPEN' && 
                    vote.deadline && 
                    new Date(vote.deadline) > new Date() &&
                    !myVote; // User hasn't voted yet
    
    const isCreator = vote.createdBy && vote.createdBy.id === currentUser?.id;
    
    const body = document.getElementById('viewVoteBody');
    body.innerHTML = `
        <div class="mb-3">
          <strong>Nhóm:</strong> ${vote.group ? (vote.group.name || `Nhóm ${vote.group.id}`) : '-'}
        </div>
        <div class="mb-3">
          <strong>Mô tả:</strong>
          <p>${escapeHtml(vote.description || '-')}</p>
        </div>
        <div class="mb-3">
          <strong>Phương thức bỏ phiếu:</strong> ${getVotingMethodText(vote.votingMethod)}
        </div>
        <div class="mb-3">
          <strong>Trạng thái:</strong> <span class="badge bg-${vote.status === 'OPEN' ? 'success' : 'secondary'}">${getStatusText(vote.status)}</span>
        </div>
        <div class="mb-3">
          <strong>Hạn chót:</strong> ${formatDateTime(vote.deadline)}
        </div>
        <div class="mb-3">
          <strong>Người tạo:</strong> ${vote.createdBy ? (vote.createdBy.fullName || vote.createdBy.email) : '-'}
        </div>
        <hr>
        <div class="mb-3">
          <h6>Bỏ phiếu</h6>
          ${myVote 
            ? `<p class="text-success">Bạn đã bỏ phiếu: <strong>${escapeHtml(myVote.choice)}</strong></p>`
            : canVote 
              ? `<div class="d-flex gap-2">
                  <button class="btn btn-success" onclick="castVote(${vote.id}, 'YES')">Đồng ý</button>
                  <button class="btn btn-danger" onclick="castVote(${vote.id}, 'NO')">Không đồng ý</button>
                </div>`
              : '<p class="text-muted">Bạn không thể bỏ phiếu cho phiếu bầu này</p>'}
        </div>
        <div class="mb-3">
          <h6>Kết quả</h6>
          ${voteOptions.length > 0
            ? `<ul class="list-group">
                ${voteOptions.map(opt => {
                  const total = voteOptions.reduce((sum, o) => sum + (o.count || 0), 0);
                  const percentage = total > 0 ? ((opt.count || 0) / total * 100).toFixed(1) : 0;
                  return `<li class="list-group-item d-flex justify-content-between align-items-center">
                    ${escapeHtml(opt.option)}
                    <span class="badge bg-primary rounded-pill">${opt.count || 0} phiếu (${percentage}%)</span>
                  </li>`;
                }).join('')}
              </ul>`
            : '<p class="text-muted">Chưa có kết quả</p>'}
        </div>
    `;
    
    const modal = new bootstrap.Modal(document.getElementById('viewVoteModal'));
    modal.show();
}

// Get voting method text
function getVotingMethodText(method) {
    const methodMap = {
        'SIMPLE_MAJORITY': 'Đa số đơn giản',
        'UNANIMOUS': 'Nhất trí',
        'OWNERSHIP_WEIGHTED': 'Theo tỉ lệ sở hữu'
    };
    return methodMap[method] || method;
}

// Cast vote
window.castVote = async function(voteId, choice) {
    if (!confirm(`Bạn có chắc muốn bỏ phiếu "${choice === 'YES' ? 'Đồng ý' : 'Không đồng ý'}"?`)) {
        return;
    }
    
    try {
        const result = await apiCall(`/votes/${voteId}/cast`, {
            method: 'POST',
            body: JSON.stringify({
                choice: choice
            })
        });
        
        if (result && result.response && result.response.ok) {
            showAlert('Bỏ phiếu thành công!', 'success');
            const modal = bootstrap.Modal.getInstance(document.getElementById('viewVoteModal'));
            if (modal) modal.hide();
            await loadAllVotes();
        } else {
            const errorMsg = result?.data?.message || 'Không thể bỏ phiếu';
            showAlert(errorMsg, 'error');
        }
    } catch (error) {
        console.error('Error casting vote:', error);
        showAlert('Lỗi khi bỏ phiếu: ' + error.message, 'error');
    }
};

// Close vote
window.closeVote = async function(voteId) {
    if (!confirm('Bạn có chắc muốn đóng phiếu bầu này?')) {
        return;
    }
    
    try {
        const result = await apiCall(`/votes/${voteId}/close`, {
            method: 'PUT'
        });
        
        if (result && result.response && result.response.ok) {
            showAlert('Đóng phiếu bầu thành công!', 'success');
            await loadAllVotes();
        } else {
            const errorMsg = result?.data?.message || 'Không thể đóng phiếu bầu';
            showAlert(errorMsg, 'error');
        }
    } catch (error) {
        console.error('Error closing vote:', error);
        showAlert('Lỗi khi đóng phiếu bầu: ' + error.message, 'error');
    }
};

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
