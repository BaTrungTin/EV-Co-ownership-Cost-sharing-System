// Common JavaScript Functions
const API_BASE_URL = 'http://localhost:8080/api';

// Get JWT token from localStorage
function getToken() {
    return localStorage.getItem('jwt_token');
}

// Check if token exists and is valid (basic check)
function isTokenValid() {
    const token = getToken();
    if (!token) {
        return false;
    }
    // Basic validation: check if token has the expected structure
    // JWT tokens have 3 parts separated by dots
    const parts = token.split('.');
    if (parts.length !== 3) {
        console.warn('Invalid token format');
        return false;
    }
    return true;
}

// Check authentication
function checkAuth() {
    const token = getToken();
    if (!token) {
        // Check if we're already on login page
        if (!window.location.pathname.includes('login.html')) {
            window.location.href = 'login.html';
        }
        return false;
    }
    
    // Validate token format
    if (!isTokenValid()) {
        console.warn('Token format is invalid');
        localStorage.removeItem('jwt_token');
        localStorage.removeItem('user_email');
        if (!window.location.pathname.includes('login.html')) {
            window.location.href = 'login.html';
        }
        return false;
    }
    
    return true;
}

// API call with authentication
async function apiCall(endpoint, options = {}) {
    const token = getToken();
    const defaultHeaders = {
        'Content-Type': 'application/json'
    };
    
    // Only add Authorization header if token exists
    if (token) {
        defaultHeaders['Authorization'] = `Bearer ${token}`;
    } else {
        // Log warning if no token for authenticated endpoint
        console.warn(`API call to ${endpoint} without token - request may fail with 401`);
    }
    
    const defaultOptions = {
        headers: defaultHeaders
    };

    const mergedOptions = {
        ...defaultOptions,
        ...options,
        headers: {
            ...defaultOptions.headers,
            ...options.headers
        }
    };

    try {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, mergedOptions);
        
        // Log request details for debugging
        if (!response.ok) {
            console.warn(`API call failed: ${response.status} ${response.statusText} for ${endpoint}`);
        }
        
        if (response.status === 401) {
            // Unauthorized - try to parse error message from response
            let errorData = { 
                message: 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.',
                code: 'UNAUTHORIZED'
            };
            
            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('application/json')) {
                try {
                    const text = await response.text();
                    if (text) {
                        errorData = JSON.parse(text);
                    }
                } catch (e) {
                    console.warn('Could not parse 401 response body:', e);
                }
            }
            
            // Clear token and redirect to login
            console.warn('401 Unauthorized - Token may be expired or invalid');
            localStorage.removeItem('jwt_token');
            localStorage.removeItem('user_email');
            
            // Only redirect if not already on login page
            if (!window.location.pathname.includes('login.html')) {
                window.location.href = 'login.html';
            }
            
            return { response, data: errorData };
        }
        
        if (response.status === 403) {
            // Forbidden - log error and return response with error message
            console.error('403 Forbidden - User does not have permission to access this resource');
            const contentType = response.headers.get('content-type');
            let errorData = { message: 'Forbidden - You do not have permission to access this resource' };
            if (contentType && contentType.includes('application/json')) {
                try {
                    const text = await response.text();
                    if (text) {
                        errorData = JSON.parse(text);
                    }
                } catch (e) {
                    // Ignore parse error - might be empty response
                    console.warn('Could not parse 403 response body:', e);
                }
            } else {
                // If no JSON content type, check if we have a token
                const token = getToken();
                if (!token) {
                    errorData = { 
                        message: 'Authentication required. Please login first.',
                        code: 'UNAUTHORIZED'
                    };
                    // Redirect to login if no token
                    localStorage.removeItem('jwt_token');
                    window.location.href = 'login.html';
                    return { response, data: errorData };
                }
            }
            return { response, data: errorData };
        }

        // Handle 204 No Content (DELETE success)
        if (response.status === 204) {
            return { response, data: null };
        }

        // Try to parse JSON, but handle empty response
        let data = null;
        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
            const text = await response.text();
            if (text) {
                try {
                    data = JSON.parse(text);
                } catch (e) {
                    console.warn('Failed to parse JSON response:', text);
                }
            }
        }

        return { response, data };
    } catch (error) {
        console.error('API call error:', error);
        throw error;
    }
}

// Format currency
function formatCurrency(amount) {
    if (!amount) return '0 ₫';
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
}

// Format date
function formatDate(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
    });
}

// Format datetime
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

