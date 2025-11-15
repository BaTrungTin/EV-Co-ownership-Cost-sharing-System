// Dashboard Router - Tự động redirect đến đúng dashboard dựa trên role
// Sử dụng script này trong tất cả các trang để cập nhật dashboard links

document.addEventListener('DOMContentLoaded', async () => {
    await updateDashboardLinks();
});

async function updateDashboardLinks() {
    const token = getToken();
    if (!token) {
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/auth/me`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        
        if (response.ok) {
            const user = await response.json();
            const isAdmin = user.roles && Array.isArray(user.roles) && user.roles.includes('ADMIN');
            const dashboardUrl = isAdmin ? 'admin-dashboard.html' : 'user-dashboard.html';
            
            // Update all dashboard links
            const dashboardLinks = document.querySelectorAll('a[href="admin-dashboard.html"], a[href="user-dashboard.html"], a[href="dashboard.html"]');
            dashboardLinks.forEach(link => {
                link.href = dashboardUrl;
                
                // Update active state
                const currentPage = window.location.pathname.split('/').pop();
                if (currentPage === dashboardUrl) {
                    link.classList.add('active');
                } else {
                    link.classList.remove('active');
                }
            });
            
            // Update navbar brand link
            const brandLinks = document.querySelectorAll('.navbar-brand[href="admin-dashboard.html"], .navbar-brand[href="user-dashboard.html"]');
            brandLinks.forEach(link => {
                link.href = dashboardUrl;
            });
        }
    } catch (error) {
        console.error('Error updating dashboard links:', error);
    }
}

