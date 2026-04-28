$(document).ready(function () {
    // Mark all read from page
    $('#pageMarkAllRead').on('click', function () {
        // Optimistic
        $('.notification-page-item').removeClass('unread');
        $('.mark-read-badge').remove();

        $.post('/api/notifications/mark-all-read', function () {
            location.reload();
        });
    });

    // Mark single read when clicking from this page
    $('.notification-page-item.unread').on('click', function (e) {
        const id = $(this).data('id');
        const $item = $(this);
        const link = $item.attr('href');

        if (id) {
            e.preventDefault();
            // Optimistic
            $item.removeClass('unread');
            $item.find('.mark-read-badge').remove();

            $.post('/api/notifications/mark-read/' + id).always(function () {
                if (link && link !== 'javascript:void(0)') {
                    window.location.href = link;
                }
            });
        }
    });


    $(document).ready(function () {
        // --- Các hàm đóng/mở Modal ---
        window.showDeleteAllConfirm = function () {
            $('#deleteAllConfirmBox').css('display', 'flex');
        }

        window.hideDeleteAllConfirm = function () {
            $('#deleteAllConfirmBox').hide();
        }

        // --- Xử lý sự kiện Delete All ---
        $('#pageDeleteAll').on('click', function () {
            showDeleteAllConfirm(); // Thay vì dùng confirm()
        });

        // Nút xác nhận cuối cùng trong Modal
        $('#confirmDeleteAllBtn').on('click', function () {
            hideDeleteAllConfirm();

            $.ajax({
                url: '/api/notifications/all',
                type: 'DELETE',
                success: function () {
                    location.reload();
                }
            });
        });

        // --- Tối ưu thêm phần Xóa từng cái (Nếu muốn đồng bộ) ---
        // Thay thế đoạn window.deleteFromPage cũ của bạn:
        window.deleteFromPage = function (id, e) {
            if (e) e.stopPropagation();

            // Bạn có thể tạo thêm 1 modal nhỏ hơn hoặc dùng confirm tạm thời,
            // nhưng với "Xóa tất cả" thì Modal lớn ở trên là quan trọng nhất.
            if (!confirm('Bạn có chắc muốn xóa thông báo này?')) return;

            const $item = $(`.notification-page-item[data-id="${id}"]`);
            $item.fadeOut(300, function () {
                $(this).remove();
                if ($('.notification-page-item').length === 0) {
                    location.reload();
                }
            });

            $.ajax({
                url: '/api/notifications/' + id,
                type: 'DELETE'
            });
        }

        // Đóng modal khi click ra ngoài hoặc phím Esc
        $(window).on('click', function (e) {
            if ($(e.target).hasClass('modal-overlay')) hideDeleteAllConfirm();
        });
    });
});