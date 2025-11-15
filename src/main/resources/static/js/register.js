// Register JavaScript

const API_BASE_URL = 'http://localhost:8080/api';

document.getElementById('registerForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const fullName = document.getElementById('fullName').value.trim();
    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    const errorMessage = document.getElementById('errorMessage');
    const btnText = document.getElementById('btnText');
    const btnLoader = document.getElementById('btnLoader');
    const submitBtn = e.target.querySelector('button[type="submit"]');
    
    // Reset error message
    errorMessage.style.display = 'none';
    errorMessage.textContent = '';
    
    // Validation
    if (password.length < 6) {
        showError('Mật khẩu phải có ít nhất 6 ký tự');
        return;
    }
    
    if (password !== confirmPassword) {
        showError('Mật khẩu xác nhận không khớp');
        return;
    }
    
    // Show loading
    submitBtn.disabled = true;
    btnText.style.display = 'none';
    btnLoader.style.display = 'inline-block';
    
    try {
        const response = await fetch(`${API_BASE_URL}/users/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                fullName: fullName,
                email: email,
                password: password
            })
        });
        
        // Check if response has content before parsing JSON
        const contentType = response.headers.get('content-type');
        let data = null;
        
        if (contentType && contentType.includes('application/json')) {
            const text = await response.text();
            if (text && text.trim().length > 0) {
                try {
                    data = JSON.parse(text);
                } catch (parseError) {
                    console.error('Failed to parse JSON response:', parseError, 'Response text:', text);
                    showError('Lỗi khi xử lý phản hồi từ server. Vui lòng thử lại.');
                    return;
                }
            }
        }
        
        if (response.ok) {
            // Registration successful
            alert('Đăng ký thành công! Vui lòng đăng nhập.');
            window.location.href = 'login.html';
        } else {
            // Registration failed
            let errorMsg = 'Đăng ký thất bại. Vui lòng thử lại.';
            if (data) {
                if (data.message) {
                    errorMsg = data.message;
                } else if (data.error) {
                    errorMsg = data.error;
                } else if (data.details) {
                    // Handle validation errors
                    const details = data.details;
                    if (typeof details === 'object') {
                        const errorMessages = Object.values(details).join(', ');
                        errorMsg = errorMessages || errorMsg;
                    }
                }
            } else {
                // If no JSON data, try to get error from status text
                errorMsg = response.statusText || errorMsg;
            }
            showError(errorMsg);
        }
    } catch (error) {
        console.error('Registration error:', error);
        const errorMsg = error.message || 'Đã xảy ra lỗi. Vui lòng thử lại sau.';
        showError(errorMsg);
    } finally {
        // Hide loading
        submitBtn.disabled = false;
        btnText.style.display = 'inline';
        btnLoader.style.display = 'none';
    }
});

function showError(message) {
    const errorMessage = document.getElementById('errorMessage');
    errorMessage.textContent = message;
    errorMessage.style.display = 'block';
}


