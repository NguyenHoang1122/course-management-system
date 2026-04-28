    function showAddSectionModal() {
        document.getElementById("addSectionModal").style.display = "flex";
    }

    function hideAddSectionModal() {
        document.getElementById("addSectionModal").style.display = "none";
    }

    function showAddResourceModal() {
        document.getElementById("addResourceModal").style.display = "flex";
    }

    function hideAddResourceModal() {
        document.getElementById("addResourceModal").style.display = "none";
    }

    function toggleResourceMode(isExternal) {
        document.getElementById("fileUploadGroup").style.display = isExternal ? "none" : "block";
        document.getElementById("externalLinkGroup").style.display = isExternal ? "block" : "none";
    }

    function showDeleteResourceConfirm(id) {
        document.getElementById("deleteResourceForm").action = '/admin/resources/delete/' + id;
        document.getElementById("deleteResourceConfirmBox").style.display = "flex";
    }

    function hideDeleteResourceConfirm() {
        document.getElementById("deleteResourceConfirmBox").style.display = "none";
    }

    function showDeleteSectionConfirm(id) {
        document.getElementById("deleteSectionForm").action = '/admin/sections/delete/' + id;
        document.getElementById("deleteSectionConfirmBox").style.display = "flex";
    }

    function hideDeleteSectionConfirm() {
        document.getElementById("deleteSectionConfirmBox").style.display = "none";
    }

    function showEditSectionModal(id, title) {
        document.getElementById("editSectionId").value = id;
        document.getElementById("editSectionTitle").value = title;
        document.getElementById("editSectionModal").style.display = "flex";
    }

    function hideEditSectionModal() {
        document.getElementById("editSectionModal").style.display = "none";
    }

    function showConfirm() {
        document.getElementById("confirmBox").style.display = "flex";
    }

    function hideConfirm() {
        document.getElementById("confirmBox").style.display = "none";
    }

    document.addEventListener('click', function (event) {
        if (event.target.closest('.btn-delete-lesson')) {
            const button = event.target.closest('.btn-delete-lesson');
            const lessonId = button.getAttribute('data-id');
            const lessonTitle = button.getAttribute('data-title');
            showLessonConfirm(lessonId, lessonTitle);
        }
        
        if (event.target.closest('.btn-edit-section')) {
            const button = event.target.closest('.btn-edit-section');
            const sectionId = button.getAttribute('data-id');
            const sectionTitle = button.getAttribute('data-title');
            showEditSectionModal(sectionId, sectionTitle);
        }
    });

    function showLessonConfirm(id, title) {
        document.getElementById("lessonTitleDisplay").innerText = title;
        document.getElementById("deleteLessonForm").action = '/admin/delete-lesson/' + id;
        document.getElementById("lessonConfirmBox").style.display = "flex";
    }

    function hideLessonConfirm() {
        document.getElementById("lessonConfirmBox").style.display = "none";
    }

    // Accordion functionality
    document.addEventListener('DOMContentLoaded', function() {
        // Use setTimeout to ensure DOM is fully rendered
        setTimeout(function() {
            const headers = document.querySelectorAll('.admin-acc-header');
            headers.forEach(header => {
                const targetId = header.getAttribute('data-acc-target');
                const body = document.getElementById(targetId);

                if (!body) {
                    console.error('Accordion body not found for target:', targetId);
                    return;
                }

                // Set initial state
                if (header.classList.contains('open')) {
                    body.classList.add('open');
                    body.style.maxHeight = body.scrollHeight + 'px';
                } else {
                    body.classList.remove('open');
                    body.style.maxHeight = '0';
                }

                // Add click event listener
                header.addEventListener('click', function() {
                    const isOpen = this.classList.contains('open');

                    if (isOpen) {
                        // Close the section
                        this.classList.remove('open');
                        body.classList.remove('open');
                        body.style.maxHeight = '0';
                    } else {
                        // Open the section
                        this.classList.add('open');
                        body.classList.add('open');
                        body.style.maxHeight = body.scrollHeight + 'px';
                    }
                });
            });
        }, 500); // Increased timeout to ensure DOM is ready
    });

    window.onclick = function(event) {
        if (event.target.classList.contains('modal-overlay')) {
            hideConfirm();
            hideLessonConfirm();
            hideAddSectionModal();
            hideEditSectionModal();
            hideDeleteSectionConfirm();
        }
    }

    document.addEventListener("keydown", function(event) {
        if (event.key === "Escape") {
            hideConfirm();
            hideLessonConfirm();
            hideAddSectionModal();
            hideEditSectionModal();
            hideDeleteSectionConfirm();
        }
    });