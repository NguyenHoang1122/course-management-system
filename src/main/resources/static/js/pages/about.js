    $(document).ready(function() {
        // Ensure header behavior is correct for transparent hero
        $(window).scroll(function() {
            if ($(window).scrollTop() > 50) {
                $('.site-header-unified').removeClass('header-transparent').addClass('header-solid header-sticky');
            } else {
                $('.site-header-unified').addClass('header-transparent').removeClass('header-solid header-sticky');
            }
        });
    });