$(document).ready(function() {
    // Re-bind events to the document to handle fragments loaded dynamically
    $(document).on('click', '.search-trigger', function(e) {
        e.preventDefault();
        $('#search-overlay').addClass('active');
        $('body').css('overflow', 'hidden');
        setTimeout(function() {
            $('.unified-search-input').focus();
        }, 400);
    });

    $(document).on('click', '.search-overlay-close-btn', function() {
        $('#search-overlay').removeClass('active');
        $('body').css('overflow', '');
    });

    $(document).on('keydown', function(e) {
        if (e.key === 'Escape' && $('#search-overlay').hasClass('active')) {
            $('#search-overlay').removeClass('active');
            $('body').css('overflow', '');
        }
    });

    $(document).on('click', '#search-overlay', function(e) {
        if ($(e.target).is('#search-overlay')) {
            $('#search-overlay').removeClass('active');
            $('body').css('overflow', '');
        }
    });

    // AJAX CSRF Configuration
    const token = $("meta[name='_csrf']").attr("content");
    const header = $("meta[name='_csrf_header']").attr("content");
    $(document).ajaxSend(function(e, xhr, options) {
        if (token && header) {
            xhr.setRequestHeader(header, token);
        }
    });
});

function toggleWishlist(courseId, event, element) {
    if (event) {
        event.preventDefault();
        event.stopPropagation();
    }

    const $btn = $(element);
    const $icon = $btn.find('i');

    $.ajax({
        url: '/wishlist/toggle/' + courseId,
        type: 'POST',
        success: function(response) {
            if (response.success) {
                if (response.isAdded) {
                    $btn.addClass('active');
                    $icon.removeClass('far').addClass('fas');
                } else {
                    $btn.removeClass('active');
                    $icon.removeClass('fas').addClass('far');

                    // If we are on the wishlist page, remove the item
                    if (window.location.pathname === '/profile/wishlist') {
                        $('#wishlist-item-' + courseId).fadeOut(400, function() {
                            $(this).remove();
                            if ($('[id^="wishlist-item-"]').length === 0) {
                                location.reload();
                            }
                        });
                    }
                }

                // Special handling for the detail page button text if it exists
                const $text = $('#wishlist-text-' + courseId);
                if ($text.length) {
                     $text.text(response.isAdded ? 'Đã trong Wishlist' : 'Thêm vào Wishlist');
                }
            }
        },
        error: function(xhr) {
            if (xhr.status === 401) {
                window.location.href = '/auth/login';
            }
        }
    });
}

// Sticky Header & Scroll Effect Logic
document.addEventListener('DOMContentLoaded', function() {
    const header = document.getElementById('mainHeader');
    if (!header) return;
    const isTransparentInitial = header.classList.contains('header-transparent');

    function handleScroll() {
        if (window.scrollY > 50) {
            header.classList.add('header-sticky');
            if (isTransparentInitial) {
                header.classList.remove('header-transparent');
                header.classList.add('header-solid');
            }
        } else {
            header.classList.remove('header-sticky');
            if (isTransparentInitial) {
                header.classList.add('header-transparent');
                header.classList.remove('header-solid');
            }
        }
    }

    window.addEventListener('scroll', handleScroll);
    handleScroll();
});

