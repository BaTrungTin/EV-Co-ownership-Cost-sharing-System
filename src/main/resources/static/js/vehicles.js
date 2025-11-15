// Vehicles Management JavaScript

let vehicles = [];
let groups = [];

// Load data on page load
document.addEventListener('DOMContentLoaded', async () => {
    if (checkAuth()) {
        await loadGroups();
        await loadVehicles();
    }
});

// Load groups
async function loadGroups() {
    try {
        const result = await apiCall('/groups');
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
    const filterSelect = document.getElementById('groupIdFilter');
    const formSelect = document.getElementById('vehicleGroupId');
    
    groups.forEach(group => {
        const option1 = document.createElement('option');
        option1.value = group.id;
        option1.textContent = group.name;
        filterSelect.appendChild(option1);
        
        const option2 = document.createElement('option');
        option2.value = group.id;
        option2.textContent = group.name;
        formSelect.appendChild(option2);
    });
}

// Load vehicles
async function loadVehicles(groupId = null) {
    try {
        const url = groupId ? `/vehicles?groupId=${groupId}` : '/vehicles';
        const result = await apiCall(url);
        
        if (!result || !result.response) {
            console.error('No response from API');
            showAlert('Không thể kết nối đến server', 'error');
            populateVehiclesTable([]);
            return;
        }
        
        if (result.response.ok) {
            vehicles = Array.isArray(result.data) ? result.data : [];
            populateVehiclesTable(vehicles);
        } else if (result.response.status === 403) {
            const errorMsg = result?.data?.message || 'Bạn không có quyền truy cập danh sách xe. Vui lòng đăng nhập lại.';
            console.error('403 Forbidden:', errorMsg, result.data);
            showAlert(errorMsg, 'error');
            populateVehiclesTable([]);
        } else if (result.response.status === 401) {
            // Đã được xử lý trong apiCall - redirect to login
            populateVehiclesTable([]);
        } else {
            const errorMsg = result?.data?.message || `Lỗi khi tải danh sách xe (${result.response.status})`;
            console.error('Error loading vehicles:', result.response.status, result.data);
            showAlert(errorMsg, 'error');
            populateVehiclesTable([]);
        }
    } catch (error) {
        console.error('Error loading vehicles:', error);
        showAlert('Lỗi khi tải danh sách xe: ' + error.message, 'error');
        populateVehiclesTable([]);
    }
}

// Filter vehicles by group
function filterVehicles() {
    const groupId = document.getElementById('groupIdFilter').value;
    loadVehicles(groupId || null);
}

// Populate vehicles table
function populateVehiclesTable(vehicles) {
    const tbody = document.getElementById('vehiclesTableBody');
    if (!tbody) {
        console.error('vehiclesTableBody not found');
        return;
    }
    
    tbody.innerHTML = '';

    if (!vehicles || vehicles.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center py-4">Chưa có xe nào</td></tr>';
        return;
    }

    vehicles.forEach(vehicle => {
        const tr = document.createElement('tr');
        const vehicleId = vehicle.id || 0;
        const vin = escapeHtml(vehicle.vin || '-');
        const plate = escapeHtml(vehicle.plate || '-');
        const model = escapeHtml(vehicle.model || '-');
        const groupName = escapeHtml(vehicle.group?.name || '-');
        
        tr.innerHTML = `
            <td>
              <div class="d-flex px-2 py-1">
                <div class="d-flex flex-column justify-content-center">
                  <h6 class="mb-0 text-sm">${vehicleId}</h6>
                </div>
              </div>
            </td>
            <td>
              <p class="text-xs font-weight-bold mb-0">${vin}</p>
            </td>
            <td class="align-middle text-center text-sm">
              <span class="text-secondary text-xs font-weight-bold">${plate}</span>
            </td>
            <td class="align-middle text-center">
              <span class="text-secondary text-xs font-weight-bold">${model}</span>
            </td>
            <td class="align-middle text-center">
              <span class="text-secondary text-xs font-weight-bold">${groupName}</span>
            </td>
            <td class="align-middle text-center">
              <button class="btn btn-sm btn-primary me-1" onclick="viewVehicle(${vehicleId})">Xem</button>
              <button class="btn btn-sm btn-danger" onclick="deleteVehicle(${vehicleId}, '${plate}')">Xóa</button>
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

// Show create vehicle modal - sử dụng Bootstrap modal
function showCreateVehicleModal() {
    const modalElement = document.getElementById('createVehicleModal');
    if (modalElement) {
        const modal = new bootstrap.Modal(modalElement);
        modal.show();
        document.getElementById('createVehicleForm').reset();
    }
}

// Create vehicle - wait for DOM to be ready
document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('createVehicleForm');
    if (form) {
        form.addEventListener('submit', handleCreateVehicle);
    }
});

async function handleCreateVehicle(e) {
    e.preventDefault();
    
    const formData = {
        vin: document.getElementById('vehicleVin').value.trim(),
        plate: document.getElementById('vehiclePlate').value.trim(),
        model: document.getElementById('vehicleModel').value.trim(),
        groupId: parseInt(document.getElementById('vehicleGroupId').value)
    };

    try {
        const result = await apiCall('/vehicles', {
            method: 'POST',
            body: JSON.stringify(formData)
        });

        if (result && result.response && result.response.ok) {
            // Hide Bootstrap modal
            const modalElement = document.getElementById('createVehicleModal');
            if (modalElement) {
                const modal = bootstrap.Modal.getInstance(modalElement);
                if (modal) modal.hide();
            }
            document.getElementById('createVehicleForm').reset();
            showAlert('Thêm xe thành công!', 'success');
            await loadVehicles();
        } else {
            const errorMsg = result?.data?.message || result?.data?.error || 'Không thể thêm xe';
            console.error('Error creating vehicle:', result);
            showAlert(errorMsg, 'error');
        }
    } catch (error) {
        console.error('Error creating vehicle:', error);
        showAlert('Lỗi khi thêm xe: ' + error.message, 'error');
    }
}

// View vehicle details
function viewVehicle(vehicleId) {
    const vehicle = vehicles.find(v => v.id === vehicleId);
    if (vehicle) {
        alert(`Xe: ${vehicle.plate}\nModel: ${vehicle.model}\nVIN: ${vehicle.vin}`);
    }
}

// Delete vehicle
function deleteVehicle(vehicleId, plate) {
    if (!confirm(`Bạn có chắc chắn muốn xóa xe "${plate}"?\n\nLưu ý: Chỉ có thể xóa xe khi không có booking hoặc chi phí.`)) {
        return;
    }

    deleteVehicleAPI(vehicleId);
}

// Delete vehicle API call
async function deleteVehicleAPI(vehicleId) {
    try {
        const result = await apiCall(`/vehicles/${vehicleId}`, {
            method: 'DELETE'
        });

        if (result && result.response && (result.response.ok || result.response.status === 204)) {
            showAlert('Xóa xe thành công!', 'success');
            await loadVehicles();
        } else {
            const errorMsg = result?.data?.message || result?.data?.error || 'Không thể xóa xe';
            console.error('Error deleting vehicle:', result);
            showAlert(errorMsg, 'error');
        }
    } catch (error) {
        console.error('Error deleting vehicle:', error);
        showAlert('Lỗi khi xóa xe: ' + error.message, 'error');
    }
}

