    document.addEventListener('click', function (e) {
        if (e.target.closest('.btn-delete-course')) {
            const btn = e.target.closest('.btn-delete-course');
            const id = btn.getAttribute('data-id');
            const title = btn.getAttribute('data-title');

            document.getElementById("courseTitleDisplay").innerText = title;
            document.getElementById("deleteCourseForm").action = '/admin/delete/' + id;
            document.getElementById("courseConfirmBox").style.display = "flex";
        }
    });

    function hideCourseConfirm() {
        document.getElementById("courseConfirmBox").style.display = "none";
    }