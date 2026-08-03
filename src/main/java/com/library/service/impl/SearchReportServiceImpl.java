package com.library.service.impl;

import com.library.dao.BookDAO;
import com.library.dao.ReportDAO;
import com.library.dao.TransactionDAO;
import com.library.dao.impl.BookDAOImpl;
import com.library.dao.impl.ReportDAOImpl;
import com.library.dao.impl.TransactionDAOImpl;
import com.library.dto.BookBorrowReportDTO;
import com.library.dto.MemberActivityReportDTO;
import com.library.dto.MemberWithUnpaidFineDTO;
import com.library.model.Book;
import com.library.model.Transaction;
import com.library.service.SearchReportService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SearchReportServiceImpl implements SearchReportService {

    private final BookDAO bookDAO;
    private final TransactionDAO transactionDAO;
    private final ReportDAO reportDAO;

    public SearchReportServiceImpl() {
        this.bookDAO = new BookDAOImpl();
        this.transactionDAO = new TransactionDAOImpl();
        this.reportDAO = new ReportDAOImpl();
    }

    public SearchReportServiceImpl(BookDAO bookDAO, TransactionDAO transactionDAO, ReportDAO reportDAO) {
        this.bookDAO = bookDAO;
        this.transactionDAO = transactionDAO;
        this.reportDAO = reportDAO;
    }

    @Override
    public List<Book> searchBooks(String keyword, String category) {
        List<Book> all = bookDAO.getAllBooks();
        return all.stream()
                .filter(b -> {
                    boolean matchKeyword = true;
                    if (keyword != null && !keyword.trim().isEmpty()) {
                        String kw = keyword.toLowerCase().trim();
                        matchKeyword = b.getTitle().toLowerCase().contains(kw) ||
                                       b.getAuthor().toLowerCase().contains(kw) ||
                                       b.getIsbn().toLowerCase().contains(kw);
                    }
                    boolean matchCategory = true;
                    if (category != null && !category.trim().isEmpty()) {
                        matchCategory = b.getCategory().equalsIgnoreCase(category.trim());
                    }
                    return matchKeyword && matchCategory;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Book> getBooksIssuedToMember(int memberId) {
        List<Transaction> activeTx = transactionDAO.getActiveTransactionsByMember(memberId);
        List<Book> books = new ArrayList<>();
        for (Transaction t : activeTx) {
            try {
                books.add(bookDAO.getBookById(t.getBookId()));
            } catch (Exception ignored) {
            }
        }
        return books;
    }

    @Override
    public List<BookBorrowReportDTO> getMostBorrowedBooks(int limit) {
        return reportDAO.getMostBorrowedBooks(limit);
    }

    @Override
    public List<MemberActivityReportDTO> getMostActiveMembers(int limit) {
        return reportDAO.getMostActiveMembers(limit);
    }

    @Override
    public BigDecimal getTotalFinesCollectedThisMonth() {
        return reportDAO.getTotalFinesCollectedThisMonth();
    }

    @Override
    public List<MemberWithUnpaidFineDTO> getMembersWithUnpaidFines() {
        return reportDAO.getMembersWithUnpaidFines();
    }
}
