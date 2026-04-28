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