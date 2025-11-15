// Expenses Management JavaScript

let expenses = [];
let groups = [];
let vehicles = [];
let myExpenseShares = [];

// Load data on page load
document.addEventListener('DOMContentLoaded', async () => {
    if (checkAuth()) {
        await loadGroups();
        await loadVehicles();
        await loadExpenses();
        await loadMyExpenseShares();
    }
});

// Load groups
async function loadGroups() {
    try {
        const result = await apiCall('/groups');
        if (result && result.response.ok) {
            groups = result.data;
            populateGroupSelect();
        }
    } catch (error) {
        console.error('Error loading groups:', error);
    }
}

// Load vehicles
async function loadVehicles() {
    try {
        const result = await apiCall('/vehicles');
        if (result && result.response.ok) {
            vehicles = result.data;
            populateVehicleSelect();
        }
    } catch (error) {
        console.error('Error loading vehicles:', error);
    }
}

// Populate group select
function populateGroupSelect() {
    const select = document.getElementById('expenseGroupId');
    select.innerHTML = '<option value="">Chọn nhóm</option>';
    
    groups.forEach(group => {
        const option = document.createElement('option');
        option.value = group.id;
        option.textContent = group.name;
        select.appendChild(option);
    });
}

// Populate vehicle select
function populateVehicleSelect() {
    const select = document.getElementById('expenseVehicleId');
    select.innerHTML = '<option value="">Không có</option>';
    
    vehicles.forEach(vehicle => {
        const option = document.createElement('option');
        option.value = vehicle.id;
        option.textContent = `${vehicle.plate} - ${vehicle.model}`;
        select.appendChild(option);
    });
}

// Load expenses
async function loadExpenses() {
    try {
        // Load expenses for groups user belongs to
        const expensesList = [];
        for (const group of groups) {
            const result = await apiCall(`/expenses/group/${group.id}`);
            if (result && result.response.ok) {
                expensesList.push(...result.data);
            }
        }
        expenses = expensesList;
        populateExpensesTable(expenses);
    } catch (error) {
        console.error('Error loading expenses:', error);
        showAlert('Lỗi khi tải danh sách chi phí', 'error');
    }
}

// Load my expense shares
async function loadMyExpenseShares() {
    try {
        const result = await apiCall('/expenses/my-shares');
        if (result && result.response.ok) {
            myExpenseShares = result.data;
            populateMyExpenseSharesTable(myExpenseShares);
        }
    } catch (error) {
        console.error('Error loading my expense shares:', error);
    }
}

