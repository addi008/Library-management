/* ============================================================
   LibVerse — Library Management SPA
   Phase 8/9: Full loading states, validation, offline handling
   ============================================================ */

// Auto-detect: if served directly from Spark on 4567, use relative origin.
// If on a dev server (e.g. Python :8000), still point to :4567.
const API_BASE = (window.location.port === "4567")
    ? `${window.location.origin}/api`
    : "http://localhost:4567/api";
const HEALTH_CHECK_INTERVAL = 30_000; // 30 seconds

// ─── State Cache ───────────────────────────────────────────
let booksCache       = [];
let membersCache     = [];
let transactionsCache = [];
let finesCache       = [];
let reservationsCache = [];
let apiOnline        = false;

// ─── Boot ──────────────────────────────────────────────────
document.addEventListener("DOMContentLoaded", async () => {
    initNavigation();
    initForms();
    initSearchFilters();
    await checkApiHealth();
    if (apiOnline) {
        await loadAllData();
        loadDashboard();
    }
    setInterval(checkApiHealth, HEALTH_CHECK_INTERVAL);
});

// ============================================================
//  NAVIGATION
// ============================================================
function initNavigation() {
    document.querySelectorAll(".nav-item").forEach(item => {
        item.addEventListener("click", e => {
            e.preventDefault();
            switchView(item.getAttribute("data-view"));
        });
    });

    // Close modal when clicking backdrop
    document.querySelectorAll(".modal-overlay").forEach(overlay => {
        overlay.addEventListener("click", e => {
            if (e.target === overlay) overlay.classList.remove("active");
        });
    });

    // Close sidebar when clicking main content on mobile
    document.getElementById("main")?.addEventListener("click", () => {
        const sb = document.getElementById("sidebar");
        if (sb?.classList.contains("open")) sb.classList.remove("open");
    });

    // Close modal when pressing Escape key
    document.addEventListener("keydown", e => {
        if (e.key === "Escape") {
            document.querySelectorAll(".modal-overlay.active").forEach(m => m.classList.remove("active"));
        }
    });
}

function switchView(viewName) {
    document.querySelectorAll(".nav-item").forEach(el => el.classList.remove("active"));
    document.querySelectorAll(".view-panel").forEach(el => el.classList.remove("active"));

    const navEl  = document.querySelector(`.nav-item[data-view="${viewName}"]`);
    const viewEl = document.getElementById(`view-${viewName}`);
    if (navEl)  navEl.classList.add("active");
    if (viewEl) viewEl.classList.add("active");

    const titles = {
        dashboard:    "Dashboard Overview",
        books:        "📖 Books",
        members:      "👥 Members",
        transactions: "🔄 Transactions",
        fines:        "💰 Fines",
        reservations: "🔖 Reservations",
        reports:      "📈 Analytics",
    };
    const titleEl = document.getElementById("view-title");
    if (titleEl) titleEl.textContent = titles[viewName] || viewName;

    switch (viewName) {
        case "dashboard":    loadDashboard(); break;
        case "books":        renderBooksTable(); break;
        case "members":      renderMembersTable(); break;
        case "transactions": renderTransactionsTable(); break;
        case "fines":        renderFinesTable(); break;
        case "reservations": renderReservationsTable(); break;
        case "reports":      loadReports(); break;
    }
}

function toggleSidebar() {
    document.getElementById("sidebar")?.classList.toggle("open");
}

// ============================================================
//  API HEALTH & OFFLINE BANNER
// ============================================================
async function checkApiHealth() {
    try {
        const ctrl = new AbortController();
        const timeout = setTimeout(() => ctrl.abort(), 4000);
        const res = await fetch(`${API_BASE}/health`, { signal: ctrl.signal });
        clearTimeout(timeout);
        if (res.ok) {
            setApiStatus(true);
            return true;
        }
        throw new Error("Non-OK response");
    } catch {
        setApiStatus(false);
        return false;
    }
}

function setApiStatus(isOnline) {
    apiOnline = isOnline;
    const dot     = document.getElementById("status-dot");
    const text    = document.getElementById("api-status-text");
    const banner  = document.getElementById("offline-banner");

    if (isOnline) {
        dot?.classList.replace("offline", "online") || dot?.classList.add("online");
        dot?.classList.remove("offline");
        dot?.classList.add("online");
        if (text) text.textContent = "Connected (4567)";
        banner?.classList.remove("visible");
    } else {
        dot?.classList.remove("online");
        dot?.classList.add("offline");
        if (text) text.textContent = "Offline";
        banner?.classList.add("visible");
    }
}

async function retryConnection() {
    const btn = document.getElementById("btn-retry");
    if (btn) { btn.disabled = true; btn.textContent = "Retrying…"; }
    const ok = await checkApiHealth();
    if (btn) { btn.disabled = false; btn.textContent = "Retry Now"; }
    if (ok) {
        await loadAllData();
        showToast("✅ Reconnected to API!", "success");
        loadDashboard();
    } else {
        showToast("⚠️ Still unable to reach the API server.", "error");
    }
}

