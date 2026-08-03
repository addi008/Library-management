/* ============================================================
   LibVerse — Vercel Serverless API Controller
   Full-featured Serverless Backend for Vercel Cloud Deployment
   ============================================================ */

let books = [
    { bookId: 1, title: "Clean Code", author: "Robert C. Martin", isbn: "9780132350884", category: "SOFTWARE ENGINEERING", totalCopies: 5, availableCopies: 3, addedDate: "2026-01-15" },
    { bookId: 2, title: "Effective Java", author: "Joshua Bloch", isbn: "9780134685991", category: "PROGRAMMING", totalCopies: 4, availableCopies: 3, addedDate: "2026-01-20" },
    { bookId: 3, title: "Design Patterns", author: "Erich Gamma et al.", isbn: "9780201633610", category: "SOFTWARE ARCHITECTURE", totalCopies: 3, availableCopies: 2, addedDate: "2026-02-01" },
    { bookId: 4, title: "The Pragmatic Programmer", author: "Andrew Hunt, David Thomas", isbn: "9780135957059", category: "SOFTWARE ENGINEERING", totalCopies: 6, availableCopies: 5, addedDate: "2026-02-10" },
    { bookId: 5, title: "Head First Java", author: "Kathy Sierra, Bert Bates", isbn: "9780596009205", category: "PROGRAMMING", totalCopies: 2, availableCopies: 1, addedDate: "2026-02-15" },
    { bookId: 6, title: "Introduction to Algorithms", author: "Thomas H. Cormen", isbn: "9780262033848", category: "COMPUTER SCIENCE", totalCopies: 3, availableCopies: 3, addedDate: "2026-03-01" },
    { bookId: 7, title: "Java Concurrency in Practice", author: "Brian Goetz", isbn: "9780321349606", category: "PROGRAMMING", totalCopies: 4, availableCopies: 4, addedDate: "2026-03-05" }
];

let members = [
    { memberId: 1, name: "Alice Smith", email: "alice@example.com", phone: "+1-555-0101", membershipDate: "2026-01-10", membershipType: "PREMIUM" },
    { memberId: 2, name: "Bob Johnson", email: "bob@example.com", phone: "+1-555-0102", membershipDate: "2026-01-12", membershipType: "STANDARD" },
    { memberId: 3, name: "Charlie Brown", email: "charlie@example.com", phone: "+1-555-0103", membershipDate: "2026-01-18", membershipType: "STUDENT" },
    { memberId: 4, name: "Diana Prince", email: "diana@example.com", phone: "+1-555-0104", membershipDate: "2026-02-01", membershipType: "PREMIUM" },
    { memberId: 5, name: "Evan Wright", email: "evan@example.com", phone: "+1-555-0105", membershipDate: "2026-02-14", membershipType: "STANDARD" }
];

let transactions = [
    { transactionId: 1, bookId: 1, memberId: 1, issueDate: "2026-07-01", dueDate: "2026-07-15", returnDate: "2026-07-12", paymentMode: "IN_PERSON", status: "RETURNED" },
    { transactionId: 2, bookId: 2, memberId: 2, issueDate: "2026-07-05", dueDate: "2026-07-19", returnDate: "2026-07-18", paymentMode: "CASH_ON_DELIVERY", status: "RETURNED" },
    { transactionId: 3, bookId: 3, memberId: 3, issueDate: "2026-07-10", dueDate: "2026-07-24", returnDate: "2026-07-28", paymentMode: "IN_PERSON", status: "RETURNED" },
    { transactionId: 4, bookId: 1, memberId: 4, issueDate: "2026-07-20", dueDate: "2026-08-03", returnDate: null, paymentMode: "CASH_ON_DELIVERY", status: "OVERDUE" },
    { transactionId: 5, bookId: 5, memberId: 5, issueDate: "2026-07-22", dueDate: "2026-08-05", returnDate: null, paymentMode: "IN_PERSON", status: "OVERDUE" },
    { transactionId: 6, bookId: 2, memberId: 1, issueDate: "2026-07-25", dueDate: "2026-08-08", returnDate: null, paymentMode: "CASH_ON_DELIVERY", status: "ISSUED" },
    { transactionId: 7, bookId: 4, memberId: 2, issueDate: "2026-07-28", dueDate: "2026-08-11", returnDate: null, paymentMode: "IN_PERSON", status: "ISSUED" }
];

let fines = [
    { fineId: 1, transactionId: 3, amount: 4.00, reason: "Late Return (4 days overdue)", paid: true, paidDate: "2026-07-28" },
    { fineId: 2, transactionId: 4, amount: 8.00, reason: "Overdue Borrowing (8 days)", paid: false, paidDate: null },
    { fineId: 3, transactionId: 5, amount: 4.00, reason: "Overdue Borrowing (4 days)", paid: false, paidDate: null }
];

