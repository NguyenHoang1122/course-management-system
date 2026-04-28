// ===================================================
//  REGISTER FORM - FULL VALIDATION MODULE
// ===================================================

const RULES = {
    userName: {
        required: true,
        minLength: 4,
        maxLength: 30,
        pattern: /^[a-zA-Z0-9_]+$/,
        messages: {
            required:  'Tên đăng nhập không được để trống',
            minLength: 'Tên đăng nhập phải có ít nhất 4 ký tự',
            maxLength: 'Tên đăng nhập không được vượt quá 30 ký tự',
            pattern:   'Chỉ được dùng chữ cái, số và dấu gạch dưới (_)'
        }
    },
    fullName: {
        required: true,
        minLength: 2,
        maxLength: 100,
        pattern: /^[\p{L} ]+$/u,
        messages: {
            required:  'Họ tên không được để trống',
            minLength: 'Họ tên phải có ít nhất 2 ký tự',
            maxLength: 'Họ tên không được vượt quá 100 ký tự',
            pattern:   'Họ tên không được chứa số hoặc ký tự đặc biệt'
        }
    },
    email: {
        required: true,
        pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
        messages: {
            required: 'Email không được để trống',
            pattern:  'Email không đúng định dạng (ví dụ: name@gmail.com)'
        }
    },
    password: {
        required: true,
        minLength: 8,
        messages: {
            required:  'Mật khẩu không được để trống',
            minLength: 'Mật khẩu phải có ít nhất 8 ký tự',
            weak:      'Mật khẩu phải chứa chữ hoa, chữ thường và chữ số'
        }
    },
    confirmPassword: {
        required: true,
        messages: {
            required: 'Vui lòng xác nhận mật khẩu',
            mismatch: 'Mật khẩu xác nhận không khớp'
        }
    }
};

// ---- Helpers ----

function showError(fieldId, msg) {
    const input   = document.getElementById(fieldId);
    const errDiv  = document.getElementById('err-' + fieldId);
    const errMsg  = document.getElementById('err-' + fieldId + '-msg');
    const wrapper = input ? input.closest('.input-wrapper') : null;

    if (input)  { input.classList.add('is-invalid'); input.classList.remove('is-valid'); }
    if (errDiv) { errDiv.style.display = 'flex'; }
    if (errMsg) { errMsg.textContent   = msg; }
    if (wrapper) {
        wrapper.querySelector('.icon-valid')  && (wrapper.querySelector('.icon-valid').style.display   = 'none');
        wrapper.querySelector('.icon-invalid') && (wrapper.querySelector('.icon-invalid').style.display = 'inline');
    }
}

function showSuccess(fieldId) {
    const input   = document.getElementById(fieldId);
    const errDiv  = document.getElementById('err-' + fieldId);
    const wrapper = input ? input.closest('.input-wrapper') : null;

    if (input)  { input.classList.add('is-valid'); input.classList.remove('is-invalid'); }
    if (errDiv) { errDiv.style.display = 'none'; }
    if (wrapper) {
        wrapper.querySelector('.icon-valid')  && (wrapper.querySelector('.icon-valid').style.display   = 'inline');
        wrapper.querySelector('.icon-invalid') && (wrapper.querySelector('.icon-invalid').style.display = 'none');
    }
}

function clearState(fieldId) {
    const input   = document.getElementById(fieldId);
    const errDiv  = document.getElementById('err-' + fieldId);
    const wrapper = input ? input.closest('.input-wrapper') : null;

    if (input)  { input.classList.remove('is-valid', 'is-invalid'); }
    if (errDiv) { errDiv.style.display = 'none'; }
    if (wrapper) {
        wrapper.querySelector('.icon-valid')  && (wrapper.querySelector('.icon-valid').style.display   = 'none');
        wrapper.querySelector('.icon-invalid') && (wrapper.querySelector('.icon-invalid').style.display = 'none');
    }
}

// ---- Individual Validators ----

function validateUserName() {
    const val   = document.getElementById('username').value.trim();
    const rules = RULES.userName;
    if (!val)                          return showError('username', rules.messages.required),   false;
    if (val.length < rules.minLength)  return showError('username', rules.messages.minLength),  false;
    if (val.length > rules.maxLength)  return showError('username', rules.messages.maxLength),  false;
    if (!rules.pattern.test(val))      return showError('username', rules.messages.pattern),    false;
    showSuccess('username');
    return true;
}

function validateFullName() {
    const val   = document.getElementById('fullName').value.trim();
    const rules = RULES.fullName;
    if (!val)                          return showError('fullName', rules.messages.required),   false;
    if (val.length < rules.minLength)  return showError('fullName', rules.messages.minLength),  false;
    if (val.length > rules.maxLength)  return showError('fullName', rules.messages.maxLength),  false;
    if (!rules.pattern.test(val))      return showError('fullName', rules.messages.pattern),    false;
    showSuccess('fullName');
    return true;
}

function validateEmail() {
    const val   = document.getElementById('email').value.trim();
    const rules = RULES.email;
    if (!val)                      return showError('email', rules.messages.required), false;
    if (!rules.pattern.test(val))  return showError('email', rules.messages.pattern),  false;
    showSuccess('email');
    return true;
}

