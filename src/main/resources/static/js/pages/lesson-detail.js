document.addEventListener('DOMContentLoaded', function () {
    var playerWrap = document.querySelector('.lesson-video-player-wrap');
    if (!playerWrap) {
        return;
    }

    var rawUrl = playerWrap.getAttribute('data-video-url') || '';
    var frame = document.getElementById('lessonYoutubeFrame');
    var notice = document.getElementById('lessonYoutubeNotice');
    var openLink = document.getElementById('lessonVideoOpenLink');

    function getYouTubeEmbedUrl(url) {
        if (!url) {
            return '';
        }

        var value = url.trim();
        var videoId = '';

        if (value.indexOf('youtu.be/') !== -1) {
            videoId = value.split('youtu.be/')[1].split('?')[0];
        } else if (value.indexOf('youtube.com/watch') !== -1 && value.indexOf('v=') !== -1) {
            videoId = value.split('v=')[1].split('&')[0];
        } else if (value.indexOf('youtube.com/embed/') !== -1) {
            videoId = value.split('youtube.com/embed/')[1].split('?')[0];
        } else if (value.indexOf('youtube.com/shorts/') !== -1) {
            videoId = value.split('youtube.com/shorts/')[1].split('?')[0];
        }

        return videoId ? 'https://www.youtube.com/embed/' + videoId : '';
    }

    function getVimeoEmbedUrl(url) {
        if (!url || url.indexOf('vimeo.com/') === -1) {
            return '';
        }

        var id = url.split('vimeo.com/')[1];
        if (!id) {
            return '';
        }

        id = id.split('?')[0].split('/')[0];
        return id ? 'https://player.vimeo.com/video/' + id : '';
    }

    function getEmbedUrl(url) {
        if (!url) {
            return '';
        }

        var value = url.trim();
        var youtubeUrl = getYouTubeEmbedUrl(value);
        if (youtubeUrl) {
            return youtubeUrl;
        }

        var vimeoUrl = getVimeoEmbedUrl(value);
        if (vimeoUrl) {
            return vimeoUrl;
        }

        if (value.indexOf('http://') === 0 || value.indexOf('https://') === 0) {
            return value;
        }

        return '';
    }

    var embedUrl = getEmbedUrl(rawUrl);
    if (openLink) {
        openLink.href = rawUrl;
    }

    if (embedUrl) {
        frame.src = embedUrl;
        var isYoutubeOrVimeo = embedUrl.indexOf('youtube.com/embed/') !== -1 || embedUrl.indexOf('player.vimeo.com/video/') !== -1;
        notice.style.display = isYoutubeOrVimeo ? 'none' : 'inline-block';
    } else {
        frame.style.display = 'none';
        notice.style.display = 'inline-block';
    }
});