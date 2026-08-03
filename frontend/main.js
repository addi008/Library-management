const API_BASE = "http://localhost:4567/api";

// State cache
let booksCache = [];
let membersCache = [];
let transactionsCache = [];
let finesCache = [];
let reservationsCache = [];

document.addEventListener("DOMContentLoaded", () => {
    initNavigation();
    initForms();
    checkApiHealth();
    loadAllData();
});

// ----------------------------------------------------
// NAVIGATION & VIEWS
// ----------------------------------------------------
function initNavigation() {
    const navItems = document.querySelectorAll(".nav-item");
    navItems.forEach(item => {
        item.addEventListener("click", (e) => {
            e.preventDefault();
            const viewTarget = item.getAttribute("data-view");
            switchView(viewTarget);
        });
    });
}

function switchView(viewName) {
    document.querySelectorAll(".nav-item").forEach(el => el.classList.remove("active"));
    document.querySelectorAll(".view-panel").forEach(el => el.classList.remove("active"));

    const navEl = document.querySelector(`.nav-item[data-view="${viewName}"]`);
    const viewEl = document.getElementById(`view-${viewName}`);

    if (navEl && viewEl) {
        navEl.classList.add("active");
        viewEl.classList.add("active");
        document.getElementById("view-title").textContent = navEl.textContent.trim() + " Overview";
    }

    if (viewName === "dashboard") loadDashboard();
    if (viewName === "books") renderBooksTable();
    if (viewName === "members") renderMembersTable();
    if (viewName === "transactions") renderTransactionsTable();
    if (viewName === "fines") renderFinesTable();
    if (viewName === "reservations") renderReservationsTable();
    if (viewName === "reports") loadReports();
}

// ----------------------------------------------------
// API HEALTH & FETCH HELPERS
// ----------------------------------------------------
async function checkApiHealth() {
    try {
        const res = await fetch(`${API_BASE}/health`);
        if (res.ok) {
            document.querySelector(".status-dot").classList.add("online");
            document.getElementById("api-status-text").textContent = "Connected (4567)";
        } else {
            throw new Error();
        }
    } catch (e) {
        document.querySelector(".status-dot").classList.remove("online");
        document.getElementById("api-status-text").textContent = "Offline";
        showToast("⚠️ Cannot connect to Java REST API at http://localhost:4567/api", "error");
    }
}

async function apiRequest(endpoint, options = {}) {
    try {
        const res = await fetch(`${API_BASE}${endpoint}`, {
            headers: { "Content-Type": "application/json" },
            ...options
        });
        const data = await res.json();
        if (!res.ok) {
            throw new Error(data.error || "API Request Failed");
        }
        return data;
    } catch (err) {
        showToast(err.message, "error");
        throw err;
    }
}

// ----------------------------------------------------
// DATA LOADERS
// ----------------------------------------------------
async function loadAllData() {
    try {
        booksCache = await apiRequest("/books");
        membersCache = await apiRequest("/members");
        transactionsCache = await apiRequest("/transactions");
        finesCache = await apiRequest("/fines");
        reservationsCache = await apiRequest("/reservations");
        loadDashboard();
    } catch (e) {
        console.error("Failed to load initial data", e);
    }
}

async function loadDashboard() {
    try {
        booksCache = await apiRequest("/books");
        membersCache = await apiRequest("/members");
        transactionsCache = await apiRequest("/transactions");
        
        document.getElementById("stat-books-count").textContent = booksCache.length;
        document.getElementById("stat-members-count").textContent = membersCache.length;
        
        const activeTx = transactionsCache.filter(t => t.status === "ISSUED" || t.status === "OVERDUE");
        document.getElementById("stat-issued-count").textContent = activeTx.length;

        const finesData = await apiRequest("/reports/fines-collected");
        document.getElementById("stat-fines-collected").textContent = `$${(finesData.totalCollected || 0).toFixed(2)}`;

        const topBooks = await apiRequest("/reports/most-borrowed");
        const topBooksTb = document.getElementById("dash-top-books");
        topBooksTb.innerHTML = topBooks.slice(0, 5).map(b => `
            <tr>
                <td><strong>${escapeHtml(b.title)}</strong></td>
                <td>${escapeHtml(b.author)}</td>
                <td><span class="badge badge-info">${b.borrowCount} times</span></td>
            </tr>
        `).join("") || "<tr><td colspan='3'>No borrowing history yet</td></tr>";

        const overdueTx = await apiRequest("/transactions/overdue");
        const overdueTb = document.getElementById("dash-overdue-list");
        overdueTb.innerHTML = overdueTx.map(t => {
            const member = membersCache.find(m => m.memberId === t.memberId);
            return `
                <tr>
                    <td>#${t.transactionId}</td>
                    <td>${member ? escapeHtml(member.name) : 'ID ' + t.memberId}</td>
                    <td><span class="badge badge-danger">${t.dueDate}</span></td>
                </tr>
            `;
        }).join("") || "<tr><td colspan='3'>No overdue transactions! 🎉</td></tr>";

    } catch (e) {
        console.error("Dashboard error", e);
    }
}

