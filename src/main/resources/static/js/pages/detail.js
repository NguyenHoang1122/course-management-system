/**
 * Course Detail Page Functionality
 * Handles tabs, video player, reviews, and learning progress.
 */

$(document).ready(function() {
    const appContainer = document.getElementById('courseDetailApp');
    const COURSE_ID = appContainer ? appContainer.getAttribute('data-course-id') : 0;
    const IS_ENROLLED = appContainer ? appContainer.getAttribute('data-is-enrolled') === 'true' : false;

    // ===== TABS LOGIC =====
    $(document).on('click', '.cd-tab-btn', function() {
        const target = $(this).data('tab');
        $('.cd-tab-btn').removeClass('active');
        $(this).addClass('active');
        $('.cd-tab-content').removeClass('active');
        $('#' + target).addClass('active');
    });

    // Default tab for enrolled users
    if (IS_ENROLLED) {
        const contentTabBtn = $('.cd-tab-btn[data-tab="tab-curriculum"]');
        if (contentTabBtn.length) {
            contentTabBtn.click();
        }
    }

    // ===== SHOW MORE DESCRIPTION =====
    const showMoreBtn = document.getElementById('showMoreBtn');
    const aboutText = document.getElementById('aboutText');

    if (showMoreBtn && aboutText) {
        showMoreBtn.addEventListener('click', function(e) {
            e.preventDefault();
            if (aboutText.classList.contains('expanded')) {
                aboutText.classList.remove('expanded');
                showMoreBtn.innerHTML = '<i class="fas fa-plus-circle"></i> SHOW MORE';
            } else {
                aboutText.classList.add('expanded');
                showMoreBtn.innerHTML = '<i class="fas fa-minus-circle"></i> SHOW LESS';
            }
        });
    }

    // ===== ACCORDION (Curriculum) =====
    $(document).on('click', '.cd-acc-header', function() {
        const target = $(this).data('acc-target');
        const $body = $('#' + target);
        if ($(this).hasClass('open')) {
            $(this).removeClass('open');
            $body.removeClass('open');
        } else {
            $(this).addClass('open');
            $body.addClass('open');
        }
    });

    // Initialize sidebar progress if enrolled
    if (IS_ENROLLED) {
        updateSidebarProgress();
    }
});

// ===== AJAX REVIEWS LOGIC =====
let filterDebounceTimer;
function debounceFilter() {
    clearTimeout(filterDebounceTimer);
    filterDebounceTimer = setTimeout(() => {
        handleReviewFilter();
    }, 500);
}

function handleReviewFilter(event) {
    if (event) event.preventDefault();
    const form = document.getElementById('reviewFilterForm');
    if (!form) return;
    const formData = new FormData(form);
    const params = new URLSearchParams(formData);
    
    loadReviewsFragment(params.toString());
    return false;
}

function loadMoreReviews(newSize) {
    const sizeInput = document.getElementById('reviewSizeInput');
    if (sizeInput) {
        sizeInput.value = newSize;
        handleReviewFilter();
    }
}

function loadReviewsFragment(queryString) {
    const appContainer = document.getElementById('courseDetailApp');
    const courseId = appContainer ? appContainer.getAttribute('data-course-id') : 0;
    const container = document.getElementById('reviews-list-container');
    
    if (!container) return;
    
    container.style.opacity = '0.5';
    container.style.pointerEvents = 'none';

    fetch(`/courses/${courseId}?${queryString}`, {
        headers: { 'X-Requested-With': 'XMLHttpRequest' }
    })
    .then(response => response.text())
    .then(html => {
        container.innerHTML = html;
        container.style.opacity = '1';
        container.style.pointerEvents = 'auto';
    })
    .catch(err => {
        console.error('Error loading reviews:', err);
        container.style.opacity = '1';
        container.style.pointerEvents = 'auto';
    });
}

