    document.addEventListener('DOMContentLoaded', function () {
        const now = new Date();
        document.querySelectorAll('.deleted-duration').forEach(function (cell) {
            const deletedAt = cell.getAttribute('data-deleted-at');
            if (!deletedAt) {
                cell.textContent = '-';
                return;
            }

            const deletedDate = new Date(deletedAt);
            const diffMs = now - deletedDate;
            if (Number.isNaN(diffMs) || diffMs < 0) {
                cell.textContent = '-';
                return;
            }

            const totalMinutes = Math.floor(diffMs / 60000);
            const days = Math.floor(totalMinutes / 1440);
            const hours = Math.floor((totalMinutes % 1440) / 60);
            const minutes = totalMinutes % 60;

            if (days > 0) {
                cell.textContent = days + ' ngày ' + hours + ' giờ';
                return;
            }

            if (hours > 0) {
                cell.textContent = hours + ' giờ ' + minutes + ' phút';
                return;
            }

            cell.textContent = Math.max(minutes, 1) + ' phút';
        });
    });