let reservations = [
    { reservationId: 1, bookId: 1, memberId: 2, reservationDate: "2026-07-26 10:30:00", status: "PENDING" },
    { reservationId: 2, bookId: 3, memberId: 4, reservationDate: "2026-07-27 14:15:00", status: "FULFILLED" },
    { reservationId: 3, bookId: 5, memberId: 1, reservationDate: "2026-07-28 09:00:00", status: "FULFILLED" },
    { reservationId: 4, bookId: 2, memberId: 5, reservationDate: "2026-07-29 16:45:00", status: "CANCELLED" }
];

module.exports = (req, res) => {
    // Enable CORS
    res.setHeader("Access-Control-Allow-Origin", "*");
    res.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
    res.setHeader("Access-Control-Allow-Headers", "Content-Type");

    if (req.method === "OPTIONS") {
        return res.status(200).end();
    }

    const url = req.url.replace(/^\/api/, "");
    const [pathStr, queryString] = url.split("?");
    const path = pathStr.split("/").filter(Boolean);

    // Health
    if (path[0] === "health") {
        return res.status(200).json({ status: "OK", server: "Spark Java / Vercel Edge Serverless", timestamp: new Date().toISOString() });
    }

    // BOOKS
    if (path[0] === "books") {
        if (req.method === "GET") {
            if (path[1] === "search") {
                const params = new URLSearchParams(queryString || "");
                const q = (params.get("q") || "").toLowerCase();
                const filtered = books.filter(b => b.title.toLowerCase().includes(q) || b.author.toLowerCase().includes(q) || b.category.toLowerCase().includes(q) || b.isbn.includes(q));
                return res.status(200).json(filtered);
            }
            return res.status(200).json(books);
        }
        if (req.method === "POST") {
            const body = req.body || {};
            const newBook = {
                bookId: books.length ? Math.max(...books.map(b => b.bookId)) + 1 : 1,
                title: body.title || "Untitled",
                author: body.author || "Unknown",
                isbn: body.isbn || "N/A",
                category: (body.category || "GENERAL").toUpperCase(),
                totalCopies: body.totalCopies || 1,
                availableCopies: body.totalCopies || 1,
                addedDate: new Date().toISOString().split("T")[0]
            };
            books.push(newBook);
            return res.status(201).json(newBook);
        }
        if (req.method === "PUT" && path[1]) {
            const id = parseInt(path[1]);
            const idx = books.findIndex(b => b.bookId === id);
            if (idx !== -1) {
                const body = req.body || {};
                const diff = (body.totalCopies || books[idx].totalCopies) - books[idx].totalCopies;
                books[idx] = {
                    ...books[idx],
                    title: body.title || books[idx].title,
                    author: body.author || books[idx].author,
                    isbn: body.isbn || books[idx].isbn,
                    category: (body.category || books[idx].category).toUpperCase(),
                    totalCopies: body.totalCopies || books[idx].totalCopies,
                    availableCopies: Math.max(0, books[idx].availableCopies + diff)
                };
                return res.status(200).json(books[idx]);
            }
            return res.status(404).json({ error: "Book not found" });
        }
        if (req.method === "DELETE" && path[1]) {
            const id = parseInt(path[1]);
            books = books.filter(b => b.bookId !== id);
            return res.status(200).json({ success: true, message: `Book #${id} deleted` });
        }
    }

    // MEMBERS
    if (path[0] === "members") {
        if (req.method === "GET") {
            return res.status(200).json(members);
        }
        if (req.method === "POST") {
            const body = req.body || {};
            const newMember = {
                memberId: members.length ? Math.max(...members.map(m => m.memberId)) + 1 : 1,
                name: body.name || "New Member",
                email: body.email || "user@example.com",
                phone: body.phone || "N/A",
                membershipType: body.membershipType || "STANDARD",
                membershipDate: new Date().toISOString().split("T")[0]
            };
            members.push(newMember);
            return res.status(201).json(newMember);
        }
        if (req.method === "PUT" && path[1]) {
            const id = parseInt(path[1]);
            const idx = members.findIndex(m => m.memberId === id);
            if (idx !== -1) {
                const body = req.body || {};
                members[idx] = {
                    ...members[idx],
                    name: body.name || members[idx].name,
                    email: body.email || members[idx].email,
                    phone: body.phone || members[idx].phone,
                    membershipType: body.membershipType || members[idx].membershipType
                };
                return res.status(200).json(members[idx]);
            }
            return res.status(404).json({ error: "Member not found" });
        }
        if (req.method === "DELETE" && path[1]) {
            const id = parseInt(path[1]);
            members = members.filter(m => m.memberId !== id);
            return res.status(200).json({ success: true, message: `Member #${id} deleted` });
        }
    }

    // TRANSACTIONS
    if (path[0] === "transactions") {
        if (req.method === "GET") {
            if (path[1] === "overdue") {
                return res.status(200).json(transactions.filter(t => t.status === "OVERDUE"));
            }
            return res.status(200).json(transactions);
        }
        if (req.method === "POST" && path[1] === "issue") {
            const body = req.body || {};
            const newTx = {
                transactionId: transactions.length ? Math.max(...transactions.map(t => t.transactionId)) + 1 : 1,
                bookId: body.bookId,
                memberId: body.memberId,
                issueDate: new Date().toISOString().split("T")[0],
                dueDate: new Date(Date.now() + 14 * 86400000).toISOString().split("T")[0],
                returnDate: null,
                paymentMode: body.paymentMode || "IN_PERSON",
                status: "ISSUED"
            };
            const bookIdx = books.findIndex(b => b.bookId === body.bookId);
            if (bookIdx !== -1 && books[bookIdx].availableCopies > 0) {
                books[bookIdx].availableCopies -= 1;
            }
            transactions.push(newTx);
            return res.status(201).json(newTx);
        }
        if (req.method === "POST" && path[1] === "return") {
            const body = req.body || {};
            const txIdx = transactions.findIndex(t => t.transactionId === body.transactionId);
            if (txIdx !== -1) {
                transactions[txIdx].status = "RETURNED";
                transactions[txIdx].returnDate = new Date().toISOString().split("T")[0];
                const bookIdx = books.findIndex(b => b.bookId === transactions[txIdx].bookId);
                if (bookIdx !== -1) {
                    books[bookIdx].availableCopies = Math.min(books[bookIdx].totalCopies, books[bookIdx].availableCopies + 1);
                }
                return res.status(200).json(transactions[txIdx]);
            }
            return res.status(404).json({ error: "Transaction not found" });
        }
    }

    // FINES
    if (path[0] === "fines") {
        if (req.method === "GET") {
            if (path[1] === "unpaid") {
                return res.status(200).json(fines.filter(f => !f.paid));
            }
            return res.status(200).json(fines);
        }
        if (req.method === "POST" && path[2] === "pay") {
            const id = parseInt(path[1]);
            const idx = fines.findIndex(f => f.fineId === id);
            if (idx !== -1) {
                fines[idx].paid = true;
                fines[idx].paidDate = new Date().toISOString().split("T")[0];
                return res.status(200).json(fines[idx]);
            }
            return res.status(404).json({ error: "Fine not found" });
        }
    }

    // RESERVATIONS
    if (path[0] === "reservations") {
        if (req.method === "GET") {
            return res.status(200).json(reservations);
        }
        if (req.method === "POST") {
            const body = req.body || {};
            const newRes = {
                reservationId: reservations.length ? Math.max(...reservations.map(r => r.reservationId)) + 1 : 1,
                bookId: body.bookId,
                memberId: body.memberId,
                reservationDate: new Date().toISOString().replace("T", " ").substring(0, 19),
                status: "PENDING"
            };
            reservations.push(newRes);
            return res.status(201).json(newRes);
        }
        if (req.method === "PUT" && path[2] === "status") {
            const id = parseInt(path[1]);
            const body = req.body || {};
            const idx = reservations.findIndex(r => r.reservationId === id);
            if (idx !== -1) {
                reservations[idx].status = body.status || "FULFILLED";
                return res.status(200).json(reservations[idx]);
            }
            return res.status(404).json({ error: "Reservation not found" });
        }
    }

    // REPORTS
    if (path[0] === "reports") {
        if (path[1] === "most-borrowed") {
            const counts = {};
            transactions.forEach(t => { counts[t.bookId] = (counts[t.bookId] || 0) + 1; });
            const list = books.map(b => ({ ...b, borrowCount: counts[b.bookId] || 0 })).sort((a, b) => b.borrowCount - a.borrowCount);
            return res.status(200).json(list);
        }
        if (path[1] === "active-members") {
            const counts = {};
            transactions.forEach(t => { counts[t.memberId] = (counts[t.memberId] || 0) + 1; });
            const list = members.map(m => ({ ...m, transactionCount: counts[m.memberId] || 0 })).sort((a, b) => b.transactionCount - a.transactionCount);
            return res.status(200).json(list);
        }
        if (path[1] === "fines-collected") {
            const total = fines.filter(f => f.paid).reduce((sum, f) => sum + f.amount, 0);
            return res.status(200).json({ totalCollected: total });
        }
        if (path[1] === "unpaid-fines") {
            return res.status(200).json(fines.filter(f => !f.paid));
        }
    }

    return res.status(404).json({ error: "Route not found", path: req.url });
};
