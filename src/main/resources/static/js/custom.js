// Initialize WOW animations if available
document.addEventListener('DOMContentLoaded', function() {
    // Check if WOW.js is loaded
    if (typeof WOW !== 'undefined') {
        new WOW().init();
    }

    // Mobile menu handler
    const mobileMenu = document.querySelector('.mobile_menu');
    if (mobileMenu && typeof $.slicknav !== 'undefined') {
        $('#navigation').slicknav({
            appendTo: mobileMenu,
            duration: 1000
        });
    }

    // Sticky header
    window.addEventListener('scroll', function() {
        const header = document.querySelector('.header-sticky');
        if (header) {
            if (window.scrollY > 100) {
                header.classList.add('sticky');
            } else {
                header.classList.remove('sticky');
            }
        }
    });

    // Smooth scroll for anchor links
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            const href = this.getAttribute('href');
            if (href !== '#' && document.querySelector(href)) {
                e.preventDefault();
                document.querySelector(href).scrollIntoView({
                    behavior: 'smooth'
                });
            }
        });
    });
});