function toggleHelpful(courseId, reviewId, btn) {
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
    const headers = { 'X-Requested-With': 'XMLHttpRequest' };
    if (csrfToken) headers[csrfHeader] = csrfToken;

    fetch(`/courses/${courseId}/reviews/${reviewId}/helpful`, {
        method: 'POST',
        headers: headers
    })
    .then(response => {
        if (response.status === 401) { window.location.href = '/auth/login'; return; }
        return response.json();
    })
    .then(data => {
        if (data) {
            const countSpan = btn.querySelector('.helpful-count');
            const icon = btn.querySelector('i');
            countSpan.textContent = data.helpfulCount;
            if (data.isHelpful) {
                btn.classList.add('active');
                icon.className = 'fas fa-thumbs-up';
            } else {
                btn.classList.remove('active');
                icon.className = 'far fa-thumbs-up';
            }
        }
    })
    .catch(err => console.error('Error toggling helpful:', err));
}

function handleReviewSubmit(event) {
    event.preventDefault();
    const form = document.getElementById('reviewSubmitForm');
    const formData = new FormData(form);
    
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
    const headers = { 'X-Requested-With': 'XMLHttpRequest' };
    if (csrfToken) headers[csrfHeader] = csrfToken;

    const btn = form.querySelector('button[type="submit"]');
    const originalText = btn.innerHTML;
    btn.disabled = true;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang gửi...';

    fetch(form.action, {
        method: 'POST',
        headers: headers,
        body: formData
    })
    .then(response => {
        if (response.ok) {
            handleReviewFilter();
            const statusMsg = document.getElementById('review-status-msg');
            if (statusMsg) {
                statusMsg.innerHTML = '<div class="alert alert-success mt-4"><i class="fas fa-check-circle mr-2"></i> Đánh giá của bạn đã được ghi lại thành công!</div>';
            }
        } else {
            alert('Có lỗi xảy ra khi gửi đánh giá. Vui lòng thử lại.');
        }
    })
    .finally(() => {
        btn.disabled = false;
        btn.innerHTML = originalText;
    });

    return false;
}

function handleReportSubmit(event) {
    event.preventDefault();
    const form = document.getElementById('reportForm');
    const formData = new FormData(form);
    
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
    const headers = { 'X-Requested-With': 'XMLHttpRequest' };
    if (csrfToken) headers[csrfHeader] = csrfToken;

    fetch(form.action, {
        method: 'POST',
        headers: headers,
        body: formData
    })
    .then(response => {
        if (response.ok) {
            closeReportModal();
            const statusMsg = document.getElementById('review-status-msg');
            if (statusMsg) {
                statusMsg.innerHTML = '<div class="alert alert-success mt-4"><i class="fas fa-check-circle mr-2"></i> Báo cáo đã được gửi. Cảm ơn bạn!</div>';
            }
        } else {
            alert('Có lỗi xảy ra khi gửi báo cáo.');
        }
    })
    .catch(err => console.error('Error reporting review:', err));

    return false;
}

function showReportModal(reviewId) {
    const appContainer = document.getElementById('courseDetailApp');
    const courseId = appContainer ? appContainer.getAttribute('data-course-id') : 0;
    const form = document.getElementById('reportForm');
    if (form) {
        form.action = `/courses/${courseId}/reviews/${reviewId}/report`;
        const modal = document.getElementById('reportReviewModal');
        if (modal) modal.style.display = 'flex';
    }
}

function closeReportModal() {
    const modal = document.getElementById('reportReviewModal');
    if (modal) modal.style.display = 'none';
}

// ===== VIDEO ENGINE =====
let currentActiveLessonId = null;
let ytPlayer = null;
let isYtApiReady = false;

// Load YouTube API
(function() {
    var tag = document.createElement('script');
    tag.src = "https://www.youtube.com/iframe_api";
    var firstScriptTag = document.getElementsByTagName('script')[0];
    firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);
})();

window.onYouTubeIframeAPIReady = function() {
    isYtApiReady = true;
};

