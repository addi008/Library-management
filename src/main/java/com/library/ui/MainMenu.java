package com.library.ui;

import com.library.dao.BookDAO;
import com.library.dao.MemberDAO;
import com.library.dao.impl.BookDAOImpl;
import com.library.dao.impl.MemberDAOImpl;
import com.library.dto.BookBorrowReportDTO;
import com.library.dto.MemberActivityReportDTO;
import com.library.dto.MemberWithUnpaidFineDTO;
import com.library.model.*;
import com.library.service.*;
import com.library.service.impl.*;
import com.library.util.AppLogger;
import com.library.util.InputValidator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

public class MainMenu {

    private static final Logger LOGGER = AppLogger.getLogger(MainMenu.class.getName());

    private final BookDAO bookDAO;
    private final MemberDAO memberDAO;
    private final TransactionService transactionService;
    private final FineService fineService;
    private final ReservationService reservationService;
    private final SearchReportService searchReportService;
    private final Scanner scanner;

    public MainMenu() {
        this.bookDAO = new BookDAOImpl();
        this.memberDAO = new MemberDAOImpl();
        this.transactionService = new TransactionServiceImpl();
        this.fineService = new FineServiceImpl();
        this.reservationService = new ReservationServiceImpl();
        this.searchReportService = new SearchReportServiceImpl();
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        LOGGER.info("Starting Library Management System console session.");
        while (true) {
            printHeader("LIBRARY MANAGEMENT SYSTEM - MAIN MENU");
            System.out.println(" 1. 📚 Book Management");
            System.out.println(" 2. 👤 Member Management");
            System.out.println(" 3. 🔄 Borrow & Return Transactions");
            System.out.println(" 4. 💰 Fine Management");
            System.out.println(" 5. 🔖 Book Reservations");
            System.out.println(" 6. 📊 Reports & Search Analytics");
            System.out.println(" 0. 🚪 Exit Application");
            System.out.print("\nEnter option: ");

            String input = scanner.nextLine().trim();
            switch (input) {
                case "1":
                    bookMenu();
                    break;
                case "2":
                    memberMenu();
                    break;
                case "3":
                    transactionMenu();
                    break;
                case "4":
                    fineMenu();
                    break;
                case "5":
                    reservationMenu();
                    break;
                case "6":
                    reportMenu();
                    break;
                case "0":
                    System.out.println("\nThank you for using Library Management System. Goodbye!");
                    LOGGER.info("User exited session.");
                    return;
                default:
                    System.out.println("❌ Invalid option. Please select 0-6.");
            }
        }
    }

