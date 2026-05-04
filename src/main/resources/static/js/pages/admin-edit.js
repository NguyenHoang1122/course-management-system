// =========================================================
//  PROFILE EDIT PAGE — FULL VALIDATION MODULE
// =========================================================

// ---------- Shared helpers ----------

function setValid(inputEl, msgId) {
    inputEl.classList.add('is-valid');
    inputEl.classList.remove('is-invalid');
    const msgEl = document.getElementById(msgId);
    if (msgEl) msgEl.textContent = '';
}

function setInvalid(inputEl, msgId, msg) {
    inputEl.classList.add('is-invalid');
    inputEl.classList.remove('is-valid');
    const msgEl = document.getElementById(msgId);
    if (msgEl) {
        msgEl.textContent = msg;
        // Hiển thị div feedback nếu nó đang bị ẩn (đối với Bootstrap 5 là d-block khi có is-invalid)
    }
}

function clearState(inputEl, msgId) {
    inputEl.classList.remove('is-valid', 'is-invalid');
    const msgEl = document.getElementById(msgId);
    if (msgEl) msgEl.textContent = '';
}

function togglePw(fieldId, toggleBtn) {
    const input = document.getElementById(fieldId);
    const icon  = toggleBtn.querySelector('i');
    if (input.type === 'password') {
        input.type = 'text';
        icon.classList.replace('fa-eye', 'fa-eye-slash');
    } else {
        input.type = 'password';
        icon.classList.replace('fa-eye-slash', 'fa-eye');
    }
}

// =========================================================
//  FORM 1 — PROFILE INFORMATION
// =========================================================

function validatePFullName() {
    const el  = document.getElementById('pFullName');
    const val = el.value.trim();
    if (!val)               return setInvalid(el, 'err-pFullName-msg', 'Họ tên không được để trống'), false;
    if (val.length < 2)     return setInvalid(el, 'err-pFullName-msg', 'Họ tên phải có ít nhất 2 ký tự'), false;
    if (val.length > 100)   return setInvalid(el, 'err-pFullName-msg', 'Họ tên không được vượt quá 100 ký tự'), false;
    if (!/^[\p{L} ]+$/u.test(val)) return setInvalid(el, 'err-pFullName-msg', 'Họ tên không được chứa số hoặc ký tự đặc biệt'), false;
    setValid(el, 'err-pFullName-msg');
    return true;
}

function validatePEmail() {
    const el  = document.getElementById('pEmail');
    const val = el.value.trim();
    if (!val)  return setInvalid(el, 'err-pEmail-msg', 'Email không được để trống'), false;
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(val))
        return setInvalid(el, 'err-pEmail-msg', 'Email không đúng định dạng (ví dụ: name@gmail.com)'), false;
    setValid(el, 'err-pEmail-msg');
    return true;
}

function validatePPhone() {
    const el  = document.getElementById('pPhone');
    const val = el.value.trim();
    if (!val) { clearState(el, 'err-pPhone-msg'); return true; }
    if (!/^\+?[0-9]{10,15}$/.test(val))
        return setInvalid(el, 'err-pPhone-msg', 'Số điện thoại không hợp lệ (10-15 chữ số)'), false;
    setValid(el, 'err-pPhone-msg');
    return true;
}

function validateAvatar() {
    const input   = document.getElementById('avatarFile');
    const msgId   = 'err-avatar-msg';

    if (!input.files || input.files.length === 0) return true;

    const file     = input.files[0];
    const maxSize  = 5 * 1024 * 1024; // 5MB
    const allowed  = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];

    if (!allowed.includes(file.type)) {
        setInvalid(input, msgId, 'Chỉ chấp nhận file ảnh JPG, PNG, GIF, WEBP');
        return false;
    }
    if (file.size > maxSize) {
        setInvalid(input, msgId, 'Kích thước file không được vượt quá 5MB');
        return false;
    }
    setValid(input, msgId);
    return true;
}

// Event Listeners for Profile Form
document.getElementById('pFullName').addEventListener('input', validatePFullName);
document.getElementById('pEmail').addEventListener('input', validatePEmail);
document.getElementById('pPhone').addEventListener('input', validatePPhone);

document.getElementById('avatarFile').addEventListener('change', function() {
    const previewBox = document.getElementById('avatarPreviewBox');
    const previewImg = document.getElementById('avatarPreviewImg');

    if (!validateAvatar()) {
        previewBox.style.display = 'none';
        return;
    }

    if (this.files && this.files[0]) {
        const reader = new FileReader();
        reader.onload = function(e) {
            previewImg.src = e.target.result;
            previewBox.style.display = 'block';
        };
        reader.readAsDataURL(this.files[0]);
    }
});

