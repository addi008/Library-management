-- ===================================================
-- Library Management System Schema (MySQL & H2 Compatible)
-- ===================================================

CREATE TABLE IF NOT EXISTS books (
    book_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    isbn VARCHAR(20) UNIQUE NOT NULL,
    category VARCHAR(100) NOT NULL,
    total_copies INT NOT NULL DEFAULT 1,
    available_copies INT NOT NULL DEFAULT 1,
    added_date DATE NOT NULL,
    INDEX idx_books_isbn (isbn)
);

CREATE TABLE IF NOT EXISTS members (
    member_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20),
    membership_date DATE NOT NULL,
    membership_type VARCHAR(50) NOT NULL DEFAULT 'STANDARD',
    INDEX idx_members_email (email)
);

CREATE TABLE IF NOT EXISTS transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    book_id INT NOT NULL,
    member_id INT NOT NULL,
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE DEFAULT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ISSUED',
    payment_mode VARCHAR(50) DEFAULT 'IN_PERSON',
    CONSTRAINT fk_trans_book FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE CASCADE,
    CONSTRAINT fk_trans_member FOREIGN KEY (member_id) REFERENCES members(member_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS fines (
    fine_id INT AUTO_INCREMENT PRIMARY KEY,
    transaction_id INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    paid BOOLEAN NOT NULL DEFAULT FALSE,
    paid_date DATE DEFAULT NULL,
    reason VARCHAR(255) DEFAULT 'Late Return',
    CONSTRAINT fk_fines_transaction FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS reservations (
    reservation_id INT AUTO_INCREMENT PRIMARY KEY,
    book_id INT NOT NULL,
    member_id INT NOT NULL,
    reservation_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    CONSTRAINT fk_res_book FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE CASCADE,
    CONSTRAINT fk_res_member FOREIGN KEY (member_id) REFERENCES members(member_id) ON DELETE CASCADE
);

-- ===================================================
-- Sample Seed Data
-- ===================================================

INSERT IGNORE INTO books (book_id, title, author, isbn, category, total_copies, available_copies, added_date) VALUES
(1, 'Clean Code', 'Robert C. Martin', '9780132350884', 'Software Engineering', 5, 3, '2025-01-10'),
(2, 'Effective Java', 'Joshua Bloch', '9780134685991', 'Programming', 4, 3, '2025-01-12'),
(3, 'Design Patterns', 'Erich Gamma et al.', '9780201633610', 'Software Architecture', 3, 2, '2025-01-15'),
(4, 'The Pragmatic Programmer', 'Andrew Hunt, David Thomas', '9780135957059', 'Software Engineering', 6, 5, '2025-02-01'),
(5, 'Head First Java', 'Kathy Sierra, Bert Bates', '9780596009205', 'Programming', 2, 1, '2025-02-10'),
(6, 'Introduction to Algorithms', 'Thomas H. Cormen', '9780262033848', 'Computer Science', 3, 3, '2025-02-15'),
(7, 'Database System Concepts', 'Abraham Silberschatz', '9780073523323', 'Databases', 4, 4, '2025-03-01');

INSERT IGNORE INTO members (member_id, name, email, phone, membership_date, membership_type) VALUES
(1, 'Alice Smith', 'alice.smith@example.com', '555-0101', '2025-01-01', 'PREMIUM'),
(2, 'Bob Johnson', 'bob.johnson@example.com', '555-0102', '2025-01-05', 'STANDARD'),
(3, 'Charlie Brown', 'charlie.brown@example.com', '555-0103', '2025-01-15', 'STUDENT'),
(4, 'Diana Prince', 'diana.prince@example.com', '555-0104', '2025-02-01', 'PREMIUM'),
(5, 'Evan Wright', 'evan.wright@example.com', '555-0105', '2025-02-10', 'STANDARD');

INSERT IGNORE INTO transactions (transaction_id, book_id, member_id, issue_date, due_date, return_date, status, payment_mode) VALUES
(1, 1, 1, '2025-07-01', '2025-07-15', '2025-07-14', 'RETURNED', 'CASH_ON_DELIVERY'),
(2, 2, 2, '2025-07-05', '2025-07-19', '2025-07-25', 'RETURNED', 'IN_PERSON'),
(3, 3, 3, '2025-07-10', '2025-07-24', NULL, 'OVERDUE', 'CASH_ON_DELIVERY'),
(4, 1, 4, '2025-07-20', '2025-08-03', NULL, 'ISSUED', 'CASH_ON_DELIVERY'),
(5, 5, 5, '2025-07-22', '2025-08-05', NULL, 'ISSUED', 'IN_PERSON'),
(6, 4, 1, '2025-07-25', '2025-08-08', NULL, 'ISSUED', 'CASH_ON_DELIVERY'),
(7, 2, 3, '2025-06-10', '2025-06-24', '2025-06-30', 'RETURNED', 'CASH_ON_DELIVERY');

INSERT IGNORE INTO fines (fine_id, transaction_id, amount, paid, paid_date, reason) VALUES
(1, 2, 6.00, TRUE, '2025-07-25', 'Late Return (6 days overdue)'),
(2, 3, 10.00, FALSE, NULL, 'Overdue Fine (Cash on Delivery Order #3)'),
(3, 7, 6.00, TRUE, '2025-06-30', 'Late Return (6 days overdue)');

INSERT IGNORE INTO reservations (reservation_id, book_id, member_id, reservation_date, status) VALUES
(1, 1, 2, '2025-07-26 10:30:00', 'PENDING'),
(2, 3, 4, '2025-07-27 14:15:00', 'PENDING'),
(3, 5, 1, '2025-07-28 09:00:00', 'FULFILLED'),
(4, 2, 5, '2025-07-29 16:45:00', 'CANCELLED');