// Notification Logic
$(document).ready(function() {
    function updateNotifications() {
        $.ajax({
            url: '/api/notifications',
            type: 'GET',
            success: function(data) {
                const count = data.unreadCount;
                const $badge = $('#notificationCount');
                if (count > 0) {
                    $badge.text(count > 99 ? '99+' : count).show();
                } else {
                    $badge.hide();
                }

                const $list = $('#notificationList');
                $list.empty();

                if (data.notifications.length === 0) {
                    $list.append('<div class="notification-item empty">Không có thông báo mới</div>');
                } else {
                    data.notifications.forEach(function(n) {
                        const unreadClass = n.isRead ? '' : 'unread';
                        const timeStr = formatTime(n.createdAt);
                        const iconClass = getIconByType(n.type);

                        const html = `
                            <div class="notification-item-wrapper" style="position: relative;">
                                <a href="${n.link || 'javascript:void(0)'}" class="notification-item ${unreadClass}" data-id="${n.id}">
                                    <div class="notification-icon-box noti-type-${n.type}">
                                        <i class="${iconClass}"></i>
                                    </div>
                                    <div class="notification-content">
                                        <span class="notification-title">${n.title}</span>
                                        <span class="notification-message">${n.message}</span>
                                        <span class="notification-time">${timeStr}</span>
                                    </div>
                                </a>
                                <button class="noti-delete-btn" data-id="${n.id}" onclick="deleteNotification(${n.id}, event)" title="Xóa thông báo">
                                    <i class="fas fa-times"></i>
                                </button>
                            </div>
                        `;
                        $list.append(html);
                    });
                }
            }
        });
    }

    function formatTime(dateStr) {
        const date = new Date(dateStr);
        const now = new Date();
        const diff = (now - date) / 1000; // seconds

        if (diff < 60) return 'Vừa xong';
        if (diff < 3600) return Math.floor(diff / 60) + ' phút trước';
        if (diff < 86400) return Math.floor(diff / 3600) + ' giờ trước';
        return date.toLocaleDateString('vi-VN');
    }

    function getIconByType(type) {
        switch(type) {
            case 'SUCCESS': return 'fas fa-check-circle';
            case 'DANGER': return 'fas fa-exclamation-circle';
            case 'WARNING': return 'fas fa-exclamation-triangle';
            default: return 'fas fa-info-circle';
        }
    }

    // Click trigger to fetch and show
    $('#notificationBtn').on('click', function(e) {
        e.stopPropagation();
        $('.notification-dropdown-container').toggleClass('active');
        if ($('.notification-dropdown-container').hasClass('active')) {
            updateNotifications();
        }
    });

    $(document).on('click', function() {
        $('.notification-dropdown-container').removeClass('active');
    });

    $('.notification-dropdown-menu').on('click', function(e) {
        e.stopPropagation();
    });

    // Mark single as read - Attach to the list instead of document to avoid propagation issues
    $('#notificationList').on('click', '.notification-item', function(e) {
        const id = $(this).data('id');
        const $item = $(this);
        const link = $item.attr('href');

        if (id && $item.hasClass('unread')) {
            e.preventDefault(); // Stop navigation temporarily

            // Optimistic UI Update: Giảm số lượng ngay lập tức trên giao diện
            $item.removeClass('unread');
            const $badge = $('#notificationCount');
            let count = parseInt($badge.text()) || 0;
            if (count > 0) {
                count--;
                if (count > 0) {
                    $badge.text(count > 99 ? '99+' : count);
                } else {
                    $badge.hide();
                }
            }

            // Gửi request ngầm và chuyển trang
            $.post('/api/notifications/mark-read/' + id).always(function() {
                if (link && link !== 'javascript:void(0)') {
                    window.location.href = link;
                } else {
                    updateNotifications();
                }
            });
        }
    });

    // Mark all as read
    $('#markAllReadBtn').on('click', function() {
        // Optimistic UI: Hide badge and remove unread classes instantly
        $('#notificationCount').hide();
        $('.notification-item').removeClass('unread');

        $.post('/api/notifications/mark-all-read', function() {
            updateNotifications();
        });
    });

    // Delete notification logic
    window.deleteNotification = function(id, e) {
        if (e) e.stopPropagation();

        const $itemWrapper = $(`[data-id="${id}"]`).closest('.notification-item-wrapper');
        $itemWrapper.fadeOut(300, function() {
            $(this).remove();
            // If no items left
            if ($('#notificationList').children().length === 0) {
                $('#notificationList').append('<div class="notification-item empty">Không có thông báo mới</div>');
                $('#notificationCount').hide();
            }
        });

        $.ajax({
            url: '/api/notifications/' + id,
            type: 'DELETE',
            success: function() {
                // Success - count already updated optimistically if we wanted,
                // but let's refresh to be sure of the state
                updateNotifications();
            }
        });
    }

    // Initial fetch if logged in
    if ($('#notificationBtn').length) {
        updateNotifications();
        // Optional: Poll every 30 seconds
        // setInterval(updateNotifications, 30000);
    }
});