// ============================================================
//  API FETCH HELPER (with loading-state button support)
// ============================================================
async function apiRequest(endpoint, options = {}, loadingBtn = null) {
    if (loadingBtn) setButtonLoading(loadingBtn, true);
    try {
        const res = await fetch(`${API_BASE}${endpoint}`, {
            headers: { "Content-Type": "application/json" },
            ...options,
        });
        // Safely parse JSON — Spark may set content-type=json but body is an error page
        let data = {};
        try {
            const text = await res.text();
            if (text) data = JSON.parse(text);
        } catch {
            if (!res.ok) throw new Error(`Server error (HTTP ${res.status})`);
        }
        if (!res.ok) throw new Error(data.error || `HTTP ${res.status}`);
        if (loadingBtn) setButtonLoading(loadingBtn, false);
        return data;
    } catch (err) {
        if (loadingBtn) setButtonLoading(loadingBtn, false);
        if (err.name === "TypeError") {
            setApiStatus(false);
            showToast("⚠️ API unreachable. Is the Java server running?", "error");
        } else {
            showToast(`❌ ${err.message}`, "error");
        }
        throw err;
    }
}

// ============================================================
//  DATA LOADERS
// ============================================================
async function loadAllData() {
    try {
        [booksCache, membersCache, transactionsCache, finesCache, reservationsCache] =
            await Promise.all([
                apiRequest("/books"),
                apiRequest("/members"),
                apiRequest("/transactions"),
                apiRequest("/fines"),
                apiRequest("/reservations"),
            ]);
        populateFinesMemberFilter();
    } catch {/* handled in apiRequest */}
}

