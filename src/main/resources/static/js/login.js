// API Base URL
const API_BASE_URL = 'http://localhost:8080/api';

// Lấy form và các elements
const loginForm = document.getElementById('loginForm');
const emailInput = document.getElementById('email');
const passwordInput = document.getElementById('password');
const errorMessage = document.getElementById('errorMessage');
const btnText = document.getElementById('btnText');
const btnLoader = document.getElementById('btnLoader');

// Xử lý submit form
loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    // Ẩn error message
    errorMessage.style.display = 'none';
    
    // Lấy giá trị từ form
    const email = emailInput.value.trim();
    const password = passwordInput.value;
    
    // Validate
    if (!email || !password) {
        showError('Vui lòng nhập đầy đủ email và mật khẩu');
        return;
    }
    
    // Hiển thị loading
    setLoading(true);
    
    try {
        // Gọi API login
        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                email: email,
                password: password
            })
        });
        
        const data = await response.json();
        
        if (response.ok) {
            // Login thành công
            const token = data.token;
            
            // Lưu token vào localStorage
            localStorage.setItem('jwt_token', token);
            localStorage.setItem('user_email', email);
            
            // Check role và redirect đúng trang
            // Admin -> dashboard.html, User -> profile.html
            const userRoles = data.user?.roles || data.roles || [];
            const isAdmin = Array.isArray(userRoles) && userRoles.includes('ADMIN');
            
            if (isAdmin) {
                window.location.href = 'admin-dashboard.html';
            } else {
                window.location.href = 'user-dashboard.html';
            }
        } else {
            // Login thất bại
            const errorMsg = data.message || 'Email hoặc mật khẩu không đúng';
            showError(errorMsg);
        }
    } catch (error) {
        console.error('Login error:', error);
        showError('Không thể kết nối đến server. Vui lòng thử lại sau.');
    } finally {
        setLoading(false);
    }
});

// Hàm hiển thị lỗi
function showError(message) {
    errorMessage.textContent = message;
    errorMessage.style.display = 'block';
}

// Hàm set loading state
function setLoading(loading) {
    if (loading) {
        loginForm.querySelector('button').disabled = true;
        btnText.style.display = 'none';
        btnLoader.style.display = 'inline-block';
    } else {
        loginForm.querySelector('button').disabled = false;
        btnText.style.display = 'inline';
        btnLoader.style.display = 'none';
    }
}

// Kiểm tra nếu đã đăng nhập thì chuyển hướng
window.addEventListener('DOMContentLoaded', async () => {
    const token = localStorage.getItem('jwt_token');
    if (token) {
        // Đã có token, check role và redirect đúng trang
        try {
            const response = await fetch(`${API_BASE_URL}/auth/me`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (response.ok) {
                const user = await response.json();
                const isAdmin = user.roles && Array.isArray(user.roles) && user.roles.includes('ADMIN');
                window.location.href = isAdmin ? 'admin-dashboard.html' : 'user-dashboard.html';
            }
        } catch (error) {
            // Nếu không lấy được user info, giữ nguyên trang login
            console.error('Error checking user role:', error);
        }
    }
    // Nếu chưa có token, giữ nguyên trang login
});