// ----------------------------------------------------
// BOOKS RENDERING & SEARCH
// ----------------------------------------------------
async function renderBooksTable() {
    booksCache = await apiRequest("/books");
    const tbody = document.getElementById("books-table-body");
    tbody.innerHTML = booksCache.map(b => `
        <tr>
            <td>#${b.bookId}</td>
            <td><strong>${escapeHtml(b.title)}</strong></td>
            <td>${escapeHtml(b.author)}</td>
            <td><code>${escapeHtml(b.isbn)}</code></td>
            <td><span class="badge badge-secondary">${escapeHtml(b.category)}</span></td>
            <td><span class="badge ${b.availableCopies > 0 ? 'badge-success' : 'badge-danger'}">${b.availableCopies}</span></td>
            <td>${b.totalCopies}</td>
            <td>
                <button class="btn btn-secondary btn-sm" onclick="deleteBook(${b.bookId})">🗑️ Delete</button>
            </td>
        </tr>
    `).join("") || "<tr><td colspan='8'>No books registered.</td></tr>";
}

document.getElementById("search-books-input")?.addEventListener("input", async (e) => {
    const q = e.target.value.trim();
    if (!q) {
        renderBooksTable();
        return;
    }
    const results = await apiRequest(`/books/search?q=${encodeURIComponent(q)}`);
    const tbody = document.getElementById("books-table-body");
    tbody.innerHTML = results.map(b => `
        <tr>
            <td>#${b.bookId}</td>
            <td><strong>${escapeHtml(b.title)}</strong></td>
            <td>${escapeHtml(b.author)}</td>
            <td><code>${escapeHtml(b.isbn)}</code></td>
            <td><span class="badge badge-secondary">${escapeHtml(b.category)}</span></td>
            <td><span class="badge ${b.availableCopies > 0 ? 'badge-success' : 'badge-danger'}">${b.availableCopies}</span></td>
            <td>${b.totalCopies}</td>
            <td>
                <button class="btn btn-secondary btn-sm" onclick="deleteBook(${b.bookId})">🗑️ Delete</button>
            </td>
        </tr>
    `).join("") || "<tr><td colspan='8'>No matching books found.</td></tr>";
});

async function deleteBook(id) {
    if (!confirm(`Delete book ID #${id}?`)) return;
    try {
        await apiRequest(`/books/${id}`, { method: "DELETE" });
        showToast(`Book #${id} deleted successfully.`, "success");
        renderBooksTable();
    } catch (e) {}
}

// ----------------------------------------------------
// MEMBERS RENDERING
// ----------------------------------------------------
async function renderMembersTable() {
    membersCache = await apiRequest("/members");
    const tbody = document.getElementById("members-table-body");
    tbody.innerHTML = membersCache.map(m => `
        <tr>
            <td>#${m.memberId}</td>
            <td><strong>${escapeHtml(m.name)}</strong></td>
            <td>${escapeHtml(m.email)}</td>
            <td>${escapeHtml(m.phone)}</td>
            <td><span class="badge badge-info">${m.membershipType}</span></td>
            <td>${m.membershipDate}</td>
            <td>
                <button class="btn btn-secondary btn-sm" onclick="deleteMember(${m.memberId})">🗑️ Delete</button>
            </td>
        </tr>
    `).join("") || "<tr><td colspan='7'>No members registered.</td></tr>";
}

async function deleteMember(id) {
    if (!confirm(`Delete member ID #${id}?`)) return;
    try {
        await apiRequest(`/members/${id}`, { method: "DELETE" });
        showToast(`Member #${id} deleted.`, "success");
        renderMembersTable();
    } catch (e) {}
}