document.getElementById('profileForm').addEventListener('submit', function(e) {
    const v1 = validatePFullName();
    const v2 = validatePEmail();
    const v3 = validatePPhone();
    const v4 = validateAvatar();

    if (!v1 || !v2 || !v3 || !v4) {
        e.preventDefault();
        const first = this.querySelector('.is-invalid');
        if (first) { first.scrollIntoView({ behavior: 'smooth', block: 'center' }); first.focus(); }
    }
});

// =========================================================
//  FORM 2 — CHANGE PASSWORD (Sửa lại logic đồng bộ)
// =========================================================

function updateCpRequirements(pwd) {
    const toggle = (id, test) => {
        const li = document.getElementById(id);
        const icon = li.querySelector('i');
        if (test) {
            li.style.color = '#10b981';
            icon.className = 'fas fa-check-circle me-1';
        } else {
            li.style.color = '';
            icon.className = 'fas fa-circle me-1';
        }
    };
    toggle('cp-req-length', pwd.length >= 8);
    toggle('cp-req-upper',  /[A-Z]/.test(pwd));
    toggle('cp-req-digit',  /\d/.test(pwd));
}

function updateCpStrengthBar(pwd) {
    const bar = document.getElementById('cpStrengthBar');
    const fill = document.getElementById('cpStrengthFill');
    const label = document.getElementById('cpStrengthLabel');

    if (!pwd) {
        bar.style.display = 'none';
        label.textContent = '';
        return;
    }

    bar.style.display = 'block';
    let strength = 0;
    if (pwd.length >= 8) strength++;
    if (/[A-Z]/.test(pwd)) strength++;
    if (/\d/.test(pwd)) strength++;
    if (/[^A-Za-z0-9]/.test(pwd)) strength++;

    let color = '#ef4444';
    let width = '25%';
    let text = 'Yếu';

    if (strength === 2) { color = '#f59e0b'; width = '50%'; text = 'Trung bình'; }
    else if (strength === 3) { color = '#3b82f6'; width = '75%'; text = 'Tốt'; }
    else if (strength === 4) { color = '#10b981'; width = '100%'; text = 'Rất mạnh'; }

    fill.style.width = width;
    fill.style.backgroundColor = color;
    label.textContent = 'Độ mạnh: ' + text;
    label.style.color = color;
}

function validateCurrentPassword() {
    const el = document.getElementById('currentPassword');
    if (!el.value) return setInvalid(el, 'err-currentPassword-msg', 'Vui lòng nhập mật khẩu hiện tại'), false;
    setValid(el, 'err-currentPassword-msg');
    return true;
}

function validateNewPassword() {
    const el = document.getElementById('newPassword');
    const val = el.value;
    if (!val) return setInvalid(el, 'err-newPassword-msg', 'Mật khẩu mới không được để trống'), false;
    if (val.length < 8 || !/[A-Z]/.test(val) || !/\d/.test(val)) {
        return setInvalid(el, 'err-newPassword-msg', 'Mật khẩu không đạt yêu cầu bảo mật'), false;
    }
    setValid(el, 'err-newPassword-msg');
    return true;
}

function validateConfirmPassword() {
    const el = document.getElementById('cpConfirmPassword');
    const newPwd = document.getElementById('newPassword').value;
    if (!el.value) return setInvalid(el, 'err-cpConfirmPassword-msg', 'Vui lòng xác nhận mật khẩu'), false;
    if (el.value !== newPwd) return setInvalid(el, 'err-cpConfirmPassword-msg', 'Mật khẩu xác nhận không khớp'), false;
    setValid(el, 'err-cpConfirmPassword-msg');
    return true;
}

// Password Events
document.getElementById('newPassword').addEventListener('input', function() {
    updateCpStrengthBar(this.value);
    updateCpRequirements(this.value);
    validateNewPassword();
});

document.getElementById('cpConfirmPassword').addEventListener('input', validateConfirmPassword);
document.getElementById('currentPassword').addEventListener('input', validateCurrentPassword);

document.getElementById('changePasswordForm').addEventListener('submit', function(e) {
    const v1 = validateCurrentPassword();
    const v2 = validateNewPassword();
    const v3 = validateConfirmPassword();
    if (!v1 || !v2 || !v3) e.preventDefault();
});