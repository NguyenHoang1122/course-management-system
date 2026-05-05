    function toggleWishlist(courseId, isFromWishlistPage) {
        $.ajax({
            url: '/wishlist/toggle/' + courseId,
            type: 'POST',
            success: function(response) {
                if (response.success) {
                    if (isFromWishlistPage && !response.isAdded) {
                        $('#wishlist-item-' + courseId).fadeOut(400, function() {
                            $(this).remove();
                            if ($('[id^="wishlist-item-"]').length === 0) {
                                location.reload(); // Reload to show empty state if last item removed
                            }
                        });
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