// ----------------------------------------------------
// TRANSACTIONS RENDERING
// ----------------------------------------------------
async function renderTransactionsTable() {
    transactionsCache = await apiRequest("/transactions");
    const tbody = document.getElementById("transactions-table-body");
    tbody.innerHTML = transactionsCache.map(t => {
        let badgeClass = "badge-info";
        if (t.status === "RETURNED") badgeClass = "badge-success";
        if (t.status === "OVERDUE") badgeClass = "badge-danger";

        return `
            <tr>
                <td>#${t.transactionId}</td>
                <td>#${t.bookId}</td>
                <td>#${t.memberId}</td>
                <td>${t.issueDate}</td>
                <td>${t.dueDate}</td>
                <td>${t.returnDate || '--'}</td>
                <td><span class="badge ${badgeClass}">${t.status}</span></td>
                <td>
                    ${t.status === 'ISSUED' || t.status === 'OVERDUE' ? 
                        `<button class="btn btn-primary btn-sm" onclick="returnBook(${t.transactionId})">↩️ Return</button>` : 
                        `<span class="text-muted">Completed</span>`}
                </td>
            </tr>
        `;
    }).join("") || "<tr><td colspan='8'>No transaction records.</td></tr>";
}

async function returnBook(transId) {
    try {
        const res = await apiRequest("/transactions/return", {
            method: "POST",
            body: JSON.stringify({ transactionId: transId })
        });
        showToast(`Book transaction #${transId} returned successfully! Status: ${res.status}`, "success");
        renderTransactionsTable();
    } catch (e) {}
}

// ----------------------------------------------------
// FINES RENDERING
// ----------------------------------------------------
async function renderFinesTable() {
    finesCache = await apiRequest("/fines");
    const tbody = document.getElementById("fines-table-body");
    tbody.innerHTML = finesCache.map(f => `
        <tr>
            <td>#${f.fineId}</td>
            <td>#${f.transactionId}</td>
            <td><strong>$${parseFloat(f.amount).toFixed(2)}</strong></td>
            <td><span class="badge ${f.paid ? 'badge-success' : 'badge-danger'}">${f.paid ? 'PAID' : 'UNPAID'}</span></td>
            <td>${f.paidDate || '--'}</td>
            <td>
                ${!f.paid ? 
                    `<button class="btn btn-primary btn-sm" onclick="payFine(${f.fineId})">💳 Pay $${parseFloat(f.amount).toFixed(2)}</button>` : 
                    `<span class="badge badge-success">Paid</span>`}
            </td>
        </tr>
    `).join("") || "<tr><td colspan='6'>No fine records.</td></tr>";
}

async function payFine(fineId) {
    try {
        await apiRequest(`/fines/${fineId}/pay`, { method: "POST" });
        showToast(`Fine #${fineId} marked as paid!`, "success");
        renderFinesTable();
    } catch (e) {}
}

// ----------------------------------------------------
// RESERVATIONS RENDERING
// ----------------------------------------------------
async function renderReservationsTable() {
    reservationsCache = await apiRequest("/reservations");
    const tbody = document.getElementById("reservations-table-body");
    tbody.innerHTML = reservationsCache.map(r => `
        <tr>
            <td>#${r.reservationId}</td>
            <td>#${r.bookId}</td>
            <td>#${r.memberId}</td>
            <td>${r.reservationDate}</td>
            <td><span class="badge ${r.status === 'PENDING' ? 'badge-warning' : 'badge-success'}">${r.status}</span></td>
        </tr>
    `).join("") || "<tr><td colspan='5'>No reservations recorded.</td></tr>";
}

// ----------------------------------------------------
// REPORTS RENDERING
// ----------------------------------------------------
async function loadReports() {
    const activeMembers = await apiRequest("/reports/active-members");
    const activeTb = document.getElementById("report-active-members");
    activeTb.innerHTML = activeMembers.map(m => `
        <tr>
            <td>#${m.memberId}</td>
            <td><strong>${escapeHtml(m.name)}</strong></td>
            <td><span class="badge badge-info">${m.transactionCount} transactions</span></td>
        </tr>
    `).join("") || "<tr><td colspan='3'>No activity recorded</td></tr>";

    const unpaidFines = await apiRequest("/reports/unpaid-fines");
    const unpaidTb = document.getElementById("report-unpaid-fines");
    unpaidTb.innerHTML = unpaidFines.map(u => `
        <tr>
            <td><strong>${escapeHtml(u.name)}</strong></td>
            <td>${escapeHtml(u.email)}</td>
            <td><span class="badge badge-danger">$${parseFloat(u.totalUnpaidFine).toFixed(2)}</span></td>
        </tr>
    `).join("") || "<tr><td colspan='3'>No unpaid fines! 🎉</td></tr>";
}

