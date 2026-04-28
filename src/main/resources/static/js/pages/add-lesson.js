    // Form validation and Real-time feedback
    document.addEventListener('DOMContentLoaded', function() {
        const form = document.getElementById('lessonForm');
        const inputs = form.querySelectorAll('input, select');

        function validateField(input) {
            const isValid = input.checkValidity();
            if (isValid) {
                input.classList.remove('is-invalid');
                input.classList.add('is-valid');
            } else {
                input.classList.remove('is-valid');
                input.classList.add('is-invalid');
            }
            return isValid;
        }

        inputs.forEach(input => {
            input.addEventListener('blur', () => validateField(input));
            input.addEventListener('input', () => {
                if (input.classList.contains('is-invalid') || input.classList.contains('is-valid')) {
                    validateField(input);
                }
            });
        });

        form.addEventListener('submit', function(event) {
            let formValid = true;
            inputs.forEach(input => {
                if (!validateField(input)) {
                    formValid = false;
                }
            });

            if (!formValid) {
                event.preventDefault();
                event.stopPropagation();
                const firstInvalid = form.querySelector('.is-invalid');
                if (firstInvalid) {
                    firstInvalid.scrollIntoView({ behavior: 'smooth', block: 'center' });
                    firstInvalid.focus();
                }
            }
        }, false);
    });

    // Video preview functionality
    document.addEventListener('DOMContentLoaded', function() {
        const videoUrlInput = document.getElementById('videoUrl');
        const videoPreview = document.getElementById('videoPreview');
        const videoFrame = document.getElementById('videoFrame');

        function updateVideoPreview() {
            const url = videoUrlInput.value.trim();

            if (url) {
                let embedUrl = '';

                // YouTube
                if (url.includes('youtube.com') || url.includes('youtu.be')) {
                    const videoId = url.includes('youtu.be')
                        ? url.split('youtu.be/')[1]?.split('?')[0]
                        : url.split('v=')[1]?.split('&')[0];

                    if (videoId) {
                        embedUrl = `https://www.youtube.com/embed/${videoId}`;
                    }
                }
                // Vimeo
                else if (url.includes('vimeo.com')) {
                    const videoId = url.split('vimeo.com/')[1]?.split('/')[0];
                    if (videoId) {
                        embedUrl = `https://player.vimeo.com/video/${videoId}`;
                    }
                }

                if (embedUrl) {
                    videoFrame.src = embedUrl;
                    videoPreview.style.display = 'block';
                } else {
                    videoPreview.style.display = 'none';
                }
            } else {
                videoPreview.style.display = 'none';
                videoFrame.src = '';
            }
        }

        // Initial check
        updateVideoPreview();

        // Listen for changes to video URL input - update preview in real-time
        videoUrlInput.addEventListener('input', function() {
            updateVideoPreview();
        });
    });

    // --- AUTO-FETCH DURATION LOGIC ---
    let ytAutoPlayer;
    let isYtApiReady = false;

    // Load YouTube IFrame API
    (function() {
        var tag = document.createElement('script');
        tag.src = "https://www.youtube.com/iframe_api";
        var firstScriptTag = document.getElementsByTagName('script')[0];
        firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);
    })();

    window.onYouTubeIframeAPIReady = function() {
        isYtApiReady = true;
    };

    // --- AUTO-FETCH LOGIC ---
    let fetchTimeout;

    // Trigger auto-fetch when URL changes (with debounce)
    document.getElementById('videoUrl').addEventListener('input', function() {
        clearTimeout(fetchTimeout);
        fetchTimeout = setTimeout(() => {
            const url = this.value.trim();
            if (url) {
                console.log("Auto-triggering duration fetch for:", url);
                fetchVideoDuration(url);
            }
        }, 1000); // Wait 1 second after user stops typing
    });

    // Manual button click
    document.getElementById('btn-auto-duration').addEventListener('click', function() {
        const url = document.getElementById('videoUrl').value.trim();
        if (!url) {
            alert('Vui lòng nhập link video trước!');
            return;
        }
        fetchVideoDuration(url);
    });

    function fetchVideoDuration(url) {
        const durationInput = document.getElementById('duration');
        const loading = document.getElementById('duration-loading');
        
        // Don't overwrite if already has value (optional, but better for UX if they edited manually)
        // if (durationInput.value) return; 

        loading.style.display = 'block';

        // 1. YouTube
        if (url.includes('youtube.com') || url.includes('youtu.be')) {
            const videoId = url.includes('youtu.be')
                ? url.split('youtu.be/')[1]?.split('?')[0]
                : url.split('v=')[1]?.split('&')[0];

            if (videoId && isYtApiReady) {
                let tempDiv = document.getElementById('yt-temp-container') || document.createElement('div');
                tempDiv.id = 'yt-temp-container';
                tempDiv.style.display = 'none';
                if (!tempDiv.parentNode) document.body.appendChild(tempDiv);
                
                tempDiv.innerHTML = '<div id="yt-auto-fetch-player"></div>';
                
                ytAutoPlayer = new YT.Player('yt-auto-fetch-player', {
                    height: '0',
                    width: '0',
                    videoId: videoId,
                    events: {
                        'onReady': function(event) {
                            const durationSeconds = event.target.getDuration();
                            if (durationSeconds > 0) {
                                durationInput.value = formatSeconds(durationSeconds);
                                durationInput.classList.add('is-valid');
                            }
                            loading.style.display = 'none';
                            tempDiv.innerHTML = '';
                        },
                        'onError': function() {
                            loading.style.display = 'none';
                        }
                    }
                });
            } else {
                loading.style.display = 'none';
            }
        } 
        // 2. Direct MP4
        else if (url.toLowerCase().slice(0, url.indexOf('?') > -1 ? url.indexOf('?') : url.length).endsWith('.mp4')) {
            const video = document.createElement('video');
            video.src = url;
            video.addEventListener('loadedmetadata', function() {
                durationInput.value = formatSeconds(video.duration);
                durationInput.classList.add('is-valid');
                loading.style.display = 'none';
            });
            video.addEventListener('error', function() {
                loading.style.display = 'none';
            });
        }
        // 3. Vimeo
        else if (url.includes('vimeo.com')) {
            const vimeoId = url.split('vimeo.com/')[1]?.split('/')[0];
            if (vimeoId) {
                fetch(`https://vimeo.com/api/oembed.json?url=https://vimeo.com/${vimeoId}`)
                    .then(response => response.json())
                    .then(data => {
                        durationInput.value = formatSeconds(data.duration);
                        durationInput.classList.add('is-valid');
                        loading.style.display = 'none';
                    })
                    .catch(() => {
                        loading.style.display = 'none';
                    });
            } else {
                loading.style.display = 'none';
            }
        }
        else {
            loading.style.display = 'none';
        }
    }

    function formatSeconds(seconds) {
        if (!seconds) return "00:00";
        const h = Math.floor(seconds / 3600);
        const m = Math.floor((seconds % 3600) / 60);
        const s = Math.floor(seconds % 60);
        
        let result = "";
        if (h > 0) {
            result += (h < 10 ? "0" + h : h) + ":";
        }
        result += (m < 10 ? "0" + m : m) + ":" + (s < 10 ? "0" + s : s);
        return result;
    }