async function loadDashboard() {
    showLoading("dashboard", true);
    try {
        // Refresh live data in parallel
        const [books, members, txs, overdue, finesReport, topBooks] = await Promise.all([
            apiRequest("/books"),
            apiRequest("/members"),
            apiRequest("/transactions"),
            apiRequest("/transactions/overdue"),
            apiRequest("/reports/fines-collected"),
            apiRequest("/reports/most-borrowed"),
        ]);

        booksCache        = books;
        membersCache      = members;
        transactionsCache = txs;

        const activeCount  = txs.filter(t => t.status === "ISSUED" || t.status === "OVERDUE").length;

        setStatCard("stat-books-count",    books.length);
        setStatCard("stat-members-count",  members.length);
        setStatCard("stat-issued-count",   activeCount);
        setStatCard("stat-overdue-count",  overdue.length, overdue.length > 0 ? "danger" : "normal");
        setStatCard("stat-fines-collected", `$${(finesReport.totalCollected || 0).toFixed(2)}`);

        // Top books table
        const topBooksEl = document.getElementById("dash-top-books");
        if (topBooksEl) {
            topBooksEl.innerHTML = topBooks.slice(0, 5).map(b => `
                <tr>
                    <td><strong>${escapeHtml(b.title)}</strong></td>
                    <td class="text-muted">${escapeHtml(b.author)}</td>
                    <td><span class="badge badge-info">${b.borrowCount}×</span></td>
                </tr>
            `).join("") || emptyRow(3, "No borrowing history yet");
        }

        // Overdue table
        const overdueEl = document.getElementById("dash-overdue-list");
        if (overdueEl) {
            overdueEl.innerHTML = overdue.map(t => {
                const member = membersCache.find(m => m.memberId === t.memberId);
                return `
                    <tr>
                        <td><code>#${t.transactionId}</code></td>
                        <td>${member ? escapeHtml(member.name) : `ID #${t.memberId}`}</td>
                        <td><span class="badge badge-overdue">${t.dueDate}</span></td>
                    </tr>
                `;
            }).join("") || emptyRow(3, "🎉 No overdue transactions!");
        }
    } catch { /* already shown */ }
    finally   { showLoading("dashboard", false); }
}

// ============================================================
//  BOOKS
// ============================================================
async function renderBooksTable(filter = "") {
    showLoading("books", true);
    try {
        if (!filter) booksCache = await apiRequest("/books");
        const list = filter
            ? await apiRequest(`/books/search?q=${encodeURIComponent(filter)}`)
            : booksCache;

        const tbody = document.getElementById("books-table-body");
        if (!tbody) return;
        tbody.innerHTML = list.map(b => `
            <tr>
                <td><code>#${b.bookId}</code></td>
                <td><strong>${escapeHtml(b.title)}</strong></td>
                <td class="text-muted">${escapeHtml(b.author)}</td>
                <td><code>${escapeHtml(b.isbn)}</code></td>
                <td><span class="badge badge-secondary">${escapeHtml(b.category)}</span></td>
                <td>
                    <span class="badge ${b.availableCopies > 0 ? 'badge-success' : 'badge-danger'}">
                        ${b.availableCopies} / ${b.totalCopies}
                    </span>
                </td>
                <td>${b.totalCopies}</td>
                <td style="display:flex;gap:6px;flex-wrap:wrap">
                    <button class="btn btn-secondary btn-sm" onclick="openEditBook(${b.bookId})">✏️ Edit</button>
                    <button class="btn btn-danger btn-sm" onclick="deleteBook(${b.bookId})">🗑️</button>
                </td>
            </tr>
        `).join("") || emptyRow(8, "No books found.");
    } catch { /* shown */ }
    finally   { showLoading("books", false); }
}

function openEditBook(id) {
    const book = booksCache.find(b => b.bookId === id);
    if (!book) return;
    document.getElementById("modal-book-title").textContent = "Edit Book";
    document.getElementById("edit-book-id").value         = book.bookId;
    document.getElementById("add-book-title").value       = book.title;
    document.getElementById("add-book-author").value      = book.author;
    document.getElementById("add-book-isbn").value        = book.isbn;
    document.getElementById("add-book-category").value    = book.category;
    document.getElementById("add-book-copies").value      = book.totalCopies;
    clearValidation("form-add-book");
    openModal("modal-add-book");
}

async function deleteBook(id) {
    if (!confirm(`Delete book #${id}? This cannot be undone.`)) return;
    try {
        await apiRequest(`/books/${id}`, { method: "DELETE" });
        showToast(`🗑️ Book #${id} deleted.`, "success");
        renderBooksTable();
    } catch { /* shown */ }
}

// ============================================================
//  MEMBERS
// ============================================================
async function renderMembersTable(filter = "") {
    showLoading("members", true);
    try {
        membersCache = await apiRequest("/members");
        const list = filter
            ? membersCache.filter(m =>
                m.name.toLowerCase().includes(filter.toLowerCase()) ||
                m.email.toLowerCase().includes(filter.toLowerCase()))
            : membersCache;

        const tbody = document.getElementById("members-table-body");
        if (!tbody) return;
        tbody.innerHTML = list.map(m => `
            <tr>
                <td><code>#${m.memberId}</code></td>
                <td><strong>${escapeHtml(m.name)}</strong></td>
                <td class="text-muted">${escapeHtml(m.email)}</td>
                <td>${escapeHtml(m.phone)}</td>
                <td><span class="badge badge-info">${m.membershipType}</span></td>
                <td class="text-muted">${m.membershipDate}</td>
                <td style="display:flex;gap:6px;flex-wrap:wrap">
                    <button class="btn btn-secondary btn-sm" onclick="openEditMember(${m.memberId})">✏️ Edit</button>
                    <button class="btn btn-danger btn-sm" onclick="deleteMember(${m.memberId})">🗑️</button>
                </td>
            </tr>
        `).join("") || emptyRow(7, "No members registered.");
    } catch { /* shown */ }
    finally   { showLoading("members", false); }
}

function openEditMember(id) {
    const m = membersCache.find(m => m.memberId === id);
    if (!m) return;
    document.getElementById("modal-member-title").textContent = "Edit Member";
    document.getElementById("edit-member-id").value     = m.memberId;
    document.getElementById("add-member-name").value    = m.name;
    document.getElementById("add-member-email").value   = m.email;
    document.getElementById("add-member-phone").value   = m.phone;
    document.getElementById("add-member-type").value    = m.membershipType;
    clearValidation("form-add-member");
    openModal("modal-add-member");
}

async function deleteMember(id) {
    if (!confirm(`Delete member #${id}? This cannot be undone.`)) return;
    try {
        await apiRequest(`/members/${id}`, { method: "DELETE" });
        showToast(`🗑️ Member #${id} deleted.`, "success");
        renderMembersTable();
    } catch { /* shown */ }
}

// ============================================================
//  TRANSACTIONS
// ============================================================
async function renderTransactionsTable() {
    showLoading("transactions", true);
    try {
        transactionsCache = await apiRequest("/transactions");
        if (!booksCache.length)  booksCache  = await apiRequest("/books");
        if (!membersCache.length) membersCache = await apiRequest("/members");

        const tbody = document.getElementById("transactions-table-body");
        if (!tbody) return;

        const sorted = [...transactionsCache].sort((a, b) => b.transactionId - a.transactionId);

        tbody.innerHTML = sorted.map(t => {
            const book   = booksCache.find(b  => b.bookId   === t.bookId);
            const member = membersCache.find(m => m.memberId === t.memberId);
            let statusBadge = "badge-info";
            if (t.status === "RETURNED") statusBadge = "badge-success";
            if (t.status === "OVERDUE")  statusBadge = "badge-overdue";

            const paymentBadge = (t.paymentMode === "CASH_ON_DELIVERY")
                ? `<span class="badge badge-warning">🚚 COD</span>`
                : `<span class="badge badge-secondary">🏢 In-Person</span>`;

            const isActive = t.status === "ISSUED" || t.status === "OVERDUE";
            return `
                <tr>
                    <td><code>#${t.transactionId}</code></td>
                    <td>${book   ? `<strong>${escapeHtml(book.title)}</strong><br><span class="text-muted" style="font-size:11px">#${t.bookId}</span>` : `#${t.bookId}`}</td>
                    <td>${member ? `${escapeHtml(member.name)}<br><span class="text-muted" style="font-size:11px">#${t.memberId}</span>`               : `#${t.memberId}`}</td>
                    <td class="text-muted">${t.issueDate}</td>
                    <td><span class="${t.status === 'OVERDUE' ? 'text-danger' : 'text-muted'}">${t.dueDate}</span></td>
                    <td class="text-muted">${t.returnDate || '—'}</td>
                    <td>${paymentBadge}</td>
                    <td><span class="badge ${statusBadge}">${t.status}</span></td>
                    <td>
                        ${isActive
                            ? `<button class="btn btn-primary btn-sm" id="btn-return-${t.transactionId}" onclick="returnBook(${t.transactionId})">
                                   <span class="btn-spinner"></span>↩️ Return
                               </button>`
                            : `<span class="text-muted" style="font-size:12px">Completed</span>`}
                    </td>
                </tr>
            `;
        }).join("") || emptyRow(9, "No transaction records.");
    } catch { /* shown */ }
    finally   { showLoading("transactions", false); }
}

async function returnBook(transId) {
    const btn = document.getElementById(`btn-return-${transId}`);
    try {
        const res = await apiRequest("/transactions/return",
            { method: "POST", body: JSON.stringify({ transactionId: transId }) },
            btn
        );
        const fineMsg = res.status === "RETURNED" ? "" : "";
        showToast(`✅ Transaction #${transId} returned! Status: ${res.status}`, "success");
        // Check if a fine was generated
        const newFines = await apiRequest("/fines");
        const prevCount = finesCache.length;
        finesCache = newFines;
        if (newFines.length > prevCount) {
            showToast("💰 A late return fine was automatically generated.", "warning");
        }
        renderTransactionsTable();
    } catch { /* shown */ }
}

// ============================================================
//  FINES
// ============================================================
function populateFinesMemberFilter() {
    const sel = document.getElementById("fines-member-filter");
    if (!sel) return;
    const existing = sel.innerHTML;
    sel.innerHTML = '<option value="">All Members</option>' +
        membersCache.map(m => `<option value="${m.memberId}">${escapeHtml(m.name)} (#${m.memberId})</option>`).join("");
    sel.addEventListener("change", () => renderFinesTable(sel.value ? parseInt(sel.value) : null));
}

async function renderFinesTable(filterMemberId = null) {
    showLoading("fines", true);
    try {
        const endpoint = filterMemberId ? `/fines/unpaid?memberId=${filterMemberId}` : "/fines";
        finesCache = await apiRequest(endpoint);
        if (!membersCache.length) membersCache = await apiRequest("/members");

        const tbody = document.getElementById("fines-table-body");
        if (!tbody) return;

        const sorted = [...finesCache].sort((a, b) => a.paid - b.paid || b.fineId - a.fineId);
        tbody.innerHTML = sorted.map(f => {
            // Look up member via transaction
            const tx     = transactionsCache.find(t => t.transactionId === f.transactionId);
            const member = tx ? membersCache.find(m => m.memberId === tx.memberId) : null;
            return `
                <tr>
                    <td><code>#${f.fineId}</code></td>
                    <td><code>#${f.transactionId}</code></td>
                    <td><strong>${escapeHtml(f.reason || 'Late Return')}</strong></td>
                    <td>${member ? escapeHtml(member.name) : '—'}</td>
                    <td><strong style="color:${f.paid ? 'var(--text-muted)' : 'var(--rose-accent)'}">$${parseFloat(f.amount).toFixed(2)}</strong></td>
                    <td><span class="badge ${f.paid ? 'badge-success' : 'badge-danger'}">${f.paid ? 'PAID' : 'UNPAID'}</span></td>
                    <td class="text-muted">${f.paidDate || '—'}</td>
                    <td>
                        ${!f.paid
                            ? `<button class="btn btn-primary btn-sm" id="btn-pay-${f.fineId}" onclick="payFine(${f.fineId})">
                                   <span class="btn-spinner"></span>💳 Pay $${parseFloat(f.amount).toFixed(2)}
                               </button>`
                            : `<span class="badge badge-success">✓ Cleared</span>`}
                    </td>
                </tr>
            `;
        }).join("") || emptyRow(8, "🎉 No fines found.");
    } catch { /* shown */ }
    finally   { showLoading("fines", false); }
}

async function payFine(fineId) {
    const btn = document.getElementById(`btn-pay-${fineId}`);
    try {
        await apiRequest(`/fines/${fineId}/pay`, { method: "POST" }, btn);
        showToast(`✅ Fine #${fineId} marked as paid!`, "success");
        renderFinesTable();
    } catch { /* shown */ }
}

// ============================================================
//  RESERVATIONS
// ============================================================
async function renderReservationsTable() {
    showLoading("reservations", true);
    try {
        reservationsCache = await apiRequest("/reservations");
        if (!booksCache.length)  booksCache  = await apiRequest("/books");
        if (!membersCache.length) membersCache = await apiRequest("/members");

        const tbody = document.getElementById("reservations-table-body");
        if (!tbody) return;

        const sorted = [...reservationsCache].sort((a, b) => b.reservationId - a.reservationId);
        tbody.innerHTML = sorted.map(r => {
            const book   = booksCache.find(b  => b.bookId   === r.bookId);
            const member = membersCache.find(m => m.memberId === r.memberId);
            const badge  = r.status === "PENDING" ? "badge-warning"
                         : r.status === "FULFILLED" ? "badge-success"
                         : "badge-secondary";
            const isPending = r.status === "PENDING";
            return `
                <tr>
                    <td><code>#${r.reservationId}</code></td>
                    <td>${book   ? `<strong>${escapeHtml(book.title)}</strong>`  : `#${r.bookId}`}</td>
                    <td>${member ? escapeHtml(member.name) : `#${r.memberId}`}</td>
                    <td class="text-muted">${r.reservationDate ? String(r.reservationDate).replace('T', ' ').split('.')[0] : '—'}</td>
                    <td><span class="badge ${badge}">${r.status}</span></td>
                    <td style="display:flex;gap:6px">
                        ${isPending
                            ? `<button class="btn btn-secondary btn-sm" id="btn-ful-${r.reservationId}" onclick="updateReservationStatus(${r.reservationId}, 'FULFILLED')">
                                   <span class="btn-spinner"></span>✅ Fulfill
                               </button>
                               <button class="btn btn-danger btn-sm" id="btn-can-${r.reservationId}" onclick="updateReservationStatus(${r.reservationId}, 'CANCELLED')">
                                   <span class="btn-spinner"></span>❌ Cancel
                               </button>`
                            : `<span class="text-muted" style="font-size:12px">Archived</span>`}
                    </td>
                </tr>
            `;
        }).join("") || emptyRow(6, "No reservations recorded.");
    } catch { /* shown */ }
    finally   { showLoading("reservations", false); }
}

async function updateReservationStatus(resId, status) {
    const btn = document.getElementById(status === 'FULFILLED' ? `btn-ful-${resId}` : `btn-can-${resId}`);
    try {
        await apiRequest(`/reservations/${resId}/status`, {
            method: "PUT",
            body: JSON.stringify({ status })
        }, btn);
        showToast(`✅ Reservation #${resId} marked as ${status}!`, "success");
        renderReservationsTable();
    } catch { /* shown */ }
}

// ============================================================
//  REPORTS / ANALYTICS
// ============================================================
async function loadReports() {
    showLoading("reports-members", true);
    showLoading("reports-fines", true);
    showLoading("reports-books", true);
    showLoading("reports-reservations", true);

    try {
        const [activeMembers, unpaidFines, topBooks, finesReport, reservations, txs] = await Promise.all([
            apiRequest("/reports/active-members"),
            apiRequest("/reports/unpaid-fines"),
            apiRequest("/reports/most-borrowed"),
            apiRequest("/reports/fines-collected"),
            apiRequest("/reservations"),
            apiRequest("/transactions"),
        ]);

        if (!booksCache.length)   booksCache   = await apiRequest("/books");
        if (!membersCache.length) membersCache = await apiRequest("/members");

        // Stats summary
        setStatCard("report-total-collected", `$${(finesReport.totalCollected || 0).toFixed(2)}`);
        const pendingCount = reservations.filter(r => r.status === "PENDING").length;
        setStatCard("report-pending-res", pendingCount);
        const codCount = txs.filter(t => t.paymentMode === "CASH_ON_DELIVERY").length;
        setStatCard("report-cod-count", codCount);

        const activeTb = document.getElementById("report-active-members");
        if (activeTb) {
            activeTb.innerHTML = activeMembers.map((m, i) => `
                <tr>
                    <td>${i === 0 ? '🥇' : i === 1 ? '🥈' : i === 2 ? '🥉' : i + 1}</td>
                    <td><strong>${escapeHtml(m.name)}</strong></td>
                    <td><span class="badge badge-info">${m.transactionCount} txns</span></td>
                </tr>
            `).join("") || emptyRow(3, "No activity recorded.");
        }

        const unpaidTb = document.getElementById("report-unpaid-fines");
        if (unpaidTb) {
            unpaidTb.innerHTML = unpaidFines.map(u => `
                <tr>
                    <td><strong>${escapeHtml(u.name)}</strong></td>
                    <td class="text-muted">${escapeHtml(u.email)}</td>
                    <td><span class="badge badge-danger">$${parseFloat(u.totalUnpaidFine).toFixed(2)}</span></td>
                </tr>
            `).join("") || emptyRow(3, "🎉 No unpaid fines!");
        }

        const topBooksTb = document.getElementById("report-top-books");
        if (topBooksTb) {
            topBooksTb.innerHTML = topBooks.map((b, i) => `
                <tr>
                    <td>${i + 1}</td>
                    <td><strong>${escapeHtml(b.title)}</strong></td>
                    <td class="text-muted">${escapeHtml(b.author)}</td>
                    <td><span class="badge badge-info">${b.borrowCount}×</span></td>
                </tr>
            `).join("") || emptyRow(4, "No borrow history yet.");
        }

        const resTb = document.getElementById("report-reservations-list");
        if (resTb) {
            resTb.innerHTML = reservations.map(r => {
                const book   = booksCache.find(b => b.bookId === r.bookId);
                const member = membersCache.find(m => m.memberId === r.memberId);
                const badge  = r.status === "PENDING" ? "badge-warning"
                             : r.status === "FULFILLED" ? "badge-success"
                             : "badge-secondary";
                return `
                    <tr>
                        <td><strong>${book ? escapeHtml(book.title) : `#${r.bookId}`}</strong></td>
                        <td>${member ? escapeHtml(member.name) : `#${r.memberId}`}</td>
                        <td class="text-muted">${r.reservationDate ? String(r.reservationDate).split('T')[0] : '—'}</td>
                        <td><span class="badge ${badge}">${r.status}</span></td>
                    </tr>
                `;
            }).join("") || emptyRow(4, "No reservations.");
        }
    } catch { /* shown */ }
    finally {
        showLoading("reports-members", false);
        showLoading("reports-fines", false);
        showLoading("reports-books", false);
        showLoading("reports-reservations", false);
    }
}

// ============================================================
//  FORMS, MODALS & CLIENT-SIDE VALIDATION
// ============================================================
function initForms() {
    // ── Add / Edit Book ──────────────────────────────────────
    document.getElementById("form-add-book")?.addEventListener("submit", async e => {
        e.preventDefault();
        if (!validateBookForm()) return;

        const editId  = document.getElementById("edit-book-id").value;
        const isEdit  = !!editId;
        const btn     = document.getElementById("btn-save-book");
        const newTotal    = parseInt(document.getElementById("add-book-copies").value);
        // When editing, preserve the checked-out count; only add/remove available copies by delta
        let newAvailable  = newTotal;
        if (isEdit) {
            const orig = booksCache.find(b => b.bookId === parseInt(editId));
            if (orig) {
                const delta = newTotal - orig.totalCopies;
                newAvailable = Math.max(0, orig.availableCopies + delta);
            }
        }
        const payload = {
            title:           document.getElementById("add-book-title").value.trim(),
            author:          document.getElementById("add-book-author").value.trim(),
            isbn:            document.getElementById("add-book-isbn").value.trim(),
            category:        document.getElementById("add-book-category").value.trim(),
            totalCopies:     newTotal,
            availableCopies: newAvailable,
        };

        try {
            if (isEdit) {
                await apiRequest(`/books/${editId}`, { method: "PUT", body: JSON.stringify(payload) }, btn);
                showToast(`✅ Book #${editId} updated successfully.`, "success");
            } else {
                await apiRequest("/books", { method: "POST", body: JSON.stringify(payload) }, btn);
                showToast("✅ Book added successfully!", "success");
            }
            closeModal("modal-add-book");
            resetBookForm();
            renderBooksTable();
        } catch { /* shown */ }
    });

    // ── Add / Edit Member ────────────────────────────────────
    document.getElementById("form-add-member")?.addEventListener("submit", async e => {
        e.preventDefault();
        if (!validateMemberForm()) return;

        const editId  = document.getElementById("edit-member-id").value;
        const isEdit  = !!editId;
        const btn     = document.getElementById("btn-save-member");
        const payload = {
            name:           document.getElementById("add-member-name").value.trim(),
            email:          document.getElementById("add-member-email").value.trim(),
            phone:          document.getElementById("add-member-phone").value.trim(),
            membershipType: document.getElementById("add-member-type").value,
        };

        try {
            if (isEdit) {
                await apiRequest(`/members/${editId}`, { method: "PUT", body: JSON.stringify(payload) }, btn);
                showToast(`✅ Member #${editId} updated successfully.`, "success");
            } else {
                await apiRequest("/members", { method: "POST", body: JSON.stringify(payload) }, btn);
                showToast("✅ Member registered successfully!", "success");
            }
            closeModal("modal-add-member");
            resetMemberForm();
            renderMembersTable();
        } catch { /* shown */ }
    });

    // ── Issue Book ───────────────────────────────────────────
    document.getElementById("form-issue-book")?.addEventListener("submit", async e => {
        e.preventDefault();
        if (!validateIssueForm()) return;

        const btn  = document.getElementById("btn-issue-book");
        const body = {
            memberId: parseInt(document.getElementById("issue-select-member").value),
            bookId:   parseInt(document.getElementById("issue-select-book").value),
            paymentMode: document.getElementById("issue-select-payment")?.value || "IN_PERSON",
        };
        try {
            await apiRequest("/transactions/issue", { method: "POST", body: JSON.stringify(body) }, btn);
            showToast("✅ Book issued successfully! Due in 14 days.", "success");
            closeModal("modal-issue-book");
            booksCache = await apiRequest("/books");
            renderTransactionsTable();
        } catch { /* shown */ }
    });

    // ── Reserve Book ─────────────────────────────────────────
    document.getElementById("form-reserve-book")?.addEventListener("submit", async e => {
        e.preventDefault();
        if (!validateReserveForm()) return;

        const btn  = document.getElementById("btn-reserve-book");
        const body = {
            memberId: parseInt(document.getElementById("reserve-select-member").value),
            bookId:   parseInt(document.getElementById("reserve-select-book").value),
        };
        try {
            await apiRequest("/reservations", { method: "POST", body: JSON.stringify(body) }, btn);
            showToast("✅ Reservation placed! Member will be notified when available.", "success");
            closeModal("modal-reserve-book");
            renderReservationsTable();
        } catch { /* shown */ }
    });
}

// ── Client-side Validation ──────────────────────────────────
const EMAIL_RE  = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;
const PHONE_RE  = /^[\d\-\s\(\)\+]{7,15}$/;
const ISBN_RE   = /^[\d\-Xx]{10,17}$/;

function validateBookForm() {
    let valid = true;
    const title = document.getElementById("add-book-title").value.trim();
    const author = document.getElementById("add-book-author").value.trim();
    const isbn   = document.getElementById("add-book-isbn").value.trim();
    const cat    = document.getElementById("add-book-category").value.trim();
    const copies = parseInt(document.getElementById("add-book-copies").value);

    valid = setField("add-book-title",    "err-book-title",    !!title)          && valid;
    valid = setField("add-book-author",   "err-book-author",   !!author)         && valid;
    valid = setField("add-book-isbn",     "err-book-isbn",     ISBN_RE.test(isbn)) && valid;
    valid = setField("add-book-category", "err-book-category", !!cat)            && valid;
    valid = setField("add-book-copies",   "err-book-copies",   copies >= 1)      && valid;
    return valid;
}

function validateMemberForm() {
    let valid = true;
    const name  = document.getElementById("add-member-name").value.trim();
    const email = document.getElementById("add-member-email").value.trim();
    const phone = document.getElementById("add-member-phone").value.trim();

    valid = setField("add-member-name",  "err-member-name",  name.length >= 2)   && valid;
    valid = setField("add-member-email", "err-member-email", EMAIL_RE.test(email)) && valid;
    valid = setField("add-member-phone", "err-member-phone", PHONE_RE.test(phone)) && valid;
    return valid;
}

function validateIssueForm() {
    let valid = true;
    const mem  = document.getElementById("issue-select-member").value;
    const book = document.getElementById("issue-select-book").value;
    valid = setField("issue-select-member", "err-issue-member", !!mem)  && valid;
    valid = setField("issue-select-book",   "err-issue-book",   !!book) && valid;
    return valid;
}

function validateReserveForm() {
    let valid = true;
    const mem  = document.getElementById("reserve-select-member").value;
    const book = document.getElementById("reserve-select-book").value;
    valid = setField("reserve-select-member", "err-reserve-member", !!mem)  && valid;
    valid = setField("reserve-select-book",   "err-reserve-book",   !!book) && valid;
    return valid;
}

/** Sets/clears field invalid state. Returns isValid. */
function setField(inputId, errId, isValid) {
    const input = document.getElementById(inputId);
    const err   = document.getElementById(errId);
    if (!input) return isValid;
    if (isValid) {
        input.classList.remove("invalid");
        err?.classList.remove("visible");
    } else {
        input.classList.add("invalid");
        err?.classList.add("visible");
        input.focus();
    }
    return isValid;
}

function clearValidation(formId) {
    const form = document.getElementById(formId);
    if (!form) return;
    form.querySelectorAll(".invalid").forEach(el => el.classList.remove("invalid"));
    form.querySelectorAll(".field-error.visible").forEach(el => el.classList.remove("visible"));
}

// ── Search Filters ────────────────────────────────────────
function initSearchFilters() {
    let bookDebounce, memberDebounce;

    document.getElementById("search-books-input")?.addEventListener("input", e => {
        clearTimeout(bookDebounce);
        bookDebounce = setTimeout(() => renderBooksTable(e.target.value.trim()), 300);
    });

    document.getElementById("search-members-input")?.addEventListener("input", e => {
        clearTimeout(memberDebounce);
        memberDebounce = setTimeout(() => renderMembersTable(e.target.value.trim()), 200);
    });
}

// ── Modal Management ──────────────────────────────────────
function openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (!modal) return;

    if (modalId === "modal-add-book") {
        const isEdit = !!document.getElementById("edit-book-id")?.value;
        if (!isEdit) resetBookForm();
    }
    if (modalId === "modal-add-member") {
        const isEdit = !!document.getElementById("edit-member-id")?.value;
        if (!isEdit) resetMemberForm();
    }

    if (modalId === "modal-issue-book") {
        populateSelect("issue-select-member", membersCache, "memberId",
            m => `${m.name} (#${m.memberId})`);
        populateSelect("issue-select-book",
            booksCache.filter(b => b.availableCopies > 0), "bookId",
            b => `${b.title} — ${b.availableCopies} available`);
    }

    if (modalId === "modal-reserve-book") {
        populateSelect("reserve-select-member", membersCache, "memberId",
            m => `${m.name} (#${m.memberId})`);
        populateSelect("reserve-select-book", booksCache, "bookId",
            b => `${b.title} (${b.availableCopies} available)`);
    }

    modal.classList.add("active");
}

function closeModal(modalId) {
    document.getElementById(modalId)?.classList.remove("active");
}

function resetBookForm() {
    document.getElementById("edit-book-id").value   = "";
    document.getElementById("add-book-title").value  = "";
    document.getElementById("add-book-author").value = "";
    document.getElementById("add-book-isbn").value   = "";
    document.getElementById("add-book-category").value = "";
    document.getElementById("add-book-copies").value = "1";
    document.getElementById("modal-book-title").textContent = "Add New Book";
    clearValidation("form-add-book");
}

function resetMemberForm() {
    document.getElementById("edit-member-id").value   = "";
    document.getElementById("add-member-name").value  = "";
    document.getElementById("add-member-email").value = "";
    document.getElementById("add-member-phone").value = "";
    document.getElementById("add-member-type").value  = "STANDARD";
    document.getElementById("modal-member-title").textContent = "Register New Member";
    clearValidation("form-add-member");
}

function populateSelect(selectId, items, valKey, labelFn) {
    const sel = document.getElementById(selectId);
    if (!sel) return;
    if (!items.length) {
        sel.innerHTML = `<option value="">— None available —</option>`;
        return;
    }
    sel.innerHTML = items.map(item =>
        `<option value="${item[valKey]}">${escapeHtml(labelFn(item))}</option>`
    ).join("");
}

// ============================================================
//  UI HELPERS
// ============================================================
function showLoading(viewId, visible) {
    const el = document.getElementById(`loading-${viewId}`);
    if (visible) el?.classList.add("visible");
    else el?.classList.remove("visible");
}

function setStatCard(id, value, style = "normal") {
    const el = document.getElementById(id);
    if (!el) return;
    el.classList.remove("loading-val");
    el.textContent = value;
    if (style === "danger") el.style.color = "var(--rose-accent)";
    else el.style.color = "";
}

function setButtonLoading(btn, isLoading) {
    if (!btn) return;
    const spinner = btn.querySelector(".btn-spinner");
    if (isLoading) {
        btn.disabled = true;
        btn.classList.add("loading");
        if (spinner) spinner.style.display = "block";
    } else {
        btn.disabled = false;
        btn.classList.remove("loading");
        if (spinner) spinner.style.display = "none";
    }
}

function emptyRow(cols, msg) {
    return `<tr class="empty-row"><td colspan="${cols}">${msg}</td></tr>`;
}

// ============================================================
//  TOAST NOTIFICATIONS
// ============================================================
function showToast(msg, type = "success") {
    const container = document.getElementById("toast-container");
    const toast = document.createElement("div");
    toast.className = `toast toast-${type}`;

    const icon = { success: "✅", error: "❌", warning: "⚠️", info: "ℹ️" }[type] || "";
    toast.innerHTML = `<span>${icon}</span><span>${escapeHtml(msg)}</span>`;

    container.appendChild(toast);
    // Auto-remove
    const delay = type === "error" ? 6000 : 4000;
    setTimeout(() => {
        toast.style.animation = "none";
        toast.style.opacity = "0";
        toast.style.transform = "translateX(120%)";
        toast.style.transition = "all 0.3s ease";
        setTimeout(() => toast.remove(), 300);
    }, delay);
}

// ============================================================
//  SECURITY — HTML Escaping
// ============================================================
function escapeHtml(str) {
    if (str == null) return "";
    return String(str).replace(/[&<>"']/g, m => ({
        "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#039;"
    })[m]);
}