// ----------------------------------------------------
// FORM HANDLERS & MODALS
// ----------------------------------------------------
function initForms() {
    document.getElementById("form-add-book")?.addEventListener("submit", async (e) => {
        e.preventDefault();
        const book = {
            title: document.getElementById("add-book-title").value,
            author: document.getElementById("add-book-author").value,
            isbn: document.getElementById("add-book-isbn").value,
            category: document.getElementById("add-book-category").value,
            totalCopies: parseInt(document.getElementById("add-book-copies").value)
        };
        try {
            await apiRequest("/books", { method: "POST", body: JSON.stringify(book) });
            showToast("Book added successfully!", "success");
            closeModal("modal-add-book");
            renderBooksTable();
        } catch (err) {}
    });

    document.getElementById("form-add-member")?.addEventListener("submit", async (e) => {
        e.preventDefault();
        const member = {
            name: document.getElementById("add-member-name").value,
            email: document.getElementById("add-member-email").value,
            phone: document.getElementById("add-member-phone").value,
            membershipType: document.getElementById("add-member-type").value
        };
        try {
            await apiRequest("/members", { method: "POST", body: JSON.stringify(member) });
            showToast("Member registered successfully!", "success");
            closeModal("modal-add-member");
            renderMembersTable();
        } catch (err) {}
    });

    document.getElementById("form-issue-book")?.addEventListener("submit", async (e) => {
        e.preventDefault();
        const body = {
            memberId: parseInt(document.getElementById("issue-select-member").value),
            bookId: parseInt(document.getElementById("issue-select-book").value)
        };
        try {
            await apiRequest("/transactions/issue", { method: "POST", body: JSON.stringify(body) });
            showToast("Book issued successfully!", "success");
            closeModal("modal-issue-book");
            renderTransactionsTable();
        } catch (err) {}
    });

    document.getElementById("form-reserve-book")?.addEventListener("submit", async (e) => {
        e.preventDefault();
        const body = {
            memberId: parseInt(document.getElementById("reserve-select-member").value),
            bookId: parseInt(document.getElementById("reserve-select-book").value)
        };
        try {
            await apiRequest("/reservations", { method: "POST", body: JSON.stringify(body) });
            showToast("Reservation placed successfully!", "success");
            closeModal("modal-reserve-book");
            renderReservationsTable();
        } catch (err) {}
    });
}

function openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (!modal) return;
    
    if (modalId === "modal-issue-book") {
        populateSelectOptions("issue-select-member", membersCache, "memberId", m => `${m.name} (ID #${m.memberId})`);
        populateSelectOptions("issue-select-book", booksCache.filter(b => b.availableCopies > 0), "bookId", b => `${b.title} (${b.availableCopies} available)`);
    }

    if (modalId === "modal-reserve-book") {
        populateSelectOptions("reserve-select-member", membersCache, "memberId", m => `${m.name} (ID #${m.memberId})`);
        populateSelectOptions("reserve-select-book", booksCache.filter(b => b.availableCopies === 0), "bookId", b => `${b.title} (0 available)`);
    }

    modal.classList.add("active");
}

function closeModal(modalId) {
    document.getElementById(modalId)?.classList.remove("active");
}

function populateSelectOptions(selectId, items, valKey, labelFn) {
    const select = document.getElementById(selectId);
    if (!select) return;
    select.innerHTML = items.map(item => `
        <option value="${item[valKey]}">${escapeHtml(labelFn(item))}</option>
    `).join("");
}

// ----------------------------------------------------
// TOAST & UTILS
// ----------------------------------------------------
function showToast(msg, type = "success") {
    const container = document.getElementById("toast-container");
    const toast = document.createElement("div");
    toast.className = `toast toast-${type}`;
    toast.textContent = msg;
    container.appendChild(toast);
    setTimeout(() => {
        toast.remove();
    }, 4000);
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str).replace(/[&<>"']/g, m => ({
        '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;'
    })[m]);
}