    private void bookMenu() {
        while (true) {
            printHeader("BOOK MANAGEMENT");
            System.out.println(" 1. Add New Book");
            System.out.println(" 2. Update Book Details");
            System.out.println(" 3. Delete Book");
            System.out.println(" 4. Search Book by ID");
            System.out.println(" 5. Search Books by Title / Author / ISBN");
            System.out.println(" 6. List All Books");
            System.out.println(" 0. Back to Main Menu");
            System.out.print("\nEnter choice: ");

            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1":
                        String title = InputValidator.readNonEmptyString(scanner, "Enter Title: ");
                        String author = InputValidator.readNonEmptyString(scanner, "Enter Author: ");
                        String isbn = InputValidator.readNonEmptyString(scanner, "Enter ISBN: ");
                        String cat = InputValidator.readNonEmptyString(scanner, "Enter Category: ");
                        int copies = InputValidator.readPositiveInt(scanner, "Enter Total Copies (> 0): ");
                        Book newBook = new Book(title, author, isbn, cat, copies, copies, LocalDate.now());
                        if (bookDAO.addBook(newBook)) {
                            System.out.println("✅ Book added successfully! Assigned ID: " + newBook.getBookId());
                            LOGGER.info("Book added: " + title + " (ID: " + newBook.getBookId() + ")");
                        }
                        break;
                    case "2":
                        int updateId = InputValidator.readPositiveInt(scanner, "Enter Book ID to Update: ");
                        Book b = bookDAO.getBookById(updateId);
                        System.out.print("Enter New Title (leave blank to keep '" + b.getTitle() + "'): ");
                        String nt = scanner.nextLine().trim();
                        if (!nt.isEmpty()) b.setTitle(nt);

                        System.out.print("Enter New Author (leave blank to keep '" + b.getAuthor() + "'): ");
                        String na = scanner.nextLine().trim();
                        if (!na.isEmpty()) b.setAuthor(na);

                        System.out.print("Enter New Available Copies (leave blank to keep " + b.getAvailableCopies() + "): ");
                        String nc = scanner.nextLine().trim();
                        if (!nc.isEmpty() && InputValidator.isNonNegativeInteger(nc)) {
                            b.setAvailableCopies(Integer.parseInt(nc));
                        }
                        if (bookDAO.updateBook(b)) {
                            System.out.println("✅ Book updated successfully!");
                            LOGGER.info("Book updated ID: " + updateId);
                        }
                        break;
                    case "3":
                        int delId = InputValidator.readPositiveInt(scanner, "Enter Book ID to Delete: ");
                        if (bookDAO.deleteBook(delId)) {
                            System.out.println("✅ Book ID " + delId + " deleted.");
                            LOGGER.info("Book deleted ID: " + delId);
                        } else {
                            System.out.println("❌ Could not delete book ID " + delId + " (Not found or active references).");
                        }
                        break;
                    case "4":
                        int searchId = InputValidator.readPositiveInt(scanner, "Enter Book ID: ");
                        Book found = bookDAO.getBookById(searchId);
                        System.out.println("📖 " + found);
                        break;
                    case "5":
                        String kw = InputValidator.readNonEmptyString(scanner, "Enter Search Keyword (Title/Author/ISBN): ");
                        List<Book> searchRes = searchReportService.searchBooks(kw, null);
                        System.out.println("\n--- Search Results (" + searchRes.size() + ") ---");
                        searchRes.forEach(bk -> System.out.println("  " + bk));
                        break;
                    case "6":
                        List<Book> all = bookDAO.getAllBooks();
                        System.out.println("\n--- All Books (" + all.size() + ") ---");
                        all.forEach(bk -> System.out.println("  " + bk));
                        break;
                    case "0":
                        return;
                    default:
                        System.out.println("❌ Invalid choice. Please select 0-6.");
                }
            } catch (Exception e) {
                System.out.println("❌ Error: " + e.getMessage());
            }
        }
    }

    private void memberMenu() {
        while (true) {
            printHeader("MEMBER MANAGEMENT");
            System.out.println(" 1. Add New Member");
            System.out.println(" 2. Update Member Details");
            System.out.println(" 3. Delete Member");
            System.out.println(" 4. Search Member by ID");
            System.out.println(" 5. Search Member by Email");
            System.out.println(" 6. List All Members");
            System.out.println(" 0. Back to Main Menu");
            System.out.print("\nEnter choice: ");

            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1":
                        String name = InputValidator.readNonEmptyString(scanner, "Enter Member Name: ");
                        String email = InputValidator.readEmail(scanner, "Enter Email: ");
                        String phone = InputValidator.readPhone(scanner, "Enter Phone (e.g. 555-0101): ");
                        String type = InputValidator.readNonEmptyString(scanner, "Enter Membership Type (STANDARD/PREMIUM/STUDENT): ");
                        Member m = new Member(name, email, phone, LocalDate.now(), type.toUpperCase());
                        if (memberDAO.addMember(m)) {
                            System.out.println("✅ Member added successfully! Assigned ID: " + m.getMemberId());
                            LOGGER.info("Member registered: " + name + " (ID: " + m.getMemberId() + ")");
                        }
                        break;
                    case "2":
                        int updateId = InputValidator.readPositiveInt(scanner, "Enter Member ID to Update: ");
                        Member mem = memberDAO.getMemberById(updateId);
                        System.out.print("Enter New Name (leave blank to keep '" + mem.getName() + "'): ");
                        String nn = scanner.nextLine().trim();
                        if (!nn.isEmpty()) mem.setName(nn);

                        System.out.print("Enter New Phone (leave blank to keep '" + mem.getPhone() + "'): ");
                        String np = scanner.nextLine().trim();
                        if (!np.isEmpty()) {
                            if (InputValidator.isValidPhone(np)) {
                                mem.setPhone(np);
                            } else {
                                System.out.println("⚠️ Invalid phone format, retaining original.");
                            }
                        }
                        if (memberDAO.updateMember(mem)) {
                            System.out.println("✅ Member updated successfully!");
                            LOGGER.info("Member updated ID: " + updateId);
                        }
                        break;
                    case "3":
                        int delId = InputValidator.readPositiveInt(scanner, "Enter Member ID to Delete: ");
                        if (memberDAO.deleteMember(delId)) {
                            System.out.println("✅ Member ID " + delId + " deleted.");
                            LOGGER.info("Member deleted ID: " + delId);
                        } else {
                            System.out.println("❌ Could not delete member ID " + delId + ".");
                        }
                        break;
                    case "4":
                        int searchId = InputValidator.readPositiveInt(scanner, "Enter Member ID: ");
                        Member found = memberDAO.getMemberById(searchId);
                        System.out.println("👤 " + found);
                        break;
                    case "5":
                        String semail = InputValidator.readEmail(scanner, "Enter Email: ");
                        Member mByEmail = memberDAO.getMemberByEmail(semail);
                        System.out.println("👤 " + mByEmail);
                        break;
                    case "6":
                        List<Member> all = memberDAO.getAllMembers();
                        System.out.println("\n--- All Members (" + all.size() + ") ---");
                        all.forEach(mb -> System.out.println("  " + mb));
                        break;
                    case "0":
                        return;
                    default:
                        System.out.println("❌ Invalid choice. Please select 0-6.");
                }
            } catch (Exception e) {
                System.out.println("❌ Error: " + e.getMessage());
            }
        }
    }

    private void transactionMenu() {
        while (true) {
            printHeader("BORROW & RETURN TRANSACTIONS");
            System.out.println(" 1. Issue Book to Member");
            System.out.println(" 2. Return Book");
            System.out.println(" 3. View Overdue Transactions");
            System.out.println(" 4. View Books Currently Issued to Member");
            System.out.println(" 5. View All Transactions");
            System.out.println(" 0. Back to Main Menu");
            System.out.print("\nEnter choice: ");

            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1":
                        int mId = InputValidator.readPositiveInt(scanner, "Enter Member ID: ");
                        int bId = InputValidator.readPositiveInt(scanner, "Enter Book ID: ");
                        Transaction t = transactionService.issueBook(mId, bId);
                        System.out.println("✅ Book Issued Successfully! Transaction details: " + t);
                        LOGGER.info("Issued Book ID " + bId + " to Member ID " + mId);
                        break;
                    case "2":
                        int tId = InputValidator.readPositiveInt(scanner, "Enter Transaction ID to Return: ");
                        Transaction ret = transactionService.returnBook(tId);
                        System.out.println("✅ Book Returned! Transaction status updated to: " + ret.getStatus());
                        LOGGER.info("Returned Book for Transaction ID " + tId);
                        break;
                    case "3":
                        List<Transaction> overdue = transactionService.getOverdueBooks();
                        System.out.println("\n--- Overdue Transactions (" + overdue.size() + ") ---");
                        overdue.forEach(tx -> System.out.println("  " + tx));
                        break;
                    case "4":
                        int memId = InputValidator.readPositiveInt(scanner, "Enter Member ID: ");
                        List<Book> issuedBooks = searchReportService.getBooksIssuedToMember(memId);
                        System.out.println("\n--- Books Currently Issued to Member ID " + memId + " (" + issuedBooks.size() + ") ---");
                        issuedBooks.forEach(bk -> System.out.println("  " + bk));
                        break;
                    case "5":
                        List<Transaction> all = transactionService.getAllTransactions();
                        System.out.println("\n--- All Transactions (" + all.size() + ") ---");
                        all.forEach(tx -> System.out.println("  " + tx));
                        break;
                    case "0":
                        return;
                    default:
                        System.out.println("❌ Invalid choice. Please select 0-5.");
                }
            } catch (Exception e) {
                System.out.println("❌ Transaction Action Failed: " + e.getMessage());
            }
        }
    }

    private void fineMenu() {
        while (true) {
            printHeader("FINE MANAGEMENT");
            System.out.println(" 1. View Unpaid Fines for Member");
            System.out.println(" 2. Pay Fine");
            System.out.println(" 3. View All Fines");
            System.out.println(" 0. Back to Main Menu");
            System.out.print("\nEnter choice: ");

            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1":
                        int mId = InputValidator.readPositiveInt(scanner, "Enter Member ID: ");
                        List<Fine> unpaid = fineService.getUnpaidFines(mId);
                        System.out.println("\n--- Unpaid Fines for Member ID " + mId + " (" + unpaid.size() + ") ---");
                        unpaid.forEach(f -> System.out.println("  " + f));
                        break;
                    case "2":
                        int fId = InputValidator.readPositiveInt(scanner, "Enter Fine ID to Pay: ");
                        if (fineService.payFine(fId)) {
                            System.out.println("✅ Fine ID " + fId + " paid successfully!");
                            LOGGER.info("Paid Fine ID: " + fId);
                        }
                        break;
                    case "3":
                        List<Fine> all = fineService.getAllFines();
                        System.out.println("\n--- All Fines (" + all.size() + ") ---");
                        all.forEach(f -> System.out.println("  " + f));
                        break;
                    case "0":
                        return;
                    default:
                        System.out.println("❌ Invalid choice. Please select 0-3.");
                }
            } catch (Exception e) {
                System.out.println("❌ Fine Action Failed: " + e.getMessage());
            }
        }
    }

    private void reservationMenu() {
        while (true) {
            printHeader("BOOK RESERVATIONS");
            System.out.println(" 1. Reserve an Out-of-Stock Book");
            System.out.println(" 2. View Reservations for Member");
            System.out.println(" 3. View All Reservations");
            System.out.println(" 0. Back to Main Menu");
            System.out.print("\nEnter choice: ");

            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1":
                        int mId = InputValidator.readPositiveInt(scanner, "Enter Member ID: ");
                        int bId = InputValidator.readPositiveInt(scanner, "Enter Book ID: ");
                        Reservation r = reservationService.reserveBook(mId, bId);
                        System.out.println("✅ Reservation Confirmed! ID: " + r.getReservationId());
                        LOGGER.info("Reservation placed ID " + r.getReservationId() + " for Book " + bId + " by Member " + mId);
                        break;
                    case "2":
                        int memId = InputValidator.readPositiveInt(scanner, "Enter Member ID: ");
                        List<Reservation> mRes = reservationService.getReservationsByMember(memId);
                        System.out.println("\n--- Member Reservations (" + mRes.size() + ") ---");
                        mRes.forEach(res -> System.out.println("  " + res));
                        break;
                    case "3":
                        List<Reservation> all = reservationService.getAllReservations();
                        System.out.println("\n--- All Reservations (" + all.size() + ") ---");
                        all.forEach(res -> System.out.println("  " + res));
                        break;
                    case "0":
                        return;
                    default:
                        System.out.println("❌ Invalid choice. Please select 0-3.");
                }
            } catch (Exception e) {
                System.out.println("❌ Reservation Action Failed: " + e.getMessage());
            }
        }
    }

    private void reportMenu() {
        while (true) {
            printHeader("REPORTS & SEARCH ANALYTICS");
            System.out.println(" 1. Top Most Borrowed Books");
            System.out.println(" 2. Top Most Active Members");
            System.out.println(" 3. Total Fines Collected");
            System.out.println(" 4. Members with Unpaid Fines");
            System.out.println(" 0. Back to Main Menu");
            System.out.print("\nEnter choice: ");

            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1":
                        List<BookBorrowReportDTO> topBooks = searchReportService.getMostBorrowedBooks(5);
                        System.out.println("\n--- Top Borrowed Books ---");
                        topBooks.forEach(dto -> System.out.println("  " + dto));
                        break;
                    case "2":
                        List<MemberActivityReportDTO> topMembers = searchReportService.getMostActiveMembers(5);
                        System.out.println("\n--- Top Active Members ---");
                        topMembers.forEach(dto -> System.out.println("  " + dto));
                        break;
                    case "3":
                        BigDecimal totalFine = searchReportService.getTotalFinesCollectedThisMonth();
                        System.out.println("\n--- Financial Summary ---");
                        System.out.printf("  Total Fines Collected: $%.2f%n", totalFine);
                        break;
                    case "4":
                        List<MemberWithUnpaidFineDTO> unpaidMembers = searchReportService.getMembersWithUnpaidFines();
                        System.out.println("\n--- Members with Unpaid Fines ---");
                        unpaidMembers.forEach(dto -> System.out.println("  " + dto));
                        break;
                    case "0":
                        return;
                    default:
                        System.out.println("❌ Invalid choice. Please select 0-4.");
                }
            } catch (Exception e) {
                System.out.println("❌ Report Action Failed: " + e.getMessage());
            }
        }
    }

    private void printHeader(String title) {
        System.out.println("\n==================================================");
        System.out.println("  " + title);
        System.out.println("==================================================");
    }
}
