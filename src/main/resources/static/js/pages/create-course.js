    // Form validation and Real-time feedback
    document.addEventListener('DOMContentLoaded', function() {
        const form = document.getElementById('courseForm');
        const inputs = form.querySelectorAll('input, textarea, select');

        function validateField(input) {
            if (input.id === 'preview-date') return true;
            
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

    // Live preview
    document.addEventListener('DOMContentLoaded', function() {
        const titleInput = document.getElementById('title');
        const descriptionInput = document.getElementById('description');
        const priceInput = document.getElementById('price');
        const instructorSelect = document.getElementById('instructorId');
        const categoryInput = document.getElementById('category');
        const imageUrlInput = document.getElementById('imageUrl');

        const previewTitle = document.getElementById('preview-title');
        const previewDescription = document.getElementById('preview-description');
        const previewPrice = document.getElementById('preview-price');
        const previewInstructor = document.getElementById('preview-instructor');
        const previewCategory = document.getElementById('preview-category');
        const previewImg = document.getElementById('preview-img');
        const previewImgPlaceholder = document.getElementById('preview-img-placeholder');

        // Set current date for preview
        const today = new Date();
        document.getElementById('preview-date').textContent = today.toLocaleDateString('vi-VN');

        // Update preview on input
        titleInput.addEventListener('input', function() {
            previewTitle.textContent = this.value || 'Tên Khóa Học';
        });

        descriptionInput.addEventListener('input', function() {
            previewDescription.textContent = this.value || 'Mô tả khóa học sẽ hiển thị ở đây...';
        });

        priceInput.addEventListener('input', function() {
            const price = parseFloat(this.value) || 0;
            previewPrice.textContent = price.toLocaleString('vi-VN') + ' VND';
        });

        instructorSelect.addEventListener('change', function() {
            const selectedOption = this.options[this.selectedIndex];
            previewInstructor.textContent = selectedOption.text || 'Chưa chọn giảng viên';
        });

        categoryInput.addEventListener('input', function() {
            previewCategory.textContent = this.value || 'Chưa có danh mục';
        });

        imageUrlInput.addEventListener('input', function() {
            if (this.value) {
                let url = this.value;
                // Nếu là đường dẫn tương đối (như 'uploads/abc.png'), thêm / vào đầu để preview đúng từ gốc context
                if (!url.startsWith('http') && !url.startsWith('/')) {
                    url = '/' + url;
                }
                previewImg.src = url;
                previewImg.style.display = 'block';
                previewImgPlaceholder.style.display = 'none';
            } else {
                previewImg.style.display = 'none';
                previewImgPlaceholder.style.display = 'flex';
            }
        });
    });