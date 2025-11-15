// Payments Management JavaScript

let payments = [];
let expenseShares = [];
let currentUser = null;
let isAdmin = false;

// Load data on page load
document.addEventListener('DOMContentLoaded', async () => {
    if (checkAuth()) {
        // Load current user info to check if admin
        await loadCurrentUserInfo();
        
        await loadMyPayments();
        
        // Only load expense shares if not admin (admin doesn't create payments)
        if (!isAdmin) {
            await loadExpenseShares();
        }
        
        // Hide create payment button for admin
        const createBtn = document.querySelector('button[onclick="showCreatePaymentModal()"]');
        if (createBtn && isAdmin) {
            createBtn.style.display = 'none';
        }
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

// Load my payments
async function loadMyPayments() {
    try {
        // Admin uses admin endpoint to see all payments
        const endpoint = isAdmin ? '/admin/payments' : '/payments/my-payments';
        const result = await apiCall(endpoint);
        if (result && result.response.ok) {
            payments = Array.isArray(result.data) ? result.data : [];
            populatePaymentsTable(payments);
        } else {
            showAlert('Không thể tải danh sách thanh toán', 'error');
        }
    } catch (error) {
        console.error('Error loading payments:', error);
        showAlert('Lỗi khi tải danh sách thanh toán', 'error');
    }
}

// Load expense shares for payment
async function loadExpenseShares() {
    try {
        const result = await apiCall('/expenses/my-shares');
        if (result && result.response.ok) {
            expenseShares = result.data.filter(share => 
                share.status === 'PENDING' || share.status === 'PARTIAL'
            );
            populateExpenseShareSelect();
        }
    } catch (error) {
        console.error('Error loading expense shares:', error);
    }
}

// Populate expense share select
function populateExpenseShareSelect() {
    const select = document.getElementById('paymentExpenseShareId');
    select.innerHTML = '<option value="">Chọn chi phí</option>';
    
    expenseShares.forEach(share => {
        const remaining = share.amount - (share.paidAmount || 0);
        if (remaining > 0) {
            const option = document.createElement('option');
            option.value = share.id;
            option.textContent = `${share.expense?.type || 'Chi phí'} - Còn lại: ${formatCurrency(remaining)}`;
            option.dataset.remaining = remaining;
            select.appendChild(option);
        }
    });
}

// Update payment amount when expense share changes
document.getElementById('paymentExpenseShareId').addEventListener('change', (e) => {
    const selectedOption = e.target.options[e.target.selectedIndex];
    if (selectedOption.dataset.remaining) {
        document.getElementById('paymentAmount').max = selectedOption.dataset.remaining;
        document.getElementById('paymentAmount').value = selectedOption.dataset.remaining;
    }
});

// Populate payments table
function populatePaymentsTable(payments) {
    const tbody = document.getElementById('paymentsTableBody');
    if (!tbody) return;
    
    tbody.innerHTML = '';

    if (payments.length === 0) {
        const colspan = isAdmin ? '6' : '7';
        tbody.innerHTML = `<tr><td colspan="${colspan}" class="text-center">Chưa có thanh toán nào</td></tr>`;
        return;
    }

    payments.forEach(payment => {
        const tr = document.createElement('tr');
        // For admin, show user info; for regular users, show expense info
        const expenseInfo = isAdmin 
            ? (payment.user ? (payment.user.fullName || payment.user.email) : '-')
            : (payment.expenseShare?.expense?.type || '-');
        
        tr.innerHTML = `
            <td>
              <div class="d-flex px-2 py-1">
                <div class="d-flex flex-column justify-content-center">
                  <h6 class="mb-0 text-sm">${payment.id || '-'}</h6>
                </div>
              </div>
            </td>
            <td>
              <p class="text-xs font-weight-bold mb-0">${expenseInfo}</p>
            </td>
            <td class="align-middle text-center text-sm">
              <span class="text-secondary text-xs font-weight-bold">${formatCurrency(payment.amount || 0)}</span>
            </td>
            <td class="align-middle text-center text-sm">
              <span class="text-secondary text-xs font-weight-bold">${payment.method || '-'}</span>
            </td>
            <td class="align-middle text-center">
              <span class="badge badge-sm ${getStatusBadgeClass(payment.status)}">${payment.status || '-'}</span>
            </td>
            <td class="align-middle text-center">
              <span class="text-secondary text-xs font-weight-bold">${formatDateTime(payment.createdAt)}</span>
            </td>
            ${!isAdmin ? `
            <td class="align-middle text-center">
                ${payment.status === 'PROCESSING' 
                    ? `<button class="btn btn-sm btn-success" onclick="showPaymentModal(${payment.id})">Xem chi tiết</button>` 
                    : '-'}
            </td>
            ` : ''}
        `;
        tbody.appendChild(tr);
    });
}

// Get status badge class
function getStatusBadgeClass(status) {
    if (!status) return 'bg-secondary';
    const statusLower = status.toLowerCase();
    if (statusLower === 'completed' || statusLower === 'approved') return 'bg-success';
    if (statusLower === 'processing' || statusLower === 'pending') return 'bg-warning';
    if (statusLower === 'failed' || statusLower === 'rejected') return 'bg-danger';
    return 'bg-secondary';
}

// Show create payment modal
function showCreatePaymentModal() {
    showModal('createPaymentModal');
    document.getElementById('createPaymentForm').reset();
    loadExpenseShares();
}

// Create payment
document.getElementById('createPaymentForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const expenseShareId = parseInt(document.getElementById('paymentExpenseShareId').value);
    const amount = parseFloat(document.getElementById('paymentAmount').value);
    const method = document.getElementById('paymentMethod').value;

    try {
        const result = await apiCall('/payments', {
            method: 'POST',
            body: JSON.stringify({
                expenseShareId: expenseShareId,
                amount: amount,
                method: method
            })
        });

        if (result && result.response.ok) {
            showAlert('Tạo thanh toán thành công!', 'success');
            hideModal('createPaymentModal');
            await loadMyPayments();
            await loadExpenseShares();
        } else {
            const errorMsg = result.data?.message || 'Không thể tạo thanh toán';
            showAlert(errorMsg, 'error');
        }
    } catch (error) {
        console.error('Error creating payment:', error);
        showAlert('Lỗi khi tạo thanh toán', 'error');
    }
});

// Add button to show create payment modal in table header
document.addEventListener('DOMContentLoaded', () => {
    const tableSection = document.querySelector('.table-section h2');
    if (tableSection && !tableSection.querySelector('button')) {
        const btn = document.createElement('button');
        btn.className = 'btn btn-primary';
        btn.textContent = '+ Thanh toán mới';
        btn.onclick = showCreatePaymentModal;
        tableSection.appendChild(btn);
    }
});

