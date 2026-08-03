package com.library.service.impl;

import com.library.dao.BookDAO;
import com.library.dao.FineDAO;
import com.library.dao.MemberDAO;
import com.library.dao.TransactionDAO;
import com.library.dao.impl.BookDAOImpl;
import com.library.dao.impl.FineDAOImpl;
import com.library.dao.impl.MemberDAOImpl;
import com.library.dao.impl.TransactionDAOImpl;
import com.library.exception.*;
import com.library.model.Book;
import com.library.model.Fine;
import com.library.model.Member;
import com.library.model.Transaction;
import com.library.service.ReservationService;
import com.library.service.TransactionService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.logging.Logger;

public class TransactionServiceImpl implements TransactionService {

    private static final Logger LOGGER = Logger.getLogger(TransactionServiceImpl.class.getName());
    public static final int MAX_BORROW_LIMIT = 3;
    public static final BigDecimal DAILY_FINE_RATE = new BigDecimal("1.00"); // $1.00 per day overdue

    private final TransactionDAO transactionDAO;
    private final BookDAO bookDAO;
    private final MemberDAO memberDAO;
    private final FineDAO fineDAO;
    private final ReservationService reservationService;

    public TransactionServiceImpl() {
        this.transactionDAO = new TransactionDAOImpl();
        this.bookDAO = new BookDAOImpl();
        this.memberDAO = new MemberDAOImpl();
        this.fineDAO = new FineDAOImpl();
        this.reservationService = new ReservationServiceImpl();
    }

    public TransactionServiceImpl(TransactionDAO transactionDAO, BookDAO bookDAO, MemberDAO memberDAO, FineDAO fineDAO, ReservationService reservationService) {
        this.transactionDAO = transactionDAO;
        this.bookDAO = bookDAO;
        this.memberDAO = memberDAO;
        this.fineDAO = fineDAO;
        this.reservationService = reservationService;
    }

    @Override
    public Transaction issueBook(int memberId, int bookId)
            throws MemberNotFoundException, BookNotFoundException, BookNotAvailableException, MaxBooksBorrowedException {

        // 1. Verify member exists
        Member member = memberDAO.getMemberById(memberId);

        // 2. Verify book exists
        Book book = bookDAO.getBookById(bookId);

        // 3. Check member borrowing limit (Max 3 books)
        List<Transaction> activeTransactions = transactionDAO.getActiveTransactionsByMember(memberId);
        if (activeTransactions.size() >= MAX_BORROW_LIMIT) {
            throw new MaxBooksBorrowedException("Member '" + member.getName() + "' (ID: " + memberId +
                    ") has reached the maximum borrowing limit of " + MAX_BORROW_LIMIT + " books.");
        }

        // 4. Check book stock availability
        if (book.getAvailableCopies() <= 0) {
            throw new BookNotAvailableException("Book '" + book.getTitle() + "' (ID: " + bookId + ") is currently out of stock.");
        }

        // 5. Decrement available copies
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookDAO.updateBook(book);

        // 6. Create transaction record (+14 days due date)
        LocalDate issueDate = LocalDate.now();
        LocalDate dueDate = issueDate.plusDays(14);
        Transaction transaction = new Transaction(bookId, memberId, issueDate, dueDate, Transaction.TransactionStatus.ISSUED);

        int transId = transactionDAO.createTransaction(transaction);
        if (transId <= 0) {
            // Rollback stock decrement if transaction creation failed
            book.setAvailableCopies(book.getAvailableCopies() + 1);
            bookDAO.updateBook(book);
            throw new RuntimeException("Failed to record transaction in database.");
        }

        LOGGER.info("Successfully issued Book ID " + bookId + " to Member ID " + memberId + ". Transaction ID: " + transId);
        return transaction;
    }

    @Override
    public Transaction returnBook(int transactionId)
            throws InvalidTransactionException, BookNotFoundException {

        // 1. Fetch transaction
        Transaction transaction = transactionDAO.getTransactionById(transactionId);
        if (transaction == null) {
            throw new InvalidTransactionException("Transaction not found with ID: " + transactionId);
        }

        if (transaction.getStatus() == Transaction.TransactionStatus.RETURNED) {
            throw new InvalidTransactionException("Transaction ID " + transactionId + " has already been returned.");
        }

        // 2. Increment book available copies
        Book book = bookDAO.getBookById(transaction.getBookId());
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookDAO.updateBook(book);

        // 3. Set return date
        LocalDate returnDate = LocalDate.now();
        transaction.setReturnDate(returnDate);

        // 4. Check if overdue & calculate fine
        if (returnDate.isAfter(transaction.getDueDate())) {
            long daysOverdue = ChronoUnit.DAYS.between(transaction.getDueDate(), returnDate);
            if (daysOverdue > 0) {
                BigDecimal fineAmount = DAILY_FINE_RATE.multiply(BigDecimal.valueOf(daysOverdue));
                Fine fine = new Fine(transactionId, fineAmount, false);
                fineDAO.createFine(fine);
                LOGGER.info("Book return is OVERDUE by " + daysOverdue + " day(s). Fine generated: $" + fineAmount);
                transaction.setStatus(Transaction.TransactionStatus.OVERDUE);
            } else {
                transaction.setStatus(Transaction.TransactionStatus.RETURNED);
            }
        } else {
            transaction.setStatus(Transaction.TransactionStatus.RETURNED);
        }

        // 5. Update transaction record
        transactionDAO.updateTransaction(transaction);
        LOGGER.info("Transaction ID " + transactionId + " marked as " + transaction.getStatus());

        // 6. Check & notify earliest pending reservation for this book
        reservationService.notifyNextPendingReservation(transaction.getBookId());

        return transaction;
    }

    @Override
    public List<Transaction> getOverdueBooks() {
        return transactionDAO.getOverdueTransactions();
    }

    @Override
    public List<Transaction> getTransactionsByMember(int memberId) {
        return transactionDAO.getTransactionsByMember(memberId);
    }

    @Override
    public List<Transaction> getAllTransactions() {
        return transactionDAO.getAllTransactions();
    }
}
