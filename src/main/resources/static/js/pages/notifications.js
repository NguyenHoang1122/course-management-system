$(document).ready(function () {
    // 1. Quản lý Modal
    window.showDeleteAllConfirm = function () {
        // Sử dụng fadeIn để mượt mà hơn
        $('#deleteAllConfirmBox').fadeIn(200).css('display', 'flex');
    }

    window.hideDeleteAllConfirm = function () {
        $('#deleteAllConfirmBox').fadeOut(200);
    }

    // 2. Sự kiện nút Xóa tất cả (Mở Modal)
    $('#pageDeleteAll').on('click', function (e) {
        e.preventDefault();
        showDeleteAllConfirm();
    });

    // 3. Nút xác nhận cuối cùng TRONG Modal
    // LƯU Ý: Đảm bảo nút này trong HTML có id="confirmDeleteAllBtn"
    $('#confirmDeleteAllBtn').on('click', function () {
        $.ajax({
            url: '/api/notifications/all',
            type: 'DELETE',
            success: function () {
                location.reload();
            }
        });
    });

    // 4. Các sự kiện khác (Đánh dấu đã đọc...)
    $('#pageMarkAllRead').on('click', function () {
        $('.notification-page-item').removeClass('unread');
        $('.mark-read-badge').remove();
        $.post('/api/notifications/mark-all-read', function () {
            location.reload();
        });
    });

    // Đóng modal khi nhấn Esc
    $(document).on('keydown', function(e) {
        if (e.key === "Escape") hideDeleteAllConfirm();
    });
});