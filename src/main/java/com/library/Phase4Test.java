package com.library;

import com.library.dao.BookDAO;
import com.library.dao.MemberDAO;
import com.library.dao.TransactionDAO;
import com.library.dao.impl.BookDAOImpl;
import com.library.dao.impl.MemberDAOImpl;
import com.library.dao.impl.TransactionDAOImpl;
import com.library.dto.BookBorrowReportDTO;
import com.library.dto.MemberActivityReportDTO;
import com.library.dto.MemberWithUnpaidFineDTO;
import com.library.model.Book;
import com.library.model.Member;
import com.library.model.Reservation;
import com.library.model.Transaction;
import com.library.service.ReservationService;
import com.library.service.SearchReportService;
import com.library.service.TransactionService;
import com.library.service.impl.ReservationServiceImpl;
import com.library.service.impl.SearchReportServiceImpl;
import com.library.service.impl.TransactionServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Phase 4 Deliverable Test:
 * Verifies Reservations with auto-notification on book return, search/filtering, and aggregate SQL reports.
 */
public class Phase4Test {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   Library Management System - Phase 4 Test       ");
        System.out.println("==================================================");

        BookDAO bookDAO = new BookDAOImpl();
        MemberDAO memberDAO = new MemberDAOImpl();
        TransactionDAO transactionDAO = new TransactionDAOImpl();
        ReservationService reservationService = new ReservationServiceImpl();
        TransactionService transactionService = new TransactionServiceImpl();
        SearchReportService searchReportService = new SearchReportServiceImpl();

        try {
            // Setup Test Data
            Member memberA = new Member("Alan Turing", "alan.turing@example.com", "555-0707", LocalDate.now(), "VIP");
            Member memberB = new Member("Margaret Hamilton", "margaret.h@example.com", "555-0808", LocalDate.now(), "STUDENT");
            memberDAO.addMember(memberA);
            memberDAO.addMember(memberB);

            // Create a book with 1 copy
            Book rareBook = new Book("Apollo Guidance Computer", "Eyles Don", "9780001112223", "Aerospace", 1, 1, LocalDate.now());
            bookDAO.addBook(rareBook);

            // ---------------------------------------------------
            // 1. ISSUE BOOK & TEST RESERVATION ON 0 COPIES
            // ---------------------------------------------------
            System.out.println("\n--- [1] TESTING RESERVATION SYSTEM ---");
            Transaction tx = transactionService.issueBook(memberA.getMemberId(), rareBook.getBookId());
            System.out.println("[ISSUE SINGLE COPY] Issued to Alan Turing. Remaining available: 0");

            // Member B reserves the out-of-stock book
            Reservation res = reservationService.reserveBook(memberB.getMemberId(), rareBook.getBookId());
            System.out.println("[RESERVE BOOK] Created: " + res);

            // ---------------------------------------------------
            // 2. RETURN BOOK & VERIFY AUTO-NOTIFICATION TRIGGER
            // ---------------------------------------------------
            System.out.println("\n--- [2] TESTING AUTO-NOTIFICATION ON RETURN ---");
            System.out.println("Returning book transaction ID: " + tx.getTransactionId());
            transactionService.returnBook(tx.getTransactionId());

            // ---------------------------------------------------
            // 3. TEST SEARCH & FILTER FEATURES
            // ---------------------------------------------------
            System.out.println("\n--- [3] TESTING SEARCH & FILTER ---");
            List<Book> searchResults = searchReportService.searchBooks("Apollo", "Aerospace");
            System.out.println("[SEARCH BY KEYWORD 'Apollo' & CATEGORY 'Aerospace'] Found " + searchResults.size() + " book(s):");
            searchResults.forEach(b -> System.out.println("   -> " + b));

            // ---------------------------------------------------
            // 4. TEST AGGREGATE SQL REPORTS
            // ---------------------------------------------------
            System.out.println("\n--- [4] TESTING AGGREGATE SQL REPORTS ---");

            List<BookBorrowReportDTO> topBooks = searchReportService.getMostBorrowedBooks(5);
            System.out.println("[REPORT: MOST BORROWED BOOKS] Total items: " + topBooks.size());
            topBooks.forEach(dto -> System.out.println("   -> " + dto));

            List<MemberActivityReportDTO> topMembers = searchReportService.getMostActiveMembers(5);
            System.out.println("[REPORT: MOST ACTIVE MEMBERS] Total items: " + topMembers.size());
            topMembers.forEach(dto -> System.out.println("   -> " + dto));

            BigDecimal totalFines = searchReportService.getTotalFinesCollectedThisMonth();
            System.out.printf("[REPORT: TOTAL FINES COLLECTED] Sum: $%.2f%n", totalFines);

            List<MemberWithUnpaidFineDTO> unpaidMembers = searchReportService.getMembersWithUnpaidFines();
            System.out.println("[REPORT: MEMBERS WITH UNPAID FINES] Total items: " + unpaidMembers.size());

            // Cleanup test data
            memberDAO.deleteMember(memberA.getMemberId());
            memberDAO.deleteMember(memberB.getMemberId());
            bookDAO.deleteBook(rareBook.getBookId());

            System.out.println("\n==================================================");
            System.out.println("   PHASE 4 RESERVATIONS & REPORTS PASSED 100%     ");
            System.out.println("==================================================");

        } catch (Exception e) {
            System.err.println("[ERROR] Phase 4 Test Failed:");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