// Show alert message
function showAlert(message, type = 'info') {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type}`;
    alertDiv.textContent = message;
    
    const main = document.querySelector('.dashboard-main');
    if (main) {
        main.insertBefore(alertDiv, main.firstChild);
        
        // Auto remove after 5 seconds
        setTimeout(() => {
            alertDiv.remove();
        }, 5000);
    }
}

// Show modal
function showModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.add('active');
    }
}

// Hide modal
function hideModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.remove('active');
    }
}

// Close modal on click outside
document.addEventListener('click', (e) => {
    if (e.target.classList.contains('modal')) {
        e.target.classList.remove('active');
    }
});

// Initialize navigation
function initNavigation() {
    // Update dashboard links based on user role first
    const token = getToken();
    if (!token) {
        // If no token, just set active links based on current page
        setActiveLinks();
        return;
    }
    
    // Get user info and update navigation
    fetch(`${API_BASE_URL}/auth/me`, {
        headers: { 'Authorization': `Bearer ${token}` }
    })
    .then(response => {
        if (response.ok) {
            return response.json();
        }
        return null;
    })
    .then(user => {
        if (user) {
            const isAdmin = user.roles && Array.isArray(user.roles) && user.roles.includes('ADMIN');
            const currentPage = window.location.pathname.split('/').pop();
            const isAdminDashboardPage = currentPage === 'admin-dashboard.html';
            const isUserDashboardPage = currentPage === 'user-dashboard.html';
            
            // Update all dashboard links in navigation
            const dashboardLinks = document.querySelectorAll('a[href="admin-dashboard.html"], a[href="user-dashboard.html"], a[href="dashboard.html"]');
            dashboardLinks.forEach(link => {
                link.classList.remove('active');
                if (isAdmin) {
                    link.href = 'admin-dashboard.html';
                    if (isAdminDashboardPage) {
                        link.classList.add('active');
                    }
                } else {
                    link.href = 'user-dashboard.html';
                    if (isUserDashboardPage) {
                        link.classList.add('active');
                    }
                }
            });
            
            // Set active links for other pages (after dashboard is handled)
            setActiveLinks();
        }
    })
    .catch(error => {
        console.error('Error loading user info for navigation:', error);
        setActiveLinks();
    });
}

// Get dashboard URL based on user role
async function getDashboardUrl() {
    const token = getToken();
    if (!token) {
        return 'login.html';
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/auth/me`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (response.ok) {
            const user = await response.json();
            const isAdmin = user.roles && Array.isArray(user.roles) && user.roles.includes('ADMIN');
            return isAdmin ? 'admin-dashboard.html' : 'user-dashboard.html';
        }
    } catch (error) {
        console.error('Error getting dashboard URL:', error);
    }
    return 'user-dashboard.html'; // Default to user dashboard
}

// Set active links based on current page (excluding dashboard/profile)
function setActiveLinks() {
    const currentPage = window.location.pathname.split('/').pop();
    const navLinks = document.querySelectorAll('.nav-menu a');
    navLinks.forEach(link => {
        const href = link.getAttribute('href');
        // Only set active for non-dashboard/profile links
        if (href && href !== 'dashboard.html' && href !== 'profile.html' && href.includes(currentPage)) {
            link.classList.add('active');
        }
    });
}

// Load current user info
async function loadCurrentUser() {
    try {
        const result = await apiCall('/auth/me');
        if (result && result.response.ok) {
            return result.data;
        }
        return null;
    } catch (error) {
        console.error('Error loading current user:', error);
        return null;
    }
}

// Initialize header with user info
async function initHeader() {
    const user = await loadCurrentUser();
    if (user) {
        const userName = document.getElementById('userName');
        const userRole = document.getElementById('userRole');
        
        if (userName) {
            userName.textContent = user.fullName || user.email;
        }
        
        if (userRole) {
            userRole.textContent = user.roles?.join(', ') || 'USER';
        }
    }
}

// Display user info in navbar (with ID)
function displayUserInfoInNavbar(user) {
    if (!user) return;
    
    const userNameText = document.getElementById('userNameText');
    const userIdText = document.getElementById('userIdText');
    
    if (userNameText) {
        userNameText.textContent = user.fullName || user.email;
    }
    
    // Create or update userIdText element
    if (user.id) {
        if (!userIdText) {
            // Create userIdText element if it doesn't exist
            const userNameNav = document.getElementById('userNameNav');
            if (userNameNav && userNameText) {
                const idSpan = document.createElement('span');
                idSpan.id = 'userIdText';
                idSpan.className = 'd-sm-inline d-none ms-2 text-xs text-secondary';
                userNameNav.appendChild(idSpan);
            }
        }
        const finalUserIdText = document.getElementById('userIdText');
        if (finalUserIdText) {
            finalUserIdText.textContent = `(ID: ${user.id})`;
        }
    }
}

// Logout
function logout() {
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('user_email');
    window.location.href = 'login.html';
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', () => {
    // Check if we're on a protected page (not login)
    const isLoginPage = window.location.pathname.includes('login.html');
    if (!isLoginPage && checkAuth()) {
        initHeader();
        initNavigation();
    }
    
    // Logout button
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', logout);
    }
});

