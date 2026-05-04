document.addEventListener('DOMContentLoaded', function () {
    const sidebar = document.querySelector('.admin-sidebar');
    const toggleBtn = document.getElementById('sidebarToggle');
    const backdrop = document.getElementById('sidebarBackdrop');
    const dropdowns = document.querySelectorAll('.sidebar-dropdown');

    // Lấy activeMenu từ data attribute (truyền từ Thymeleaf)
    const currentMenu = document.body.dataset.activeMenu || '';

    // ===== 1. Restore Sidebar State (Desktop only) =====
    if (window.innerWidth > 768) {
        const isCollapsed = localStorage.getItem('admin-sidebar-collapsed') === 'true';
        if (isCollapsed) {
            document.body.classList.add('admin-sidebar-collapsed');
        }
    }

    // ===== 2. Toggle Sidebar =====
    toggleBtn?.addEventListener('click', function () {
        if (window.innerWidth <= 768) {
            document.body.classList.toggle('admin-sidebar-mobile-open');
        } else {
            const newState = document.body.classList.toggle('admin-sidebar-collapsed');
            localStorage.setItem('admin-sidebar-collapsed', newState);
        }
    });

    // ===== 3. Backdrop click (Mobile) =====
    backdrop?.addEventListener('click', function () {
        document.body.classList.remove('admin-sidebar-mobile-open');
    });

    // ===== 4. Dropdown logic =====
    dropdowns.forEach(dropdown => {
        const btn = dropdown.querySelector('.sidebar-dropdown-btn');

        // Auto open based on activeMenu
        if (
            (dropdown.dataset.label === 'Khóa học' && currentMenu === 'courses') ||
            (dropdown.dataset.label === 'Bài học' && currentMenu === 'lessons')
        ) {
            dropdown.classList.add('open');
        }

        btn?.addEventListener('click', function () {
            if (document.body.classList.contains('admin-sidebar-collapsed')) return;

            dropdowns.forEach(d => {
                if (d !== dropdown) d.classList.remove('open');
            });

            dropdown.classList.toggle('open');
        });
    });

    // ===== 5. Highlight active sublink =====
    const currentPath = window.location.pathname;

    document.querySelectorAll('.admin-sidebar-sublink').forEach(link => {
        if (link.getAttribute('href') === currentPath) {
            link.classList.add('active');
            link.closest('.sidebar-dropdown')?.classList.add('open');
        }
    });
});