// Populate expenses table
function populateExpensesTable(expenses) {
    const tbody = document.getElementById('expensesTableBody');
    tbody.innerHTML = '';

    if (expenses.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7">Chưa có chi phí nào</td></tr>';
        return;
    }

    expenses.forEach(expense => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${expense.id}</td>
            <td>${expense.type || '-'}</td>
            <td>${formatCurrency(expense.amount)}</td>
            <td>${formatDate(expense.date)}</td>
            <td>${expense.group?.name || '-'}</td>
            <td><span class="status-badge status-${expense.status?.toLowerCase()}">${expense.status || '-'}</span></td>
            <td>
                <div class="action-buttons">
                    ${expense.status === 'PENDING' 
                        ? `<button class="btn btn-small btn-success" onclick="approveExpense(${expense.id})">Duyệt</button>
                           <button class="btn btn-small btn-danger" onclick="rejectExpense(${expense.id})">Từ chối</button>` 
                        : ''}
                    <button class="btn btn-small btn-primary" onclick="viewExpense(${expense.id})">Xem</button>
                </div>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

// Populate my expense shares table
function populateMyExpenseSharesTable(shares) {
    const tbody = document.getElementById('myExpenseSharesTableBody');
    if (!tbody) {
        // Element doesn't exist, skip population
        console.warn('myExpenseSharesTableBody element not found, skipping table population');
        return;
    }
    
    tbody.innerHTML = '';

    if (!shares || shares.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5">Bạn chưa có chi phí nào</td></tr>';
        return;
    }

    shares.forEach(share => {
        const remaining = share.amount - (share.paidAmount || 0);
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${share.expense?.type || '-'} - ${formatCurrency(share.expense?.amount || 0)}</td>
            <td>${formatCurrency(share.amount)}</td>
            <td>${formatCurrency(share.paidAmount || 0)}</td>
            <td>${formatCurrency(remaining)}</td>
            <td><span class="status-badge status-${share.status?.toLowerCase()}">${share.status || '-'}</span></td>
        `;
        tbody.appendChild(tr);
    });
}

// Show create expense modal
function showCreateExpenseModal() {
    const modal = document.getElementById('createExpenseModal');
    if (modal) {
        const bootstrapModal = new bootstrap.Modal(modal);
        bootstrapModal.show();
    }
    const form = document.getElementById('createExpenseForm');
    if (form) {
        form.reset();
    }
    const expenseDate = document.getElementById('expenseDate');
    if (expenseDate) {
        expenseDate.valueAsDate = new Date();
    }
}

// Create expense
const createExpenseForm = document.getElementById('createExpenseForm');
if (createExpenseForm) {
    createExpenseForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const expenseGroupId = document.getElementById('expenseGroupId');
        const expenseVehicleId = document.getElementById('expenseVehicleId');
        const expenseType = document.getElementById('expenseType');
        const expenseAmount = document.getElementById('expenseAmount');
        const expenseDate = document.getElementById('expenseDate');
        const expenseDescription = document.getElementById('expenseDescription');
        const expenseSplitMethod = document.getElementById('expenseSplitMethod');
        
        if (!expenseGroupId || !expenseType || !expenseAmount || !expenseDate) {
            showAlert('Vui lòng điền đầy đủ thông tin bắt buộc', 'error');
            return;
        }
        
        const formData = {
            groupId: parseInt(expenseGroupId.value),
            vehicleId: expenseVehicleId && expenseVehicleId.value ? parseInt(expenseVehicleId.value) : null,
            type: expenseType.value,
            amount: parseFloat(expenseAmount.value),
            date: expenseDate.value,
            description: expenseDescription ? expenseDescription.value : null,
            splitMethod: expenseSplitMethod ? expenseSplitMethod.value : 'EQUAL'
        };

        try {
            const result = await apiCall('/expenses', {
                method: 'POST',
                body: JSON.stringify(formData)
            });

            if (result && result.response.ok) {
                showAlert('Tạo chi phí thành công!', 'success');
                const modalElement = document.getElementById('createExpenseModal');
                if (modalElement) {
                    const modal = bootstrap.Modal.getInstance(modalElement);
                    if (modal) modal.hide();
                }
                await loadExpenses();
                await loadMyExpenseShares();
            } else {
                const errorMsg = result.data?.message || 'Không thể tạo chi phí';
                showAlert(errorMsg, 'error');
            }
        } catch (error) {
            console.error('Error creating expense:', error);
            showAlert('Lỗi khi tạo chi phí', 'error');
        }
    });
}

// Approve expense
async function approveExpense(expenseId) {
    try {
        const result = await apiCall(`/expenses/${expenseId}/approve`, {
            method: 'PUT'
        });

        if (result && result.response.ok) {
            showAlert('Duyệt chi phí thành công!', 'success');
            await loadExpenses();
            await loadMyExpenseShares();
        } else {
            const errorMsg = result.data?.message || 'Không thể duyệt chi phí';
            showAlert(errorMsg, 'error');
        }
    } catch (error) {
        console.error('Error approving expense:', error);
        showAlert('Lỗi khi duyệt chi phí', 'error');
    }
}

// Reject expense
async function rejectExpense(expenseId) {
    if (!confirm('Bạn có chắc muốn từ chối chi phí này?')) {
        return;
    }

    try {
        const result = await apiCall(`/expenses/${expenseId}/reject`, {
            method: 'PUT'
        });

        if (result && result.response.ok) {
            showAlert('Từ chối chi phí thành công!', 'success');
            await loadExpenses();
        } else {
            const errorMsg = result.data?.message || 'Không thể từ chối chi phí';
            showAlert(errorMsg, 'error');
        }
    } catch (error) {
        console.error('Error rejecting expense:', error);
        showAlert('Lỗi khi từ chối chi phí', 'error');
    }
}

// View expense
function viewExpense(expenseId) {
    const expense = expenses.find(e => e.id === expenseId);
    if (expense) {
        alert(`Chi phí: ${expense.type}\nSố tiền: ${formatCurrency(expense.amount)}\nNgày: ${formatDate(expense.date)}\nTrạng thái: ${expense.status}`);
    }
}