function validatePassword() {
    const val   = document.getElementById('password').value;
    const rules = RULES.password;
    if (!val)                         return showError('password', rules.messages.required),   false;
    if (val.length < rules.minLength) return showError('password', rules.messages.minLength),  false;
    if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/.test(val))
                                      return showError('password', rules.messages.weak), false;
    showSuccess('password');
    return true;
}

function validateConfirmPassword() {
    const pass    = document.getElementById('password').value;
    const confirm = document.getElementById('confirmPassword').value;
    const rules   = RULES.confirmPassword;
    if (!confirm)          return showError('confirmPassword', rules.messages.required),  false;
    if (pass !== confirm)  return showError('confirmPassword', rules.messages.mismatch),  false;
    showSuccess('confirmPassword');
    return true;
}

// ---- Password Strength ----

function getStrength(pwd) {
    let score = 0;
    if (pwd.length >= 8)  score++;
    if (pwd.length >= 12) score++;
    if (/[a-z]/.test(pwd)) score++;
    if (/[A-Z]/.test(pwd)) score++;
    if (/\d/.test(pwd))    score++;
    if (/[^a-zA-Z0-9]/.test(pwd)) score++;
    return score;
}

function updateStrengthBar(pwd) {
    const bar   = document.getElementById('strengthBar');
    const fill  = document.getElementById('strengthFill');
    const label = document.getElementById('strengthLabel');

    bar.style.display   = pwd ? 'block' : 'none';
    label.style.display = pwd ? 'block' : 'none';

    const score = getStrength(pwd);
    let width, color, text;

    if (score <= 2)      { width = '33%'; color = '#ef4444'; text = '🔴 Yếu'; }
    else if (score <= 4) { width = '66%'; color = '#f59e0b'; text = '🟡 Trung bình'; }
    else                 { width = '100%'; color = '#10b981'; text = '🟢 Mạnh'; }

    fill.style.width      = width;
    fill.style.background = color;
    label.textContent     = `Độ mạnh mật khẩu: ${text}`;
    label.style.color     = color;
}

function updateRequirements(pwd) {
    const toggle = (id, test) => {
        const li = document.getElementById(id);
        const i  = li.querySelector('i');
        if (test) {
            li.classList.add('req-met');
            i.className = 'fas fa-check-circle';
        } else {
            li.classList.remove('req-met');
            i.className = 'fas fa-circle';
        }
    };
    toggle('req-length', pwd.length >= 8);
    toggle('req-upper',  /[A-Z]/.test(pwd));
    toggle('req-lower',  /[a-z]/.test(pwd));
    toggle('req-digit',  /\d/.test(pwd));
}

// ---- Toggle Password Visibility ----

function togglePasswordRegister(fieldId) {
    const input      = document.getElementById(fieldId);
    const toggleBtn  = event.target.closest('.password-toggle');
    const icon       = toggleBtn.querySelector('i');

    if (input.type === 'password') {
        input.type = 'text';
        icon.classList.replace('fa-eye', 'fa-eye-slash');
    } else {
        input.type = 'password';
        icon.classList.replace('fa-eye-slash', 'fa-eye');
    }
}

// ---- Event Listeners (real-time) ----

document.getElementById('username').addEventListener('blur',  validateUserName);
document.getElementById('username').addEventListener('input', function() {
    if (this.value) validateUserName(); else clearState('username');
});

document.getElementById('fullName').addEventListener('blur',  validateFullName);
document.getElementById('fullName').addEventListener('input', function() {
    if (this.value) validateFullName(); else clearState('fullName');
});

document.getElementById('email').addEventListener('blur',  validateEmail);
document.getElementById('email').addEventListener('input', function() {
    if (this.value) validateEmail(); else clearState('email');
});

document.getElementById('password').addEventListener('input', function() {
    const pwd = this.value;
    updateStrengthBar(pwd);
    updateRequirements(pwd);
    if (pwd) validatePassword(); else clearState('password');
    // Re-validate confirm if already typed
    const confirmVal = document.getElementById('confirmPassword').value;
    if (confirmVal) validateConfirmPassword();
});

document.getElementById('confirmPassword').addEventListener('input', function() {
    if (this.value) validateConfirmPassword(); else clearState('confirmPassword');
});

document.getElementById('confirmPassword').addEventListener('blur', validateConfirmPassword);

// ---- Form Submit ----

document.getElementById('registerForm').addEventListener('submit', function(e) {
    const v1 = validateUserName();
    const v2 = validateFullName();
    const v3 = validateEmail();
    const v4 = validatePassword();
    const v5 = validateConfirmPassword();

    const terms = document.getElementById('terms');
    const termsErr = document.getElementById('err-terms');
    let   v6 = true;
    if (!terms.checked) {
        termsErr.style.display = 'flex';
        v6 = false;
    } else {
        termsErr.style.display = 'none';
    }

    if (!v1 || !v2 || !v3 || !v4 || !v5 || !v6) {
        e.preventDefault();
        // Scroll to first error
        const firstInvalid = document.querySelector('.is-invalid');
        if (firstInvalid) {
            firstInvalid.scrollIntoView({ behavior: 'smooth', block: 'center' });
            firstInvalid.focus();
        }
    }
});

// ---- On page load: apply server-side errors visually ----
document.querySelectorAll('input.is-invalid').forEach(function(input) {
    const wrapper = input.closest('.input-wrapper');
    if (wrapper) {
        const icon = wrapper.querySelector('.icon-invalid');
        if (icon) icon.style.display = 'inline';
    }
});