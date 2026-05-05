const CourseApp = (() => {

    let COURSE_ID = 0;
    let IS_ENROLLED = false;

    let ytPlayer = null;
    let currentLessonId = null;
    let isYtApiReady = false;

    let debounceTimer;

    // ===== INIT =====
    function init() {
        const app = document.getElementById('courseDetailApp');
        if (!app) return;

        COURSE_ID = app.dataset.courseId || 0;
        IS_ENROLLED = app.dataset.isEnrolled === 'true';

        bindEvents();
        loadYouTubeAPI();

        if (IS_ENROLLED) updateProgress();
    }

    // ===== EVENTS =====
    function bindEvents() {
        $(document).on('click', '.cd-tab-btn', handleTabSwitch);
        $(document).on('click', '.cd-acc-header', toggleAccordion);

        $('#showMoreBtn').on('click', toggleDescription);

        // expose global
        window.playLesson = playLesson;
        window.playCoursePreview = playCoursePreview;
        window.closePlayer = closePlayer;
    }

    // ===== TAB =====
    function handleTabSwitch() {
        const target = $(this).data('tab');
        $('.cd-tab-btn').removeClass('active');
        $(this).addClass('active');
        $('.cd-tab-content').removeClass('active');
        $('#' + target).addClass('active');
    }

    // ===== DESCRIPTION =====
    function toggleDescription(e) {
        e.preventDefault();

        const text = $('#aboutText');
        const btn = $('#showMoreBtn');

        text.toggleClass('expanded');

        btn.html(
            text.hasClass('expanded')
                ? 'SHOW LESS'
                : 'SHOW MORE'
        );
    }

    // ===== ACCORDION =====
    function toggleAccordion() {
        const target = $(this).data('acc-target');
        $('#' + target).toggleClass('open');
        $(this).toggleClass('open');
    }

    // ===== YOUTUBE =====
    function loadYouTubeAPI() {
        const tag = document.createElement('script');
        tag.src = "https://www.youtube.com/iframe_api";
        document.body.appendChild(tag);

        window.onYouTubeIframeAPIReady = () => {
            isYtApiReady = true;
        };
    }

    function extractYoutubeId(url) {
        const match = url.match(/youtu.*(?:\/|v=)([^"&?\/\s]{11})/);
        return match ? match[1] : null;
    }

    // ===== PLAY VIDEO =====
    function playLesson(el) {
        currentLessonId = el.dataset.lessonId;

        $('.cd-lesson-row').removeClass('active-lesson');
        el.classList.add('active-lesson');

        $('#currentLessonTitle').text(el.dataset.lessonTitle);

        renderVideo(el.dataset.videoUrl);
    }

    function playCoursePreview(el) {
        currentLessonId = null;
        renderVideo(el.dataset.videoUrl);
    }

    function renderVideo(url) {
        const wrapper = $('#videoEmbedWrapperEnrolled:visible, #videoEmbedWrapperGuest:visible');
        const placeholder = $('#playerPlaceholderEnrolled:visible, #playerPlaceholderGuest:visible');

        wrapper.show();
        placeholder.html('<div class="yt-player"></div>');

        const ytId = extractYoutubeId(url);

        if (ytId && isYtApiReady) {
            ytPlayer = new YT.Player(placeholder.find('.yt-player')[0], {
                videoId: ytId,
                events: {
                    onStateChange: e => {
                        if (e.data === 0) markComplete();
                    }
                }
            });
        } else {
            placeholder.html(`<iframe src="${url}" allowfullscreen></iframe>`);
        }
    }

    function closePlayer() {
        $('#videoEmbedWrapperEnrolled, #videoEmbedWrapperGuest').hide();
        $('#playerPlaceholderEnrolled, #playerPlaceholderGuest').empty();
    }

    function markComplete() {
        if (!currentLessonId) return;
        toggleLessonStatus(currentLessonId);
    }

    // ===== PROGRESS =====
    function toggleLessonStatus(id) {
        fetch(`/lessons/${id}/toggle-progress`, { method: 'POST' })
            .then(res => res.text())
            .then(status => {
                const row = document.querySelector(`[data-lesson-id="${id}"]`);
                if (!row) return;

                row.classList.toggle('completed', status === 'COMPLETED');
                updateProgress();
            });
    }

    function updateProgress() {
        const total = +$('#dashboardProgressCard').data('total-lessons');
        const completed = $('.cd-lesson-row.completed').length;

        const percent = Math.round((completed / total) * 100);

        $('#dashboardProgressBar').css('width', percent + '%');
        $('#dashboardProgressCount').text(`${completed}/${total}`);
        $('#dashboardProgressPercent').text(percent + '%');
    }

    return { init };

})();

// INIT
$(document).ready(() => CourseApp.init());