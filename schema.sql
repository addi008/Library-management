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
    status ENUM('ISSUED', 'RETURNED', 'OVERDUE') NOT NULL DEFAULT 'ISSUED',
    CONSTRAINT fk_trans_book FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE CASCADE,
    CONSTRAINT fk_trans_member FOREIGN KEY (member_id) REFERENCES members(member_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS fines (
    fine_id INT AUTO_INCREMENT PRIMARY KEY,
    transaction_id INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    paid BOOLEAN NOT NULL DEFAULT FALSE,
    paid_date DATE DEFAULT NULL,
    CONSTRAINT fk_fines_transaction FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS reservations (
    reservation_id INT AUTO_INCREMENT PRIMARY KEY,
    book_id INT NOT NULL,
    member_id INT NOT NULL,
    reservation_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status ENUM('PENDING', 'FULFILLED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    CONSTRAINT fk_res_book FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE CASCADE,
    CONSTRAINT fk_res_member FOREIGN KEY (member_id) REFERENCES members(member_id) ON DELETE CASCADE
);

-- ===================================================
-- Sample Seed Data (5-10 rows in books and members)
-- ===================================================

INSERT INTO books (title, author, isbn, category, total_copies, available_copies, added_date) VALUES
('Clean Code', 'Robert C. Martin', '9780132350884', 'Software Engineering', 5, 5, '2025-01-10'),
('Effective Java', 'Joshua Bloch', '9780134685991', 'Programming', 4, 4, '2025-01-12'),
('Design Patterns', 'Erich Gamma et al.', '9780201633610', 'Software Architecture', 3, 3, '2025-01-15'),
('The Pragmatic Programmer', 'Andrew Hunt, David Thomas', '9780135957059', 'Software Engineering', 6, 6, '2025-02-01'),
('Head First Java', 'Kathy Sierra, Bert Bates', '9780596009205', 'Programming', 2, 2, '2025-02-10'),
('Introduction to Algorithms', 'Thomas H. Cormen', '9780262033848', 'Computer Science', 3, 3, '2025-02-15'),
('Database System Concepts', 'Abraham Silberschatz', '9780073523323', 'Databases', 4, 4, '2025-03-01');

INSERT INTO members (name, email, phone, membership_date, membership_type) VALUES
('Alice Smith', 'alice.smith@example.com', '555-0101', '2025-01-01', 'PREMIUM'),
('Bob Johnson', 'bob.johnson@example.com', '555-0102', '2025-01-05', 'STANDARD'),
('Charlie Brown', 'charlie.brown@example.com', '555-0103', '2025-01-15', 'STUDENT'),
('Diana Prince', 'diana.prince@example.com', '555-0104', '2025-02-01', 'PREMIUM'),
('Evan Wright', 'evan.wright@example.com', '555-0105', '2025-02-10', 'STANDARD');
