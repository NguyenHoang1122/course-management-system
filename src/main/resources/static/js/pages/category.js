function editCategory() {
    document.getElementById('editCategoryModal').classList.add('show');
}
function hideEditModal() {
    document.getElementById('editCategoryModal').classList.remove('show');
}
function showDeleteConfirm() {
    document.getElementById('deleteConfirmModal').classList.add('show');
}

function hideDeleteConfirm() {
    document.getElementById('deleteConfirmModal').classList.remove('show');
}

window.addEventListener('click', function(event) {
    const deleteModal = document.getElementById('deleteConfirmModal');
    const editModal = document.getElementById('editCategoryModal');

    if (event.target === deleteModal) {
        deleteModal.classList.remove('show');
    }
    if (event.target === editModal) {
        editModal.classList.remove('show');
    }
});