function playLessonById(lessonId) {
    const row = document.querySelector(`.cd-lesson-row[data-lesson-id="${lessonId}"]`);
    if (row) {
        const sectionBody = row.closest('.cd-acc-body');
        if (sectionBody && !sectionBody.classList.contains('open')) {
            const header = document.querySelector(`.cd-acc-header[data-acc-target="${sectionBody.id}"]`);
            if (header) header.click();
        }
        row.click();
    } else {
        const acc = document.querySelector('.cd-accordion');
        if (acc) acc.scrollIntoView({ behavior: 'smooth' });
    }
}

function updateSidebarProgress() {
    // Check for either sidebar card or dashboard card
    const card = document.getElementById('dashboardProgressCard') || document.getElementById('sidebarProgressCard');
    if (!card) return;
    
    const total = parseInt(card.getAttribute('data-total-lessons') || 0);
    if (total === 0) return;
    
    const completed = document.querySelectorAll('.cd-lesson-row.completed').length;
    const percent = Math.round((completed / total) * 100);
    
    // Support both ID systems for backward compatibility
    const bar = document.getElementById('dashboardProgressBar') || document.getElementById('sidebarProgressBar');
    const countText = document.getElementById('dashboardProgressCount') || document.getElementById('sidebarProgressCount');
    const percentText = document.getElementById('dashboardProgressPercent') || document.getElementById('sidebarProgressPercent');
    
    if (bar) bar.style.width = percent + '%';
    
    if (countText) {
        if (countText.id === 'dashboardProgressCount') {
            countText.textContent = completed + '/' + total;
        } else {
            countText.textContent = completed + '/' + total + ' bài học';
        }
    }
    
    if (percentText) percentText.textContent = percent + '%';

    // Resume button logic
    const nextLessonRow = document.querySelector('.cd-lesson-row.playable:not(.completed)');
    const resumeBtn = document.querySelector('.btn-resume');
    const resumeBtnWrap = resumeBtn ? resumeBtn.parentElement : null;
    const nextHint = resumeBtnWrap ? resumeBtnWrap.querySelector('p') : null;

    if (nextLessonRow) {
        const nextId = nextLessonRow.getAttribute('data-lesson-id');
        const nextTitle = nextLessonRow.getAttribute('data-lesson-title');
        
        if (resumeBtn) {
            resumeBtn.setAttribute('onclick', `playLessonById(${nextId})`);
            const btnText = resumeBtn.querySelector('span');
            if (btnText) btnText.textContent = (completed === 0) ? 'Bắt đầu học' : 'Học tiếp';
            resumeBtn.style.display = 'flex';
        }
        if (nextHint) {
            nextHint.textContent = 'Bài kế tiếp: ' + nextTitle;
            nextHint.style.display = 'block';
        }
        const completionMsg = document.getElementById('completionMessage');
        if (completionMsg) completionMsg.remove();
    } else {
        if (resumeBtn) resumeBtn.style.display = 'none';
        if (nextHint) nextHint.style.display = 'none';
        
        if (!document.getElementById('completionMessage')) {
            const msg = document.createElement('div');
            msg.id = 'completionMessage';
            msg.className = 'completion-msg';
            msg.innerHTML = '<i class="fas fa-check-circle"></i> Bạn đã hoàn thành khóa học!';
            if (resumeBtnWrap) resumeBtnWrap.appendChild(msg);
        }
    }
}

function playCoursePreview(element) {
    currentActiveLessonId = null; 
    const videoUrl = element.getAttribute('data-video-url');
    if (!videoUrl) return;
    
    // Update display title
    const currentTitle = document.getElementById('currentLessonTitle');
    if (currentTitle) currentTitle.textContent = 'Video giới thiệu';
    
    document.querySelectorAll('.cd-lesson-row').forEach(row => row.classList.remove('active-lesson'));
    renderVideoPlayer(videoUrl);
}

