    document.addEventListener('click', function (e) {
        if (e.target.closest('.btn-delete-user')) {
            const btn = e.target.closest('.btn-delete-user');
            const id = btn.getAttribute('data-id');
            const name = btn.getAttribute('data-name');

            document.getElementById("userNameDisplay").innerText = name;
            document.getElementById("deleteUserForm").action = '/admin/users/' + id + '/delete';
            document.getElementById("userConfirmBox").style.display = "flex";
        }
    });

    function hideUserConfirm() {
        document.getElementById("userConfirmBox").style.display = "none";
    }