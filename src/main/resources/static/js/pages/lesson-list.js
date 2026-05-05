    document.addEventListener('click', function (e) {
        if (e.target.closest('.btn-delete-lesson')) {
            const btn = e.target.closest('.btn-delete-lesson');
            const id = btn.getAttribute('data-id');
            const title = btn.getAttribute('data-title');

            document.getElementById("lessonTitleDisplay").innerText = title;
            document.getElementById("deleteLessonForm").action = '/admin/delete-lesson-from-list/' + id;
            document.getElementById("lessonConfirmBox").style.display = "flex";
        }
    });

    function hideLessonConfirm() {
        document.getElementById("lessonConfirmBox").style.display = "none";
    }

    document.addEventListener('DOMContentLoaded', function () {
        // Hàm chuyển đổi URL YouTube sang Embed (giống trang chi tiết của bạn)
        function getEmbedUrl(url) {
            if (!url) return '';
            var value = url.trim();
            var videoId = '';

            if (value.indexOf('youtu.be/') !== -1) {
                videoId = value.split('youtu.be/')[1].split('?')[0];
            } else if (value.indexOf('youtube.com/watch') !== -1 && value.indexOf('v=') !== -1) {
                videoId = value.split('v=')[1].split('&')[0];
            } else if (value.indexOf('youtube.com/shorts/') !== -1) {
                videoId = value.split('youtube.com/shorts/')[1].split('?')[0];
            }

            return videoId ? 'https://www.youtube.com/embed/' + videoId : '';
        }

        // Quét tất cả các card bài học
        var videoItems = document.querySelectorAll('.lesson-video-item');
        videoItems.forEach(function(item) {
            var rawUrl = item.getAttribute('data-video-url');
            var embedUrl = getEmbedUrl(rawUrl);
            var iframe = item.querySelector('.lesson-card-iframe');
            var placeholder = item.querySelector('.video-placeholder');

            if (embedUrl && iframe) {
                iframe.src = embedUrl;
                iframe.style.display = 'block';
                if (placeholder) placeholder.style.display = 'none';
            }
        });
    });