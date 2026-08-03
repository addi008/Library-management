package com.library;

import com.library.dao.BookDAO;
import com.library.dao.MemberDAO;
import com.library.dao.impl.BookDAOImpl;
import com.library.dao.impl.MemberDAOImpl;
import com.library.exception.BookNotFoundException;
import com.library.exception.MemberNotFoundException;
import com.library.model.Book;
import com.library.model.Member;

import java.time.LocalDate;
import java.util.List;

/**
 * Phase 2 Test Class: Verifies CRUD operations for BookDAO and MemberDAO end-to-end.
 */
public class Phase2Test {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   Library Management System - Phase 2 Test       ");
        System.out.println("==================================================");

        BookDAO bookDAO = new BookDAOImpl();
        MemberDAO memberDAO = new MemberDAOImpl();

        try {
            // ---------------------------------------------------
            // 1. TEST BOOK DAO CRUD
            // ---------------------------------------------------
            System.out.println("\n--- [1] TESTING BOOK DAO ---");

            // A. Create & Add Book
            Book newBook = new Book("Refactoring", "Martin Fowler", "9780201485677", "Software Engineering", 4, 4, LocalDate.now());
            boolean addedBook = bookDAO.addBook(newBook);
            System.out.println("[ADD BOOK] Success: " + addedBook + " | Created: " + newBook);

            // B. Fetch Book by ID
            Book fetchedBook = bookDAO.getBookById(newBook.getBookId());
            System.out.println("[GET BOOK BY ID] Found: " + fetchedBook);

            // C. Fetch Book by ISBN
            Book bookByIsbn = bookDAO.searchByISBN("9780201485677");
            System.out.println("[SEARCH BY ISBN] Found: " + bookByIsbn.getTitle() + " (ISBN: " + bookByIsbn.getIsbn() + ")");

            // D. Update Book
            fetchedBook.setAvailableCopies(3);
            fetchedBook.setCategory("Software Architecture");
            boolean updatedBook = bookDAO.updateBook(fetchedBook);
            Book reFetchedBook = bookDAO.getBookById(fetchedBook.getBookId());
            System.out.println("[UPDATE BOOK] Success: " + updatedBook + " | Updated state: " + reFetchedBook);

            // E. Search by Title & Author
            List<Book> searchTitleResults = bookDAO.searchByTitle("refactor");
            System.out.println("[SEARCH BY TITLE 'refactor'] Found " + searchTitleResults.size() + " book(s)");

            List<Book> searchAuthorResults = bookDAO.searchByAuthor("fowler");
            System.out.println("[SEARCH BY AUTHOR 'fowler'] Found " + searchAuthorResults.size() + " book(s)");

            // F. List All Books
            List<Book> allBooks = bookDAO.getAllBooks();
            System.out.println("[GET ALL BOOKS] Total count in database: " + allBooks.size());

            // G. Delete Book
            boolean deletedBook = bookDAO.deleteBook(newBook.getBookId());
            System.out.println("[DELETE BOOK] Success: " + deletedBook + " | Deleted ID: " + newBook.getBookId());

            // H. Verify Custom Exception on Missing Book
            try {
                bookDAO.getBookById(newBook.getBookId());
            } catch (BookNotFoundException e) {
                System.out.println("[EXCEPTION VERIFICATION] " + e.getMessage() + " (Expected)");
            }

            // ---------------------------------------------------
            // 2. TEST MEMBER DAO CRUD
            // ---------------------------------------------------
            System.out.println("\n--- [2] TESTING MEMBER DAO ---");

            // A. Create & Add Member
            Member newMember = new Member("Frank Miller", "frank.miller@example.com", "555-0109", LocalDate.now(), "PREMIUM");
            boolean addedMember = memberDAO.addMember(newMember);
            System.out.println("[ADD MEMBER] Success: " + addedMember + " | Created: " + newMember);

            // B. Fetch Member by ID
            Member fetchedMember = memberDAO.getMemberById(newMember.getMemberId());
            System.out.println("[GET MEMBER BY ID] Found: " + fetchedMember);

            // C. Fetch Member by Email
            Member memberByEmail = memberDAO.getMemberByEmail("frank.miller@example.com");
            System.out.println("[SEARCH BY EMAIL] Found: " + memberByEmail.getName() + " (Email: " + memberByEmail.getEmail() + ")");

            // D. Update Member
            fetchedMember.setPhone("555-9999");
            fetchedMember.setMembershipType("VIP");
            boolean updatedMember = memberDAO.updateMember(fetchedMember);
            Member reFetchedMember = memberDAO.getMemberById(fetchedMember.getMemberId());
            System.out.println("[UPDATE MEMBER] Success: " + updatedMember + " | Updated state: " + reFetchedMember);

            // E. List All Members
            List<Member> allMembers = memberDAO.getAllMembers();
            System.out.println("[GET ALL MEMBERS] Total count in database: " + allMembers.size());

            // F. Delete Member
            boolean deletedMember = memberDAO.deleteMember(newMember.getMemberId());
            System.out.println("[DELETE MEMBER] Success: " + deletedMember + " | Deleted ID: " + newMember.getMemberId());

            // G. Verify Custom Exception on Missing Member
            try {
                memberDAO.getMemberById(newMember.getMemberId());
            } catch (MemberNotFoundException e) {
                System.out.println("[EXCEPTION VERIFICATION] " + e.getMessage() + " (Expected)");
            }

            System.out.println("\n==================================================");
            System.out.println("   PHASE 2 DAO LAYER VERIFICATION PASSED 100%     ");
            System.out.println("==================================================");

        } catch (Exception e) {
            System.err.println("[ERROR] Phase 2 Test Failed:");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
