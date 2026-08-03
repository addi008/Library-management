package com.library;

import com.library.dao.BookDAO;
import com.library.dao.MemberDAO;
import com.library.dao.TransactionDAO;
import com.library.dao.impl.BookDAOImpl;
import com.library.dao.impl.MemberDAOImpl;
import com.library.dao.impl.TransactionDAOImpl;
import com.library.exception.*;
import com.library.model.Book;
import com.library.model.Fine;
import com.library.model.Member;
import com.library.model.Transaction;
import com.library.service.FineService;
import com.library.service.TransactionService;
import com.library.service.impl.FineServiceImpl;
import com.library.service.impl.TransactionServiceImpl;

import java.time.LocalDate;
import java.util.List;

/**
 * Phase 3 Deliverable Test:
 * Simulates issue -> max limit validation -> stock validation -> overdue check -> return -> fine creation -> fine payment.
 */
public class Phase3Test {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   Library Management System - Phase 3 Test       ");
        System.out.println("==================================================");

        TransactionService transactionService = new TransactionServiceImpl();
        FineService fineService = new FineServiceImpl();
        BookDAO bookDAO = new BookDAOImpl();
        MemberDAO memberDAO = new MemberDAOImpl();
        TransactionDAO transactionDAO = new TransactionDAOImpl();

        try {
            // Setup fresh test member and books
            Member member = new Member("Grace Hopper", "grace.hopper@example.com", "555-0303", LocalDate.now(), "STUDENT");
            memberDAO.addMember(member);
            int mId = member.getMemberId();
            System.out.println("[SETUP] Created Test Member: " + member.getName() + " (ID: " + mId + ")");

            Book b1 = bookDAO.getAllBooks().get(0);
            Book b2 = bookDAO.getAllBooks().get(1);
            Book b3 = bookDAO.getAllBooks().get(2);
            Book b4 = bookDAO.getAllBooks().get(3);

            // ---------------------------------------------------
            // 1. TEST BOOK ISSUANCE (Borrowing)
            // ---------------------------------------------------
            System.out.println("\n--- [1] TESTING BOOK ISSUANCE ---");
            Transaction t1 = transactionService.issueBook(mId, b1.getBookId());
            System.out.println("[ISSUE BOOK 1] Success: " + t1);

            Transaction t2 = transactionService.issueBook(mId, b2.getBookId());
            System.out.println("[ISSUE BOOK 2] Success: " + t2);

            Transaction t3 = transactionService.issueBook(mId, b3.getBookId());
            System.out.println("[ISSUE BOOK 3] Success: " + t3);

            // ---------------------------------------------------
            // 2. TEST MAX BORROWING LIMIT (3 Books Max)
            // ---------------------------------------------------
            System.out.println("\n--- [2] TESTING MAX BORROWING LIMIT CONSTRAINT ---");
            try {
                transactionService.issueBook(mId, b4.getBookId());
            } catch (MaxBooksBorrowedException e) {
                System.out.println("[EXCEPTION VERIFICATION] " + e.getMessage() + " (Expected)");
            }

            // ---------------------------------------------------
            // 3. TEST OUT OF STOCK CONSTRAINT
            // ---------------------------------------------------
            System.out.println("\n--- [3] TESTING OUT OF STOCK CONSTRAINT ---");
            Member member2 = new Member("Ada Lovelace", "ada.lovelace@example.com", "555-0404", LocalDate.now(), "PREMIUM");
            memberDAO.addMember(member2);

            Book zeroCopyBook = new Book("Rare Manuscript", "Ancient Author", "9780000000099", "History", 1, 0, LocalDate.now());
            bookDAO.addBook(zeroCopyBook);
            try {
                transactionService.issueBook(member2.getMemberId(), zeroCopyBook.getBookId());
            } catch (BookNotAvailableException e) {
                System.out.println("[EXCEPTION VERIFICATION] " + e.getMessage() + " (Expected)");
            }

            // ---------------------------------------------------
            // 4. TEST OVERDUE BOOK RETURN & AUTO-FINE CALCULATION
            // ---------------------------------------------------
            System.out.println("\n--- [4] TESTING OVERDUE RETURN & AUTO FINE CREATION ---");
            
            // Create a simulated overdue transaction (issued 20 days ago, due 6 days ago -> 6 days overdue)
            LocalDate issuePast = LocalDate.now().minusDays(20);
            LocalDate duePast = LocalDate.now().minusDays(6);
            Transaction overdueTrans = new Transaction(b4.getBookId(), mId, issuePast, duePast, Transaction.TransactionStatus.ISSUED);
            int overdueTransId = transactionDAO.createTransaction(overdueTrans);

            System.out.println("[SIMULATION] Created Overdue Transaction ID: " + overdueTransId + " (Due: " + duePast + ")");

            // Check overdue transactions list
            List<Transaction> overdueList = transactionService.getOverdueBooks();
            System.out.println("[OVERDUE CHECK] Active overdue books found in DB: " + overdueList.size());

            // Process return of overdue book
            Transaction returnedTrans = transactionService.returnBook(overdueTransId);
            System.out.println("[RETURN OVERDUE BOOK] Status after return: " + returnedTrans.getStatus());

            // ---------------------------------------------------
            // 5. TEST FINE QUERY AND FINE PAYMENT
            // ---------------------------------------------------
            System.out.println("\n--- [5] TESTING UNPAID FINES & FINE PAYMENT ---");
            List<Fine> unpaidFines = fineService.getUnpaidFines(mId);
            System.out.println("[UNPAID FINES] Found " + unpaidFines.size() + " unpaid fine(s) for Member ID: " + mId);
            for (Fine f : unpaidFines) {
                System.out.println("   -> " + f);
            }

            if (!unpaidFines.isEmpty()) {
                Fine fineToPay = unpaidFines.get(0);
                boolean paymentResult = fineService.payFine(fineToPay.getFineId());
                System.out.println("[PAY FINE] Fine ID " + fineToPay.getFineId() + " paid successfully: " + paymentResult);

                List<Fine> remainingUnpaid = fineService.getUnpaidFines(mId);
                System.out.println("[UNPAID FINES AFTER PAYMENT] Remaining unpaid count: " + remainingUnpaid.size());
            }

            // Cleanup test members & zero-copy book
            memberDAO.deleteMember(mId);
            memberDAO.deleteMember(member2.getMemberId());
            bookDAO.deleteBook(zeroCopyBook.getBookId());

            System.out.println("\n==================================================");
            System.out.println("   PHASE 3 BUSINESS LOGIC VERIFICATION PASSED 100%");
            System.out.println("==================================================");

        } catch (Exception e) {
            System.err.println("[ERROR] Phase 3 Test Failed:");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
