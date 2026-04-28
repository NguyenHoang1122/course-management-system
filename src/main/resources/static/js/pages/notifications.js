    $(document).ready(function() {
        // Mark all read from page
        $('#pageMarkAllRead').on('click', function() {
            // Optimistic
            $('.notification-page-item').removeClass('unread');
            $('.mark-read-badge').remove();
            
            $.post('/api/notifications/mark-all-read', function() {
                location.reload();
            });
        });

        // Mark single read when clicking from this page
        $('.notification-page-item.unread').on('click', function(e) {
            const id = $(this).data('id');
            const $item = $(this);
            const link = $item.attr('href');
            
            if (id) {
                e.preventDefault();
                // Optimistic
                $item.removeClass('unread');
                $item.find('.mark-read-badge').remove();
                
                $.post('/api/notifications/mark-read/' + id).always(function() {
                    if (link && link !== 'javascript:void(0)') {
                        window.location.href = link;
                    }
                });
            }
        });

        // Individual delete from page
        window.deleteFromPage = function(id, e) {
            if (e) e.stopPropagation();
            if (!confirm('Bạn có chắc muốn xóa thông báo này?')) return;

            const $item = $(`.notification-page-item[data-id="${id}"]`);
            $item.fadeOut(300, function() {
                $(this).remove();
                if ($('.notification-page-item').length === 0) {
                    location.reload(); // Show empty state
                }
            });

            $.ajax({
                url: '/api/notifications/' + id,
                type: 'DELETE'
            });
        }

        // Delete all
        $('#pageDeleteAll').on('click', function() {
            if (!confirm('Bạn có chắc muốn xóa TOÀN BỘ thông báo không? Hành động này không thể hoàn tác.')) return;
            
            $.ajax({
                url: '/api/notifications/all',
                type: 'DELETE',
                success: function() {
                    location.reload();
                }
            });
        });
    });