function playLesson(element) {
    currentActiveLessonId = element.getAttribute('data-lesson-id');
    const videoUrl = element.getAttribute('data-video-url');
    if (!videoUrl) return;
    
    // Update display title
    const currentTitle = document.getElementById('currentLessonTitle');
    if (currentTitle) {
        currentTitle.textContent = element.getAttribute('data-lesson-title');
    }
    
    document.querySelectorAll('.cd-lesson-row').forEach(row => row.classList.remove('active-lesson'));
    element.classList.add('active-lesson');
    renderVideoPlayer(videoUrl);
    if (window.innerWidth < 992) {
        const player = document.getElementById('mainPlayerContainer');
        if (player) player.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
}

function markActiveLessonComplete() {
    if (!currentActiveLessonId) return;
    const row = document.querySelector(`.cd-lesson-row[data-lesson-id="${currentActiveLessonId}"]`);
    if (row && !row.classList.contains('completed')) {
        const statusBox = row.querySelector('.lesson-status-box');
        if (statusBox) toggleLessonStatus(currentActiveLessonId, statusBox);
    }
}

function toggleLessonStatus(lessonId, element) {
    if (!lessonId) return;
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
    const headers = { 'X-Requested-With': 'XMLHttpRequest' };
    if (csrfToken) headers[csrfHeader] = csrfToken;

    fetch(`/lessons/${lessonId}/toggle-progress`, {
        method: 'POST',
        headers: headers
    })
    .then(response => {
        if (response.status === 401) { window.location.href = '/auth/login'; return; }
        return response.text();
    })
    .then(status => {
        const row = document.querySelector(`.cd-lesson-row[data-lesson-id="${lessonId}"]`);
        if (!row) return;
        
        const icon = row.querySelector('.status-icon');
        if (status === 'COMPLETED') {
            row.classList.add('completed');
            if (icon) icon.className = 'fas fa-check-circle status-icon';
        } else if (status === 'REMOVED') {
            row.classList.remove('completed');
            if (icon) icon.className = 'far fa-circle status-icon';
        }
        updateSidebarProgress();
    })
    .catch(err => console.error('Error toggling progress:', err));
}

function handleWishlistClick(element, event) {
    const courseId = element.getAttribute('data-course-id');
    if (typeof toggleWishlist === 'function') {
        toggleWishlist(courseId, event, element);
    }
}

function renderVideoPlayer(url) {
    const wrapper = document.getElementById('videoEmbedWrapper');
    const placeholder = document.getElementById('playerPlaceholder');
    if (!wrapper || !placeholder) return;
    wrapper.style.display = 'block';
    
    const youtubeRegex = /(?:youtube\.com\/(?:[^\/]+\/.+\/|(?:v|e(?:mbed)?)\/|.*[?&]v=)|youtu\.be\/)([^"&?\/\s]{11})/;
    const vimeoRegex = /(?:vimeo\.com\/|player\.vimeo\.com\/video\/|vimeo\.com\/channels\/.+\/|vimeo\.com\/groups\/.+\/videos\/)(\d+)(?:[#?].*)?/;
    const ytMatch = url.match(youtubeRegex);
    const vimeoMatch = url.match(vimeoRegex);
    
    placeholder.innerHTML = '<div id="ytPlayerContainer"></div>';
    ytPlayer = null;

    if (ytMatch && isYtApiReady) {
        const videoId = ytMatch[1];
        ytPlayer = new YT.Player('ytPlayerContainer', {
            height: '100%',
            width: '100%',
            videoId: videoId,
            playerVars: { 'autoplay': 1, 'modestbranding': 1, 'rel': 0 },
            events: {
                'onStateChange': function(event) {
                    if (event.data === YT.PlayerState.ENDED) {
                        markActiveLessonComplete();
                    }
                }
            }
        });
    } else if (ytMatch && !isYtApiReady) {
        const videoId = ytMatch[1];
        placeholder.innerHTML = `<iframe id="mainIframePlayer" width="100%" height="100%" src="https://www.youtube.com/embed/${videoId}?autoplay=1&enablejsapi=1" frameborder="0" allowfullscreen></iframe>`;
    } else if (vimeoMatch) {
        const videoId = vimeoMatch[1];
        placeholder.innerHTML = `<iframe id="mainIframePlayer" src="https://player.vimeo.com/video/${videoId}?autoplay=1" width="100%" height="100%" frameborder="0" allow="autoplay; fullscreen; picture-in-picture" allowfullscreen></iframe>`;
    } else if (url.toLowerCase().endsWith('.mp4') || url.includes('.mp4?') || url.includes('.mp4#')) {
        placeholder.innerHTML = `<video id="mainVideoPlayer" width="100%" height="100%" controls autoplay style="background:#000;" onended="markActiveLessonComplete()"><source src="${url}" type="video/mp4">Your browser does not support the video tag.</video>`;
    } else {
        placeholder.innerHTML = `<iframe id="mainIframePlayer" width="100%" height="100%" src="${url}" frameborder="0" allowfullscreen></iframe>`;
    }
}

function closePlayer() {
    const wrapper = document.getElementById('videoEmbedWrapper');
    const placeholder = document.getElementById('playerPlaceholder');
    if (wrapper) wrapper.style.display = 'none';
    if (placeholder) placeholder.innerHTML = '';
}

// ===== FOCUS MUSIC WIDGET LOGIC =====
let activeMusicChannel = null;
let focusYtPlayer = null;

function toggleMusicPlayer() {
    const panel = document.getElementById('musicPlayerPanel');
    const bubble = document.getElementById('musicBubble');
    if (!panel || !bubble) return;
    if (panel.style.display === 'flex') {
        panel.style.display = 'none';
        bubble.classList.remove('active');
    } else {
        panel.style.display = 'flex';
        bubble.classList.add('active');
    }
}

function switchMusicChannel(channelId, videoId) {
    const items = document.querySelectorAll('.channel-item');
    const clickedItem = document.querySelector(`.channel-item[onclick*="${channelId}"]`);
    const iframeContainer = document.getElementById('musicIframeContainer');

    if (activeMusicChannel === channelId) {
        stopMusic();
        return;
    }

    items.forEach(item => item.classList.remove('active'));
    if (clickedItem) clickedItem.classList.add('active');
    
    activeMusicChannel = channelId;
    iframeContainer.innerHTML = '<div id="focusIframe"></div>';
    
    if (typeof YT !== 'undefined' && YT.Player) {
        focusYtPlayer = new YT.Player('focusIframe', {
            height: '100', width: '100', videoId: videoId,
            playerVars: { 'autoplay': 1, 'enablejsapi': 1 },
            events: {
                'onReady': function(event) {
                    const vol = document.querySelector('.volume-slider').value;
                    event.target.setVolume(vol);
                    event.target.playVideo();
                }
            }
        });
    } else {
        iframeContainer.innerHTML = `<iframe id="focusIframe" width="100" height="100" src="https://www.youtube.com/embed/${videoId}?autoplay=1&enablejsapi=1" frameborder="0" allow="autoplay"></iframe>`;
    }
    
    document.getElementById('musicBubble').innerHTML = '<i class="fas fa-volume-up"></i>';
}

function stopMusic() {
    activeMusicChannel = null;
    if (focusYtPlayer) {
        try { focusYtPlayer.destroy(); } catch(e) {}
        focusYtPlayer = null;
    }
    document.querySelectorAll('.channel-item').forEach(item => item.classList.remove('active'));
    document.getElementById('musicIframeContainer').innerHTML = '';
    document.getElementById('musicBubble').innerHTML = '<i class="fas fa-headphones-alt"></i>';
}

function changeVolume(val) {
    if (focusYtPlayer && typeof focusYtPlayer.setVolume === 'function') {
        focusYtPlayer.setVolume(val);
    }
}
